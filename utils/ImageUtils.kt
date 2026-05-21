package com.example.helpern2.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.InputStream

object ImageUtils {
    /**
     * Compresses an image from a Uri and returns it as a ByteArray.
     * @param context The context to access the content resolver.
     * @param uri The Uri of the image to compress.
     * @param maxWidth The maximum width of the compressed image.
     * @param maxHeight The maximum height of the compressed image.
     * @param quality The quality of the compression (0-100).
     * @return The compressed image as a ByteArray, or null if compression failed.
     */
    fun compressImage(
        context: Context,
        uri: Uri,
        maxWidth: Int = 1024,
        maxHeight: Int = 1024,
        quality: Int = 80
    ): ByteArray? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()

            var (width, height) = options.outWidth to options.outHeight
            var inSampleSize = 1

            if (width > maxWidth || height > maxHeight) {
                val halfHeight = height / 2
                val halfWidth = width / 2
                while (halfHeight / inSampleSize >= maxHeight && halfWidth / inSampleSize >= maxWidth) {
                    inSampleSize *= 2
                }
            }

            val finalOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            
            val finalInputStream = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(finalInputStream, null, finalOptions)
            finalInputStream?.close()

            bitmap?.let {
                val outputStream = ByteArrayOutputStream()
                it.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                val byteArray = outputStream.toByteArray()
                it.recycle()
                byteArray
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
