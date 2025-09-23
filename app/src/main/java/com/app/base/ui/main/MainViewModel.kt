package com.app.base.ui.main

import android.content.ContentUris
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.brally.mobile.base.viewmodel.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.FileOutputStream

class MainViewModel : BaseViewModel() {

    data class OutfitData(
        val id: String = System.currentTimeMillis().toString(), // auto id duy nhất
        val json: String,
        val bitmapFile: File? = null,
        val uri: Uri? = null,
        val mediaId: Long? = null
    )

    val _outfitListData = MutableStateFlow<List<OutfitData>>(emptyList())
    val outfitListData: StateFlow<List<OutfitData>> = _outfitListData

    private val _currentOutfit = MutableStateFlow<OutfitData?>(null)
    val currentOutfit: StateFlow<OutfitData?> = _currentOutfit

    private var resetToDefaultNextTime = true

    fun markResetOnNextEnter() {
        resetToDefaultNextTime = true
    }

    fun shouldResetToDefault(): Boolean {
        return if (resetToDefaultNextTime) {
            resetToDefaultNextTime = false
            true
        } else false
    }
    fun updateCurrentOutfit(json: String, uri: Uri? = null, bitmapFile: File? = null, mediaId: Long? = null) {
        val current = _currentOutfit.value
        if (current != null) {
            val updated = current.copy(json = json, uri = uri ?: current.uri, bitmapFile = bitmapFile ?: current.bitmapFile, mediaId = mediaId ?: current.mediaId)
            _currentOutfit.value = updated

            val updatedList = _outfitListData.value.map {
                if (it.id == current.id) updated else it
            }
            _outfitListData.value = updatedList
        }
    }

    fun applyAndSaveOutfit(json: String, bitmap: Bitmap) {
        saveBitmapToGallery(bitmap, json) { uri, mediaId ->
            val outfit = OutfitData(json = json, uri = uri, mediaId = mediaId)
            commitOutfit(outfit)
            _currentOutfit.value = outfit
        }
    }

    fun commitOutfit(outfit: OutfitData) {
        val current = _outfitListData.value.toMutableList()
        if (current.none { it.id == outfit.id }) {
            current.add(outfit)
        }
        _outfitListData.value = current
    }

    fun createNewOutfit(json: String) {
        val newOutfit = OutfitData(json = json)
        _currentOutfit.value = newOutfit
        _outfitListData.value = _outfitListData.value + newOutfit
    }

    fun commitOutfitWithBitmap(json: String, bitmapFile: File?, uri: Uri?, mediaId: Long? = null) {
        val outfit = OutfitData(json = json, bitmapFile = bitmapFile, uri = uri, mediaId = mediaId)
        _currentOutfit.value = outfit
        commitOutfit(outfit)
    }

    // ✅ Thêm hàm update
    fun updateOutfit(id: String, newJson: String) {
        val updatedList = _outfitListData.value.map {
            if (it.id == id) it.copy(json = newJson) else it
        }
        _outfitListData.value = updatedList
        if (_currentOutfit.value?.id == id) {
            _currentOutfit.value = _currentOutfit.value?.copy(json = newJson)
        }
    }

    // ✅ Thêm hàm delete
    fun deleteOutfit(id: String) {
        _outfitListData.value = _outfitListData.value.filterNot { it.id == id }
        if (_currentOutfit.value?.id == id) {
            _currentOutfit.value = null
        }
    }

    fun saveBitmapToGallery(bitmap: Bitmap, json: String, onDone: (Uri?, Long?) -> Unit) {
        val resolver = context.contentResolver
        try {
            val fileName = "outfit_${System.currentTimeMillis()}.png"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                uri?.let {
                    resolver.openOutputStream(uri)?.use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, values, null, null)

                    val mediaId = ContentUris.parseId(uri)
                    commitOutfitWithBitmap(json, null, uri, mediaId)
                    onDone(uri, mediaId)
                } ?: onDone(null, null)
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val file = File(picturesDir, fileName)
                FileOutputStream(file).use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
                context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)))
                onDone(Uri.fromFile(file), null)
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            onDone(null, null)
        }
    }

    fun getOutfitDataByUri(uri: Uri?): OutfitData? {
        return _outfitListData.value.find { it.uri == uri }
    }
}
