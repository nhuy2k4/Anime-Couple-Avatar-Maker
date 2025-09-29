package com.app.base.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.FrameLayout
import android.widget.ImageView
import ja.burhanrashid52.photoeditor.PhotoEditorView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CharacterManager(
    private val context: Context,
    private val photoEditorView: PhotoEditorView
) {
    private val characters = mutableMapOf<String, CharacterLayer>()
    private val _activeCharacterFlow = MutableStateFlow("female")
    val activeCharacterFlow = _activeCharacterFlow.asStateFlow()

    var activeCharacterId: String
        get() = _activeCharacterFlow.value
        private set(value) { _activeCharacterFlow.value = value }

    fun switchActiveCharacter() {
        activeCharacterId = if (activeCharacterId == "female") "male" else "female"
        // Khi set giá trị mới, _activeCharacterFlow sẽ emit -> collectLatest nhận được
    }
    /** Thêm nhân vật mới, giữ nguyên kích thước ảnh */
    fun addCharacter(id: String, bodyRes: Int, offsetX: Int, offsetY: Int) {
        val bitmap = BitmapFactory.decodeResource(context.resources, bodyRes)
        val bodyView = ImageView(context).apply {
            setImageBitmap(bitmap)
            layoutParams = FrameLayout.LayoutParams(bitmap.width, bitmap.height).apply {
                leftMargin = offsetX
                topMargin = offsetY
            }
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        photoEditorView.addView(bodyView)

        characters[id] = CharacterLayer(
            id = id,
            bodyRes = bodyRes,
            offsetX = offsetX,
            offsetY = offsetY,
            bodyView = bodyView,
            bodyOriginalWidth = bitmap.width,
            bodyOriginalHeight = bitmap.height
        )
    }

    /** Thêm hoặc thay feature đã có, từ drawable */
    fun addOrReplaceFeature(characterId: String, slot: String, resId: Int) {
        val character = characters[characterId] ?: return

        // Xóa feature cũ
        character.features[slot]?.let { oldView ->
            photoEditorView.removeView(oldView)
        }

        val bitmap = BitmapFactory.decodeResource(context.resources, resId)
        val offset = character.featureOffsets[slot] ?: FeatureOffset(0,0)

        val featureView = ImageView(context).apply {
            setImageBitmap(bitmap)
            layoutParams = FrameLayout.LayoutParams(bitmap.width, bitmap.height).apply {
                leftMargin = character.offsetX + offset.x
                topMargin = character.offsetY + offset.y
            }
            scaleType = ImageView.ScaleType.FIT_XY
        }

        photoEditorView.addView(featureView)
        character.features[slot] = featureView
    }

    /** Thêm feature từ assets, giữ nguyên kích thước ảnh */
    fun addFeatureFromAssets(
        slot: String,
        assetPath: String,
        featureOffsetRatioX: Float = 0f, // tỉ lệ X trên body gốc (0..1)
        featureOffsetRatioY: Float = 0f  // tỉ lệ Y trên body gốc (0..1)
    ) {
        val character = characters[activeCharacterId] ?: return

        // Xóa feature cũ nếu có
        character.features[slot]?.let { oldView ->
            photoEditorView.removeView(oldView)
        }

        // Load bitmap feature từ assets
        val featureBitmap = try {
            context.assets.open(assetPath).use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }

        val bodyView = character.bodyView ?: return

        // Đảm bảo body đã đo xong
        bodyView.post {
            val bodyWidth = bodyView.width
            val bodyHeight = bodyView.height

            // Tỉ lệ scale feature dựa trên body hiện tại / body gốc
            val scaleX = bodyWidth / character.bodyOriginalWidth.toFloat()
            val scaleY = bodyHeight / character.bodyOriginalHeight.toFloat()

            val scaledWidth = (featureBitmap.width * scaleX).toInt()
            val scaledHeight = (featureBitmap.height * scaleY).toInt()

            val scaledBitmap = Bitmap.createScaledBitmap(featureBitmap, scaledWidth, scaledHeight, true)

            // Vị trí feature dựa trên tỉ lệ offset
            val featureX = character.offsetX + (featureOffsetRatioX * bodyWidth).toInt()
            val featureY = character.offsetY + (featureOffsetRatioY * bodyHeight).toInt()

            val featureView = ImageView(context).apply {
                setImageBitmap(scaledBitmap)
                layoutParams = FrameLayout.LayoutParams(scaledWidth, scaledHeight).apply {
                    leftMargin = featureX
                    topMargin = featureY
                }
                scaleType = ImageView.ScaleType.FIT_XY
            }

            photoEditorView.addView(featureView)
            character.features[slot] = featureView
        }
    }



    /** Xóa tất cả feature của 1 nhân vật */
    fun clearFeatures(characterId: String) {
        val character = characters[characterId] ?: return
        character.features.values.forEach { photoEditorView.removeView(it) }
        character.features.clear()
    }

    /** Reset toàn bộ nhân vật và feature */
    fun resetAll() {
        characters.values.forEach { char ->
            char.features.values.forEach { photoEditorView.removeView(it) }
            char.features.clear()
        }
    }
}
