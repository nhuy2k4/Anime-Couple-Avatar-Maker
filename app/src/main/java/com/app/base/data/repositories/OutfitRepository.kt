//package com.app.base.data.repositories
//
//import com.app.base.data.local.GameDatabase
//import com.app.base.data.models.Outfit
//
//class OutfitRepository(private val db: GameDatabase) {
//    private val outfitDao = db.outfitDao()
//
//    suspend fun getOutfitsForPlayer(playerId: Int): List<Outfit> = outfitDao.getOutfitsForPlayer(playerId)
//
//    suspend fun saveOutfit(outfit: Outfit): Long = outfitDao.insert(outfit)
//
//    suspend fun deleteOutfit(outfit: Outfit) = outfitDao.delete(outfit)
//}
