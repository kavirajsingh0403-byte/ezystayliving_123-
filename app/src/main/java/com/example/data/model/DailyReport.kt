package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class DailyReport(
    @DocumentId val id: String = "",
    val reportDate: String = "",
    val submittedBy: String = "",
    val cutoffMet: Boolean = true,
    val totalRooms: Int = 0,
    val occupiedRooms: Int = 0,
    val vacantRooms: Int = 0,
    val maintenanceRooms: Int = 0,
    val occupancyRate: Double = 0.0,
    val todayCollections: Double = 0.0,
    val pendingDuesCount: Int? = 0,
    val pendingDuesAmount: Double? = 0.0,
    val remarks: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "reportDate" to reportDate,
        "submittedBy" to submittedBy,
        "cutoffMet" to cutoffMet,
        "totalRooms" to totalRooms,
        "occupiedRooms" to occupiedRooms,
        "vacantRooms" to vacantRooms,
        "maintenanceRooms" to maintenanceRooms,
        "occupancyRate" to occupancyRate,
        "todayCollections" to todayCollections,
        "pendingDuesCount" to pendingDuesCount,
        "pendingDuesAmount" to pendingDuesAmount,
        "remarks" to remarks
    ).filterValues { it != null }
}
