package com.app.base.data.model

import com.app.base.database.entity.BotEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class BotData(
    val id: Int,
    val name: String,
    val avatar: BotAvatar,
    val difficulty: BotDifficulty = BotDifficulty.NORMAL,
    val avatarImage: String = "avatar_1" // Add avatar image field
)

data class BotAvatar(
    val maleFeatures: Map<String, String>,
    val femaleFeatures: Map<String, String>
)

enum class BotDifficulty {
    EASY,    // Bot scores slower
    NORMAL,  // Standard scoring
    HARD     // Bot scores faster
}

// Extension functions to convert between BotEntity and BotData
fun BotEntity.toBotData(): BotData {
    val gson = Gson()
    val mapType = object : TypeToken<Map<String, String>>() {}.type

    val maleFeatures: Map<String, String> = try {
        gson.fromJson(this.maleFeatures, mapType)
    } catch (e: Exception) {
        emptyMap()
    }

    val femaleFeatures: Map<String, String> = try {
        gson.fromJson(this.femaleFeatures, mapType)
    } catch (e: Exception) {
        emptyMap()
    }

    val difficulty = try {
        BotDifficulty.valueOf(this.difficulty)
    } catch (e: Exception) {
        BotDifficulty.NORMAL
    }

    return BotData(
        id = this.id,
        name = this.name,
        avatar = BotAvatar(
            maleFeatures = maleFeatures,
            femaleFeatures = femaleFeatures
        ),
        difficulty = difficulty,
        avatarImage = this.avatarImage
    )
}
