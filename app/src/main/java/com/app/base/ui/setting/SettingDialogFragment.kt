package com.app.base.ui.setting

import android.app.Dialog
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import com.app.base.R
import com.app.base.component.dialog.RateAppBottomDialog
import com.app.base.databinding.FragmentSettingBinding
import com.bg.common.Common
import com.brally.mobile.base.activity.isCmpRequired
import com.brally.mobile.base.activity.navigate
import com.brally.mobile.base.activity.showDialog
import com.brally.mobile.base.activity.showPrivacyOptionForm
import com.brally.mobile.base.activity.popBackStack
import com.brally.mobile.base.application.appInfo
import com.brally.mobile.service.event.*
import com.brally.mobile.service.firebase.AppRemoteConfig
import com.brally.mobile.service.session.isMusic
import com.brally.mobile.service.session.isSound
import com.brally.mobile.service.session.isVibrate
import com.brally.mobile.service.session.saveMusic
import com.brally.mobile.service.session.saveSound
import com.brally.mobile.service.session.saveVibrate
import com.brally.mobile.service.sound.AppMusicPlayer
import com.brally.mobile.utils.singleClick
import com.braly.ads.ads.interf.BralyResultConsentForm
import com.google.android.ump.FormError
import com.language_onboard.utils.tracking

class SettingDialogFragment : DialogFragment(), BralyResultConsentForm {
    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        val view = layoutInflater.inflate(R.layout.fragment_setting, null)
        dialog.setContentView(view)
        dialog.setCanceledOnTouchOutside(false)
        _binding = FragmentSettingBinding.bind(view)
        initView()
        initListener()
        return dialog
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initView() {
        tracking(SETTING_SHOW)
        // Không cần adjustInsetsForBottomNavigation vì là dialog
    }

    private fun initListener() {
        binding.btnClose.singleClick {
            dismiss()
        }
        binding.llMusic.singleClick {
            saveMusic(!isMusic())
            val tag = if (isMusic()) SETTING_CLICK_MUSIC_ON else SETTING_CLICK_MUSIC_OFF
            tracking(tag)
            checkPlaySound()
            updateView()
        }
        binding.llSound.singleClick {
            saveSound(!isSound())
            val tag = if (isSound()) SETTING_CLICK_SOUND_ON else SETTING_CLICK_SOUND_OFF
            tracking(tag)
            updateView()
        }
        binding.llVibrate.singleClick {
            saveVibrate(!isVibrate())
            updateView()
        }
        updateView()
    }

    private fun checkPlaySound() {
        if (isMusic()) {
            AppMusicPlayer.playBackgroundMusic()
        } else {
            AppMusicPlayer.stop()
            AppMusicPlayer.releaseBackgroundMusic()
        }
    }

    private fun updateView() {
        // Music
        val musicDrawable = binding.switchMusic.drawable as? LayerDrawable
        musicDrawable?.let {
            val cross = it.findDrawableByLayerId(com.app.base.R.id.cross)
            cross?.alpha = if (isMusic()) 0 else 255
        }
        // Sound
        val soundDrawable = binding.switchSoundFx.drawable as? LayerDrawable
        soundDrawable?.let {
            val cross = it.findDrawableByLayerId(com.app.base.R.id.cross)
            cross?.alpha = if (isSound()) 0 else 255
        }
        // Vibrate
        val vibrateDrawable = binding.switchVibrate.drawable as? LayerDrawable
        vibrateDrawable?.let {
            val cross = it.findDrawableByLayerId(com.app.base.R.id.cross)
            cross?.alpha = if (isVibrate()) 0 else 255
        }
    }

    override fun onConsentCustom(consentPurpose: Int, consentVendor: Int) {
        tracking(
            CONSENT_SOME_OPTIONS,
            hashMapOf(
                PURPOSE_AMOUNT to consentPurpose.toString(),
                VENDORS_AMOUNT to consentVendor.toString()
            )
        )
        tracking(CONSENT_SOME_OPTIONS)
    }

    override fun onConsentFull(isConsentFullBefore: Boolean) {
        tracking(CONSENT_ALL_SUCCESS)
    }

    override fun onConsentReject() {
        tracking(CONSENT_ALL_FAILED)
    }

    override fun onConsentSkip() {}
    override fun onError(error: FormError) {}
    override fun onShowConsentForm() {
        tracking(CMP_MESSAGE_SHOW)
    }

    companion object {
        fun newInstance(): SettingDialogFragment = SettingDialogFragment()
    }
}
