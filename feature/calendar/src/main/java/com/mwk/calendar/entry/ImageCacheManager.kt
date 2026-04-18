package com.mwk.calendar.entry

import android.content.Context
import android.net.Uri
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ImageCacheManager {

    suspend fun saveImageToCache(
        context: Context,
        imageUrl: String,
    ): String? = withContext(Dispatchers.IO) {
        val bitmap = runCatching {
            Picasso.get()
                .load(imageUrl)
                .resize(480, 480)
                .centerInside()
                .onlyScaleDown()
                .get()
        }.getOrNull() ?: return@withContext null

        val imagesDir = File(context.cacheDir, "entry_images").apply {
            mkdirs()
        }
        val imageFile = File(imagesDir, "entry_${System.currentTimeMillis()}.jpg")

        FileOutputStream(imageFile).use { stream ->
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 85, stream)
        }

        imageFile.absolutePath
    }
}
