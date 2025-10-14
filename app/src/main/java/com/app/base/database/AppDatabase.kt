package com.app.base.database
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.app.base.database.dao.*
import com.app.base.database.entity.*

@Database(
    entities = [
        OutfitEntity::class,
        PlayerEntity::class,
        PlayerItemEntity::class,
        PlayerRewardEntity::class,
        DailyCheckInEntity::class,
        BackgroundEntity::class,
        BotEntity::class,
        CosplayResultEntity::class,
        Feature::class,
        MissionEntity::class
    ],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun outfitDao(): OutfitDao
    abstract fun playerDao(): PlayerDao
    abstract fun playerItemDao(): PlayerItemDao
    abstract fun playerRewardDao(): PlayerRewardDao
    abstract fun dailyCheckInDao(): DailyCheckInDao
    abstract fun backgroundDao(): BackgroundDao
    abstract fun botDao(): BotDao
    abstract fun cosplayResultDao(): CosplayResultDao
    abstract fun featureDao(): FeatureDao
    abstract fun missionDao(): MissionDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dressup_db"
                ).fallbackToDestructiveMigration() // For development - remove in production
                .build().also { instance = it }
            }
        }

        // Alias for compatibility with older callers
        fun getDatabase(context: Context): AppDatabase = getInstance(context)
    }
}
