package com.braly.ads.data


data class Configuration(
    val defaultAds: String,
    val isDebug: Boolean = false,
    val testDeviceIds: List<String> = emptyList(),
    val appsflyerKey: String? = null,
    val appmetricaNetwork: String? = null
) {

    private var useDebugConfig: Boolean = false


    fun useDebugConfig(useDebug: Boolean) = apply { useDebugConfig = useDebug }


    fun isUseDebugConfig() = useDebugConfig

    fun isUseAppmetrica() = !appmetricaNetwork.isNullOrEmpty()

}
