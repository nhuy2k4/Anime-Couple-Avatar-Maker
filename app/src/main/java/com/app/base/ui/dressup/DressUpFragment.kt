package com.app.base.ui.dressup

import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.base.R
import com.app.base.core.utils.CharacterManager
import com.app.base.databinding.FragmentDressUpBinding
import com.app.base.model.FeatureItem
import com.brally.mobile.base.activity.BaseFragment
import com.brally.mobile.data.model.TabItem
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DressUpFragment : BaseFragment<FragmentDressUpBinding, DressUpViewModel>() {
    private lateinit var characterManager: CharacterManager
    // Adapter cho tab, callback trả về TabItem
    private val tabAdapter by lazy {
        TabAdapter(onItemTapped = ::onTabSelected)
    }

    private val featureAdapter by lazy {
        FeatureAdapter(onFeatureSelected = ::onFeatureSelected)
    }

    override fun initView() {
        characterManager = CharacterManager(requireContext(), binding.photoEditorView)

        // Thêm 2 nhân vật (nữ bên trái, nam bên phải)
        characterManager.addCharacter("female", R.drawable.body_base_female, -300, 0)
        characterManager.addCharacter("male", R.drawable.body_base_male, 300, 0)
        setupTabRecyclerView()
        setupFeatureRecyclerView()


    }

    override fun initListener() {
        binding.btnHome.setOnClickListener { /* TODO: navigate home */ }
        binding.btnReset.setOnClickListener { /* TODO: reset outfit */ }
        binding.btnChangeMale.setOnClickListener {
            characterManager.switchActiveCharacter()
            val current = characterManager.activeCharacterId
            Toast.makeText(requireContext(), "Switched to $current", Toast.LENGTH_SHORT).show()
        }
        binding.btnCamera.setOnClickListener { /* TODO: save outfit */ }
    }

    override fun initData() {
        observeViewModel()
        viewModel.loadInitialData(requireContext())
    }

    private fun setupTabRecyclerView() {
        binding.rcvTabs.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = tabAdapter
        }
    }

    private fun setupFeatureRecyclerView() {
        binding.rcvFeatures.apply {
            layoutManager = GridLayoutManager(context, 3) // hiển thị dạng grid 3 cột
            adapter = featureAdapter
        }
    }

    private fun observeViewModel() {
        // Observe tabs
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.tabs.collectLatest { tabs ->
                tabAdapter.setTabs(tabs)
            }
        }

        // Observe selected tab
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.tabSelected.collectLatest { position ->
                tabAdapter.selectTab(position)
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            characterManager.activeCharacterFlow.collectLatest { currentId ->
                // Lấy tab hiện tại
                val currentTabPos = viewModel.tabSelected.value
                // Load lại feature cho nhân vật active
                viewModel.loadFeaturesByTab(currentTabPos, currentId)
            }
        }
        // Observe features
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.features.collectLatest { features ->
                android.util.Log.d("DressUpFragment", "Loaded features size = ${features.size}")
                features.forEach { android.util.Log.d("DressUpFragment", "Feature: ${it.name}") }
                featureAdapter.setFeatures(features)
            }
        }
    }

    private fun onTabSelected(tab: TabItem) {
        val position = viewModel.getTabPosition(tab)
        val currentGender = characterManager.activeCharacterId // "male" hoặc "female"
        viewModel.loadFeaturesByTab(position, currentGender)
    }


    private fun onFeatureSelected(feature: FeatureItem) {
        characterManager.addFeatureFromAssets(feature.category, feature.image)
    }
}
