package com.app.base.core.helper

import android.util.Log

class CharacterDisplayHelper(private val layerSetupHelper: LayerSetupHelper) {

    /**
     * Load và hiển thị một cặp nhân vật, có thể truyền offset X/Y và scale riêng
     * @param maleJson: dữ liệu male
     * @param femaleJson: dữ liệu female
     * @param translateX: dịch X cho toàn bộ layers
     * @param translateY: dịch Y cho toàn bộ layers
     * @param scale: tỉ lệ nhân vật
     */
    fun loadAndDisplayCharacterPair(
        maleJson: Any,
        femaleJson: Any,
        translateX: Float = 0f,
        translateY: Float = 0f,
        scale: Float = 1f,
        maleOffset: Float = 0f,
        femaleOffset: Float = 0f
    ) {
        val newJson = org.json.JSONObject().apply {
            put("male", maleJson)
            put("female", femaleJson)
        }

        layerSetupHelper.mergeOutfitJson(newJson)
        layerSetupHelper.applyFeaturesToLayers()

        layerSetupHelper.photoContainer.post {
            adjustCharacter("male", translateX + maleOffset, translateY, scale)
            adjustCharacter("female", translateX + femaleOffset, translateY, scale)
        }
    }

    /**
     * Chỉnh vị trí + scale cho từng character
     */
    fun adjustCharacter(character: String, translateX: Float, translateY: Float, scale: Float) {
        val containerHeight = layerSetupHelper.photoContainer.height.toFloat()
        layerSetupHelper.layerManager.getLayersForCharacter(character).forEach { layerView ->
            layerView.scaleX = scale
            layerView.scaleY = scale
            val offsetY = (containerHeight * (1 - scale)) / 2
            layerView.translationX = translateX
            layerView.translationY = translateY + offsetY

            Log.d(
                "CharacterDisplay",
                "$character layer ${layerView.tag} -> scale=$scale, transX=$translateX, transY=${translateY + offsetY}"
            )
        }
    }
}
