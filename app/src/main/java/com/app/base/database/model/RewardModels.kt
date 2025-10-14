package com.app.base.database.model

import com.google.gson.annotations.SerializedName

// Reward data models
data class RewardData(
    val type: String, // "DIAMOND", "FEATURE", "BACKGROUND", "AVATAR"
    val amount: Int = 0, // for diamonds
    val itemId: String? = null, // for items
    val category: String? = null // for features
)

data class MissionData(
    val id: String,
    val title: String,
    val description: String,
    val type: String, // "WIN_MATCHES", "PLAY_MATCHES", "WATCH_ADS", "LOGIN_DAYS"
    val target: Int,
    val reward: RewardData,
    val isDaily: Boolean = false
)

data class DailyCheckInReward(
    val day: Int,
    val reward: RewardData,
    val isSpecial: Boolean = false
)

// Enum classes for better type safety
enum class RewardSource {
    DAILY_CHECKIN,
    MISSION,
    AD,
    PK_BOT
}

enum class RewardStatus {
    LOCKED,
    AVAILABLE,
    IN_PROGRESS,
    COMPLETED,
    CLAIMED
}

enum class RewardType {
    DIAMOND,
    FEATURE,
    BACKGROUND,
    AVATAR
}

enum class MissionType {
    WIN_MATCHES,
    PLAY_MATCHES,
    WATCH_ADS,
    LOGIN_DAYS,
    COLLECT_FEATURES,
    SPEND_DIAMONDS
}
