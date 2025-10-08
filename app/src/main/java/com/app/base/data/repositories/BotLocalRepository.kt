package com.app.base.data.repositories

import com.app.base.data.local.GameDatabase
import com.app.base.data.models.Bot

class BotLocalRepository(private val db: GameDatabase) {
    private val botDao = db.botDao()

    suspend fun getAllBots(): List<Bot> = botDao.getAllBots()

    suspend fun saveBot(bot: Bot) = botDao.insert(bot)

    suspend fun saveAll(bots: List<Bot>) = botDao.insertAll(bots)

    suspend fun deleteBot(bot: Bot) = botDao.delete(bot)
}
