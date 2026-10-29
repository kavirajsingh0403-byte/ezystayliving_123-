package com.example.ui.guests

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Guest
import com.example.ui.theme.IndianSaffron
import com.example.ui.theme.KumkumContainer
import com.example.ui.theme.KumkumCrimson
import com.example.ui.theme.MarigoldContainer
import com.example.ui.theme.MarigoldGold
import com.example.ui.theme.MehendiContainer
import com.example.ui.theme.MehendiGreen
import com.example.ui.theme.OnMarigoldContainer
import com.example.ui.theme.PeacockTeal
import com.example.ui.viewmodel.EzyStayViewModel
import com.example.ui.viewmodel.UiState
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuestManagementScreen(
    viewModel: EzyStayViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPayments: () -> Unit
) {
    BackHandler { onNavigateBack() }

    val guestsState by viewModel.guestsState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentFilter by viewModel.guestFilter.collectAsState()

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingGuest by remember { mutableStateOf<Guest?>(null) }
    var guestToDelete by remember { mutableStateOf<Guest?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Bookings & Residents",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingGuest = null
                    showAddEditDialog = true
                },
                containerColor = IndianSaffron,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_guest_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Booking")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search by name, room, or contract no...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("guest_search_field"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val filters = listOf("ALL" to "All Residents", "DUE_SOON" to "Due Soon", "OVERDUE" to "Overdue Dues", "PAID" to "Paid")
                items(filters) { (key, label) ->
                    FilterChip(
                        selected = currentFilter == key,
                        onClick = { viewModel.setGuestFilter(key) },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IndianSaffron,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (val state = guestsState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = IndianSaffron)
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is UiState.Success -> {
                    val filtered = state.data.filter { guest ->
                        val matchesQuery = guest.name.contains(searchQuery, ignoreCase = true) ||
                                guest.roomNo.contains(searchQuery, ignoreCase = true) ||
                                guest.contractNo.contains(searchQuery, ignoreCase = true) ||
                                guest.phone.contains(searchQuery)
                        val matchesFilter = when (currentFilter) {
                            "DUE_SOON" -> guest.paymentStatus == "DUE_SOON"
                            "OVERDUE" -> guest.paymentStatus == "OVERDUE"
                            "PAID" -> guest.paymentStatus == "PAID"
                            else -> true
                        }
                        matchesQuery && matchesFilter
                    }

                    if (filtered.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No residents found matching your criteria.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filtered, key = { it.id }) { guest ->
                                GuestCard(
                                    guest = guest,
                                    onSendAlert = { viewModel.sendDuesAlert(guest) },
                                    onEdit = {
                                        editingGuest = guest
                                        showAddEditDialog = true
                                    },
                                    onDelete = { guestToDelete = guest },
                                    onCollectRent = onNavigateToPayments
                                )
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }

    if (showAddEditDialog) {
        AddEditGuestDialog(
            existing = editingGuest,
            onDismiss = { showAddEditDialog = false },
            onSave = { updated ->
                viewModel.saveGuest(updated) {
                    showAddEditDialog = false
                }
            }
        )
    }

    if (guestToDelete != null) {
        AlertDialog(
            onDismissRequest = { guestToDelete = null },
            title = { Text("Delete Guest Record?") },
            text = { Text("Are you sure you want to delete ${guestToDelete?.name} (Contract #${guestToDelete?.contractNo})? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        guestToDelete?.id?.let { viewModel.deleteGuest(it) }
                        guestToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = KumkumCrimson)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { guestToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun GuestCard(
    guest: Guest,
    onSendAlert: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCollectRent: () -> Unit
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(IndianSaffron.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Hotel, contentDescription = null, tint = IndianSaffron, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = guest.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Contract #${guest.contractNo}",
                            style = MaterialTheme.typography.labelSmall,
                            color = PeacockTeal,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(
                            when (guest.paymentStatus) {
                                "PAID" -> MehendiContainer
                                "OVERDUE" -> KumkumContainer
                                else -> MarigoldContainer
                            },
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = guest.paymentStatus,
                        color = when (guest.paymentStatus) {
                            "PAID" -> MehendiGreen
                            "OVERDUE" -> KumkumCrimson
                            else -> OnMarigoldContainer
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Room and Bed Badges
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                BadgeInfoChip(label = "Room ${guest.roomNo}")
                BadgeInfoChip(label = "Bed ${guest.bedNo}")
                BadgeInfoChip(label = guest.roomType)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Details info
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(
                        text = "Monthly Rent",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currencyFormat.format(guest.monthlyRent),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column {
                    Text(
                        text = "Security Deposit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currencyFormat.format(guest.securityDeposit),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column {
                    Text(
                        text = "Next Due Date",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = guest.nextDueDate.ifBlank { "Not Set" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (guest.paymentStatus != "PAID") KumkumCrimson else MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            // Communication & Action Toolbar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onSendAlert) {
                        Icon(Icons.Default.Send, contentDescription = "Send SMS Alert", tint = IndianSaffron, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = PeacockTeal, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                }

                Button(
                    onClick = onCollectRent,
                    colors = ButtonDefaults.buttonColors(containerColor = PeacockTeal),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Collect Rent", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun BadgeInfoChip(label: String) {
    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun AddEditGuestDialog(
    existing: Guest?,
    onDismiss: () -> Unit,
    onSave: (Guest) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var phone by remember { mutableStateOf(existing?.phone ?: "") }
    var email by remember { mutableStateOf(existing?.email ?: "") }
    var contractNo by remember { mutableStateOf(existing?.contractNo ?: "EZS-2026-${(1000..9999).random()}") }
    var roomNo by remember { mutableStateOf(existing?.roomNo ?: "101") }
    var bedNo by remember { mutableStateOf(existing?.bedNo ?: "A") }
    var roomType by remember { mutableStateOf(existing?.roomType ?: "Double AC") }
    var monthlyRent by remember { mutableStateOf(if (existing != null) existing.monthlyRent.toInt().toString() else "8500") }
    var securityDeposit by remember { mutableStateOf(if (existing != null) existing.securityDeposit.toInt().toString() else "8500") }
    var nextDueDate by remember { mutableStateOf(existing?.nextDueDate ?: "05-11-2026") }
    var paymentStatus by remember { mutableStateOf(existing?.paymentStatus ?: "DUE_SOON") }
    var emergencyContact by remember { mutableStateOf(existing?.emergencyContact ?: "") }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "New Guest Booking" else "Edit Booking Details") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Guest Full Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone No *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = contractNo,
                        onValueChange = { contractNo = it },
                        label = { Text("Contract No *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Registered Email (For Receipts)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = roomNo,
                        onValueChange = { roomNo = it },
                        label = { Text("Room No *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = bedNo,
                        onValueChange = { bedNo = it },
                        label = { Text("Bed (A/B/C) *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = roomType,
                    onValueChange = { roomType = it },
                    label = { Text("Room Type (e.g. Single AC, Double AC)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = monthlyRent,
                        onValueChange = { monthlyRent = it },
                        label = { Text("Monthly Rent (₹) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = securityDeposit,
                        onValueChange = { securityDeposit = it },
                        label = { Text("Deposit (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = nextDueDate,
                    onValueChange = { nextDueDate = it },
                    label = { Text("Payment Due Date (DD-MM-YYYY)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = emergencyContact,
                    onValueChange = { emergencyContact = it },
                    label = { Text("Emergency Contact & Relation") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Internal Notes (College/Work/KYC)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank() && roomNo.isNotBlank()) {
                        val g = (existing ?: Guest()).copy(
                            name = name.trim(),
                            phone = phone.trim(),
                            email = email.trim(),
                            contractNo = contractNo.trim(),
                            roomNo = roomNo.trim(),
                            bedNo = bedNo.trim(),
                            roomType = roomType.trim(),
                            monthlyRent = monthlyRent.toDoubleOrNull() ?: 0.0,
                            securityDeposit = securityDeposit.toDoubleOrNull() ?: 0.0,
                            nextDueDate = nextDueDate.trim(),
                            paymentStatus = paymentStatus,
                            emergencyContact = emergencyContact.trim(),
                            notes = notes.trim()
                        )
                        onSave(g)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = IndianSaffron)
            ) {
                Text("Save Booking")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
