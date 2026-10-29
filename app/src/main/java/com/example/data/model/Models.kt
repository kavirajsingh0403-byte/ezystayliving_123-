package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "Hostel Admin",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Guest(
    @DocumentId val id: String = "",
    val contractNo: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val roomNo: String = "",
    val bedNo: String = "",
    val roomType: String = "Double AC",
    val checkInDate: String = "",
    val checkOutDate: String = "",
    val monthlyRent: Double = 0.0,
    val securityDeposit: Double = 0.0,
    val paymentStatus: String = "DUE_SOON", // PAID, DUE_SOON, OVERDUE, PARTIAL
    val nextDueDate: String = "",
    val emergencyContact: String = "",
    val idProofType: String = "Aadhaar Card",
    val idProofNumber: String = "",
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Payment(
    @DocumentId val id: String = "",
    val receiptNo: String = "",
    val guestId: String = "",
    val guestName: String = "",
    val contractNo: String = "",
    val amount: Double = 0.0,
    val paymentMode: String = "UPI", // UPI, Cash, Bank Transfer, Card
    val transactionRef: String = "",
    val rentMonth: String = "",
    val paymentDate: String = "",
    val smsSent: Boolean = false,
    val emailSent: Boolean = false,
    val recipientEmail: String = "",
    val recipientPhone: String = "",
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Employee(
    @DocumentId val id: String = "",
    val name: String = "",
    val role: String = "Warden", // Manager, Warden, Housekeeping, Accountant, Security
    val phone: String = "",
    val email: String = "",
    val shift: String = "Morning (7 AM - 3 PM)",
    val branch: String = "Laxmi Nagar Main Campus",
    val isActive: Boolean = true,
    val tasksAssigned: String = "Daily room inspections and mess check",
    val lastCheckIn: String = "Today, 08:30 AM",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Expense(
    @DocumentId val id: String = "",
    val title: String = "",
    val category: String = "Electricity", // Electricity, Water, WiFi, Maintenance, Salaries, Food/Mess, Rent, Other
    val amount: Double = 0.0,
    val date: String = "",
    val paidTo: String = "",
    val paymentMode: String = "UPI",
    val receiptUrl: String = "",
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class DailyReport(
    @DocumentId val id: String = "",
    val reportDate: String = "",
    val submittedAt: String = "",
    val submittedBeforeCutoff: Boolean = true,
    val submittedBy: String = "",
    val occupancyRate: Double = 0.0,
    val totalBedsOccupied: Long = 0L,
    val totalBedsAvailable: Long = 0L,
    val collectionsToday: Double = 0.0,
    val pendingDuesCollected: Double = 0.0,
    val newCheckIns: Long = 0L,
    val checkOuts: Long = 0L,
    val maintenanceIssuesCount: Long = 0L,
    val maintenanceResolvedCount: Long = 0L,
    val messStatus: String = "Breakfast, Lunch, Dinner served as per menu",
    val summaryRemarks: String = "All operations normal. Dues follow-ups sent to registered guests.",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Announcement(
    @DocumentId val id: String = "",
    val title: String = "",
    val message: String = "",
    val priority: String = "NORMAL", // URGENT, HIGH, NORMAL
    val sentBy: String = "Mr. Kavi Raj Singh (CEO)",
    val targetAudience: String = "All Employees",
    val sentViaSms: Boolean = true,
    val sentViaEmail: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
