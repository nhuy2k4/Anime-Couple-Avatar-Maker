package com.app.base.ui.battle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.base.R
import com.app.base.core.utils.HexagonImageView

class ArenaAdapter(
    private val onArenaClick: (ArenaStage) -> Unit
) : ListAdapter<ArenaStage, ArenaAdapter.ArenaViewHolder>(DiffCallback()) {

    private var highlightedPos: Int = -1

    fun setHighlighted(position: Int) {
        val old = highlightedPos
        highlightedPos = position
        if (old != -1) notifyItemChanged(old)
        if (highlightedPos != -1) notifyItemChanged(highlightedPos)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ArenaViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_stage, parent, false))

    override fun onBindViewHolder(holder: ArenaViewHolder, position: Int) {
        holder.bind(getItem(position), position == highlightedPos)
    }

    inner class ArenaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imgStage: HexagonImageView = itemView.findViewById(R.id.imgStage)
        private val highlight: View = itemView.findViewById(R.id.highlightOverlay)

        fun bind(stage: ArenaStage, isHighlighted: Boolean) {
            imgStage.setImageResource(stage.imageRes)
            if (isHighlighted) {
                highlight.alpha = 1f
                highlight.animate().alpha(0.3f).setDuration(400).withEndAction {
                    highlight.animate().alpha(1f).setDuration(400).start()
                }.start()
            } else {
                highlight.animate().cancel()
                highlight.alpha = 0f
            }

            itemView.setOnClickListener { onArenaClick(stage) }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ArenaStage>() {
        override fun areItemsTheSame(oldItem: ArenaStage, newItem: ArenaStage) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ArenaStage, newItem: ArenaStage) = oldItem == newItem
    }
}

data class ArenaStage(
    val id: Int,
    val imageRes: Int
)
