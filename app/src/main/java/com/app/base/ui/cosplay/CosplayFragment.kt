package com.app.base.ui.cosplay

import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.base.databinding.FragmentCosplayBinding
import com.app.base.utils.MissionTracker
import com.app.base.utils.PlayerManager
import com.brally.mobile.base.activity.BaseFragment
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch

class CosplayFragment : BaseFragment<FragmentCosplayBinding, CosplayViewModel>() {

    private val cosplayViewModel: CosplayViewModel by viewModel()
    private var _adapter: CharactersAdapter? = null
    private val adapter get() = _adapter!!
    private lateinit var playerManager: PlayerManager

    override fun initView() {
        setupRecyclerView()
        playerManager = PlayerManager.getInstance(requireContext())
        // Load current diamond count
        lifecycleScope.launch {
            try {
                val diamonds = playerManager.getPlayerDiamonds()
                binding.tvDiamond.text = diamonds.toString()
            } catch (e: Exception) {
                // ignore
            }
        }
    }

    override fun initListener() {
        binding.btnHome.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun initData() {
        observeViewModel()
        cosplayViewModel.loadCharacters()
    }

    private fun observeViewModel() {
        cosplayViewModel.characters.observe(viewLifecycleOwner) { characters ->
            try {
                _adapter = CharactersAdapter(characters) { selectedRes ->
                    navigateToCategory(selectedRes)
                }
                binding.rcvCharacters.adapter = adapter
            } catch (e: Exception) {
                Log.e("CosplayFragment", "Error setting up characters adapter", e)
            }
        }
    }

    private fun navigateToCategory(selectedRes: Int) {
        try {
            // 🎯 Update mission progress for playing a match
            MissionTracker.Mission.playMatch(requireContext(), lifecycleScope)

            // Navigation graph expects an integer resource id for `selectedCharacter`.
            val gender = cosplayViewModel.getDefaultGender()
            val action = CosplayFragmentDirections
                .actionCosplayFragmentToCategoryFragment(
                    mode = "C",
                    selectedCharacter = selectedRes,
                    gender = gender
                )
            findNavController().navigate(action)
        } catch (e: Exception) {
            Log.e("CosplayFragment", "Error navigating to category", e)
        }
    }

    private fun setupRecyclerView() {
        binding.rcvCharacters.apply {
            layoutManager = LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
            setHasFixedSize(true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.rcvCharacters.adapter = null
        _adapter = null
    }
}
