package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Alert(
    @DocumentId val id: String = "",
    val alertType: String = "DUE_REMINDER", // DUE_REMINDER, CEO_BROADCAST, PAYMENT_RECEIPT, MAINTENANCE
    val title: String = "",
    val message: String = "",
    val targetRecipient: String? = null,
    val recipientPhone: String? = null,
    val recipientEmail: String? = null,
    val contractNo: String? = null,
    val channel: String = "SMS_AND_EMAIL", // SMS_AND_EMAIL, SMS, EMAIL
    val status: String = "Sent", // Sent, Scheduled, Pending
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "alertType" to alertType,
        "title" to title,
        "message" to message,
        "targetRecipient" to targetRecipient,
        "recipientPhone" to recipientPhone,
        "recipientEmail" to recipientEmail,
        "contractNo" to contractNo,
        "channel" to channel,
        "status" to status
    ).filterValues { it != null }
}
