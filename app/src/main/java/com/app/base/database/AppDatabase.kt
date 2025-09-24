package com.app.base.database
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(entities = [OutfitEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun outfitDao(): OutfitDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "dressup_db"
                ).build().also { instance = it }
            }
        }
    }
}
