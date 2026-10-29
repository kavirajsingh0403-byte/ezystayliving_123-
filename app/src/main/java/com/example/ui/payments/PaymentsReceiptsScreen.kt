package com.example.ui.payments

import android.content.Intent
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Guest
import com.example.data.model.Payment
import com.example.ui.theme.IndianSaffron
import com.example.ui.theme.MarigoldGold
import com.example.ui.theme.MehendiContainer
import com.example.ui.theme.MehendiGreen
import com.example.ui.theme.PeacockTeal
import com.example.ui.viewmodel.EzyStayViewModel
import com.example.ui.viewmodel.UiState
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsReceiptsScreen(
    viewModel: EzyStayViewModel,
    onNavigateBack: () -> Unit
) {
    BackHandler { onNavigateBack() }

    val paymentsState by viewModel.paymentsState.collectAsState()
    val guestsState by viewModel.guestsState.collectAsState()

    var showCollectPaymentDialog by remember { mutableStateOf(false) }
    var selectedReceiptForView by remember { mutableStateOf<Payment?>(null) }

    val guestsList = (guestsState as? UiState.Success)?.data ?: emptyList()
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Payments & Receipts",
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
                onClick = { showCollectPaymentDialog = true },
                containerColor = PeacockTeal,
                contentColor = Color.White,
                modifier = Modifier.testTag("collect_payment_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Record Payment")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Header summary card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Automated Digital Receipts",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Instant receipt generated & sent via SMS / Email with Contract No.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { showCollectPaymentDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PeacockTeal),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Collect", fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Transaction History & Generated Receipts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            when (val state = paymentsState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PeacockTeal)
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is UiState.Success -> {
                    val payments = state.data
                    if (payments.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No payments recorded yet. Tap + to record first rent payment.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(payments, key = { it.id }) { payment ->
                                PaymentReceiptItemCard(
                                    payment = payment,
                                    onViewReceipt = { selectedReceiptForView = payment }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }

    if (showCollectPaymentDialog) {
        CollectPaymentDialog(
            guests = guestsList,
            onDismiss = { showCollectPaymentDialog = false },
            onConfirm = { guest, amount, mode, txnRef, rentMonth ->
                viewModel.recordPaymentAndGenerateReceipt(guest, amount, mode, txnRef, rentMonth) { generated ->
                    showCollectPaymentDialog = false
                    selectedReceiptForView = generated
                }
            }
        )
    }

    selectedReceiptForView?.let { payment ->
        DigitalReceiptDialog(
            payment = payment,
            onDismiss = { selectedReceiptForView = null }
        )
    }
}

@Composable
fun PaymentReceiptItemCard(
    payment: Payment,
    onViewReceipt: () -> Unit
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewReceipt() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MehendiContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = MehendiGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = payment.guestName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Receipt #${payment.receiptNo} • Contract #${payment.contractNo}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${payment.paymentDate} • ${payment.paymentMode} • ${payment.rentMonth}",
                        style = MaterialTheme.typography.labelSmall,
                        color = PeacockTeal
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = currencyFormat.format(payment.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MehendiGreen
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Sent",
                        tint = MehendiGreen,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "SMS & Email Sent",
                        fontSize = 10.sp,
                        color = MehendiGreen
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectPaymentDialog(
    guests: List<Guest>,
    onDismiss: () -> Unit,
    onConfirm: (Guest?, Double, String, String, String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedGuest by remember { mutableStateOf<Guest?>(guests.firstOrNull()) }
    var amountText by remember { mutableStateOf(selectedGuest?.monthlyRent?.toInt()?.toString() ?: "8500") }
    var paymentMode by remember { mutableStateOf("UPI") }
    var transactionRef by remember { mutableStateOf("UPI-${(100000..999999).random()}") }
    var rentMonth by remember { mutableStateOf("November 2026") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Collect Rent & Generate Receipt") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Select Resident",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                // Guest Selector
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedGuest?.let { "${it.name} (Room ${it.roomNo})" } ?: "Select Resident",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        guests.forEach { guest ->
                            DropdownMenuItem(
                                text = { Text("${guest.name} — Room ${guest.roomNo} (Contract #${guest.contractNo})") },
                                onClick = {
                                    selectedGuest = guest
                                    amountText = guest.monthlyRent.toInt().toString()
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                selectedGuest?.let { g ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("Contract: ${g.contractNo}", style = MaterialTheme.typography.labelSmall)
                            Text("Phone for SMS: ${g.phone}", style = MaterialTheme.typography.labelSmall)
                            Text("Email for Receipt: ${g.email}", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount Paid (₹) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = rentMonth,
                    onValueChange = { rentMonth = it },
                    label = { Text("For Month (e.g. October 2026)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = paymentMode,
                    onValueChange = { paymentMode = it },
                    label = { Text("Payment Mode (UPI / Cash / Bank Transfer)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = transactionRef,
                    onValueChange = { transactionRef = it },
                    label = { Text("UPI / Cheque / Txn Ref Number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        onConfirm(selectedGuest, amount, paymentMode, transactionRef, rentMonth)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PeacockTeal)
            ) {
                Text("Generate Receipt & Send")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun DigitalReceiptDialog(
    payment: Payment,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Official Rent Receipt",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = PeacockTeal
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Official Receipt Document Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, IndianSaffron.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Header
                        Text(
                            text = "EZYSTAY LIVING",
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleLarge,
                            color = IndianSaffron
                        )
                        Text(
                            text = "Registered Hostel & Coliving Operations",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Regd. Office: D-256 CR road, Laxmi Nagar, Delhi 110092, India",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = IndianSaffron)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "RECEIPT NO: ${payment.receiptNo}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(text = "DATE: ${payment.paymentDate}", fontSize = 12.sp)
                        }

                        Text(
                            text = "CONTRACT NO: ${payment.contractNo}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = PeacockTeal
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Column {
                                Text(text = "Received with thanks from:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(text = payment.guestName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text(text = "For Rent Period: ${payment.rentMonth}", fontSize = 12.sp)
                                Text(text = "Mode: ${payment.paymentMode} | Ref: ${payment.transactionRef}", fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MehendiContainer, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "TOTAL AMOUNT PAID:", fontWeight = FontWeight.Bold, color = MehendiGreen)
                            Text(
                                text = currencyFormat.format(payment.amount),
                                fontWeight = FontWeight.Black,
                                style = MaterialTheme.typography.titleLarge,
                                color = MehendiGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Footer signature & verification
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Text(text = "Recipient SMS: ${payment.recipientPhone}", fontSize = 10.sp)
                                Text(text = "Recipient Email: ${payment.recipientEmail}", fontSize = 10.sp)
                                Text(text = "Status: SMS & Email Alert Dispatched", fontSize = 10.sp, color = MehendiGreen, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "Mr. Kavi Raj Singh", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text(text = "Authorized Signatory", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action buttons: Send via SMS / Email share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val smsIntent = Intent(Intent.ACTION_VIEW).apply {
                                data = android.net.Uri.parse("sms:${payment.recipientPhone}")
                                putExtra("sms_body", "EzyStay Living: Payment received for Contract #${payment.contractNo}. Receipt No: ${payment.receiptNo}, Amount: INR ${payment.amount.toInt()}. Thank you! D-256 CR road, Laxmi Nagar Delhi.")
                            }
                            context.startActivity(smsIntent)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SMS", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = {
                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = android.net.Uri.parse("mailto:${payment.recipientEmail}")
                                putExtra(Intent.EXTRA_SUBJECT, "Rent Receipt: ${payment.receiptNo} (Contract #${payment.contractNo})")
                                putExtra(Intent.EXTRA_TEXT, "Dear ${payment.guestName},\n\nPayment of INR ${payment.amount} has been successfully recorded for ${payment.rentMonth}.\n\nReceipt Details:\nReceipt No: ${payment.receiptNo}\nContract No: ${payment.contractNo}\nMode: ${payment.paymentMode}\nRef: ${payment.transactionRef}\n\nWarm regards,\nEzyStay Living\nDeveloper: Mr. Kavi Raj Singh\nReg. Office: D-256 CR road, Laxmi Nagar, Delhi 110092\nMobile: 9873677108")
                            }
                            context.startActivity(emailIntent)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Email", fontSize = 11.sp)
                    }

                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "EzyStay Living Official Receipt\nReceipt No: ${payment.receiptNo}\nGuest: ${payment.guestName}\nContract: ${payment.contractNo}\nAmount: INR ${payment.amount}\nMonth: ${payment.rentMonth}\nVerified by Mr. Kavi Raj Singh\nReg. Office: D-256 CR road, Laxmi Nagar Delhi 110092\nMobile: 9873677108")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Receipt"))
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PeacockTeal),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", fontSize = 11.sp)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Done") }
        }
    )
}
