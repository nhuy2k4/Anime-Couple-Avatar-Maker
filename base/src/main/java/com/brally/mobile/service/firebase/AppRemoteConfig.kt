package com.brally.mobile.service.firebase

import com.brally.mobile.base.application.appInfo
import com.brally.mobile.base.application.getBaseApplication
import com.brally.mobile.data.model.ArtItem
import com.brally.mobile.data.model.CategoryItem
import com.brally.mobile.utils.Constant
import com.brally.mobile.utils.Constant.DEFAULT_ITEM_HOME_ORDER
import com.brally.mobile.utils.gsonStrToList
import com.brally.mobile.utils.readAssetsFile
import com.braly.analytics.config.BralyRemoteConfigImpl
import com.google.gson.JsonArray
import com.google.gson.JsonParser

object AppRemoteConfig {
    private val braly by lazy { BralyRemoteConfigImpl() }

    /*************** KEY ************************/
    private const val ACCESS_KEY_REMOTE_DATA = "access_key_remote_data"
    private const val KEY_SHOW_CMP = "show_cmp"
    private const val DATA_PIXEL_ART = "data_pixel_art"
    private const val DATA_CATEGORIES = "data_categories_pixel_art"
    private val KEY_ORDER_ITEM_HOME = "order_item_home_glow_dot_art"
    private const val KEY_DATA_RESOURCE_TYPE = "pixel_art_data_resource_type"

    /*******************************************/

    enum class DataResourceType(val type: String, val domainUrl: String) {
        GITHUB("GITHUB", Constant.DATA_BASE_URL_GITHUB), CLOUD_FLARE("CLOUD_FLARE", Constant.DATA_BASE_URL_CLOUDFLARE);
    }

    fun getDataResourceType(): DataResourceType {
        return try {
            val type = BralyRemoteConfigImpl().getString(KEY_DATA_RESOURCE_TYPE)
                ?: DataResourceType.GITHUB.name
            DataResourceType.valueOf(type)
        } catch (ex: Exception) {
            ex.printStackTrace()
            DataResourceType.GITHUB
        }
    }

    fun getAccessToken(): String {
        return braly.getString(ACCESS_KEY_REMOTE_DATA) ?: ""
    }

    fun getShowCmp(): Boolean {
        return braly.getBoolean(KEY_SHOW_CMP) == true
    }

    fun getOrderItemHome(): String {
        return braly.getString(KEY_ORDER_ITEM_HOME) ?: DEFAULT_ITEM_HOME_ORDER
    }

    fun <T> getListData(key: String, claszz: Class<T>): List<T> {
        return try {
            val json = braly.getString(key)?.ifEmpty {
                getBaseApplication().assets.readAssetsFile("$key.json")
            }
            gsonStrToList(json, claszz)

        } catch (e: Exception) {
            e.printStackTrace()
//            emptyList()
           if (appInfo().isDebug) {
               val json = getBaseApplication().assets.readAssetsFile("$key.json")
               gsonStrToList(json, claszz)
           } else {
                emptyList()
           }
        }
    }

    fun getListArt(): List<ArtItem> {
        val listPathArtItem = getListData(DATA_PIXEL_ART, ArtItem::class.java)
        return listPathArtItem
    }

    fun getListCategory(languageKey: String): List<CategoryItem> {
        return try {
            val json = braly.getString(DATA_CATEGORIES)?.ifEmpty {
                getBaseApplication().assets.readAssetsFile("$DATA_CATEGORIES.json")
            }
            json?.let { convertJsonToCategoryList(it, languageKey) } ?: emptyList()

        } catch (e: Exception) {
            e.printStackTrace()
//            emptyList()
            if (appInfo().isDebug) {
                val json = getBaseApplication().assets.readAssetsFile("$DATA_CATEGORIES.json")
                convertJsonToCategoryList(json, languageKey)
            } else {
                emptyList()
            }
        }
    }

    fun convertJsonToCategoryList(jsonString: String, langKey: String): List<CategoryItem> {
        val jsonArray: JsonArray = JsonParser.parseString(jsonString).asJsonArray

        return jsonArray.map { jsonElement ->
            val obj = jsonElement.asJsonObject
            val type = obj.get("type")?.asString ?: ""
            val value = obj.get(langKey)?.asString ?: ""
            CategoryItem(type = type, value = value)
        }
    }
}
