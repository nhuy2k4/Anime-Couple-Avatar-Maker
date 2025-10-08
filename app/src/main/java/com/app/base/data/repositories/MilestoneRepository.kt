package com.app.base.data.repositories

import com.app.base.data.local.GameDatabase
import com.app.base.data.models.Milestone

class MilestoneRepository(private val db: GameDatabase) {
    private val milestoneDao = db.milestoneDao()

    suspend fun getAllMilestones(): List<Milestone> = milestoneDao.getAllMilestones()

    suspend fun saveMilestone(milestone: Milestone) = milestoneDao.insert(milestone)

    suspend fun updateMilestone(milestone: Milestone) = milestoneDao.update(milestone)

    suspend fun deleteMilestone(milestone: Milestone) = milestoneDao.delete(milestone)
}
