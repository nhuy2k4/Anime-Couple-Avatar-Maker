package com.app.base.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.app.base.database.entity.PlayerEntity

@Dao
interface PlayerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(player: PlayerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(players: List<PlayerEntity>)

    @Query("SELECT * FROM players ORDER BY id")
    suspend fun getAllPlayers(): List<PlayerEntity>

    @Query("SELECT * FROM players WHERE id = :id LIMIT 1")
    suspend fun getPlayerById(id: Int): PlayerEntity?

    @Query("SELECT * FROM players ORDER BY rankingPoints DESC")
    suspend fun getPlayersByRanking(): List<PlayerEntity>

    @Query("UPDATE players SET diamond = diamond + :amount WHERE id = :playerId")
    suspend fun addDiamonds(playerId: Int, amount: Int)

    @Query("UPDATE players SET diamond = diamond - :amount WHERE id = :playerId AND diamond >= :amount")
    suspend fun spendDiamonds(playerId: Int, amount: Int): Int

    @Query("UPDATE players SET match = match + 1 WHERE id = :playerId")
    suspend fun incrementMatch(playerId: Int)

    @Query("UPDATE players SET rankingPoints = :points WHERE id = :playerId")
    suspend fun updateRankingPoints(playerId: Int, points: Int)

    @Query("DELETE FROM players WHERE id = :id")
    suspend fun deletePlayer(id: Int)

    @Query("DELETE FROM players")
    suspend fun deleteAll()

    @Update
    suspend fun update(player: PlayerEntity)

    // Alias for compatibility with RewardRepository calls
    suspend fun updatePlayer(player: PlayerEntity) = update(player)
}
