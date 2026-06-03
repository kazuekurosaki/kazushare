// name=app/src/main/java/com/example/filestorage/repository/AuthRepository.kt
package com.example.filestorage.repository

import com.example.filestorage.data.model.User
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

// Abstraksi repository auth/user
interface AuthRepository {
    val currentUserFlow: Flow<User?>

    suspend fun registerWithEmail(email: String, password: String): Result<FirebaseUser>
    suspend fun loginWithEmail(email: String, password: String): Result<FirebaseUser>
    suspend fun loginWithGoogle(idToken: String): Result<FirebaseUser>
    suspend fun logout()
    suspend fun fetchAndCacheUser(uid: String): Result<User>
    suspend fun getLocalUser(uid: String): User?
}
