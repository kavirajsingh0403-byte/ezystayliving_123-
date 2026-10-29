package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class Room(
    @DocumentId val id: String = "",
    val roomNumber: String = "",
    val floor: String = "",
    val roomType: String = "",
    val totalBeds: Int = 2,
    val occupiedBeds: Int = 0,
    val status: String = "vacant", // vacant, occupied, maintenance, partially_occupied
    val rentPerBed: Double = 0.0,
    val maintenanceNote: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "roomNumber" to roomNumber,
        "floor" to floor,
        "roomType" to roomType,
        "totalBeds" to totalBeds,
        "occupiedBeds" to occupiedBeds,
        "status" to status,
        "rentPerBed" to rentPerBed,
        "maintenanceNote" to maintenanceNote
    ).filterValues { it != null }
}
