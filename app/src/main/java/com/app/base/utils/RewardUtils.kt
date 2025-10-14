package com.app.base.utils

import com.app.base.database.model.RewardData
import com.app.base.database.model.RewardType
import com.app.base.database.model.MissionType
import com.app.base.database.model.MissionData

object RewardUtils {

    // Daily check-in rewards configuration
    fun getDailyCheckInRewards(): Map<Int, RewardData> {
        return mapOf(
            1 to RewardData(RewardType.DIAMOND.name, 50),
            2 to RewardData(RewardType.DIAMOND.name, 75),
            3 to RewardData(RewardType.FEATURE.name, itemId = "random_feature"),
            4 to RewardData(RewardType.DIAMOND.name, 100),
            5 to RewardData(RewardType.BACKGROUND.name, itemId = "random_background"),
            6 to RewardData(RewardType.DIAMOND.name, 150),
            7 to RewardData(RewardType.AVATAR.name, itemId = "special_avatar")
        )
    }

    // Mission templates
    fun getDefaultMissionTemplates(): List<MissionData> {
        return listOf(
            MissionData(
                id = "daily_login",
                title = "Daily Login",
                description = "Login to the game",
                type = MissionType.LOGIN_DAYS.name,
                target = 1,
                reward = RewardData(RewardType.DIAMOND.name, 30),
                isDaily = true
            ),
            MissionData(
                id = "play_3_matches",
                title = "Battle Master",
                description = "Play 3 PK matches",
                type = MissionType.PLAY_MATCHES.name,
                target = 3,
                reward = RewardData(RewardType.DIAMOND.name, 100)
            ),
            MissionData(
                id = "win_5_matches",
                title = "Victory Streak",
                description = "Win 5 PK matches",
                type = MissionType.WIN_MATCHES.name,
                target = 5,
                reward = RewardData(RewardType.FEATURE.name, itemId = "rare_feature")
            ),
            MissionData(
                id = "watch_3_ads",
                title = "Ad Watcher",
                description = "Watch 3 advertisements",
                type = MissionType.WATCH_ADS.name,
                target = 3,
                reward = RewardData(RewardType.DIAMOND.name, 75)
            ),
            MissionData(
                id = "collect_10_features",
                title = "Collector",
                description = "Collect 10 different features",
                type = MissionType.COLLECT_FEATURES.name,
                target = 10,
                reward = RewardData(RewardType.BACKGROUND.name, itemId = "special_bg")
            ),
            MissionData(
                id = "spend_500_diamonds",
                title = "Big Spender",
                description = "Spend 500 diamonds",
                type = MissionType.SPEND_DIAMONDS.name,
                target = 500,
                reward = RewardData(RewardType.AVATAR.name, itemId = "premium_avatar")
            )
        )
    }

    // Reward calculation for PK battles
    fun calculatePkReward(isWin: Boolean, streak: Int = 0): RewardData {
        val baseReward = if (isWin) 50 else 25
        val bonusReward = if (isWin && streak >= 3) 25 else 0 // Bonus for win streak
        return RewardData(RewardType.DIAMOND.name, baseReward + bonusReward)
    }

    // Ad reward configuration
    fun getAdReward(): RewardData {
        return RewardData(RewardType.DIAMOND.name, 25)
    }

    // Convert reward to display string
    fun getRewardDisplayText(reward: RewardData): String {
        return when (reward.type) {
            RewardType.DIAMOND.name -> "${reward.amount} Diamonds"
            RewardType.FEATURE.name -> "New Feature"
            RewardType.BACKGROUND.name -> "New Background"
            RewardType.AVATAR.name -> "New Avatar"
            else -> "Reward"
        }
    }

    // Check if reward can be claimed
    fun canClaimReward(status: String): Boolean {
        return status == "COMPLETED"
    }

    // Generate random item IDs (you can replace with actual item selection logic)
    fun getRandomFeatureId(): String = "feature_${(1..100).random()}"
    fun getRandomBackgroundId(): String = "bg_${(1..20).random()}"
    fun getRandomAvatarId(): String = "avatar_${(1..50).random()}"
}
