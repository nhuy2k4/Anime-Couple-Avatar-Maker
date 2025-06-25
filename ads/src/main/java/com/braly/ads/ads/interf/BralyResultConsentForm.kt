package com.braly.ads.ads.interf

import com.google.android.ump.FormError


interface BralyResultConsentForm {

    fun onShowConsentForm()
    fun onConsentFull(isConsentFullBefore: Boolean)
    fun onConsentReject()
    fun onConsentCustom(consentPurpose: Int, consentVendor: Int)
    fun onError(error: FormError)
    fun onConsentSkip()

}