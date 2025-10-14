package com.app.base.ui.dialog

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.base.R
import com.app.base.databinding.DialogRankingBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.launch

class DialogRankingFragment : BottomSheetDialogFragment() {
    private var _binding: DialogRankingBinding? = null
    private val binding get() = _binding!!
    private var _rankingAdapter: RankingAdapter? = null
    private val rankingAdapter get() = _rankingAdapter!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return try {
            _binding = DialogRankingBinding.inflate(inflater, container, false)
            binding.root
        } catch (e: Exception) {
            Log.e(TAG, "Error creating view", e)
            null
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        loadRankingData()
    }

    private fun setupViews() {
        try {
            setupCloseButton()
            setupRecyclerView()
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up views", e)
        }
    }

    private fun setupCloseButton() {
        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }

    private fun setupRecyclerView() {
        _rankingAdapter = RankingAdapter()
        binding.rcvRanking.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = rankingAdapter
            setHasFixedSize(true)
        }
    }

    private fun loadRankingData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val top3 = loadTop3Rankings()
                val rankingList = loadRankingList()

                displayTop3(top3)
                rankingAdapter.submitList(rankingList)
            } catch (e: Exception) {
                Log.e(TAG, "Error loading ranking data", e)
            }
        }
    }

    private fun loadTop3Rankings(): List<TopRank> {
        return listOf(
            TopRank(
                name = "Stanley",
                avatarRes = R.drawable.ic_avatar_placeholder,
                medalRes = R.drawable.ic_medal_gold,
                chestRes = R.drawable.ic_chest_gold,
                score = 950
            ),
            TopRank(
                name = "Gomez",
                avatarRes = R.drawable.ic_avatar_placeholder,
                medalRes = R.drawable.ic_medal_silver,
                chestRes = R.drawable.ic_chest_silver,
                score = 900
            ),
            TopRank(
                name = "Langley",
                avatarRes = R.drawable.ic_avatar_placeholder,
                medalRes = R.drawable.ic_medal_bronze,
                chestRes = R.drawable.ic_chest_bronze,
                score = 850
            )
        )
    }

    private fun loadRankingList(): List<RankingItem> {
        return listOf(
            RankingItem(996, "Fischer", R.drawable.ic_avatar_placeholder, 171, false),
            RankingItem(997, "Sullivan", R.drawable.ic_avatar_placeholder, 126, false),
            RankingItem(998, "Burch", R.drawable.ic_avatar_placeholder, 85, false),
            RankingItem(999, "Witt", R.drawable.ic_avatar_placeholder, 41, false),
            RankingItem(1000, "Player", R.drawable.ic_avatar_placeholder, 0, true)
        )
    }

    private fun displayTop3(top3: List<TopRank>) {
        try {
            // TODO: Implement top 3 display logic using binding.layoutTop3
            // Each top player should be displayed with their medal and chest
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying top 3", e)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        cleanupViews()
    }

    private fun cleanupViews() {
        binding.rcvRanking.adapter = null
        _rankingAdapter = null
        _binding = null
    }

    companion object {
        private const val TAG = "DialogRankingFragment"
    }
}

data class TopRank(
    val name: String,
    val avatarRes: Int,
    val medalRes: Int,
    val chestRes: Int,
    val score: Int
)

data class RankingItem(
    val rank: Int,
    val name: String,
    val avatarRes: Int,
    val score: Int,
    val isPlayer: Boolean
)
