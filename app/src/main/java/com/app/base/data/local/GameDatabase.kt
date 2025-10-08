package com.app.base.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.base.data.models.*

@Database(
    entities = [Player::class, Outfit::class, Mission::class, Milestone::class, CosplayResult::class, Bot::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GameDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun outfitDao(): OutfitDao
    abstract fun missionDao(): MissionDao
    abstract fun milestoneDao(): MilestoneDao
    abstract fun cosplayResultDao(): CosplayResultDao
    abstract fun botDao(): BotDao

    companion object {
        @Volatile
        private var INSTANCE: GameDatabase? = null

        fun getInstance(context: Context): GameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "game_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

