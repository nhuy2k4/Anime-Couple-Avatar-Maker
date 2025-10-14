package com.app.base.database.repository

import android.content.Context
import com.app.base.database.AppDatabase
import com.app.base.database.entity.OutfitEntity

class OutfitRepository(context: Context) {

    private val outfitDao = AppDatabase.Companion.getInstance(context).outfitDao()
    suspend fun insertOutfit(outfit: OutfitEntity): Long {
        return outfitDao.insert(outfit)
    }

    suspend fun getAllOutfits(): List<OutfitEntity> {
        return outfitDao.getAllOutfits()
    }

    suspend fun deleteOutfit(id: Long) {
        outfitDao.deleteOutfit(id)
    }
}