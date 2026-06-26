package com.jayjaycode.miniproject.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.jayjaycode.miniproject.data.firebase.FirestoreConstants
import kotlinx.coroutines.tasks.await

class FcmTokenRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val messaging: FirebaseMessaging = FirebaseMessaging.getInstance(),
) {

    companion object {
        val instance = FcmTokenRepository()
        private const val FCM_TOKENS_FIELD = "fcmTokens"
    }

    suspend fun registerCurrentToken() {
        val uid = auth.currentUser?.uid ?: return
        val token = runCatching { messaging.token.await() }.getOrNull() ?: return
        if (token.isBlank()) return
        firestore.collection(FirestoreConstants.USERS)
            .document(uid)
            .set(
                mapOf(FCM_TOKENS_FIELD to FieldValue.arrayUnion(token)),
                com.google.firebase.firestore.SetOptions.merge(),
            )
            .await()
    }

    suspend fun removeCurrentToken() {
        val uid = auth.currentUser?.uid ?: return
        val token = runCatching { messaging.token.await() }.getOrNull() ?: return
        if (token.isBlank()) return
        firestore.collection(FirestoreConstants.USERS)
            .document(uid)
            .update(FCM_TOKENS_FIELD, FieldValue.arrayRemove(token))
            .await()
    }
}
