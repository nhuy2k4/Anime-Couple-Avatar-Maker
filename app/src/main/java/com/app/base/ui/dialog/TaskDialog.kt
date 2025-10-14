package com.app.base.ui.dialog

import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.base.R
import com.app.base.database.entity.MissionEntity
import com.app.base.database.repository.ClaimResult
import com.app.base.database.repository.MissionProgress

class TaskDialog(
    context: Context,
    private val playerId: Int,
    private val lifecycleOwner: LifecycleOwner,
    private val viewModelStoreOwner: ViewModelStoreOwner,
    private val onGoToAction: (String) -> Unit = {} // Callback để chuyển đến màn hình tương ứng
) : Dialog(context) {

    private lateinit var viewModel: TaskViewModel
    private lateinit var taskAdapter: TaskAdapter
    private var currentProgress: MissionProgress? = null

    // UI Components
    private lateinit var btnClose: ImageView
    private lateinit var rcvTasks: RecyclerView
    private lateinit var tvAllMissionProgress: TextView
    private lateinit var progressAllMission: ProgressBar
    private lateinit var allmissionContainer: View

    init {
        try {
            setupViewModel()
            setupDialog()
            loadMissions()
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing TaskDialog", e)
            dismiss()
        }
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(viewModelStoreOwner)[TaskViewModel::class.java]

        viewModel.missionProgress.observe(lifecycleOwner) { progress ->
            currentProgress = progress
            updateUI(progress)
        }

        viewModel.claimResult.observe(lifecycleOwner) { result ->
            handleClaimResult(result)
        }

        viewModel.isLoading.observe(lifecycleOwner) { isLoading ->
            // Có thể thêm loading indicator nếu cần
        }
    }

    private fun setupDialog() {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_task, null, false)
        setContentView(view)
        setCancelable(true)
        setCanceledOnTouchOutside(false)

        initViews(view)
        setupRecyclerView()
        setupClickListeners()
    }

    private fun initViews(view: View) {
        btnClose = view.findViewById(R.id.btnClose)
        rcvTasks = view.findViewById(R.id.rcvTasks)
        tvAllMissionProgress = view.findViewById(R.id.tvAllMissionProgress)
        progressAllMission = view.findViewById(R.id.progressAllMission)
        allmissionContainer = view.findViewById(R.id.allmissionContainer)
    }

    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onGoNowClick = { mission ->
                handleGoNowClick(mission)
            },
            onClaimClick = { mission ->
                handleClaimClick(mission)
            }
        )

        rcvTasks.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = taskAdapter
        }
    }

    private fun setupClickListeners() {
        btnClose.setOnClickListener {
            dismiss()
        }

        // Click vào "Complete All Missions" để claim tất cả
        allmissionContainer.setOnClickListener {
            currentProgress?.let { progress: MissionProgress ->
                if (progress.canClaimAll) {
                    viewModel.claimAllMissionRewards(playerId)
                }
            }
        }
    }

    private fun loadMissions() {
        viewModel.loadMissions(playerId)
    }

    private fun updateUI(progress: MissionProgress) {
        try {
            // Update overall progress
            progressAllMission.max = progress.totalCount
            progressAllMission.progress = progress.completedCount
            tvAllMissionProgress.text = context.getString(R.string.mission_progress_format, progress.completedCount, progress.totalCount)

            // Update missions list
            taskAdapter.updateMissions(progress.missions)

            // Update "Complete All Missions" container
            allmissionContainer.isEnabled = progress.canClaimAll
            allmissionContainer.alpha = if (progress.canClaimAll) 1.0f else 0.6f

        } catch (e: Exception) {
            Log.e(TAG, "Error updating UI", e)
        }
    }

    private fun handleGoNowClick(mission: MissionEntity) {
        // Gọi callback để chuyển đến màn hình tương ứng
        when (mission.type) {
            "SAVE_PHOTO" -> {
                onGoToAction("EDITOR")
                dismiss()
            }
            "PLAY_MATCH" -> {
                onGoToAction("PK_MODE")
                dismiss()
            }
            "WATCH_AD" -> {
                onGoToAction("WATCH_AD")
                // Không dismiss để user có thể xem ad và quay lại
            }
            "WIN_MATCH" -> {
                onGoToAction("PK_MODE")
                dismiss()
            }
            "LOGIN" -> {
                // Tự động hoàn thành khi mở dialog (đã login rồi)
                viewModel.updateMissionProgress(playerId, "LOGIN")
            }
            else -> {
                Toast.makeText(context, "Chức năng đang phát triển", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleClaimClick(mission: MissionEntity) {
        if (mission.status == "COMPLETED") {
            viewModel.claimMissionReward(playerId, mission.id)
        }
    }

    private fun handleClaimResult(result: ClaimResult?) {
        when (result) {
            is ClaimResult.Success -> {
                Toast.makeText(context, "Đã nhận phần thưởng!", Toast.LENGTH_SHORT).show()
            }
            is ClaimResult.AlreadyClaimed -> {
                Toast.makeText(context, "Phần thưởng đã được nhận!", Toast.LENGTH_SHORT).show()
            }
            is ClaimResult.Error -> {
                Toast.makeText(context, "Lỗi: ${result.message}", Toast.LENGTH_SHORT).show()
            }
            null -> {
                // Do nothing
            }
        }

        // Clear result after handling
        if (result != null) {
            viewModel.clearClaimResult()
        }
    }

    // Public methods để update progress từ bên ngoài
    fun updateMissionProgress(missionType: String, amount: Int = 1) {
        viewModel.updateMissionProgress(playerId, missionType, amount)
    }

    override fun dismiss() {
        super.dismiss()
        cleanup()
    }

    private fun cleanup() {
        // Cleanup nếu cần
    }

    companion object {
        private const val TAG = "TaskDialog"
    }
}
