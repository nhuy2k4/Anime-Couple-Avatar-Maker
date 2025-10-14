package com.app.base.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.app.base.database.entity.BackgroundEntity

@Dao
interface BackgroundDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(background: BackgroundEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(backgrounds: List<BackgroundEntity>)

    @Query("SELECT * FROM backgrounds ORDER BY id")
    suspend fun getAllBackgrounds(): List<BackgroundEntity>

    @Query("SELECT * FROM backgrounds WHERE id = :id LIMIT 1")
    suspend fun getBackgroundById(id: String): BackgroundEntity?

    @Query("SELECT * FROM backgrounds WHERE unlockCondition IS NULL OR unlockCondition = ''")
    suspend fun getUnlockedBackgrounds(): List<BackgroundEntity>

    @Query("DELETE FROM backgrounds WHERE id = :id")
    suspend fun deleteBackground(id: String)

    @Query("DELETE FROM backgrounds")
    suspend fun deleteAll()

    @Update
    suspend fun update(background: BackgroundEntity)
}
