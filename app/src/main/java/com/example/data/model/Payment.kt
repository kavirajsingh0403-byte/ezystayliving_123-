package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Payment(
    @DocumentId val id: String = "",
    val receiptNo: String = "",
    val guestId: String? = null,
    val guestName: String = "",
    val contractNo: String = "",
    val amount: Double = 0.0,
    val paymentType: String = "Rent", // Rent, Security Deposit, Maintenance, Electricity
    val paymentMode: String = "UPI", // UPI, Cash, Bank Transfer, Card
    val transactionRef: String? = null,
    val monthFor: String? = null,
    val status: String = "Success", // Success, Pending, Failed
    val receiptGenerated: Boolean = true,
    val smsAlertSent: Boolean = false,
    val emailReceiptSent: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "receiptNo" to receiptNo,
        "guestId" to guestId,
        "guestName" to guestName,
        "contractNo" to contractNo,
        "amount" to amount,
        "paymentType" to paymentType,
        "paymentMode" to paymentMode,
        "transactionRef" to transactionRef,
        "monthFor" to monthFor,
        "status" to status,
        "receiptGenerated" to receiptGenerated,
        "smsAlertSent" to smsAlertSent,
        "emailReceiptSent" to emailReceiptSent
    ).filterValues { it != null }
}
