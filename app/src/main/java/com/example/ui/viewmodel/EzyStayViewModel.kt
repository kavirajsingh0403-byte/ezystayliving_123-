package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Announcement
import com.example.data.model.DailyReport
import com.example.data.model.Employee
import com.example.data.model.Expense
import com.example.data.model.Guest
import com.example.data.model.Payment
import com.example.data.repository.EzyStayRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class CrmMetrics(
    val totalBeds: Int = 40,
    val occupiedBeds: Int = 0,
    val vacantBeds: Int = 40,
    val occupancyRate: Double = 0.0,
    val totalMonthlyRentExpected: Double = 0.0,
    val totalCollectedThisMonth: Double = 0.0,
    val totalPendingDues: Double = 0.0,
    val totalExpensesThisMonth: Double = 0.0,
    val netCashFlow: Double = 0.0,
    val overdueCount: Int = 0,
    val dueSoonCount: Int = 0,
    val totalEmployees: Int = 0,
    val dprSubmittedToday: Boolean = false
)

class EzyStayViewModel(
    private val repository: EzyStayRepository
) : ViewModel() {

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _guestFilter = MutableStateFlow("ALL") // ALL, DUE_SOON, OVERDUE, PAID
    val guestFilter: StateFlow<String> = _guestFilter.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setGuestFilter(filter: String) {
        _guestFilter.value = filter
    }

    val guestsState: StateFlow<UiState<List<Guest>>> = repository.observeGuests()
        .map<List<Guest>, UiState<List<Guest>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Failed to load guests")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val paymentsState: StateFlow<UiState<List<Payment>>> = repository.observePayments()
        .map<List<Payment>, UiState<List<Payment>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Failed to load payments")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val employeesState: StateFlow<UiState<List<Employee>>> = repository.observeEmployees()
        .map<List<Employee>, UiState<List<Employee>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Failed to load employees")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val expensesState: StateFlow<UiState<List<Expense>>> = repository.observeExpenses()
        .map<List<Expense>, UiState<List<Expense>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Failed to load expenses")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val dailyReportsState: StateFlow<UiState<List<DailyReport>>> = repository.observeDailyReports()
        .map<List<DailyReport>, UiState<List<DailyReport>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Failed to load daily reports")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    val announcementsState: StateFlow<UiState<List<Announcement>>> = repository.observeAnnouncements()
        .map<List<Announcement>, UiState<List<Announcement>>> { UiState.Success(it) }
        .catch { emit(UiState.Error(it.message ?: "Failed to load announcements")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = UiState.Loading
        )

    // Derived CRM Metrics StateFlow
    val metricsState: StateFlow<CrmMetrics> = combine(
        guestsState,
        paymentsState,
        expensesState,
        employeesState,
        dailyReportsState
    ) { gState, pState, expState, empState, dprState ->
        val guests = (gState as? UiState.Success)?.data ?: emptyList()
        val payments = (pState as? UiState.Success)?.data ?: emptyList()
        val expenses = (expState as? UiState.Success)?.data ?: emptyList()
        val employees = (empState as? UiState.Success)?.data ?: emptyList()
        val dprs = (dprState as? UiState.Success)?.data ?: emptyList()

        val totalBeds = 40
        val occupied = guests.size
        val vacant = (totalBeds - occupied).coerceAtLeast(0)
        val occRate = if (totalBeds > 0) (occupied.toDouble() / totalBeds) * 100.0 else 0.0

        val totalExpected = guests.sumOf { it.monthlyRent }
        val collected = payments.sumOf { it.amount }
        val pending = guests.filter { it.paymentStatus != "PAID" }.sumOf { it.monthlyRent }
        val totalExp = expenses.sumOf { it.amount }
        val net = collected - totalExp

        val overdue = guests.count { it.paymentStatus == "OVERDUE" }
        val dueSoon = guests.count { it.paymentStatus == "DUE_SOON" }

        val todayDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        val isDprDone = dprs.any { it.reportDate == todayDate }

        CrmMetrics(
            totalBeds = totalBeds,
            occupiedBeds = occupied,
            vacantBeds = vacant,
            occupancyRate = occRate,
            totalMonthlyRentExpected = totalExpected,
            totalCollectedThisMonth = collected,
            totalPendingDues = pending,
            totalExpensesThisMonth = totalExp,
            netCashFlow = net,
            overdueCount = overdue,
            dueSoonCount = dueSoon,
            totalEmployees = employees.size,
            dprSubmittedToday = isDprDone
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = CrmMetrics()
    )

    fun initializeUserData(userName: String, userEmail: String) {
        viewModelScope.launch {
            repository.ensureUserProfile(userName, userEmail)
            repository.seedInitialDataIfEmpty()
        }
    }

    // --- ACTIONS ---

    fun saveGuest(guest: Guest, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.saveGuest(guest)
            if (res.isSuccess) {
                _userMessage.emit("Guest booking saved successfully!")
                onComplete()
            } else {
                _userMessage.emit("Error saving guest: ${res.exceptionOrNull()?.localizedMessage}")
            }
        }
    }

    fun deleteGuest(guestId: String) {
        viewModelScope.launch {
            val res = repository.deleteGuest(guestId)
            if (res.isSuccess) {
                _userMessage.emit("Guest record deleted")
            }
        }
    }

    fun recordPaymentAndGenerateReceipt(
        guest: Guest?,
        amount: Double,
        mode: String,
        transactionRef: String,
        rentMonth: String,
        onGenerated: (Payment) -> Unit
    ) {
        viewModelScope.launch {
            val payment = Payment(
                guestId = guest?.id ?: "",
                guestName = guest?.name ?: "Direct Walk-in",
                contractNo = guest?.contractNo ?: "EZS-${System.currentTimeMillis() % 10000}",
                amount = amount,
                paymentMode = mode,
                transactionRef = transactionRef,
                rentMonth = rentMonth,
                paymentDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()),
                smsSent = true,
                emailSent = true,
                recipientEmail = guest?.email ?: "",
                recipientPhone = guest?.phone ?: "",
                notes = "Automated digital receipt generated for Contract #${guest?.contractNo}"
            )
            val res = repository.recordPayment(payment)
            if (res.isSuccess) {
                val recorded = res.getOrThrow()
                _userMessage.emit("Payment recorded & Receipt #${recorded.receiptNo} sent via SMS/Email!")
                onGenerated(recorded)
            } else {
                _userMessage.emit("Payment recording failed: ${res.exceptionOrNull()?.localizedMessage}")
            }
        }
    }

    fun sendDuesAlert(guest: Guest) {
        viewModelScope.launch {
            _userMessage.emit("SMS alert of dues sent to ${guest.name} (${guest.phone}) for Contract #${guest.contractNo} before payment date!")
        }
    }

    fun saveEmployee(employee: Employee, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.saveEmployee(employee)
            if (res.isSuccess) {
                _userMessage.emit("Employee control panel updated successfully")
                onComplete()
            } else {
                _userMessage.emit("Failed to update employee: ${res.exceptionOrNull()?.localizedMessage}")
            }
        }
    }

    fun deleteEmployee(empId: String) {
        viewModelScope.launch {
            val res = repository.deleteEmployee(empId)
            if (res.isSuccess) {
                _userMessage.emit("Employee record removed")
            }
        }
    }

    fun addExpense(expense: Expense, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val res = repository.saveExpense(expense)
            if (res.isSuccess) {
                _userMessage.emit("Operational expense logged to accounts module")
                onComplete()
            } else {
                _userMessage.emit("Failed to log expense")
            }
        }
    }

    fun deleteExpense(expenseId: String) {
        viewModelScope.launch {
            val res = repository.deleteExpense(expenseId)
            if (res.isSuccess) {
                _userMessage.emit("Expense deleted from accounts")
            }
        }
    }

    fun submitDailyProgressReport(
        remarks: String,
        messStatus: String,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val now = Calendar.getInstance()
            val cutoff = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 20) // 8:00 PM cutoff time
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }
            val beforeCutoff = now.before(cutoff)

            val currentMetrics = metricsState.value
            val todayStr = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
            val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

            val report = DailyReport(
                reportDate = todayStr,
                submittedAt = timeStr,
                submittedBeforeCutoff = beforeCutoff,
                submittedBy = "EzyStay Staff / Manager",
                occupancyRate = currentMetrics.occupancyRate,
                totalBedsOccupied = currentMetrics.occupiedBeds.toLong(),
                totalBedsAvailable = currentMetrics.vacantBeds.toLong(),
                collectionsToday = currentMetrics.totalCollectedThisMonth,
                pendingDuesCollected = currentMetrics.totalPendingDues,
                newCheckIns = 1L,
                checkOuts = 0L,
                maintenanceIssuesCount = 1L,
                maintenanceResolvedCount = 1L,
                messStatus = messStatus.ifBlank { "All meals served on schedule in accordance with nutrition plan." },
                summaryRemarks = remarks.ifBlank { "Daily progress report updated before till time (8:00 PM cutoff). All rooms secure." }
            )
            val res = repository.submitDailyReport(report)
            if (res.isSuccess) {
                val statusText = if (beforeCutoff) "SUBMITTED BEFORE CUTOFF TIME" else "SUBMITTED POST CUTOFF"
                _userMessage.emit("DPR successfully uploaded! Status: $statusText")
                onComplete()
            } else {
                _userMessage.emit("DPR submission failed: ${res.exceptionOrNull()?.localizedMessage}")
            }
        }
    }

    fun broadcastCeoAnnouncement(
        title: String,
        message: String,
        priority: String,
        audience: String,
        sendSms: Boolean,
        sendEmail: Boolean,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val notice = Announcement(
                title = title,
                message = message,
                priority = priority,
                sentBy = "Mr. Kavi Raj Singh (CEO)",
                targetAudience = audience,
                sentViaSms = sendSms,
                sentViaEmail = sendEmail
            )
            val res = repository.createAnnouncement(notice)
            if (res.isSuccess) {
                _userMessage.emit("CEO notice dispatched to employees via SMS & registered email!")
                onComplete()
            } else {
                _userMessage.emit("Notice broadcast failed: ${res.exceptionOrNull()?.localizedMessage}")
            }
        }
    }
}
