//package com.app.base.ui.dialog
//
//import android.app.Dialog
//import android.graphics.drawable.ColorDrawable
//import android.os.Bundle
//import androidx.fragment.app.Fragment
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.DialogFragment
//import com.app.base.R
//
//class TaskFragment : DialogFragment() {
//
//    private val missions = listOf(
//        Mission("Save 1 Photo", "x50", 0, 1),
//        Mission("Win 1 Battle Mode", "x100", 0, 1)
//    )
//
//    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        val dialog = Dialog(requireContext())
//        dialog.setContentView(R.layout.fragment_task)
//        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//
//        val recycler: RecyclerView = dialog.findViewById(R.id.recycler_tasks)
//        val closeButton: ImageButton = dialog.findViewById(R.id.close_button)
//
//        recycler.layoutManager = LinearLayoutManager(requireContext())
//        recycler.adapter = TaskAdapter(missions)
//
//        closeButton.setOnClickListener { dismiss() }
//        return dialog
//    }
//
//    data class Mission(val description: String, val reward: String, var progress: Int, val goal: Int)
//
//    class TaskAdapter(private val missions: List<Mission>) : RecyclerView.Adapter<TaskAdapter.MissionViewHolder>() {
//        class MissionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//            val desc: TextView = view.findViewById(R.id.mission_desc)
//            val reward: TextView = view.findViewById(R.id.mission_reward)
//            val progress: TextView = view.findViewById(R.id.mission_progress)
//            val btnGo: MaterialButton = view.findViewById(R.id.btn_go_now)
//        }
//
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MissionViewHolder {
//            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mission, parent, false)
//            return MissionViewHolder(view)
//        }
//
//        override fun onBindViewHolder(holder: MissionViewHolder, position: Int) {
//            val mission = missions[position]
//            holder.desc.text = mission.description
//            holder.reward.text = mission.reward
//            holder.progress.text = "${mission.progress}/${mission.goal}"
//
//            holder.btnGo.setOnClickListener {
//                // Navigate to relevant action (gallery or battle)
//                Toast.makeText(holder.itemView.context, "Go to ${mission.description}", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        override fun getItemCount() = missions.size
//    }
//}
