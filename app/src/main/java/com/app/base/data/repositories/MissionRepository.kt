package com.app.base.data.repositories

import com.app.base.data.models.Mission
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface MissionRepository {
    suspend fun getAll(): List<Mission>
    suspend fun getById(id: String): Mission?
    suspend fun addOrUpdate(mission: Mission)
    suspend fun remove(id: String)
    suspend fun clear()
}

//class InMemoryMissionRepository : MissionRepository {
//    private val missions = mutableListOf<Mission>()
//    private val mutex = Mutex()
//
//    override suspend fun getAll(): List<Mission> = mutex.withLock {
//        missions.toList()
//    }
//
//    override suspend fun getById(id: String): Mission? = mutex.withLock {
//        missions.find { it.id == id }
//    }
//
////    override suspend fun addOrUpdate(mission: Mission) = mutex.withLock {
////        val index = missions.indexOfFirst { it.id == mission.id }
////        if (index >= 0) missions[index] = mission else missions.add(mission)
////    }
////
////    override suspend fun remove(id: String) = mutex.withLock {
////        missions.removeAll { it.id == id }
////    }
//
//    override suspend fun clear() = mutex.withLock {
//        missions.clear()
//    }
//}
