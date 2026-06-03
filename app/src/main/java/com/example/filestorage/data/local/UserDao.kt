// name=app/src/main/java/com/example/filestorage/data/local/UserDao.kt
package com.example.filestorage.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: UserEntity)

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun getUserFlow(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUser(id: String): UserEntity?

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: String)
}
