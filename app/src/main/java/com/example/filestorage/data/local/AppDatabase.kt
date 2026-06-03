// name=app/src/main/java/com/example/filestorage/data/local/AppDatabase.kt
package com.example.filestorage.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [UserEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}
