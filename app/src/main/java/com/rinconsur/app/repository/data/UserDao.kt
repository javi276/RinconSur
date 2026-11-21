package com.rinconsur.app.repository.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rinconsur.app.repository.models.User

@Dao
interface UserDao {
    // LOGIN
    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): User?

    // CREATE (ABORT: el email es único y lanza SQLiteConstraintException si se repite)
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertUser(user: User): Long
}
