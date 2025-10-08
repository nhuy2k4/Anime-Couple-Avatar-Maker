package com.app.base.data.repositories

import com.app.base.data.local.GameDatabase
import com.app.base.data.models.CosplayResult

class CosplayResultRepository(private val db: GameDatabase) {
    private val resultDao = db.cosplayResultDao()

    suspend fun getResult(id: String): CosplayResult? = resultDao.getResult(id)

    suspend fun saveResult(result: CosplayResult) = resultDao.insert(result)

    suspend fun deleteResult(result: CosplayResult) = resultDao.delete(result)
}
