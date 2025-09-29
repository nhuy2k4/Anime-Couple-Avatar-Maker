package com.app.base.ui.dialog

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.base.R
import com.google.android.material.button.MaterialButton

class RankingFragment : DialogFragment() {

    private val rankingData = listOf(
//        RankItem(1, "Stanley", 1000, R.drawable.ic_gold),
//        RankItem(2, "Gomez", 950, R.drawable.ic_silver),
//        RankItem(3, "Fischer", 900, R.drawable.ic_bronze),
        RankItem(996, "Fischer", 171, 0),
        RankItem(997, "Sullivan", 170, 0),
        RankItem(1000, "Player", 0, 0, highlight = true)
    )

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.fragment_ranking)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val recycler: RecyclerView = dialog.findViewById(R.id.recycler_ranking)
        val closeButton: ImageButton = dialog.findViewById(R.id.close_button)
        val btnPK: MaterialButton = dialog.findViewById(R.id.btn_pk_battle)

        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = RankingAdapter(rankingData)

        closeButton.setOnClickListener { dismiss() }
        btnPK.setOnClickListener { Toast.makeText(requireContext(), "Go to PK Battle", Toast.LENGTH_SHORT).show() }

        return dialog
    }

    data class RankItem(val rank: Int, val name: String, val score: Int, val medalRes: Int = 0, val highlight: Boolean = false)

    class RankingAdapter(private val list: List<RankItem>) : RecyclerView.Adapter<RankingAdapter.RankViewHolder>() {
        class RankViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvRank: TextView = view.findViewById(R.id.tv_rank)
            val ivMedal: ImageView = view.findViewById(R.id.iv_medal)
            val tvName: TextView = view.findViewById(R.id.tv_name)
            val tvScore: TextView = view.findViewById(R.id.tv_score)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_ranking, parent, false)
            return RankViewHolder(view)
        }

        override fun onBindViewHolder(holder: RankViewHolder, position: Int) {
            val item = list[position]
            holder.tvRank.text = "#${item.rank}"
            if (item.medalRes != 0) holder.ivMedal.setImageResource(item.medalRes)
            else holder.ivMedal.setImageDrawable(null)
            holder.tvName.text = item.name
            holder.tvScore.text = item.score.toString()
            if (item.highlight) holder.tvName.setTextColor(Color.YELLOW)
        }

        override fun getItemCount() = list.size
    }
}
