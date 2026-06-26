package com.jayjaycode.miniproject.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jayjaycode.miniproject.ui.theme.GreenAccent
import com.jayjaycode.miniproject.ui.theme.OrangePrimary
import com.jayjaycode.miniproject.ui.theme.TextSecondary
import com.jayjaycode.miniproject.util.DocumentPickerUtils

@Composable
fun RegistrationCertificatePicker(
    certificateUri: Uri?,
    certificateFileName: String,
    onCertificateSelected: (Uri, String) -> Unit,
    onCertificateCleared: () -> Unit,
    isError: Boolean,
    onInvalidFile: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val pdfLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        try {
            DocumentPickerUtils.requirePdfDocument(context, uri)
            val name = DocumentPickerUtils.resolveDisplayName(context, uri)
            context.contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
            onCertificateSelected(uri, name.ifBlank { "registration_certificate.pdf" })
        } catch (e: Exception) {
            onInvalidFile(e.localizedMessage ?: "Please select a PDF file")
            onCertificateCleared()
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Business registration certificate", fontWeight = FontWeight.SemiBold)
        Text(
            "Upload your official business registration certificate as a PDF. This is required.",
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
        )

        val borderColor = when {
            isError -> MaterialTheme.colorScheme.error
            certificateUri != null -> GreenAccent
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (certificateUri != null) {
                    GreenAccent.copy(alpha = 0.08f)
                } else {
                    MaterialTheme.colorScheme.surface
                },
            ),
            border = BorderStroke(1.dp, borderColor),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        if (certificateUri != null) Icons.Default.Description else Icons.Default.UploadFile,
                        contentDescription = null,
                        tint = if (certificateUri != null) GreenAccent else OrangePrimary,
                        modifier = Modifier.size(28.dp),
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (certificateUri != null) certificateFileName else "No document selected",
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            if (certificateUri != null) "PDF ready to upload" else "PDF only · max recommended 10 MB",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { pdfLauncher.launch(arrayOf("application/pdf")) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(if (certificateUri != null) "Replace PDF" else "Upload PDF")
                    }
                    if (certificateUri != null) {
                        TextButton(onClick = onCertificateCleared) {
                            Text("Remove")
                        }
                    }
                }
            }
        }

        if (isError) {
            Text(
                "A business registration certificate (PDF) is required.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}
