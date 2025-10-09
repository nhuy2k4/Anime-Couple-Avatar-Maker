package com.app.base.ui.checkpoint

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.app.base.R
import com.app.base.core.helper.LayerSetupHelper
import com.app.base.database.AppDatabase
import com.app.base.databinding.FragmentCheckPointBinding
import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.base.activity.navigate
import kotlinx.coroutines.launch
import org.json.JSONObject

class CheckPointFragment : BaseFragment<FragmentCheckPointBinding, CheckPointViewModel>() {

    private lateinit var layerHelper: LayerSetupHelper
    private var sourceMode: String? = null // "C" hoặc "Battle"

    override fun initView() {
        layerHelper = LayerSetupHelper(binding.photoContainer, binding.photoEditorView, requireContext())

        // Lấy mode được truyền vào (nếu có)
        sourceMode = arguments?.getString("sourceMode")
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
            // Khởi tạo lại layer manager
            layerHelper.initLayerManager()

            if (sourceMode == "C") {
                // 🎯 Trường hợp từ Mode C → chỉ hiển thị 1 nhân vật (ưu tiên female)
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
                // 🎭 Trường hợp bình thường (hiển thị cả 2 nhân vật)
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
