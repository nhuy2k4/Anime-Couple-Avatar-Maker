//package com.app.base.ui.dialog
//
//import android.content.Context
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import com.app.base.ui.home.HomeFragment
//
///**
// * Deprecated compatibility wrapper.
// *
// * Use `TaskDialog` directly instead. This fragment remains only to avoid
// * breaking callers that still try to show it via FragmentManager.
// */
//@Deprecated("Use TaskDialog directly. This fragment will be removed in future.")
//class DialogTaskFragment : Fragment() {
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        // Return a placeholder view; we immediately show TaskDialog in onResume
//        return View(inflater.context)
//    }
//
//    override fun onResume() {
//        super.onResume()
//        try {
//            val ctx = requireContext()
//            val playerId = getCurrentPlayerId(ctx)
//            val dialog = TaskDialog(
//                context = ctx,
//                playerId = playerId,
//                viewModelStoreOwner = requireActivity(),
//                onGoToAction = { action ->
//                    // Delegate actions back to HomeFragment if available
//                    (parentFragment as? HomeFragment)?.let { home ->
//                        when (action) {
//                            "EDITOR" -> runCatching { home.binding?.btnAvatar?.performClick() }
//                            "PK_MODE" -> runCatching { home.binding?.btnPkBattle?.performClick() }
//                            "WATCH_AD" -> runCatching { home.showRewardedAd() }
//                        }
//                    }
//                }
//            )
//            dialog.show()
//        } catch (e: Exception) {
//            // ignore - fail safe
//        }
//
//        // Remove this fragment instance immediately to avoid leaving an unused fragment on stack
//        parentFragmentManager.beginTransaction().remove(this).commitAllowingStateLoss()
//    }
//
//    private fun getCurrentPlayerId(context: Context): Int {
//        // Keep consistent with HomeFragment temporary logic
//        return 1
//    }
//}



