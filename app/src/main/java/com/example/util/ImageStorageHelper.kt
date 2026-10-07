package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.UUID

object ImageStorageHelper {

    private const val DIRECTORY_NAME = "product_images"
    private const val MAX_DIMENSION = 1280 // Perfectly crisp for high-DPI smartphone screens

    suspend fun saveImageToInternalStorage(context: Context, sourceUri: Uri): String? = withContext(Dispatchers.IO) {
        var inputStream: InputStream? = null
        try {
            val directory = File(context.filesDir, DIRECTORY_NAME).apply {
                if (!exists()) mkdirs()
            }

            val fileName = "cycle_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg"
            val targetFile = File(directory, fileName)

            // Step 1: Decode image dimensions only to prevent OutOfMemoryError
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            inputStream = context.contentResolver.openInputStream(sourceUri)
            if (inputStream == null) {
                Log.e("ImageStorageHelper", "Could not open input stream for $sourceUri")
                return@withContext null
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream.close()

            // Step 2: Compute inSampleSize to downsample large camera captures
            val origWidth = options.outWidth
            val origHeight = options.outHeight
            var inSampleSize = 1
            if (origHeight > MAX_DIMENSION || origWidth > MAX_DIMENSION) {
                val halfHeight = origHeight / 2
                val halfWidth = origWidth / 2
                while ((halfHeight / inSampleSize) >= MAX_DIMENSION && (halfWidth / inSampleSize) >= MAX_DIMENSION) {
                    inSampleSize *= 2
                }
            }

            // Step 3: Decode scaled bitmap
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.RGB_565 // 50% less RAM consumption than ARGB_8888
            }
            inputStream = context.contentResolver.openInputStream(sourceUri)
            val bitmap = BitmapFactory.decodeStream(inputStream, null, decodeOptions)
            inputStream?.close()

            if (bitmap == null) {
                Log.e("ImageStorageHelper", "Bitmap decode returned null for $sourceUri")
                return@withContext null
            }

            // Step 4: Write compressed JPEG to target file
            FileOutputStream(targetFile).use { outStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outStream)
                outStream.flush()
            }
            bitmap.recycle()

            targetFile.absolutePath
        } catch (t: Throwable) {
            Log.e("ImageStorageHelper", "Failed to save image safely: ${t.localizedMessage}", t)
            null
        } finally {
            try {
                inputStream?.close()
            } catch (_: Exception) {}
        }
    }

    suspend fun deleteImageFromInternalStorage(filePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            if (file.exists()) {
                file.delete()
            } else false
        } catch (t: Throwable) {
            Log.e("ImageStorageHelper", "Failed to delete image: $filePath", t)
            false
        }
    }
}
