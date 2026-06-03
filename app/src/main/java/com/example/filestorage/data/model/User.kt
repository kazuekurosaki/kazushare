// name=app/src/main/java/com/example/filestorage/data/model/User.kt
package com.example.filestorage.data.model

import com.google.firebase.Timestamp

// Model domain / DTO untuk data user seperti disimpan di Firestore
data class User(
    val id: String,
    val email: String?,
    val displayName: String? = null,
    val tier: Tier = Tier.FREE,
    val storageUsed: Long = 0L,
    val createdAt: Timestamp? = null
)
