package com.app.base.ui.battle

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.app.base.R
import com.app.base.core.helper.LayerSetupHelper
import com.app.base.core.helper.ScoreEffectHelper
import com.app.base.database.AppDatabase
import com.app.base.databinding.FragmentBattleBinding
import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.base.activity.navigate
import org.json.JSONObject

class BattleFragment : BaseFragment<FragmentBattleBinding, BattleViewModel>() {

    private lateinit var layerHelper1: LayerSetupHelper
    private lateinit var layerHelper2: LayerSetupHelper
    private lateinit var scoreHelper1: ScoreEffectHelper
    private lateinit var scoreHelper2: ScoreEffectHelper

    private val handler = Handler(Looper.getMainLooper())
    private var hasJumped = false
    private val jumpRunnable = Runnable {
        if (isAdded && !hasJumped) {
            hasJumped = true
            navigate(R.id.checkPointFragment)
        }
    }

    override fun initView() {
        layerHelper1 = LayerSetupHelper(binding.photoContainer1, binding.photoEditorView, requireContext())
        layerHelper2 = LayerSetupHelper(binding.photoContainer2, binding.photoEditorView1, requireContext())

        scoreHelper1 = ScoreEffectHelper(binding.battleContainer, binding.bonusContainer1)
        scoreHelper2 = ScoreEffectHelper(binding.battleContainer, binding.bonusContainer2)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler.postDelayed(jumpRunnable, 10_000)
    }

    override fun initData() {
        loadBattleOutfits()
    }

    override fun initListener() {
        binding.heartPoint.setOnClickListener {
            scoreHelper1.showClickBonus(binding.tvPlayerScore1, binding.heartPoint, 1)
        }
    }

    private fun loadBattleOutfits() {
        viewModel.outfitLiveData.observe(viewLifecycleOwner) { (maleJson, femaleJson) ->

            // Hiển thị nhân vật 1
            layerHelper1.initLayerManager()
            layerHelper1.setupInitialLayers(listOf("male", "female"), offsetsX = mapOf("male" to -100f, "female" to -350f))
            layerHelper1.setOutfitJson(JSONObject().apply {
                put("male", maleJson)
                put("female", femaleJson)
            }.toString())
            layerHelper1.applyFeaturesToLayers()

            scoreHelper1.startLoop(binding.tvPlayerScore1, binding.tvBonusScore1, binding.tvBonus1)

            // Hiển thị nhân vật 2
            layerHelper2.initLayerManager()
            layerHelper2.setupInitialLayers(listOf("male", "female"), offsetsX = mapOf("male" to 320f, "female" to 100f))
            layerHelper2.setOutfitJson(JSONObject().apply {
                put("male", maleJson)
                put("female", femaleJson)
            }.toString())
            layerHelper2.applyFeaturesToLayers()

            scoreHelper2.startLoop(binding.tvPlayerScore2, binding.tvBonusScore2, binding.tvBonus2)
        }

        viewModel.loadLatestOutfit(AppDatabase.getInstance(requireContext()))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        scoreHelper1.stopLoop()
        scoreHelper2.stopLoop()
        handler.removeCallbacks(jumpRunnable)
    }
}
