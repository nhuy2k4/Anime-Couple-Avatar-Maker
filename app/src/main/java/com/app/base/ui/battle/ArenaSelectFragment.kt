package com.app.base.ui.battle

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.app.base.R
import com.app.base.databinding.FragmentArenaSelectBinding
import com.brally.mobile.base.activity.BaseFragment
import com.app.base.core.utils.HexagonImageView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import com.app.base.data.model.BotData
import com.app.base.data.model.toBotData
import com.app.base.database.AppDatabase
import android.widget.TextView
import android.widget.ImageView
import android.animation.ObjectAnimator
import android.animation.AnimatorSet
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import com.app.base.database.entity.BackgroundEntity

class ArenaSelectFragment : BaseFragment<FragmentArenaSelectBinding, ArenaSelectViewModel>() {

    // Danh sách stage đọc từ JSON: mỗi stage gồm id (String) và imageResId (Int)
    private val demoStages = mutableListOf<Pair<String, String>>()

    private val stageViews = mutableListOf<HexagonImageView>()

    // Bot randomization data
    private val availableBots = mutableListOf<BotData>()
    private var selectedBot: BotData? = null

    override fun initView() {
        lifecycleScope.launch {
            loadBackgroundsFromDatabaseOrAssets()
            loadAvailableBots() // load bots sau khi đã có background
            binding.rootArena.post {
                addStagesToRow(binding.row1, 3)
                addStagesToRow(binding.row2, 4)
                addStagesToRow(binding.row3, 3)
                Log.d("ArenaSelect", "Available bots count = ${availableBots.size}")
                startBotRandomization()
            }
        }
    }
    private suspend fun loadBackgroundsFromDatabaseOrAssets() {
        demoStages.clear()
        val db = AppDatabase.getInstance(requireContext())
        val backgroundDao = db.backgroundDao()

        try {
            // 1️⃣ Lấy background từ DB
            val backgrounds = backgroundDao.getAllBackgrounds()

            if (backgrounds.isNotEmpty()) {
                Log.d("ArenaSelect", "Loaded ${backgrounds.size} backgrounds from DB")

                // Giới hạn tối đa 10 background
                for (bg in backgrounds.take(10)) {
                    // Không cần resId nữa — chỉ lưu file name trong assets
                    demoStages.add(bg.id to bg.image)
                }
            } else {
                // 2️⃣ Nếu DB trống → nạp đúng 10 background mẫu từ assets
                Log.d("ArenaSelect", "No backgrounds in DB → load from assets")

                val jsonStr = requireContext().assets.open("background/backgrounds.json")
                    .bufferedReader().use { it.readText() }
                val arr = JSONArray(jsonStr)

                val defaultBackgrounds = mutableListOf<BackgroundEntity>()

                for (i in 0 until arr.length().coerceAtMost(10)) {
                    val obj = arr.getJSONObject(i)
                    val id = obj.optString("id", null) ?: continue
                    val imagePath = obj.optString("image", null)
                    val name = obj.optString("name", "Unknown")
                    val unlockCondition = obj.optString("unlockCondition", null)
                    val entity = BackgroundEntity(
                        id = id,
                        name = name,
                        image = imagePath.substringAfter("background/"),
                        unlockCondition = unlockCondition
                    )

                    defaultBackgrounds.add(entity)
                }

                // ✅ Insert 1 lần duy nhất
                backgroundDao.insertAll(defaultBackgrounds)

                // Load lại để hiển thị
                for (bg in defaultBackgrounds) {
                    demoStages.add(bg.id to bg.image)
                }
            }

            // 3️⃣ Fallback nếu vẫn rỗng (VD: file JSON lỗi hoặc asset thiếu)
            if (demoStages.isEmpty()) {
                demoStages.addAll(
                    listOf(
                        "stage_a" to "stage_a.png",
                        "stage_b" to "stage_b.png",
                        "stage_c" to "stage_c.png",
                        "test_stage" to "test_stage.png"
                    )
                )
            }

            Log.d("ArenaSelect", "✅ Loaded ${demoStages.size} backgrounds for Arena")

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ArenaSelect", "❌ Failed to load backgrounds", e)
        }
    }


    private fun addStagesToRow(row: LinearLayout, count: Int) {
        repeat(count) {
            val view = layoutInflater.inflate(R.layout.item_stage, row, false)
            val imgStage = view.findViewById<HexagonImageView>(R.id.imgStage)
            val stage = demoStages[stageViews.size % demoStages.size]
            val imagePath = stage.second // tạm lát sẽ đổi kiểu

            try {
                requireContext().assets.open(stage.second).use { input ->
                    val bitmap = android.graphics.BitmapFactory.decodeStream(input)
                    imgStage.setImageBitmap(bitmap)
                }
            } catch (e: Exception) {
                Log.e("ArenaSelect", "❌ Failed to load asset: $imagePath", e)
            }

            stageViews.add(imgStage)
            row.addView(view)
        }
    }


    /**
     * Load available bots for randomization effect
     */
    private suspend fun loadAvailableBots() {
        availableBots.clear()
        try {
            val dao = AppDatabase.getInstance(requireContext()).botDao()
            val botEntities = dao.getAllBots()

            if (botEntities.isNotEmpty()) {
                availableBots.addAll(botEntities.map { it.toBotData() })
            } else {
                loadBotsFromAssets()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            loadBotsFromAssets()
        }

        Log.d("ArenaSelect", "✅ Bots loaded: ${availableBots.size}")
    }


    private fun loadBotsFromAssets() {
        try {
            val jsonStr = requireContext().assets.open("data/bots.json")
                .bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(jsonStr)
            val botsArray = jsonObject.getJSONArray("bots")

            for (i in 0 until botsArray.length()) {
                val botObj = botsArray.getJSONObject(i)

                // Parse male features
                val maleFeatures = mutableMapOf<String, String>()
                val maleObj = botObj.getJSONObject("maleFeatures")
                for (key in maleObj.keys()) {
                    maleFeatures[key] = maleObj.getString(key)
                }

                // Parse female features
                val femaleFeatures = mutableMapOf<String, String>()
                val femaleObj = botObj.getJSONObject("femaleFeatures")
                for (key in femaleObj.keys()) {
                    femaleFeatures[key] = femaleObj.getString(key)
                }

                // Create BotAvatar
                val botAvatar = com.app.base.data.model.BotAvatar(
                    maleFeatures = maleFeatures,
                    femaleFeatures = femaleFeatures
                )

                // Parse difficulty
                val difficultyStr = botObj.getString("difficulty")
                val difficulty = com.app.base.data.model.BotDifficulty.valueOf(difficultyStr)

                // Create BotData
                val botData = BotData(
                    id = botObj.getInt("id"),
                    name = botObj.getString("name"),
                    avatar = botAvatar,
                    difficulty = difficulty,
                    avatarImage = botObj.getString("avatarImage")
                )

                availableBots.add(botData)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // If everything fails, we'll work with empty bot list
        }
    }

    /**
     * Start combined bot and background randomization
     */
    private fun startBotRandomization() {
        if (availableBots.isEmpty()) {
            // If no bots available, only randomize background
            startBackgroundOnlyRandomization()
            return
        }

        lifecycleScope.launch {
            // Phase 1: Random bot selection
            var currentBotIndex = 0
            val botDuration = 2000L
            var elapsed = 0L
            var delayTime = 150L

            while (elapsed < botDuration) {
                val currentBot = availableBots[currentBotIndex % availableBots.size]
                updateBotDisplay(currentBot)

                currentBotIndex = (currentBotIndex + 1) % availableBots.size
                delay(delayTime)
                elapsed += delayTime

                if (delayTime < 400) delayTime += 20
            }

            // Select final bot
            selectedBot = availableBots[(currentBotIndex - 1 + availableBots.size) % availableBots.size]
            updateBotDisplay(selectedBot!!)
            animateBotSelection()

            delay(500)

            // Phase 2: Random background selection
            startBackgroundRandomization()
        }
    }

    /**
     * Randomize background selection with visual effect
     */
    private fun startBackgroundRandomization() {
        if (stageViews.isEmpty()) {
            // If no stages available, navigate directly
            navigateWithSelectedData()
            return
        }

        lifecycleScope.launch {
            var current = 0
            val totalDuration = 2000L
            var elapsed = 0L
            var delayTime = 80L

            while (elapsed < totalDuration) {
                highlightAt(current)
                current = (current + 1) % stageViews.size
                delay(delayTime)
                elapsed += delayTime
                if (delayTime < 400) delayTime += 12
            }

            val selectedPos = (current - 1 + stageViews.size) % stageViews.size
            highlightAt(selectedPos)

            // Get selected background
            val selectedStage = demoStages[selectedPos]
            val selectedBackgroundId = selectedStage.first

            delay(500)

            // Navigate with both selected bot and background
            navigateWithSelectedData(selectedBackgroundId)
        }
    }

    /**
     * Handle background randomization when no bots are available
     */
    private fun startBackgroundOnlyRandomization() {
        startBackgroundRandomization()
    }

    /**
     * Navigate to next screen with selected bot and background data
     */
    private fun navigateWithSelectedData(backgroundId: String? = null) {
        val bundle = Bundle().apply {
            // Pass selected bot data
            selectedBot?.let { bot ->
                putInt("botId", bot.id)
                putString("selectedBotName", bot.name)
                putString("selectedBotAvatar", bot.avatarImage)
                putString("selectedBotDifficulty", bot.difficulty.name)
            }

            // Pass selected background (or fallback)
            val finalBackgroundId = backgroundId ?: demoStages.firstOrNull()?.first ?: "stage_b"
            putString("backgroundId", finalBackgroundId)

            // Standard battle mode data
            putString("mode", "B")
            putString("gender", "female")
            putLong("outfitId", -1L)
            putBoolean("isFromGallery", false)
            putString("outfitJson", "")
        }

        findNavController().navigate(R.id.categoryFragment, bundle)
    }

    /**
     * Update bot display with avatar and name
     */
    private fun updateBotDisplay(bot: BotData) {
        try {
            // Update bot name using binding
            binding.tvSelectedBotName.text = bot.name

            // Update bot difficulty indicator color
            val difficultyColor = when (bot.difficulty) {
                com.app.base.data.model.BotDifficulty.EASY -> android.R.color.holo_green_light
                com.app.base.data.model.BotDifficulty.NORMAL -> android.R.color.holo_orange_light
                com.app.base.data.model.BotDifficulty.HARD -> android.R.color.holo_red_light
            }
            binding.tvSelectedBotName.setTextColor(
                resources.getColor(difficultyColor, null)
            )

            // Update bot avatar using binding
            binding.imgSelectedBotAvatar.setImageResource(
                getBotAvatarResource(bot)
            )

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Get avatar resource for bot display
     */
    private fun getBotAvatarResource(bot: BotData): Int {
        return try {
            // Try to get avatar based on bot's avatar image
            val resName = bot.avatarImage.substringBeforeLast('.')
            val resId = resources.getIdentifier(resName, "drawable", requireContext().packageName)
            if (resId != 0) resId else R.drawable.avatar_2
        } catch (e: Exception) {
            R.drawable.avatar_2
        }
    }

    /**
     * Animate bot selection with scale and alpha effects
     */
    private fun animateBotSelection() {
        binding.imgSelectedBotAvatar?.let { avatarView ->
            val scaleUpX = ObjectAnimator.ofFloat(avatarView, "scaleX", 1.0f, 1.2f)
            val scaleUpY = ObjectAnimator.ofFloat(avatarView, "scaleY", 1.0f, 1.2f)
            val scaleDownX = ObjectAnimator.ofFloat(avatarView, "scaleX", 1.2f, 1.0f)
            val scaleDownY = ObjectAnimator.ofFloat(avatarView, "scaleY", 1.2f, 1.0f)

            val animatorSet = AnimatorSet()
            animatorSet.play(scaleUpX).with(scaleUpY)
            animatorSet.play(scaleDownX).with(scaleDownY).after(scaleUpX)
            animatorSet.duration = 300
            animatorSet.interpolator = AccelerateDecelerateInterpolator()
            animatorSet.start()
        }

        binding.tvSelectedBotName?.let { nameView ->
            val fadeOut = ObjectAnimator.ofFloat(nameView, "alpha", 1.0f, 0.3f)
            val fadeIn = ObjectAnimator.ofFloat(nameView, "alpha", 0.3f, 1.0f)

            val nameAnimSet = AnimatorSet()
            nameAnimSet.play(fadeIn).after(fadeOut)
            nameAnimSet.duration = 200
            nameAnimSet.start()
        }
    }

    private fun highlightAt(position: Int) {
        stageViews.forEachIndexed { index, imgStage ->
            val overlay =
                (imgStage.parent as View).findViewById<HexagonImageView>(R.id.highlightOverlay)

            if (index == position) {
                overlay.alpha = 1f
                overlay.animate().alpha(0.2f).setDuration(400).withEndAction {
                    overlay.animate().alpha(1f).setDuration(400).start()
                }.start()
            } else {
                overlay.animate().cancel()
                overlay.alpha = 0f
            }
        }
    }

    override fun initData() {}
    override fun initListener() {}
}
