package com.jayjaycode.miniproject.util

import android.content.Context
import android.net.Uri
import com.google.firebase.FirebaseApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.tasks.await
import java.io.File

object FirebaseStorageHelper {

    private const val MAX_FIRESTORE_CERT_BYTES = 750_000

    fun storage(): FirebaseStorage {
        val bucket = FirebaseApp.getInstance().options.storageBucket
        if (bucket.isNullOrBlank()) {
            error(
                "Firebase Storage bucket is missing from google-services.json. " +
                    "Download the latest config from Firebase Console.",
            )
        }
        return FirebaseStorage.getInstance("gs://$bucket")
    }

    suspend fun uploadPdf(
        pathSegments: List<String>,
        sourceUri: Uri,
        context: Context,
        fileName: String = "document.pdf",
    ): String {
        var storageRef = storage().reference
        pathSegments.forEach { segment ->
            storageRef = storageRef.child(segment)
        }

        val metadata = StorageMetadata.Builder()
            .setContentType("application/pdf")
            .setCustomMetadata("originalFileName", fileName)
            .build()

        val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.pdf")
        try {
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            } ?: error("Could not read the certificate file. Please select the PDF again.")

            if (tempFile.length() == 0L) {
                error("The selected PDF file is empty. Please choose another document.")
            }

            storageRef.putFile(Uri.fromFile(tempFile), metadata).await()
            return storageRef.downloadUrl.await().toString()
        } catch (e: StorageException) {
            throw mapStorageException(e)
        } catch (e: Exception) {
            if (e is StorageException) throw mapStorageException(e)
            if (isStorageNotConfiguredMessage(e.message.orEmpty())) {
                throw StorageNotConfiguredException(storageSetupMessage())
            }
            throw e
        } finally {
            tempFile.delete()
        }
    }

    suspend fun uploadImage(
        pathSegments: List<String>,
        sourceUri: Uri,
        context: Context,
    ): String {
        var storageRef = storage().reference
        pathSegments.forEach { segment ->
            storageRef = storageRef.child(segment)
        }

        val contentType = context.contentResolver.getType(sourceUri)?.takeIf { it.startsWith("image/") }
            ?: "image/jpeg"
        val extension = when (contentType) {
            "image/png" -> "png"
            "image/webp" -> "webp"
            else -> "jpg"
        }
        val tempFile = File(context.cacheDir, "upload_${System.currentTimeMillis()}.$extension")

        val metadata = StorageMetadata.Builder()
            .setContentType(contentType)
            .build()

        try {
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                tempFile.outputStream().use { output -> input.copyTo(output) }
            } ?: error("Could not read the photo. Please select the image again.")

            if (tempFile.length() == 0L) {
                error("The selected image is empty. Please choose another photo.")
            }

            storageRef.putFile(Uri.fromFile(tempFile), metadata).await()
            return storageRef.downloadUrl.await().toString()
        } catch (e: StorageException) {
            throw mapStorageException(e)
        } catch (e: Exception) {
            if (e is StorageException) throw mapStorageException(e)
            if (isStorageNotConfiguredMessage(e.message.orEmpty())) {
                throw StorageNotConfiguredException(storageSetupMessage())
            }
            throw e
        } finally {
            tempFile.delete()
        }
    }

    fun readPdfBytes(context: Context, sourceUri: Uri): ByteArray {
        val bytes = context.contentResolver.openInputStream(sourceUri)?.use { it.readBytes() }
            ?: error("Could not read the certificate file. Please select the PDF again.")
        if (bytes.isEmpty()) {
            error("The selected PDF file is empty. Please choose another document.")
        }
        if (bytes.size > MAX_FIRESTORE_CERT_BYTES) {
            error(
                "This PDF is too large for temporary storage (${bytes.size / 1024} KB). " +
                    storageSetupMessage(),
            )
        }
        return bytes
    }

    fun isStorageNotConfigured(error: Throwable): Boolean =
        error is StorageNotConfiguredException ||
            isStorageNotConfiguredMessage(error.message.orEmpty())

    private fun isStorageNotConfiguredMessage(message: String): Boolean =
        message.contains("object does not exist", ignoreCase = true) ||
            message.contains("bucket", ignoreCase = true) ||
            message.contains("has not been set up", ignoreCase = true)

    fun storageSetupMessage(): String =
        "Firebase Storage is not set up yet. In a browser open Firebase Console → Storage → " +
            "Get started (one-time), then run: firebase deploy --only storage"

    private fun mapStorageException(e: StorageException): Exception {
        val message = when (e.errorCode) {
            StorageException.ERROR_OBJECT_NOT_FOUND,
            StorageException.ERROR_BUCKET_NOT_FOUND -> storageSetupMessage()
            StorageException.ERROR_NOT_AUTHORIZED ->
                "Photo upload denied. Ask your developer to deploy Firebase Storage rules " +
                    "(firebase deploy --only storage), then try again."
            StorageException.ERROR_NOT_AUTHENTICATED ->
                "Please sign in again to upload photos."
            StorageException.ERROR_QUOTA_EXCEEDED -> "Storage quota exceeded. Contact support."
            else -> {
                val raw = e.message.orEmpty()
                if (isStorageNotConfiguredMessage(raw)) {
                    storageSetupMessage()
                } else {
                    raw.ifBlank { "Certificate upload failed (${e.errorCode})" }
                }
            }
        }
        return if (e.errorCode == StorageException.ERROR_OBJECT_NOT_FOUND ||
            e.errorCode == StorageException.ERROR_BUCKET_NOT_FOUND ||
            isStorageNotConfiguredMessage(message)
        ) {
            StorageNotConfiguredException(message)
        } else {
            Exception(message)
        }
    }
}
