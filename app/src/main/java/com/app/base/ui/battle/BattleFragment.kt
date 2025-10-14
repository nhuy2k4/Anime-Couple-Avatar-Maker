package com.app.base.ui.battle

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.app.base.core.helper.LayerSetupHelper
import com.app.base.core.helper.ScoreEffectHelper
import com.app.base.data.model.*
import com.app.base.database.AppDatabase
import com.app.base.database.entity.BotEntity
import com.app.base.databinding.FragmentBattleBinding
import com.app.base.utils.PlayerManager
import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.base.activity.navigate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class BattleFragment : BaseFragment<FragmentBattleBinding, BattleViewModel>() {
    private val TAG = "BattleFragment"

    private lateinit var layerHelper1: LayerSetupHelper
    private lateinit var layerHelper2: LayerSetupHelper
    private lateinit var scoreHelper1: ScoreEffectHelper
    private lateinit var scoreHelper2: ScoreEffectHelper
    private lateinit var playerManager: PlayerManager
    private lateinit var currentBot: BotData

    private val uiHandler = Handler(Looper.getMainLooper())
    private var hasJumped = false
    private val battleDuration = 30_000L

    private val jumpRunnable = Runnable {
        if (isAdded && !hasJumped) {
            hasJumped = true
            finishBattle()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uiHandler.postDelayed(jumpRunnable, battleDuration)
    }

    override fun initView() {
        layerHelper1 = LayerSetupHelper(binding.photoContainer1, binding.photoEditorView, requireContext())
        layerHelper2 = LayerSetupHelper(binding.photoContainer2, binding.photoEditorView1, requireContext())
        scoreHelper1 = ScoreEffectHelper(binding.battleContainer, binding.bonusContainer1)
        scoreHelper2 = ScoreEffectHelper(binding.battleContainer, binding.bonusContainer2)
        playerManager = PlayerManager.getInstance(requireContext())

        // Khởi tạo player từ JSON
        val outfitJsonStr = arguments?.getString("outfitJson")
        if (!outfitJsonStr.isNullOrEmpty()) {
            displayPlayerFromJson(outfitJsonStr)
        } else {
            loadDefaultPlayerOutfit()
        }

        // Khởi tạo bot async từ DB
        val botId = arguments?.getInt("botId", -1) ?: -1
        lifecycleScope.launch(Dispatchers.IO) {
            val botData = loadBotFromDb(botId)
            withContext(Dispatchers.Main) {
                currentBot = botData
                binding.tvPlayerName2.text = currentBot.name
                displayBotAvatar() // apply outfit đúng DB
                startBotScoring()
            }
        }

        binding.tvPlayerScore1.text = "0"
        binding.tvPlayerScore2.text = "0"
    }

    private fun displayPlayerFromJson(outfitJsonStr: String) {
        try {
            layerHelper1.initLayerManager()
            layerHelper1.setupInitialLayers(listOf("male","female"), mapOf("male" to -100f, "female" to -350f)) {
                layerHelper1.setOutfitJsonWithBackground(outfitJsonStr)
                Log.d(TAG, "Applied player outfit JSON")
                scoreHelper1.startLoop(binding.tvPlayerScore1, binding.tvBonusScore1, binding.tvBonus1)
            }
        } catch (e: Exception) {
            Log.e(TAG, "displayPlayerFromJson failed: ${e.message}")
            loadDefaultPlayerOutfit()
        }
    }

    private fun loadDefaultPlayerOutfit() {
        val maleJson = JSONObject(mapOf("frontHair" to "frontHair_m_3", "eyes" to "eyes_m_3"))
        val femaleJson = JSONObject(mapOf("frontHair" to "frontHair_f_1", "eyes" to "eyes_f_2"))
        val outfitJson = JSONObject().apply {
            put("male", maleJson)
            put("female", femaleJson)
        }
        displayPlayerFromJson(outfitJson.toString())
    }

    private suspend fun loadBotFromDb(botId: Int): BotData {
        val dao = AppDatabase.getInstance(requireContext()).botDao()

        // Nếu có botId được truyền vào (khác -1), load bot theo ID đó
        if (botId != -1) {
            val entity = dao.getBotById(botId)
            if (entity != null) {
                Log.d(TAG, "Loaded specific bot with ID: $botId, name: ${entity.name}")
                return entity.toBotData()
            } else {
                Log.w(TAG, "Bot with ID $botId not found in database, getting random bot")
            }
        }

        // Fallback: lấy random bot nếu không có ID hoặc không tìm thấy bot theo ID
        val randomEntity = dao.getRandomBot()
        if (randomEntity == null) {
            throw IllegalStateException("No bots found in database. Please populate bot data from JSON first.")
        }

        Log.d(TAG, "Using random bot: ID=${randomEntity.id}, name=${randomEntity.name}")
        return randomEntity.toBotData()
    }


    private fun displayBotAvatar() {
        try {
            Log.d(TAG, "Initializing bot avatar display")
            layerHelper2.initLayerManager()
            layerHelper2.setupInitialLayers(
                listOf("male", "female"),
                offsetsX = mapOf("male" to 320f, "female" to 100f)
            )

            // ✅ Không dùng callback ở đây nữa
            val botOutfitJson = JSONObject().apply {
                put("male", JSONObject(currentBot.avatar.maleFeatures))
                put("female", JSONObject(currentBot.avatar.femaleFeatures))
            }

            Log.d(TAG, "Applying bot JSON: $botOutfitJson")
            layerHelper2.setOutfitJson(botOutfitJson.toString())
            layerHelper2.applyFeaturesToLayers()
            Log.d(TAG, "Bot features applied successfully")
        } catch (e: Exception) {
            Log.e(TAG, "displayBotAvatar failed: ${e.message}", e)
        }
    }



    private fun startBotScoring() {
        uiHandler.postDelayed({
            scoreHelper2.startLoop(binding.tvPlayerScore2, binding.tvBonusScore2, binding.tvBonus2)
        }, 800)
    }

    private fun finishBattle() {
        scoreHelper1.stopLoop()
        scoreHelper2.stopLoop()
        uiHandler.postDelayed({
            val playerScore = binding.tvPlayerScore1.text.toString().toInt()
            val botScore = binding.tvPlayerScore2.text.toString().toInt()
            val bundle = Bundle().apply {
                putString("sourceMode","Battle")
                putInt("playerScore", playerScore)
                putInt("botScore", botScore)
                putString("botName", currentBot.name)
                putString("battleResult", if(playerScore>botScore) "Victory" else "Defeat")
            }
            navigate(com.app.base.R.id.checkPointFragment, bundle)
        }, 800)
    }

    override fun initListener() {
        binding.heartPoint.setOnClickListener { scoreHelper1.showClickBonus(binding.tvPlayerScore1, binding.heartPoint, 1) }
        binding.btnResponse1.setOnClickListener { scoreHelper1.showClickBonus(binding.tvPlayerScore1, binding.btnResponse1, 90) }
        binding.btnResponse2.setOnClickListener { scoreHelper1.showClickBonus(binding.tvPlayerScore1, binding.btnResponse2, 118) }
        binding.btnResponse3.setOnClickListener { scoreHelper1.showClickBonus(binding.tvPlayerScore1, binding.btnResponse3, 150) }
    }

    override fun initData() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        scoreHelper1.stopLoop()
        scoreHelper2.stopLoop()
        uiHandler.removeCallbacks(jumpRunnable)
    }
}
