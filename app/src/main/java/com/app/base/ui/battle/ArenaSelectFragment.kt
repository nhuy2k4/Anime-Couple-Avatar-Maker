package com.app.base.ui.battle

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

class ArenaSelectFragment : BaseFragment<FragmentArenaSelectBinding, ArenaSelectViewModel>() {

    // Danh sách stage đọc từ JSON: mỗi stage gồm id (String) và imageResId (Int)
    private val demoStages = mutableListOf<Pair<String, Int>>()
    private val stageViews = mutableListOf<HexagonImageView>()

    override fun initView() {
        // Load backgrounds từ assets/background/backgrounds.json
        loadBackgroundsFromAssets()

        binding.rootArena.post {
            addStagesToRow(binding.row1, 3)
            addStagesToRow(binding.row2, 4)
            addStagesToRow(binding.row3, 3)
            startRandomSelection()
        }
    }

    /**
     * Đọc danh sách background từ assets/background/backgrounds.json
     * File JSON có dạng:
     * [
     *   { "id": "bg_1", "image": "backgrounds/bg_1.png" },
     *   { "id": "bg_2", "image": "backgrounds/bg_2.png" }
     * ]
     */
    private fun loadBackgroundsFromAssets() {
        demoStages.clear()
        try {
            val jsonStr = requireContext().assets.open("background/backgrounds.json")
                .bufferedReader().use { it.readText() }
            val arr = JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val id = obj.optString("id", null) ?: continue
                val imagePath = obj.optString("image", null)

                // Lấy tên file để convert thành drawable resource
                val resName = imagePath.substringAfterLast('/').substringBeforeLast('.')
                val resId = resources.getIdentifier(resName, "drawable", requireContext().packageName)
                if (resId != 0) {
                    demoStages.add(id to resId)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Fallback nếu không load được JSON
        if (demoStages.isEmpty()) {
            demoStages.addAll(
                listOf(
                    "stage_b" to R.drawable.stage_b,
                    "stage_c" to R.drawable.stage_c,
                    "stage_a" to R.drawable.stage_a,
                    "test_stage" to R.drawable.test_stage
                )
            )
        }
    }

    private fun addStagesToRow(row: LinearLayout, count: Int) {
        repeat(count) {
            val view = layoutInflater.inflate(R.layout.item_stage, row, false)
            val imgStage = view.findViewById<HexagonImageView>(R.id.imgStage)
            val stage = demoStages[stageViews.size % demoStages.size]
            imgStage.setImageResource(stage.second)
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
            val selectedStage = demoStages[selectedPos]
            val selectedBackgroundId = selectedStage.first // <-- đây là String id từ JSON

            // ✅ Điều hướng sang CategoryFragment
            val action = ArenaSelectFragmentDirections
                .actionArenaSelectFragmentToCategoryFragment(
                    outfitId = -1L,
                    fromHome = false,
                    backgroundId = selectedBackgroundId, // String từ JSON
                    mode = "B",
                    selectedCharacter = -1,
                    gender = "female",
                    isFromGallery = false
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
