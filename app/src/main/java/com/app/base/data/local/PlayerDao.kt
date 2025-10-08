package com.app.base.data.local

import androidx.room.*
import com.app.base.data.models.Player

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players WHERE id = :id")
    suspend fun getPlayer(id: Int = 1): Player?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(player: Player)

    @Update
    suspend fun update(player: Player)

    @Delete
    suspend fun delete(player: Player)
}

