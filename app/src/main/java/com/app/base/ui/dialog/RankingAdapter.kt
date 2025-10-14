package com.app.base.ui.dialog

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.base.R
import com.app.base.databinding.ItemRankingBinding

class RankingAdapter : ListAdapter<RankingItem, RankingAdapter.RankingViewHolder>(DIFF) {
    companion object {
        private const val TAG = "RankingAdapter"

        val DIFF = object : DiffUtil.ItemCallback<RankingItem>() {
            override fun areItemsTheSame(oldItem: RankingItem, newItem: RankingItem) =
                oldItem.rank == newItem.rank && oldItem.name == newItem.name

            override fun areContentsTheSame(oldItem: RankingItem, newItem: RankingItem) =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        return try {
            val binding = ItemRankingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            RankingViewHolder(binding)
        } catch (e: Exception) {
            Log.e(TAG, "Error creating ViewHolder", e)
            throw e
        }
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        try {
            holder.bind(getItem(position))
        } catch (e: Exception) {
            Log.e(TAG, "Error binding ViewHolder at position $position", e)
        }
    }

    inner class RankingViewHolder(
        private val binding: ItemRankingBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: RankingItem) {
            try {
                setupRankText(item)
                setupPlayerInfo(item)
                setupScore(item)
                setupBackground(item)
            } catch (e: Exception) {
                Log.e(TAG, "Error binding item: ${item.rank}", e)
                setDefaultValues()
            }
        }

        private fun setupRankText(item: RankingItem) {
            binding.tvRank.text = buildRankText(item.rank)
        }

        private fun buildRankText(rank: Int): String {
            return when (rank) {
                1 -> "🥇 #1"
                2 -> "🥈 #2"
                3 -> "🥉 #3"
                else -> "#$rank"
            }
        }

        private fun setupPlayerInfo(item: RankingItem) {
            with(binding) {
                tvName.text = item.name
                imgAvatar.setImageResource(item.avatarRes)
                //tvName.setTextColor(getNameTextColor(item.isPlayer))
            }
        }

//        private fun getNameTextColor(isPlayer: Boolean): Int {
//            //val colorRes = if (isPlayer) R.color.highlight_text else R.color.normal_text
//            return ContextCompat.getColor(itemView.context, colorRes)
//        }

        private fun setupScore(item: RankingItem) {
            binding.tvScore.apply {
                text = formatScore(item.score)
                //setTextColor(getScoreTextColor(item.isPlayer))
            }
        }

        private fun formatScore(score: Int): String {
            return when {
                score >= 1000 -> String.format("%.1fK", score / 1000f)
                else -> score.toString()
            }
        }

//        private fun getScoreTextColor(isPlayer: Boolean): Int {
////            val colorRes = if (isPlayer) R.color.highlight_score else R.color.normal_score
////            return ContextCompat.getColor(itemView.context, colorRes)
//        }

        private fun setupBackground(item: RankingItem) {
            val backgroundRes = if (item.isPlayer) {
                R.drawable.bg_ranking_highlight
            } else {
                R.drawable.bg_task_item
            }
            binding.root.setBackgroundResource(backgroundRes)
        }

        private fun setDefaultValues() {
            with(binding) {
                tvRank.text = "#-"
                tvName.text = "---"
                tvScore.text = "0"
                imgAvatar.setImageResource(R.drawable.ic_avatar_placeholder)
                root.setBackgroundResource(R.drawable.bg_task_item)
            }
        }
    }

    fun submitListSafely(list: List<RankingItem>?) {
        try {
            submitList(list ?: emptyList())
        } catch (e: Exception) {
            Log.e(TAG, "Error submitting list", e)
            submitList(emptyList())
        }
    }
}
