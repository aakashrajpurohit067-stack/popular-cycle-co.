package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.AdminEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AdminDao {
    @Query("SELECT * FROM admins WHERE phone = :phone AND isActive = 1 LIMIT 1")
    suspend fun getAdminByPhone(phone: String): AdminEntity?

    @Query("SELECT * FROM admins")
    fun getAllAdmins(): Flow<List<AdminEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdmin(admin: AdminEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdmins(admins: List<AdminEntity>)

    @Query("SELECT COUNT(*) FROM admins")
    suspend fun getAdminCount(): Int
}
