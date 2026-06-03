// name=app/src/main/java/com/example/filestorage/data/local/UserEntity.kt
package com.example.filestorage.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// Entity untuk menyimpan cache user
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String?,
    val displayName: String?,
    val tier: String,
    val storageUsed: Long,
    val createdAtMillis: Long?
)
