package com.app.base.data.repositories

import com.app.base.data.local.GameDatabase
import com.app.base.data.models.Player

class PlayerRepository(private val db: GameDatabase) {

    private val playerDao = db.playerDao()

    suspend fun getPlayer(id: Int = 1): Player? = playerDao.getPlayer(id)

    suspend fun savePlayer(player: Player) {
        playerDao.insert(player)
    }

    suspend fun updatePlayer(player: Player) {
        playerDao.update(player)
    }

    suspend fun deletePlayer(player: Player) {
        playerDao.delete(player)
    }
}

