package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Employee(
    @DocumentId val id: String = "",
    val name: String = "",
    val role: String = "Manager", // Manager, Warden, Housekeeping, Accounts, Receptionist
    val phone: String = "",
    val email: String = "",
    val shift: String = "Day (08:00 - 18:00)",
    val salary: Double = 0.0,
    val status: String = "active", // active, on_leave
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "role" to role,
        "phone" to phone,
        "email" to email,
        "shift" to shift,
        "salary" to salary,
        "status" to status
    ).filterValues { it != null }
}
