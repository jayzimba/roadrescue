package com.jayjaycode.miniproject.util

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object PhotoUtils {

    fun createCameraImageUri(context: Context): Uri {
        val file = File(context.cacheDir, "breakdown_${System.currentTimeMillis()}.jpg")
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
    }

    fun uriToPersistableString(uri: Uri): String = uri.toString()
}
