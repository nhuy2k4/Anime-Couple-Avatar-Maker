package com.app.base.ui.checkpoint

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.app.base.R
import com.app.base.core.helper.LayerSetupHelper
import com.app.base.database.AppDatabase
import com.app.base.databinding.FragmentCheckPointBinding
import com.app.base.utils.PlayerManager
import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.base.activity.navigate
import kotlinx.coroutines.launch
import org.json.JSONObject

class CheckPointFragment : BaseFragment<FragmentCheckPointBinding, CheckPointViewModel>() {

    private lateinit var layerHelper: LayerSetupHelper
    private lateinit var playerManager: PlayerManager
    private var sourceMode: String? = null // "C", "Battle", etc.

    override fun initView() {
        layerHelper = LayerSetupHelper(binding.photoContainer, binding.photoEditorView, requireContext())
        playerManager = PlayerManager.getInstance(requireContext())

        // Get mode and battle result from arguments
        sourceMode = arguments?.getString("sourceMode")

        // Handle battle result display
        handleBattleResult()

        // Update diamond count
        updateDiamondCount()
    }

    private fun handleBattleResult() {
        val battleResult = arguments?.getString("battleResult")
        val playerScore = arguments?.getInt("playerScore", 0) ?: 0
        val botScore = arguments?.getInt("botScore", 0) ?: 0
        val botName = arguments?.getString("botName", "Bot")

        when (sourceMode) {
            "Battle" -> {
                when (battleResult) {
                    "Victory" -> {
                        binding.tvResult.text = "VICTORY!"
                        binding.tvResult.setTextColor(resources.getColor(android.R.color.holo_green_light, null))
                        // Could add victory celebration effects here
                    }
                    "Defeat" -> {
                        binding.tvResult.text = "DEFEAT"
                        binding.tvResult.setTextColor(resources.getColor(android.R.color.holo_red_light, null))
                        // Could add defeat effects here
                    }
                    else -> {
                        binding.tvResult.text = "BATTLE END"
                        binding.tvResult.setTextColor(resources.getColor(android.R.color.white, null))
                    }
                }

                // You could also display scores somewhere in the UI if needed
                // For now, we'll keep the main result display simple

            }
            "C" -> {
                // Mode C result
                binding.tvResult.text = "COMPLETE!"
                binding.tvResult.setTextColor(resources.getColor(android.R.color.holo_blue_light, null))
            }
            else -> {
                // Default case
                binding.tvResult.text = "TRY AGAIN"
                binding.tvResult.setTextColor(resources.getColor(android.R.color.white, null))
            }
        }
    }

    private fun updateDiamondCount() {
        lifecycleScope.launch {
            try {
                val diamonds = playerManager.getPlayerDiamonds()
                binding.tvDiamondCount.text = diamonds.toString()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun initData() {
        loadRewardOutfits()
    }

    override fun initListener() {
        binding.tvNoThanks.setOnClickListener {
            lifecycleScope.launch {
                val db = AppDatabase.getInstance(requireContext())
                val outfitDao = db.outfitDao()
                val latest = outfitDao.getLatestOutfit()
                if (latest != null) {
                    outfitDao.deleteOutfit(latest.id)
                }
                navigate(R.id.homeFragment)
            }
        }
    }

    private fun loadRewardOutfits() {
        viewModel.outfitLiveData.observe(viewLifecycleOwner) { (maleJson, femaleJson) ->
            // Initialize layer manager
            layerHelper.initLayerManager()

            if (sourceMode == "C") {
                // Mode C: display single character (prioritize female)
                val mainCharacter = when {
                    femaleJson != null -> "female"
                    maleJson != null -> "male"
                    else -> return@observe
                }

                layerHelper.setupInitialLayers(listOf(mainCharacter))
                val outfitJson = JSONObject().apply {
                    put(mainCharacter, if (mainCharacter == "female") femaleJson else maleJson)
                }
                layerHelper.setOutfitJson(outfitJson.toString())
                layerHelper.applyFeaturesToLayers()

            } else {
                // Normal case: display both characters
                layerHelper.setupInitialLayers(
                    listOf("male", "female"),
                    offsetsX = mapOf("male" to -140f, "female" to 150f)
                )
                val outfitJson = JSONObject().apply {
                    put("male", maleJson)
                    put("female", femaleJson)
                }
                layerHelper.setOutfitJson(outfitJson.toString())
                layerHelper.applyFeaturesToLayers()
            }
        }

        viewModel.loadLatestOutfit(AppDatabase.getInstance(requireContext()))
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}
