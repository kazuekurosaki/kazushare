// name=app/src/main/java/com/example/filestorage/repository/AuthRepositoryImpl.kt
package com.example.filestorage.repository

import com.example.filestorage.data.local.UserDao
import com.example.filestorage.data.local.UserEntity
import com.example.filestorage.data.model.Tier
import com.example.filestorage.data.model.User
import com.example.filestorage.util.TierHelper
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

// Implementasi repository menggunakan FirebaseAuth + Firestore + Room
class AuthRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val userDao: UserDao
) : AuthRepository {

    // Expose cached user (flow) kalau ada user di local DB
    override val currentUserFlow: Flow<User?> = auth.authStateChanges().map { firebaseUser ->
        firebaseUser?.let { uid ->
            val local = runCatching { userDao.getUser(uid.uid) }.getOrNull()
            local?.let {
                User(
                    id = it.id,
                    email = it.email,
                    displayName = it.displayName,
                    tier = Tier.fromString(it.tier),
                    storageUsed = it.storageUsed,
                    createdAt = it.createdAtMillis?.let { millis -> Timestamp(millis / 1000, 0) } // approximate
                )
            }
        }
    }

    override suspend fun registerWithEmail(email: String, password: String): Result<com.google.firebase.auth.FirebaseUser> {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, password).awaitResult()
            val user = authResult?.user ?: throw Exception("User is null")
            // Create user document in Firestore with default tier FREE. NOTE: custom claims must be set server-side.
            val doc = mapOf(
                "email" to user.email,
                "displayName" to (user.displayName ?: ""),
                "tier" to "FREE",
                "storageUsed" to 0L,
                "createdAt" to com.google.firebase.Timestamp.now()
            )
            firestore.collection("users").document(user.uid).set(doc).awaitResult()
            // Cache locally
            userDao.upsert(
                UserEntity(
                    id = user.uid,
                    email = user.email,
                    displayName = user.displayName ?: "",
                    tier = "FREE",
                    storageUsed = 0L,
                    createdAtMillis = System.currentTimeMillis()
                )
            )
            Result.success(user)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    override suspend fun loginWithEmail(email: String, password: String): Result<com.google.firebase.auth.FirebaseUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).awaitResult()
            val user = authResult?.user ?: throw Exception("User is null")
            // Fetch user metadata & cache
            fetchAndCacheUser(user.uid)
            Result.success(user)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    override suspend fun loginWithGoogle(idToken: String): Result<com.google.firebase.auth.FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).awaitResult()
            val user = authResult?.user ?: throw Exception("User is null")
            // If new user, ensure Firestore doc exists (default FREE). If exists, we fetch values
            val docRef = firestore.collection("users").document(user.uid)
            val snapshot = docRef.get().awaitResult()
            if (snapshot == null || !snapshot.exists()) {
                val doc = mapOf(
                    "email" to user.email,
                    "displayName" to (user.displayName ?: ""),
                    "tier" to "FREE",
                    "storageUsed" to 0L,
                    "createdAt" to com.google.firebase.Timestamp.now()
                )
                docRef.set(doc).awaitResult()
            }
            fetchAndCacheUser(user.uid)
            Result.success(user)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    override suspend fun logout() {
        auth.signOut()
        // Note: If using GoogleSignInClient, you should also sign out there from UI Hilt-provided client.
    }

    override suspend fun fetchAndCacheUser(uid: String): Result<User> {
        return try {
            val snapshot = firestore.collection("users").document(uid).get().awaitResult()
            if (snapshot == null || !snapshot.exists()) {
                return Result.failure(Exception("User document not found"))
            }
            val email = snapshot.getString("email")
            val displayName = snapshot.getString("displayName")
            val tierStr = snapshot.getString("tier")
            val tier = Tier.fromString(tierStr)
            val storageUsed = snapshot.getLong("storageUsed") ?: 0L
            val createdAt = snapshot.getTimestamp("createdAt")
            val user = User(id = uid, email = email, displayName = displayName, tier = tier, storageUsed = storageUsed, createdAt = createdAt)
            // Cache to Room
            userDao.upsert(
                UserEntity(
                    id = user.id,
                    email = user.email,
                    displayName = user.displayName,
                    tier = tier.name,
                    storageUsed = user.storageUsed,
                    createdAtMillis = user.createdAt?.seconds?.times(1000)
                )
            )
            Result.success(user)
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    override suspend fun getLocalUser(uid: String): User? {
        val entity = userDao.getUser(uid) ?: return null
        return User(
            id = entity.id,
            email = entity.email,
            displayName = entity.displayName,
            tier = Tier.fromString(entity.tier),
            storageUsed = entity.storageUsed,
            createdAt = entity.createdAtMillis?.let { com.google.firebase.Timestamp(it / 1000, 0) }
        )
    }

    // extension helpers to await Tasks in coroutines
    private suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitResult(): T? =
        kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            addOnCompleteListener { task ->
                if (task.isSuccessful) cont.resume(task.result, null)
                else cont.resumeWithException(task.exception ?: Exception("Unknown Task exception"))
            }
        }
}
