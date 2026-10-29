package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Guest(
    @DocumentId val id: String = "",
    val contractNo: String = "",
    val fullName: String = "",
    val phone: String = "",
    val email: String = "",
    val roomNumber: String = "",
    val bedNumber: String = "",
    val monthlyRent: Double = 0.0,
    val securityDeposit: Double = 0.0,
    val rentDueDay: Int = 1, // day of month 1..31
    val status: String = "active", // active, checked_out, reserved
    val idProofType: String? = null,
    val idProofNumber: String? = null,
    val emergencyContact: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "contractNo" to contractNo,
        "fullName" to fullName,
        "phone" to phone,
        "email" to email,
        "roomNumber" to roomNumber,
        "bedNumber" to bedNumber,
        "monthlyRent" to monthlyRent,
        "securityDeposit" to securityDeposit,
        "rentDueDay" to rentDueDay,
        "status" to status,
        "idProofType" to idProofType,
        "idProofNumber" to idProofNumber,
        "emergencyContact" to emergencyContact
    ).filterValues { it != null }
}
