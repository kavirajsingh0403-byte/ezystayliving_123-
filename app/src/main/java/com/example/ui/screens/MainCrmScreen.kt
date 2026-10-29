package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import com.example.data.ai.AiAssistantService
import com.example.data.model.Guest
import com.example.data.model.HostelDetails
import com.example.data.model.Room
import com.example.data.repository.EzyStayRepository
import com.example.ui.components.DeveloperDetailsDialog
import com.example.ui.components.RoomStatusDashboard
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainCrmScreen(
    repository: EzyStayRepository,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }
    val aiService = remember { AiAssistantService() }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showDeveloperInfo by remember { mutableStateOf(false) }
    var showSignOutConfirm by remember { mutableStateOf(false) }

    // Observe Repository Real-Time Flows
    val rooms by repository.observeRooms().collectAsState(initial = emptyList())
    val guests by repository.observeGuests().collectAsState(initial = emptyList())
    val payments by repository.observePayments().collectAsState(initial = emptyList())
    val employees by repository.observeEmployees().collectAsState(initial = emptyList())
    val alerts by repository.observeAlerts().collectAsState(initial = emptyList())
    val reports by repository.observeReports().collectAsState(initial = emptyList())
    val expenses by repository.observeExpenses().collectAsState(initial = emptyList())

    // Auto-seed initial rooms & sample data if user's database is empty
    LaunchedEffect(Unit) {
        try {
            repository.seedInitialDataIfEmpty()
        } catch (e: Exception) {
            // Logged via structured handler
        }
    }

    // Support Android BackHandler
    BackHandler(enabled = selectedTabIndex != 0) {
        selectedTabIndex = 0
    }

    val totalBeds = rooms.sumOf { it.totalBeds }
    val occupiedBeds = rooms.sumOf { it.occupiedBeds }
    val occupancyPercent = if (totalBeds > 0) ((occupiedBeds.toDouble() / totalBeds) * 100).toInt() else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Hotel,
                                contentDescription = "Logo",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "EzyStay Living",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Hostel CRM • $occupancyPercent% Occupancy",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Developer & Office info button
                    IconButton(
                        onClick = { showDeveloperInfo = true },
                        modifier = Modifier.testTag("developer_info_button")
                    ) {
                        Icon(Icons.Default.Info, contentDescription = "Developer Details")
                    }

                    // Sign Out button
                    IconButton(
                        onClick = { showSignOutConfirm = true },
                        modifier = Modifier.testTag("sign_out_button")
                    ) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Sign Out")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    icon = { Icon(Icons.Default.MeetingRoom, contentDescription = "Rooms") },
                    label = { Text("Rooms", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_rooms")
                )
                NavigationBarItem(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    icon = { Icon(Icons.Default.People, contentDescription = "Guests") },
                    label = { Text("Guests", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_guests")
                )
                NavigationBarItem(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    icon = { Icon(Icons.Default.Receipt, contentDescription = "Accounts") },
                    label = { Text("Accounts", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_accounts")
                )
                NavigationBarItem(
                    selected = selectedTabIndex == 3,
                    onClick = { selectedTabIndex = 3 },
                    icon = { Icon(Icons.Default.Badge, contentDescription = "Staff") },
                    label = { Text("Staff", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_staff")
                )
                NavigationBarItem(
                    selected = selectedTabIndex == 4,
                    onClick = { selectedTabIndex = 4 },
                    icon = { Icon(Icons.Default.Assessment, contentDescription = "DPR") },
                    label = { Text("DPR", fontSize = 11.sp) },
                    modifier = Modifier.testTag("nav_dpr")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTabIndex) {
                0 -> RoomStatusDashboard(
                    rooms = rooms,
                    onUpdateRoom = { updatedRoom ->
                        scope.launch { repository.updateRoom(updatedRoom) }
                    },
                    onAddRoom = { newRoom ->
                        scope.launch { repository.addRoom(newRoom) }
                    },
                    onQuickBookRoom = { room ->
                        selectedTabIndex = 1 // Switch to guest booking screen
                    }
                )

                1 -> GuestsScreen(
                    guests = guests,
                    rooms = rooms,
                    onAddGuest = { newGuest ->
                        scope.launch { repository.addGuest(newGuest) }
                    },
                    onSendDueAlert = { guest ->
                        scope.launch { repository.sendDueAlert(guest) }
                    },
                    onRecordPaymentForGuest = { guest ->
                        selectedTabIndex = 2 // Switch to accounts screen
                    }
                )

                2 -> AccountsScreen(
                    payments = payments,
                    expenses = expenses,
                    guests = guests,
                    onRecordPayment = { guestName, contractNo, amount, pType, pMode, guestId, month, sendSms, sendEmail ->
                        scope.launch {
                            repository.recordPayment(guestName, contractNo, amount, pType, pMode, guestId, month, sendSms, sendEmail)
                        }
                    },
                    onAddExpense = { expense ->
                        scope.launch { repository.addExpense(expense) }
                    }
                )

                3 -> EmployeePanelScreen(
                    employees = employees,
                    alerts = alerts,
                    aiService = aiService,
                    onAddEmployee = { emp ->
                        scope.launch { repository.addEmployee(emp) }
                    },
                    onUpdateEmployee = { emp ->
                        scope.launch { repository.updateEmployee(emp) }
                    },
                    onDeleteEmployee = { id ->
                        scope.launch { repository.deleteEmployee(id) }
                    },
                    onSendCeoBroadcast = { title, msg, ch ->
                        scope.launch { repository.sendCeoBroadcast(title, msg, ch) }
                    }
                )

                4 -> DailyReportScreen(
                    reports = reports,
                    rooms = rooms,
                    guests = guests,
                    payments = payments,
                    aiService = aiService,
                    onSubmitReport = { report ->
                        scope.launch { repository.submitDailyReport(report) }
                    }
                )
            }
        }
    }

    // Developer Details Modal
    if (showDeveloperInfo) {
        DeveloperDetailsDialog(onDismiss = { showDeveloperInfo = false })
    }

    // Sign Out Confirmation Dialog
    if (showSignOutConfirm) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirm = false },
            title = { Text("Sign Out", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to sign out from EzyStay Living CRM?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutConfirm = false
                        Firebase.auth.signOut()
                        scope.launch {
                            try {
                                credentialManager.clearCredentialState(ClearCredentialStateRequest())
                            } catch (e: Exception) {
                                // Ignored
                            }
                            onSignOut()
                        }
                    }
                ) {
                    Text("Sign Out", color = Color(0xFFDC2626))
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
