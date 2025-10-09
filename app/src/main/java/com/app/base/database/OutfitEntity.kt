package com.app.base.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * New OutfitEntity schema:
 * - id: Int primary key auto-generated
 * - features: Map<layer, featureId>
 * - backgroundId: id of selected background (nullable)
 * - savedAt: timestamp when saved
 * - thumbnailPath: optional cached thumbnail path
 */
@Entity(tableName = "outfits")
data class OutfitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val features: Map<String, String> = emptyMap(), // layer -> featureId
    val backgroundId: String? = null,
    val savedAt: Long = System.currentTimeMillis(),
    val thumbnailPath: String? = null
)

