// name=app/src/main/java/com/example/filestorage/util/AuthTokenHelper.kt
package com.example.filestorage.util

import com.google.firebase.auth.FirebaseAuth

object AuthTokenHelper {
    suspend fun getTierFromClaims(): String? {
        val user = FirebaseAuth.getInstance().currentUser ?: return null
        return try {
            val tokenResult = user.getIdToken(true).awaitResult()
            val claims = tokenResult.claims
            (claims["tier"] as? String)
        } catch (t: Throwable) {
            null
        }
    }

    private suspend fun <T> com.google.android.gms.tasks.Task<T>.awaitResult(): T? =
        kotlinx.coroutines.suspendCancellableCoroutine { cont ->
            addOnCompleteListener { task ->
                if (task.isSuccessful) cont.resume(task.result, null)
                else cont.resumeWithException(task.exception ?: Exception("Unknown Task exception"))
            }
        }
}
