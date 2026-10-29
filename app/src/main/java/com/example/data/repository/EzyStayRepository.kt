package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.Alert
import com.example.data.model.DailyReport
import com.example.data.model.Employee
import com.example.data.model.Expense
import com.example.data.model.Guest
import com.example.data.model.Payment
import com.example.data.model.Room
import com.example.util.FirestoreErrorHandler
import com.example.util.OperationType
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class EzyStayRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeRooms(): Flow<List<Room>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/rooms"
        emitAll(
            db.collection("users").document(uid).collection("rooms")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(Room::class.java).sortedBy { it.roomNumber } }
                .catch { error ->
                    if (error is Exception) FirestoreErrorHandler.handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeGuests(): Flow<List<Guest>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/guests"
        emitAll(
            db.collection("users").document(uid).collection("guests")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(Guest::class.java).sortedBy { it.fullName } }
                .catch { error ->
                    if (error is Exception) FirestoreErrorHandler.handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observePayments(): Flow<List<Payment>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/payments"
        emitAll(
            db.collection("users").document(uid).collection("payments")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(Payment::class.java).sortedByDescending { it.createdAt } }
                .catch { error ->
                    if (error is Exception) FirestoreErrorHandler.handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeEmployees(): Flow<List<Employee>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/employees"
        emitAll(
            db.collection("users").document(uid).collection("employees")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(Employee::class.java).sortedBy { it.name } }
                .catch { error ->
                    if (error is Exception) FirestoreErrorHandler.handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeAlerts(): Flow<List<Alert>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/alerts"
        emitAll(
            db.collection("users").document(uid).collection("alerts")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(Alert::class.java).sortedByDescending { it.createdAt } }
                .catch { error ->
                    if (error is Exception) FirestoreErrorHandler.handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeReports(): Flow<List<DailyReport>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/reports"
        emitAll(
            db.collection("users").document(uid).collection("reports")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(DailyReport::class.java).sortedByDescending { it.reportDate } }
                .catch { error ->
                    if (error is Exception) FirestoreErrorHandler.handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    fun observeExpenses(): Flow<List<Expense>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/expenses"
        emitAll(
            db.collection("users").document(uid).collection("expenses")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(Expense::class.java).sortedByDescending { it.createdAt } }
                .catch { error ->
                    if (error is Exception) FirestoreErrorHandler.handleFirestoreError(error, OperationType.LIST, path)
                    throw error
                }
        )
    }

    suspend fun addRoom(room: Room) {
        val uid = requireUserId()
        val docId = if (room.id.isNotBlank()) room.id else "room_${room.roomNumber.lowercase(Locale.ROOT).replace(" ", "_")}"
        val docRef = db.collection("users").document(uid).collection("rooms").document(docId)
        val payload = room.toMap() + mapOf(
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: Exception) {
            FirestoreErrorHandler.handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateRoom(room: Room) {
        val uid = requireUserId()
        require(room.id.isNotBlank()) { "Room ID cannot be blank for update" }
        val docRef = db.collection("users").document(uid).collection("rooms").document(room.id)
        val payload = room.toMap() + mapOf(
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(payload).await()
        } catch (e: Exception) {
            FirestoreErrorHandler.handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun addGuest(guest: Guest) {
        val uid = requireUserId()
        val docId = if (guest.id.isNotBlank()) guest.id else "guest_${UUID.randomUUID().toString().take(8)}"
        val docRef = db.collection("users").document(uid).collection("guests").document(docId)
        val payload = guest.toMap() + mapOf(
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: Exception) {
            FirestoreErrorHandler.handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateGuest(guest: Guest) {
        val uid = requireUserId()
        require(guest.id.isNotBlank()) { "Guest ID cannot be blank" }
        val docRef = db.collection("users").document(uid).collection("guests").document(guest.id)
        val payload = guest.toMap() + mapOf(
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(payload).await()
        } catch (e: Exception) {
            FirestoreErrorHandler.handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun recordPayment(
        guestName: String,
        contractNo: String,
        amount: Double,
        paymentType: String,
        paymentMode: String,
        guestId: String?,
        monthFor: String?,
        sendSms: Boolean = true,
        sendEmail: Boolean = true
    ): Payment {
        val uid = requireUserId()
        val receiptNo = "REC-${SimpleDateFormat("yyMMdd", Locale.US).format(Date())}-${(1000..9999).random()}"
        val docId = "pay_${UUID.randomUUID().toString().take(8)}"
        val docRef = db.collection("users").document(uid).collection("payments").document(docId)

        val payment = Payment(
            id = docId,
            receiptNo = receiptNo,
            guestId = guestId,
            guestName = guestName,
            contractNo = contractNo,
            amount = amount,
            paymentType = paymentType,
            paymentMode = paymentMode,
            monthFor = monthFor,
            status = "Success",
            receiptGenerated = true,
            smsAlertSent = sendSms,
            emailReceiptSent = sendEmail
        )

        val payload = payment.toMap() + mapOf(
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()

            // Record alert notification
            val alertId = "alert_${UUID.randomUUID().toString().take(8)}"
            val alertRef = db.collection("users").document(uid).collection("alerts").document(alertId)
            val alertPayload = mapOf(
                "alertType" to "PAYMENT_RECEIPT",
                "title" to "Payment Received: $receiptNo",
                "message" to "Dear $guestName, your payment of Rs. ${amount.toInt()} for Contract #$contractNo has been received successfully. Receipt #$receiptNo sent via SMS & registered email.",
                "targetRecipient" to "guest",
                "contractNo" to contractNo,
                "channel" to if (sendSms && sendEmail) "SMS_AND_EMAIL" else if (sendSms) "SMS" else "EMAIL",
                "status" to "Sent",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            alertRef.set(alertPayload).await()

            return payment
        } catch (e: Exception) {
            FirestoreErrorHandler.handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun addEmployee(employee: Employee) {
        val uid = requireUserId()
        val docId = if (employee.id.isNotBlank()) employee.id else "emp_${UUID.randomUUID().toString().take(8)}"
        val docRef = db.collection("users").document(uid).collection("employees").document(docId)
        val payload = employee.toMap() + mapOf(
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: Exception) {
            FirestoreErrorHandler.handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateEmployee(employee: Employee) {
        val uid = requireUserId()
        require(employee.id.isNotBlank()) { "Employee ID cannot be blank" }
        val docRef = db.collection("users").document(uid).collection("employees").document(employee.id)
        val payload = employee.toMap() + mapOf(
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.update(payload).await()
        } catch (e: Exception) {
            FirestoreErrorHandler.handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteEmployee(employeeId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("employees").document(employeeId)
        try {
            docRef.delete().await()
        } catch (e: Exception) {
            FirestoreErrorHandler.handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    suspend fun sendDueAlert(guest: Guest) {
        val uid = requireUserId()
        val alertId = "alert_${UUID.randomUUID().toString().take(8)}"
        val alertRef = db.collection("users").document(uid).collection("alerts").document(alertId)
        val alertPayload = mapOf(
            "alertType" to "DUE_REMINDER",
            "title" to "Rent Due Alert: Contract ${guest.contractNo}",
            "message" to "Alert from EzyStay Living: Dear ${guest.fullName}, monthly rent of Rs. ${guest.monthlyRent.toInt()} is due on ${guest.rentDueDay}th of this month for Room ${guest.roomNumber} (${guest.bedNumber}). Please clear before due date to avoid late fee.",
            "targetRecipient" to "guest",
            "recipientPhone" to guest.phone,
            "recipientEmail" to guest.email,
            "contractNo" to guest.contractNo,
            "channel" to "SMS_AND_EMAIL",
            "status" to "Sent",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            alertRef.set(alertPayload).await()
        } catch (e: Exception) {
            FirestoreErrorHandler.handleFirestoreError(e, OperationType.CREATE, alertRef.path)
            throw e
        }
    }

    suspend fun sendCeoBroadcast(title: String, message: String, channel: String = "SMS_AND_EMAIL") {
        val uid = requireUserId()
        val alertId = "alert_${UUID.randomUUID().toString().take(8)}"
        val alertRef = db.collection("users").document(uid).collection("alerts").document(alertId)
        val alertPayload = mapOf(
            "alertType" to "CEO_BROADCAST",
            "title" to "[CEO Notice] $title",
            "message" to "Preinformed from CEO Mr. Kavi Raj Singh to Staff: $message. Sent via SMS & Official Email.",
            "targetRecipient" to "all_staff",
            "channel" to channel,
            "status" to "Sent",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            alertRef.set(alertPayload).await()
        } catch (e: Exception) {
            FirestoreErrorHandler.handleFirestoreError(e, OperationType.CREATE, alertRef.path)
            throw e
        }
    }

    suspend fun submitDailyReport(report: DailyReport) {
        val uid = requireUserId()
        val docId = "dpr_${report.reportDate.replace("-", "")}"
        val docRef = db.collection("users").document(uid).collection("reports").document(docId)
        val payload = report.toMap() + mapOf(
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: Exception) {
            FirestoreErrorHandler.handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun addExpense(expense: Expense) {
        val uid = requireUserId()
        val docId = "exp_${UUID.randomUUID().toString().take(8)}"
        val docRef = db.collection("users").document(uid).collection("expenses").document(docId)
        val payload = expense.toMap() + mapOf(
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: Exception) {
            FirestoreErrorHandler.handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun seedInitialDataIfEmpty() {
        val uid = requireUserId()
        val roomsColl = db.collection("users").document(uid).collection("rooms")
        val existing = roomsColl.limit(1).get().await()
        if (!existing.isEmpty) return

        // Seed 12 comprehensive rooms across floors
        val sampleRooms = listOf(
            Room(roomNumber = "101", floor = "1st Floor", roomType = "Double Sharing", totalBeds = 2, occupiedBeds = 2, status = "occupied", rentPerBed = 7500.0),
            Room(roomNumber = "102", floor = "1st Floor", roomType = "Double Sharing", totalBeds = 2, occupiedBeds = 1, status = "partially_occupied", rentPerBed = 7500.0),
            Room(roomNumber = "103", floor = "1st Floor", roomType = "Single Private", totalBeds = 1, occupiedBeds = 0, status = "vacant", rentPerBed = 12000.0),
            Room(roomNumber = "104", floor = "1st Floor", roomType = "Triple Sharing", totalBeds = 3, occupiedBeds = 0, status = "maintenance", rentPerBed = 6000.0, maintenanceNote = "AC refrigerant repair in progress"),
            Room(roomNumber = "201", floor = "2nd Floor", roomType = "Double Sharing", totalBeds = 2, occupiedBeds = 2, status = "occupied", rentPerBed = 7500.0),
            Room(roomNumber = "202", floor = "2nd Floor", roomType = "Triple Sharing", totalBeds = 3, occupiedBeds = 2, status = "partially_occupied", rentPerBed = 6000.0),
            Room(roomNumber = "203", floor = "2nd Floor", roomType = "Single Private", totalBeds = 1, occupiedBeds = 1, status = "occupied", rentPerBed = 12000.0),
            Room(roomNumber = "204", floor = "2nd Floor", roomType = "Double Sharing", totalBeds = 2, occupiedBeds = 0, status = "vacant", rentPerBed = 7500.0),
            Room(roomNumber = "301", floor = "3rd Floor", roomType = "Dorm 4-Bed", totalBeds = 4, occupiedBeds = 3, status = "partially_occupied", rentPerBed = 5000.0),
            Room(roomNumber = "302", floor = "3rd Floor", roomType = "Double Sharing", totalBeds = 2, occupiedBeds = 0, status = "vacant", rentPerBed = 7500.0),
            Room(roomNumber = "303", floor = "3rd Floor", roomType = "Double Sharing", totalBeds = 2, occupiedBeds = 0, status = "maintenance", rentPerBed = 7500.0, maintenanceNote = "Deep cleaning and geyser inspection"),
            Room(roomNumber = "304", floor = "3rd Floor", roomType = "Single Private", totalBeds = 1, occupiedBeds = 1, status = "occupied", rentPerBed = 12500.0)
        )

        for (r in sampleRooms) {
            val docId = "room_${r.roomNumber}"
            roomsColl.document(docId).set(
                r.toMap() + mapOf(
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }

        // Seed Sample Guests
        val guestsColl = db.collection("users").document(uid).collection("guests")
        val sampleGuests = listOf(
            Guest(contractNo = "EZS-2026-101A", fullName = "Aarav Sharma", phone = "9876543210", email = "aarav.sharma@example.com", roomNumber = "101", bedNumber = "Bed-A", monthlyRent = 7500.0, securityDeposit = 7500.0, rentDueDay = 5, status = "active", emergencyContact = "9876500000"),
            Guest(contractNo = "EZS-2026-101B", fullName = "Rohan Verma", phone = "9812345678", email = "rohan.v@example.com", roomNumber = "101", bedNumber = "Bed-B", monthlyRent = 7500.0, securityDeposit = 7500.0, rentDueDay = 7, status = "active", emergencyContact = "9812300000"),
            Guest(contractNo = "EZS-2026-102A", fullName = "Vikram Aditya", phone = "9711223344", email = "vikram.aditya@example.com", roomNumber = "102", bedNumber = "Bed-A", monthlyRent = 7500.0, securityDeposit = 7500.0, rentDueDay = 3, status = "active", emergencyContact = "9711200000"),
            Guest(contractNo = "EZS-2026-201A", fullName = "Priya Nair", phone = "9654321890", email = "priya.nair@example.com", roomNumber = "201", bedNumber = "Bed-A", monthlyRent = 7500.0, securityDeposit = 7500.0, rentDueDay = 10, status = "active", emergencyContact = "9654300000"),
            Guest(contractNo = "EZS-2026-203A", fullName = "Devendra Joshi", phone = "9899887766", email = "d.joshi@example.com", roomNumber = "203", bedNumber = "Bed-A", monthlyRent = 12000.0, securityDeposit = 12000.0, rentDueDay = 1, status = "active", emergencyContact = "9899800000")
        )

        for (g in sampleGuests) {
            val docId = "guest_${g.contractNo.lowercase(Locale.ROOT).replace("-", "_")}"
            guestsColl.document(docId).set(
                g.toMap() + mapOf(
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }

        // Seed Sample Staff
        val empColl = db.collection("users").document(uid).collection("employees")
        val sampleEmployees = listOf(
            Employee(name = "Suresh Kumar", role = "Property Manager", phone = "9873677108", email = "suresh.ezy@gmail.com", shift = "Morning (08:00 - 17:00)", salary = 28000.0, status = "active"),
            Employee(name = "Rajendra Yadav", role = "Hostel Warden", phone = "9811223399", email = "rajendra.warden@gmail.com", shift = "Evening (14:00 - 22:00)", salary = 24000.0, status = "active"),
            Employee(name = "Sunita Devi", role = "Housekeeping Lead", phone = "9871100223", email = "sunita.hk@gmail.com", shift = "Morning (07:00 - 15:00)", salary = 18000.0, status = "active"),
            Employee(name = "Manish Rawat", role = "Accounts Executive", phone = "9899112233", email = "accounts.ezystay@gmail.com", shift = "General (09:30 - 18:30)", salary = 26000.0, status = "active")
        )

        for (e in sampleEmployees) {
            val docId = "emp_${e.name.lowercase(Locale.ROOT).replace(" ", "_")}"
            empColl.document(docId).set(
                e.toMap() + mapOf(
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }

        // Seed Recent Payments
        val payColl = db.collection("users").document(uid).collection("payments")
        val samplePayments = listOf(
            Payment(receiptNo = "REC-261028-1001", guestName = "Aarav Sharma", contractNo = "EZS-2026-101A", amount = 7500.0, paymentType = "Rent", paymentMode = "UPI", monthFor = "October 2026", status = "Success", receiptGenerated = true, smsAlertSent = true, emailReceiptSent = true),
            Payment(receiptNo = "REC-261028-1002", guestName = "Devendra Joshi", contractNo = "EZS-2026-203A", amount = 12000.0, paymentType = "Rent", paymentMode = "Bank Transfer", monthFor = "October 2026", status = "Success", receiptGenerated = true, smsAlertSent = true, emailReceiptSent = true)
        )
        for (p in samplePayments) {
            val docId = "pay_${p.receiptNo.lowercase(Locale.ROOT).replace("-", "_")}"
            payColl.document(docId).set(
                p.toMap() + mapOf(
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }

        // Seed Sample Expense
        val expColl = db.collection("users").document(uid).collection("expenses")
        val sampleExpense = Expense(title = "High Speed Fiber WiFi 500Mbps", category = "Utilities", amount = 3500.0, paidTo = "Airtel Broadband", paymentMode = "UPI")
        expColl.document("exp_wifi_oct").set(
            sampleExpense.toMap() + mapOf(
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
        ).await()
    }
}
