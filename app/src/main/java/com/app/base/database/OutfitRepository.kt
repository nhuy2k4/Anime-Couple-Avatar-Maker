package com.app.base.database

import android.content.Context

class OutfitRepository(context: Context) {

    private val outfitDao = AppDatabase.getInstance(context).outfitDao()

    suspend fun insertOutfit(outfit: OutfitEntity) {
        outfitDao.insert(outfit)
    }

    suspend fun getAllOutfits(): List<OutfitEntity> {
        return outfitDao.getAllOutfits()
    }

    suspend fun deleteOutfit(id: Long) {
        outfitDao.deleteOutfit(id)
    }
}