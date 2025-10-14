package com.app.base.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.app.base.database.entity.Feature

@Dao
interface FeatureDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(feature: Feature)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(features: List<Feature>)

    @Query("SELECT * FROM features ORDER BY category, id")
    suspend fun getAllFeatures(): List<Feature>

    @Query("SELECT * FROM features WHERE id = :id LIMIT 1")
    suspend fun getFeatureById(id: String): Feature?

    @Query("SELECT * FROM features WHERE category = :category ORDER BY id")
    suspend fun getFeaturesByCategory(category: String): List<Feature>

    @Query("SELECT * FROM features WHERE gender = :gender ORDER BY category, id")
    suspend fun getFeaturesByGender(gender: String): List<Feature>

    @Query("SELECT * FROM features WHERE category = :category AND gender = :gender ORDER BY id")
    suspend fun getFeaturesByCategoryAndGender(category: String, gender: String): List<Feature>

    @Query("SELECT * FROM features WHERE unlockCondition IS NULL OR unlockCondition = ''")
    suspend fun getUnlockedFeatures(): List<Feature>

    @Query("SELECT * FROM features WHERE category = :category AND (unlockCondition IS NULL OR unlockCondition = '') ORDER BY id")
    suspend fun getUnlockedFeaturesByCategory(category: String): List<Feature>

    @Query("SELECT DISTINCT category FROM features ORDER BY category")
    suspend fun getAllCategories(): List<String>

    @Query("SELECT DISTINCT gender FROM features ORDER BY gender")
    suspend fun getAllGenders(): List<String>

    @Query("DELETE FROM features WHERE id = :id")
    suspend fun deleteFeature(id: String)

    @Query("DELETE FROM features WHERE category = :category")
    suspend fun deleteFeaturesByCategory(category: String)

    @Query("DELETE FROM features")
    suspend fun deleteAll()

    @Update
    suspend fun update(feature: Feature)
}
