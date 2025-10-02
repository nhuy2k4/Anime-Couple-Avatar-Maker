package com.app.base.ui.battle

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.app.base.R
import com.app.base.core.helper.CharacterDisplayHelper
import com.app.base.core.helper.ScoreEffectHelper
import com.app.base.database.AppDatabase
import com.app.base.databinding.FragmentBattleBinding
import com.app.base.core.helper.LayerSetupHelper
import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.base.activity.navigate

class BattleFragment : BaseFragment<FragmentBattleBinding, BattleViewModel>() {

    private lateinit var characterHelpers: List<CharacterDisplayHelper>
    private lateinit var scoreHelper1: ScoreEffectHelper
    private lateinit var scoreHelper2: ScoreEffectHelper
    private lateinit var characterHelper1: CharacterDisplayHelper
    private lateinit var characterHelper2: CharacterDisplayHelper
    private val handler = Handler(Looper.getMainLooper())
    private var hasJumped = false
    private val jumpRunnable = Runnable {
        if (isAdded && !hasJumped) {
            hasJumped = true
            navigate(R.id.checkPointFragment)
        }
    }
    override fun initView() {
        val layerHelper1 = LayerSetupHelper(binding.photoContainer1, binding.photoEditorView, requireContext())
        characterHelper1 = CharacterDisplayHelper(layerHelper1)
        val layerHelper2 = LayerSetupHelper(binding.photoContainer2, binding.photoEditorView1, requireContext())
        characterHelper2 = CharacterDisplayHelper(layerHelper2)
        characterHelpers = listOf(characterHelper1, characterHelper2)
        scoreHelper1 = ScoreEffectHelper(binding.battleContainer, binding.bonusContainer1)
        scoreHelper2 = ScoreEffectHelper(binding.battleContainer, binding.bonusContainer2)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // chỉ set timer 1 lần khi fragment được tạo
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
            characterHelpers[0].loadAndDisplayCharacterPair(maleJson, femaleJson, translateX = -140f, translateY = 100f, scale = 0.75f, maleOffset = 0f, femaleOffset = -80f)
            scoreHelper1.startLoop(binding.tvPlayerScore1, binding.tvBonusScore1, binding.tvBonus1)

            characterHelpers[1].loadAndDisplayCharacterPair(maleJson, femaleJson, translateX = 140f, translateY = 100f, scale = 0.75f, maleOffset = 80f)
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
