package com.app.base.database.repository

import android.content.Context
import com.app.base.database.AppDatabase
import com.app.base.database.dao.DailyCheckInDao
import com.app.base.database.dao.MissionDao
import com.app.base.database.dao.PlayerDao
import com.app.base.database.dao.PlayerItemDao
import com.app.base.database.dao.PlayerRewardDao
import com.app.base.database.entity.DailyCheckInEntity
import com.app.base.database.entity.MissionEntity
import com.app.base.database.entity.PlayerItemEntity
import com.app.base.database.entity.PlayerRewardEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

data class DailyRewardConfig(
    val day: Int,
    val type: String, // "frontHair", "eyes", etc.
    val value: Int,
    val currencyType: String = "diamond",
    val diamondAmount: Int = 0
)

data class MissionTemplate(
    val id: String,
    val title: String,
    val type: String,
    val target: Int,
    val rewardAmount: Int
)

data class MissionProgress(
    val completedCount: Int,
    val totalCount: Int,
    val missions: List<MissionEntity>,
    val canClaimAll: Boolean
)

sealed class ClaimResult {
    data class Success(val reward: DailyRewardConfig?) : ClaimResult()
    object AlreadyClaimed : ClaimResult()
    data class Error(val message: String) : ClaimResult()
}

class RewardRepository(context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val dailyCheckInDao: DailyCheckInDao = database.dailyCheckInDao()
    private val missionDao: MissionDao = database.missionDao()
    private val playerDao: PlayerDao = database.playerDao()
    private val playerItemDao: PlayerItemDao = database.playerItemDao()
    private val playerRewardDao: PlayerRewardDao = database.playerRewardDao()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Daily rewards configuration (7 days cycle)
    private val dailyRewards = listOf(
        DailyRewardConfig(1, "frontHair", 1, "diamond", 10),
        DailyRewardConfig(2, "frontHair", 2, "diamond", 15),
        DailyRewardConfig(3, "frontHair", 3, "diamond", 20),
        DailyRewardConfig(4, "frontHair", 4, "diamond", 25),
        DailyRewardConfig(5, "eyes", 1, "diamond", 30),
        DailyRewardConfig(6, "eyes", 3, "diamond", 35),
        DailyRewardConfig(7, "eyes", 2, "diamond", 50)
    )

    // Daily missions configuration
    private val dailyMissionsTemplate = listOf(
        MissionTemplate("save_photo", "Save 1 Photo", "SAVE_PHOTO", 1, 50),
        MissionTemplate("play_match", "Play 1 Match", "PLAY_MATCH", 1, 30),
        MissionTemplate("watch_ad", "Watch 1 Ad", "WATCH_AD", 1, 20),
        MissionTemplate("win_match", "Win 1 Match", "WIN_MATCH", 1, 80),
        MissionTemplate("login_daily", "Daily Login", "LOGIN", 1, 25)
    )

    // === DAILY REWARD SYSTEM ===
    private fun getClaimedDaysForCurrentCycle(currentStreak: Int): Set<Int> {
        val cycleStart = ((currentStreak - 1) / 7) * 7 + 1
        val cycleEnd = minOf(currentStreak, cycleStart + 6)
        return (cycleStart..cycleEnd).map { it % 7 }.map { if (it == 0) 7 else it }.toSet()
    }

    suspend fun getDailyCheckInStatus(playerId: Int): DailyCheckInStatus = withContext(Dispatchers.IO) {
        val today = dateFormat.format(Date())
        val checkInEntity = dailyCheckInDao.getByPlayerId(playerId)

        if (checkInEntity == null) {
            // First time check-in
            return@withContext DailyCheckInStatus(
                currentDay = 1,
                canClaim = true,
                claimedDays = emptySet(),
                streak = 0
            )
        }

        val lastCheckIn = checkInEntity.lastCheckInDate
        val currentStreak = checkInEntity.currentStreak

        // Check if last check-in was yesterday
        val calendar = Calendar.getInstance()
        calendar.time = dateFormat.parse(today) ?: Date()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterday = dateFormat.format(calendar.time)

        val canClaim = when {
            lastCheckIn == today -> false // Already claimed today
            lastCheckIn == yesterday -> true // Can continue streak
            else -> true // Reset streak, can claim
        }

        val newDay = when {
            lastCheckIn == yesterday -> (currentStreak % 7) + 1
            lastCheckIn == today -> (currentStreak % 7).let { if (it == 0) 7 else it }
            else -> 1 // Reset to day 1
        }

        // Get claimed days for this week cycle
        val claimedDays = if (lastCheckIn == today) {
            getClaimedDaysForCurrentCycle(currentStreak)
        } else {
            emptySet<Int>()
        }

        DailyCheckInStatus(
            currentDay = newDay,
            canClaim = canClaim,
            claimedDays = claimedDays,
            streak = if (lastCheckIn == yesterday || lastCheckIn == today) currentStreak else 0
        )
    }

    suspend fun claimDailyReward(playerId: Int): ClaimResult = withContext(Dispatchers.IO) {
        try {
            val status = getDailyCheckInStatus(playerId)

            if (!status.canClaim) {
                return@withContext ClaimResult.AlreadyClaimed
            }

            val today = dateFormat.format(Date())
            val currentDay = status.currentDay
            val newStreak = status.streak + 1

            // Update or create daily check-in record
            val existingCheckIn = dailyCheckInDao.getByPlayerId(playerId)
            if (existingCheckIn != null) {
                dailyCheckInDao.update(
                    existingCheckIn.copy(
                        day = currentDay,
                        lastCheckInDate = today,
                        currentStreak = newStreak,
                        totalCheckIns = existingCheckIn.totalCheckIns + 1
                    )
                )
            } else {
                dailyCheckInDao.insert(
                    DailyCheckInEntity(
                        playerId = playerId,
                        day = currentDay,
                        lastCheckInDate = today,
                        currentStreak = newStreak,
                        totalCheckIns = 1
                    )
                )
            }

            // Give reward based on current day
            val rewardConfig = dailyRewards.getOrNull(currentDay - 1)
            if (rewardConfig != null) {
                giveReward(playerId, rewardConfig, currentDay)
            }

            ClaimResult.Success(rewardConfig)

        } catch (e: Exception) {
            ClaimResult.Error(e.message ?: "Unknown error")
        }
    }

    // === MISSION/TASK SYSTEM ===
    suspend fun initializeDailyMissions(playerId: Int) = withContext(Dispatchers.IO) {
        val today = dateFormat.format(Date())
        val existingMissions = missionDao.getDailyMissions(playerId)

        // Check if missions already exist for today
        if (existingMissions.isNotEmpty()) {
            return@withContext
        }

        // Create new daily missions
        val missions = dailyMissionsTemplate.map { template ->
            MissionEntity(
                id = "${template.id}_${playerId}_$today",
                playerId = playerId,
                title = template.title,
                type = template.type,
                target = template.target,
                rewardAmount = template.rewardAmount,
                isDaily = true
            )
        }

        missionDao.insertAll(missions)
    }

    suspend fun getDailyMissions(playerId: Int): List<MissionEntity> = withContext(Dispatchers.IO) {
        initializeDailyMissions(playerId)
        missionDao.getDailyMissions(playerId)
    }

    suspend fun getMissionProgress(playerId: Int): MissionProgress = withContext(Dispatchers.IO) {
        val missions = getDailyMissions(playerId)
        val completedCount = missions.count { it.status == "COMPLETED" || it.status == "CLAIMED" }
        val totalCount = missions.size

        MissionProgress(
            completedCount = completedCount,
            totalCount = totalCount,
            missions = missions,
            canClaimAll = completedCount == totalCount && missions.any { it.status == "COMPLETED" }
        )
    }

    suspend fun updateMissionProgress(playerId: Int, missionType: String, amount: Int = 1): Boolean = withContext(Dispatchers.IO) {
        try {
            val missions = missionDao.getDailyMissions(playerId).filter { it.type == missionType && it.status == "IN_PROGRESS" }

            missions.forEach { mission ->
                val newProgress = minOf(mission.progress + amount, mission.target)
                missionDao.updateProgress(mission.id, playerId, newProgress)

                // Mark as completed if target reached
                if (newProgress >= mission.target) {
                    missionDao.markCompleted(mission.id, playerId, "COMPLETED", System.currentTimeMillis())
                }
            }

            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun claimMissionReward(playerId: Int, missionId: String): ClaimResult = withContext(Dispatchers.IO) {
        try {
            val mission = missionDao.getMissionById(missionId, playerId)
                ?: return@withContext ClaimResult.Error("Mission not found")

            if (mission.status != "COMPLETED") {
                return@withContext ClaimResult.Error("Mission not completed")
            }

            // Give reward
            val player = playerDao.getPlayerById(playerId)
            if (player != null) {
                playerDao.updatePlayer(player.copy(diamond = player.diamond + mission.rewardAmount))
            }

            // Mark as claimed
            missionDao.markClaimed(missionId, playerId, System.currentTimeMillis())

            // Record reward
            playerRewardDao.insert(
                PlayerRewardEntity(
                    playerId = playerId,
                    source = "MISSION",
                    sourceId = missionId,
                    rewardType = mission.rewardType,
                    rewardData = """{"diamond": ${mission.rewardAmount}}""",
                    status = "CLAIMED",
                    claimedDate = System.currentTimeMillis(),
                    title = mission.title,
                    description = "Completed mission: ${mission.title}"
                )
            )

            ClaimResult.Success(null)

        } catch (e: Exception) {
            ClaimResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun claimAllMissionRewards(playerId: Int): ClaimResult = withContext(Dispatchers.IO) {
        try {
            val missions = missionDao.getDailyMissions(playerId).filter { it.status == "COMPLETED" }

            if (missions.isEmpty()) {
                return@withContext ClaimResult.Error("No completed missions to claim")
            }

            var totalDiamonds = 0

            missions.forEach { mission ->
                totalDiamonds += mission.rewardAmount
                missionDao.markClaimed(mission.id, playerId, System.currentTimeMillis())

                // Record each reward
                playerRewardDao.insert(
                    PlayerRewardEntity(
                        playerId = playerId,
                        source = "MISSION",
                        sourceId = mission.id,
                        rewardType = mission.rewardType,
                        rewardData = """{"diamond": ${mission.rewardAmount}}""",
                        status = "CLAIMED",
                        claimedDate = System.currentTimeMillis(),
                        title = mission.title,
                        description = "Completed mission: ${mission.title}"
                    )
                )
            }

            // Update player diamonds
            val player = playerDao.getPlayerById(playerId)
            if (player != null) {
                playerDao.updatePlayer(player.copy(diamond = player.diamond + totalDiamonds))
            }

            ClaimResult.Success(null)

        } catch (e: Exception) {
            ClaimResult.Error(e.message ?: "Unknown error")
        }
    }

    // === SHARED HELPER METHODS ===
    private suspend fun giveReward(playerId: Int, config: DailyRewardConfig, day: Int) {
        // Give diamond reward
        if (config.diamondAmount > 0) {
            val player = playerDao.getPlayerById(playerId)
            if (player != null) {
                playerDao.updatePlayer(player.copy(diamond = player.diamond + config.diamondAmount))
            }
        }

        // Give feature/item reward
        if (config.type.isNotEmpty() && config.value > 0) {
            val featureId = "${config.type}_${if (config.type == "frontHair") "f" else "f"}_${config.value}"
            playerItemDao.insert(
                PlayerItemEntity(
                    playerId = playerId,
                    itemType = config.type,
                    itemId = featureId,
                    acquiredDate = System.currentTimeMillis()
                )
            )
        }

        // Record reward in player_rewards table
        playerRewardDao.insert(
            PlayerRewardEntity(
                playerId = playerId,
                source = "DAILY_CHECKIN",
                sourceId = "day_$day",
                rewardType = if (config.diamondAmount > 0) "DIAMOND" else "FEATURE",
                rewardData = """{"diamond": ${config.diamondAmount}, "feature": "${config.type}_${config.value}"}""",
                status = "CLAIMED",
                claimedDate = System.currentTimeMillis(),
                title = "Daily Check-in Day $day",
                description = "Claimed daily reward for day $day"
            )
        )
    }

    // New helper to read current diamond count for a player
    suspend fun getPlayerDiamonds(playerId: Int): Int = withContext(Dispatchers.IO) {
        val player = playerDao.getPlayerById(playerId)
        player?.diamond ?: 0
    }

    // === DATA CLASSES ===
    data class DailyCheckInStatus(
        val currentDay: Int,
        val canClaim: Boolean,
        val claimedDays: Set<Int>,
        val streak: Int
    )

} // end of RewardRepository class

