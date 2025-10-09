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
     * @param maleOffset: offsetX riêng cho male
     * @param femaleOffset: offsetX riêng cho female
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
        // Đảm bảo body + feature đều được apply trước khi scale/translate
        layerSetupHelper.applyFeaturesToLayers(forCharacters = listOf("male", "female"))

        layerSetupHelper.photoContainer.post {
            adjustCharacter("male", translateX + maleOffset, translateY, scale)
            adjustCharacter("female", translateX + femaleOffset, translateY, scale)
        }
    }

    /**
     * Chỉnh vị trí + scale cho từng character, đảm bảo body luôn có trước
     */
    fun adjustCharacter(character: String, translateX: Float, translateY: Float, scale: Float) {
        val containerHeight = layerSetupHelper.photoContainer.height.toFloat()
        val layers = layerSetupHelper.layerManager.getLayersForCharacter(character)

        // Lấy bodyView
        val bodyView = layers.find { layerSetupHelper.layerManager.getKeyForLayerView(it) == "$character-body" }
            ?: run {
                // Nếu chưa có body, tạo mới
                val config = layerSetupHelper.characterConfigs[character] ?: return
                layerSetupHelper.layerManager.setLayer(
                    "$character-body",
                    config.baseResId,
                    scaleX = config.scale,
                    scaleY = config.scale,
                    offsetX = config.offsetX,
                    offsetY = config.offsetY
                )
                // Lấy lại bodyView sau khi tạo
                layerSetupHelper.layerManager.getLayersForCharacter(character)
                    .firstOrNull { layerSetupHelper.layerManager.getKeyForLayerView(it) == "$character-body" }
            } ?: return

        // Tính offsetY để center body khi scale
        val offsetY = (containerHeight * (1 - scale)) / 2

        // Áp dụng transform cho body trước
        bodyView.scaleX = scale
        bodyView.scaleY = scale
        bodyView.translationX = translateX
        bodyView.translationY = translateY + offsetY

        // Áp dụng cho tất cả feature bám theo body
        layers.filter { layerSetupHelper.layerManager.getKeyForLayerView(it) != "$character-body" }
            .forEach { featureView ->
                featureView.scaleX = scale
                featureView.scaleY = scale
                featureView.translationX = translateX
                featureView.translationY = translateY + offsetY
            }

        Log.d(
            "CharacterDisplay",
            "$character body -> scale=$scale, transX=$translateX, transY=${translateY + offsetY}"
        )
    }

    /**
     * Di chuyển toàn bộ character theo trục X
     */
    fun moveCharacterX(character: String, offsetX: Float) {
        layerSetupHelper.layerManager.getLayersForCharacter(character).forEach { layer ->
            layer.translationX = offsetX
        }
    }

    /**
     * Di chuyển toàn bộ character theo trục Y
     */
    fun moveCharacterY(character: String, offsetY: Float) {
        layerSetupHelper.layerManager.getLayersForCharacter(character).forEach { layer ->
            layer.translationY = offsetY
        }
    }

    /**
     * Scale toàn bộ character mà không thay đổi vị trí offsetY ban đầu
     */
    fun scaleCharacter(character: String, scale: Float) {
        val containerHeight = layerSetupHelper.photoContainer.height.toFloat()
        val offsetY = (containerHeight * (1 - scale)) / 2
        layerSetupHelper.layerManager.getLayersForCharacter(character).forEach { layer ->
            layer.scaleX = scale
            layer.scaleY = scale
            layer.translationY = offsetY
        }
    }
}
