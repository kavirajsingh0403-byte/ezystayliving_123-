package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Expense(
    @DocumentId val id: String = "",
    val title: String = "",
    val category: String = "Utilities", // Utilities, Maintenance, Salaries, Groceries, Misc
    val amount: Double = 0.0,
    val paidTo: String = "",
    val paymentMode: String = "UPI",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "title" to title,
        "category" to category,
        "amount" to amount,
        "paidTo" to paidTo,
        "paymentMode" to paymentMode
    ).filterValues { it != null }
}
