package com.jayjaycode.miniproject.util

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

object DocumentPickerUtils {

    fun resolveDisplayName(context: Context, uri: Uri): String {
        if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) {
                    return cursor.getString(nameIndex).orEmpty()
                }
            }
        }
        return uri.lastPathSegment?.substringAfterLast('/').orEmpty()
    }

    fun requirePdfDocument(context: Context, uri: Uri) {
        val resolver = context.contentResolver
        val mimeType = resolver.getType(uri)?.lowercase()
        val fileName = resolveDisplayName(context, uri).lowercase()

        val isPdfMime = mimeType == "application/pdf"
        val isPdfExtension = fileName.endsWith(".pdf")
        if (!isPdfMime && !isPdfExtension) {
            error("Business registration certificate must be a PDF file")
        }
    }
}
