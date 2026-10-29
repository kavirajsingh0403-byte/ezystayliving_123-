package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Expense
import com.example.data.model.Guest
import com.example.data.model.Payment
import com.example.ui.components.ReceiptDialog

@Composable
fun AccountsScreen(
    payments: List<Payment>,
    expenses: List<Expense>,
    guests: List<Guest>,
    onRecordPayment: (guestName: String, contractNo: String, amount: Double, paymentType: String, paymentMode: String, guestId: String?, monthFor: String?, sendSms: Boolean, sendEmail: Boolean) -> Unit,
    onAddExpense: (Expense) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Collections & Receipts, 1: Expenses
    var showRecordPaymentDialog by remember { mutableStateOf(false) }
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var viewingReceipt by remember { mutableStateOf<Payment?>(null) }

    val totalCollections = payments.filter { it.status == "Success" }.sumOf { it.amount }
    val totalExpenses = expenses.sumOf { it.amount }
    val netOperatingProfit = totalCollections - totalExpenses

    // Estimated dues from active guests
    val estimatedMonthlyRent = guests.filter { it.status == "active" }.sumOf { it.monthlyRent }
    val pendingDues = (estimatedMonthlyRent - totalCollections).coerceAtLeast(0.0)

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Business Finance Summary KPI Card
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth().testTag("finance_kpi_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "FINANCIAL ACCOUNTS & BUSINESS MODULE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Total Revenue Collected", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "₹ ${totalCollections.toInt()}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Net Operating Profit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    "₹ ${netOperatingProfit.toInt()}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (netOperatingProfit >= 0) Color(0xFF16A34A) else Color(0xFFDC2626)
                                )
                            }
                        }

                        HorizontalDivider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(28.dp).background(Color(0xFFFEF2F2), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text("Expenses", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("₹ ${totalExpenses.toInt()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier.size(28.dp).background(Color(0xFFFFFBEB), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(16.dp))
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Est. Pending Dues", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("₹ ${pendingDues.toInt()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                                }
                            }
                        }
                    }
                }
            }

            // Tabs: Receipts & Collections vs Expenses
            item {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Payment Receipts (${payments.size})", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Hostel Expenses (${expenses.size})", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            if (selectedTab == 0) {
                // Payments list
                if (payments.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No payment receipts recorded yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                } else {
                    items(payments, key = { it.id.ifBlank { it.receiptNo } }) { payment ->
                        PaymentCard(
                            payment = payment,
                            onClick = { viewingReceipt = payment }
                        )
                    }
                }
            } else {
                // Expenses list
                if (expenses.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth().padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.AttachMoney, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No operational expenses logged yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                } else {
                    items(expenses, key = { it.id.ifBlank { it.title } }) { exp ->
                        ExpenseCard(expense = exp)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }

        // Action FAB depending on tab
        FloatingActionButton(
            onClick = {
                if (selectedTab == 0) showRecordPaymentDialog = true else showAddExpenseDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("accounts_action_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Icon(
                imageVector = if (selectedTab == 0) Icons.Default.ReceiptLong else Icons.Default.Add,
                contentDescription = if (selectedTab == 0) "Record Payment" else "Add Expense"
            )
        }
    }

    // Record Payment Dialog
    if (showRecordPaymentDialog) {
        RecordPaymentDialog(
            guests = guests,
            onDismiss = { showRecordPaymentDialog = false },
            onRecord = { guestName, contractNo, amount, pType, pMode, guestId, month, sendSms, sendEmail ->
                onRecordPayment(guestName, contractNo, amount, pType, pMode, guestId, month, sendSms, sendEmail)
                showRecordPaymentDialog = false
                Toast.makeText(context, "Payment recorded! Receipt generated & dispatch alert triggered.", Toast.LENGTH_LONG).show()
            }
        )
    }

    // Add Expense Dialog
    if (showAddExpenseDialog) {
        AddExpenseDialog(
            onDismiss = { showAddExpenseDialog = false },
            onAdd = { exp ->
                onAddExpense(exp)
                showAddExpenseDialog = false
            }
        )
    }

    // View Receipt Modal
    viewingReceipt?.let { payment ->
        ReceiptDialog(
            payment = payment,
            onDismiss = { viewingReceipt = null }
        )
    }
}

@Composable
private fun PaymentCard(
    payment: Payment,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("payment_card_${payment.receiptNo}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = payment.receiptNo,
                    style = MaterialTheme.typography.labelSmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "₹ ${payment.amount.toInt()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF16A34A)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(payment.guestName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "Contract: ${payment.contractNo} • ${payment.paymentMode}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (payment.smsAlertSent) {
                        Box(
                            modifier = Modifier.background(Color(0xFFDCFCE7), CircleShape).padding(4.dp)
                        ) {
                            Icon(Icons.Default.Sms, contentDescription = "SMS Sent", tint = Color(0xFF16A34A), modifier = Modifier.size(14.dp))
                        }
                    }
                    if (payment.emailReceiptSent) {
                        Box(
                            modifier = Modifier.background(Color(0xFFDCFCE7), CircleShape).padding(4.dp)
                        ) {
                            Icon(Icons.Default.Email, contentDescription = "Email Sent", tint = Color(0xFF16A34A), modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpenseCard(
    expense: Expense
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(expense.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "${expense.category} • Paid to ${expense.paidTo} via ${expense.paymentMode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "- ₹ ${expense.amount.toInt()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFDC2626)
            )
        }
    }
}

@Composable
private fun RecordPaymentDialog(
    guests: List<Guest>,
    onDismiss: () -> Unit,
    onRecord: (String, String, Double, String, String, String?, String?, Boolean, Boolean) -> Unit
) {
    var selectedGuest by remember { mutableStateOf(guests.firstOrNull()) }
    var manualGuestName by remember { mutableStateOf("") }
    var manualContractNo by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf(selectedGuest?.monthlyRent?.toInt()?.toString() ?: "7500") }
    var paymentType by remember { mutableStateOf("Rent") }
    var paymentMode by remember { mutableStateOf("UPI") }
    var monthFor by remember { mutableStateOf("October 2026") }
    var sendSms by remember { mutableStateOf(true) }
    var sendEmail by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Collect Rent & Generate Receipt", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        text = "Select Resident or Enter Details:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (guests.isNotEmpty()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            guests.take(5).forEach { g ->
                                val isSelected = selectedGuest?.id == g.id
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            selectedGuest = g
                                            amountText = g.monthlyRent.toInt().toString()
                                        }
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        "${g.fullName} (${g.contractNo} - Room ${g.roomNumber})",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    if (selectedGuest == null) {
                        OutlinedTextField(
                            value = manualGuestName,
                            onValueChange = { manualGuestName = it },
                            label = { Text("Resident Full Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = manualContractNo,
                            onValueChange = { manualContractNo = it },
                            label = { Text("Contract Number (e.g. EZS-2026-101A)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { amountText = it },
                        label = { Text("Amount Paid (₹)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = paymentMode,
                            onValueChange = { paymentMode = it },
                            label = { Text("Mode (UPI/Cash)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = monthFor,
                            onValueChange = { monthFor = it },
                            label = { Text("Billing Month") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                item {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = sendSms, onCheckedChange = { sendSms = it })
                            Text("Send Receipt via SMS on registered mobile", style = MaterialTheme.typography.bodySmall)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = sendEmail, onCheckedChange = { sendEmail = it })
                            Text("Dispatch digital receipt on registered email", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 7500.0
                    val name = selectedGuest?.fullName ?: manualGuestName.trim()
                    val contract = selectedGuest?.contractNo ?: manualContractNo.trim().ifBlank { "EZS-MANUAL" }
                    val gId = selectedGuest?.id

                    if (name.isNotBlank()) {
                        onRecord(name, contract, amount, paymentType, paymentMode, gId, monthFor, sendSms, sendEmail)
                    }
                }
            ) {
                Text("Issue Receipt & Record")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AddExpenseDialog(
    onDismiss: () -> Unit,
    onAdd: (Expense) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Utilities") }
    var amount by remember { mutableStateOf("") }
    var paidTo by remember { mutableStateOf("") }
    var paymentMode by remember { mutableStateOf("UPI") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Operational Expense", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Expense Description (e.g. WiFi Bill, Plumber)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category (Utilities, Maintenance, Salaries)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (₹)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = paidTo,
                    onValueChange = { paidTo = it },
                    label = { Text("Vendor / Payee") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cost = amount.toDoubleOrNull() ?: 0.0
                    if (title.isNotBlank() && cost > 0) {
                        onAdd(
                            Expense(
                                title = title.trim(),
                                category = category.trim(),
                                amount = cost,
                                paidTo = paidTo.ifBlank { "Hostel Vendor" },
                                paymentMode = paymentMode
                            )
                        )
                    }
                },
                enabled = title.isNotBlank() && amount.isNotBlank()
            ) {
                Text("Save Expense")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
