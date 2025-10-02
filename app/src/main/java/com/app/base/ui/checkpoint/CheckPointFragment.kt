package com.app.base.ui.checkpoint

import androidx.lifecycle.lifecycleScope
import com.app.base.R
import com.app.base.core.helper.CharacterDisplayHelper
import com.app.base.core.helper.LayerSetupHelper
import com.app.base.database.AppDatabase
import com.app.base.databinding.FragmentCheckPointBinding
import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.base.activity.navigate
import kotlinx.coroutines.launch

class CheckPointFragment : BaseFragment<FragmentCheckPointBinding, CheckPointViewModel>() {

    private lateinit var characterHelper: CharacterDisplayHelper

    override fun initView() {
        val layerHelper = LayerSetupHelper(binding.photoContainer, binding.photoEditorView, requireContext())
        characterHelper = CharacterDisplayHelper(layerHelper)
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
            characterHelper.loadAndDisplayCharacterPair(maleJson, femaleJson, translateX = -140f, translateY = 100f, scale = 0.75f)
        }

        viewModel.loadLatestOutfit(AppDatabase.getInstance(requireContext()))
    }
    override fun onDestroyView() {
        super.onDestroyView()
    }
}
