package com.app.base.ui.main

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.brally.mobile.base.application.getBaseApplication
import com.brally.mobile.base.viewmodel.BaseViewModel
import com.brally.mobile.data.model.DrawResult
import com.brally.mobile.service.session.saveDrawCollection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.FileOutputStream

class MainViewModel : BaseViewModel() {

    // --- Data class để lưu cả JSON + bitmap file ---
    data class OutfitData(
        val json: String,
        val bitmapFile: File?
    )

    private val _outfitListData = MutableStateFlow<List<OutfitData>>(listOf())
    val outfitListData: StateFlow<List<OutfitData>> = _outfitListData

    // --- Commit outfit với bitmap ---
    fun commitCurrentOutfitWithBitmap(json: String, bitmapFile: File) {
        val current = _outfitListData.value.toMutableList()
        current.add(OutfitData(json, bitmapFile))
        _outfitListData.value = current
    }

    // --- Lưu JSON thuần ---
    private val _outfitList = MutableStateFlow<List<String>>(listOf())
    val outfitList: StateFlow<List<String>> = _outfitList

    private val _currentOutfit = MutableStateFlow("")
    val currentOutfit: StateFlow<String> = _currentOutfit

    val savedOutfits: StateFlow<List<String>> get() = outfitList

    fun applyOutfit(json: String) {
        updateCurrentOutfit(json)
        commitCurrentOutfit()
    }

    private var resetToDefaultNextTime = true
    fun shouldResetToDefault(): Boolean {
        return if (resetToDefaultNextTime) {
            resetToDefaultNextTime = false
            true
        } else false
    }

    fun markResetOnNextEnter() {
        resetToDefaultNextTime = true
    }

    fun updateCurrentOutfit(json: String) {
        _currentOutfit.value = json
    }

    fun commitCurrentOutfit() {
        val current = _outfitList.value.toMutableList()
        val json = _currentOutfit.value
        if (json.isNotEmpty() && !current.contains(json)) {
            current.add(json)
        }
        _outfitList.value = current
    }

    fun setDefaultOutfit(json: String) {
        val current = _outfitList.value.toMutableList()
        if (current.isEmpty() || current.first() != json) {
            current.add(0, json)
        }
        _outfitList.value = current
        _currentOutfit.value = json
    }

    // --- Lưu bitmap vào cache ---
    fun saveBitmapToGallery(bitmap: Bitmap, onDone: (Uri?) -> Unit) {
        val resolver = context.contentResolver
        try {
            val fileName = "outfit_${System.currentTimeMillis()}.png"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    resolver.openOutputStream(uri)?.use { out -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, out) }
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                    onDone(uri)
                } ?: onDone(null)
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val imageFile = File(picturesDir, fileName)
                FileOutputStream(imageFile).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
                context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile)))
                onDone(Uri.fromFile(imageFile))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onDone(null)
        }
    }


    // --- Lưu bitmap vào gallery ---
    fun saveBitmapToGallery(context: Context, bitmap: Bitmap, onDone: (Uri?) -> Unit) {
        val resolver = context.contentResolver
        try {
            val fileName = "outfit_${System.currentTimeMillis()}.png"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    resolver.openOutputStream(it)?.use { out ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                    onDone(uri)
                } ?: onDone(null)
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val imageFile = File(picturesDir, fileName)
                FileOutputStream(imageFile).use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                }
                context.sendBroadcast(
                    Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(imageFile))
                )
                onDone(Uri.fromFile(imageFile))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            onDone(null)
        }
    }

    // --- Lưu DrawResult ---
    fun saveDrawResult(drawResult: DrawResult) {
        launchHandler {
            flowOnIO {
                saveDrawCollection(drawResult.copy(id = System.currentTimeMillis().toInt()))
            }.subscribe(onLoading = false) {}
        }
    }
}
