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
import com.brally.mobile.base.activity.navigate
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ArenaSelectFragment : BaseFragment<FragmentArenaSelectBinding, ArenaSelectViewModel>() {

    private val demoStages = listOf(
        R.drawable.stage_b, R.drawable.stage_b, R.drawable.stage_c,
        R.drawable.stage_a, R.drawable.stage_a, R.drawable.test_stage,
        R.drawable.stage_c, R.drawable.stage_c, R.drawable.test_stage,
        R.drawable.stage_b
    )

    private val stageViews = mutableListOf<HexagonImageView>()

    override fun initView() {
        binding.rootArena.post {
            addStagesToRow(binding.row1, 3)
            addStagesToRow(binding.row2, 4)
            addStagesToRow(binding.row3, 3)

            startRandomSelection()
        }
    }

    private fun addStagesToRow(row: LinearLayout, count: Int) {
        repeat(count) {
            val view = layoutInflater.inflate(R.layout.item_stage, row, false)
            val imgStage = view.findViewById<HexagonImageView>(R.id.imgStage)
            imgStage.setImageResource(demoStages[stageViews.size % demoStages.size])
            stageViews.add(imgStage)
            row.addView(view)
        }
    }

    private fun startRandomSelection() {
        if (stageViews.isEmpty()) return
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

            // ✅ Lấy stage được chọn
            val selectedBackgroundId = demoStages[(current - 1 + stageViews.size) % stageViews.size]


            // ✅ Điều hướng sang CategoryFragment với mode B
            val action = ArenaSelectFragmentDirections
                .actionArenaSelectFragmentToCategoryFragment(
                    outfitId = -1L,
                    fromHome = false,
                    backgroundId = selectedBackgroundId,
                    mode = "B"
                )
            findNavController().navigate(action)
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
