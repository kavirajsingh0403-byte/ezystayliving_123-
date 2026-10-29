package com.example.util

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestoreException
import org.json.JSONObject

enum class OperationType {
    CREATE,
    READ,
    UPDATE,
    DELETE,
    LIST
}

object FirestoreErrorHandler {
    private const val TAG = "FirestoreError"

    fun handleFirestoreError(e: Exception, operation: OperationType, path: String) {
        val json = JSONObject().apply {
            put("error", e.javaClass.simpleName)
            put("message", e.localizedMessage ?: "Unknown firestore error")
            put("operation", operation.name)
            put("path", path)
            if (e is FirebaseFirestoreException) {
                put("code", e.code.name)
            }
        }
        Log.e(TAG, json.toString(), e)
    }
}
