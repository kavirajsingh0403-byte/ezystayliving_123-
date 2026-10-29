package com.example.ui.dashboard

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Guest
import com.example.ui.components.DeveloperContactDialog
import com.example.ui.theme.IndianSaffron
import com.example.ui.theme.IndianSaffronContainer
import com.example.ui.theme.IndianSaffronLight
import com.example.ui.theme.KumkumContainer
import com.example.ui.theme.KumkumCrimson
import com.example.ui.theme.MarigoldContainer
import com.example.ui.theme.MarigoldGold
import com.example.ui.theme.MehendiContainer
import com.example.ui.theme.MehendiGreen
import com.example.ui.theme.OnMarigoldContainer
import com.example.ui.theme.PeacockContainer
import com.example.ui.theme.PeacockTeal
import com.example.ui.viewmodel.EzyStayViewModel
import com.example.ui.viewmodel.UiState
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrmDashboardScreen(
    viewModel: EzyStayViewModel,
    onNavigateToGuests: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToEmployees: () -> Unit,
    onNavigateToAccounts: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToBroadcast: () -> Unit,
    onSignOut: () -> Unit
) {
    val metrics by viewModel.metricsState.collectAsState()
    val guestsUiState by viewModel.guestsState.collectAsState()
    val announcementsUiState by viewModel.announcementsState.collectAsState()
    var showContactDialog by remember { mutableStateOf(false) }

    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    if (showContactDialog) {
        DeveloperContactDialog(onDismiss = { showContactDialog = false })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "EzyStay Living",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(IndianSaffronContainer, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "CRM Model",
                                    color = IndianSaffron,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "Rental Hostel Business Management",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showContactDialog = true },
                        modifier = Modifier.testTag("contact_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Developer & Contact Info",
                            tint = IndianSaffron
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Cutoff Alert Banner ("Daily progresses report update before till time")
                DailyReportCutoffBanner(
                    isSubmitted = metrics.dprSubmittedToday,
                    onClickSubmit = onNavigateToReports
                )
            }

            // Latest CEO Announcement Ticker ("Preinformed by CEO to employee by mail and sms alert")
            item {
                val notices = (announcementsUiState as? UiState.Success)?.data ?: emptyList()
                if (notices.isNotEmpty()) {
                    val latest = notices.first()
                    CeoAnnouncementCard(
                        title = latest.title,
                        message = latest.message,
                        priority = latest.priority,
                        onClick = onNavigateToBroadcast
                    )
                }
            }

            // High-Level Business Metrics Grid
            item {
                Text(
                    text = "Real-Time Hostel Performance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Occupancy Card
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Occupancy Rate",
                        value = "${metrics.occupancyRate.toInt()}%",
                        subtitle = "${metrics.occupiedBeds}/${metrics.totalBeds} Beds Booked",
                        containerColor = PeacockContainer,
                        contentColor = PeacockTeal,
                        progress = (metrics.occupancyRate / 100.0).toFloat().coerceIn(0f, 1f)
                    )

                    // Month's Rent Collections
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Rent Collected",
                        value = currencyFormat.format(metrics.totalCollectedThisMonth),
                        subtitle = "Auto Receipts Sent",
                        containerColor = MehendiContainer,
                        contentColor = MehendiGreen,
                        progress = null
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Pending Dues
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Pending Dues",
                        value = currencyFormat.format(metrics.totalPendingDues),
                        subtitle = "${metrics.overdueCount} Overdue, ${metrics.dueSoonCount} Due Soon",
                        containerColor = if (metrics.overdueCount > 0) KumkumContainer else MarigoldContainer,
                        contentColor = if (metrics.overdueCount > 0) KumkumCrimson else MarigoldGold,
                        progress = null
                    )

                    // Net Cashflow
                    MetricCard(
                        modifier = Modifier.weight(1f),
                        title = "Net Cash Flow",
                        value = currencyFormat.format(metrics.netCashFlow),
                        subtitle = "Expenses: ${currencyFormat.format(metrics.totalExpensesThisMonth)}",
                        containerColor = IndianSaffronContainer,
                        contentColor = IndianSaffron,
                        progress = null
                    )
                }
            }

            // Quick Actions Hub
            item {
                Text(
                    text = "Quick Operations Hub",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickActionButton(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Hotel,
                            title = "Bookings",
                            tag = "bookings_shortcut",
                            badgeText = "${metrics.occupiedBeds}",
                            color = IndianSaffron,
                            onClick = onNavigateToGuests
                        )

                        QuickActionButton(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.ReceiptLong,
                            title = "Receipts",
                            tag = "receipts_shortcut",
                            badgeText = null,
                            color = PeacockTeal,
                            onClick = onNavigateToPayments
                        )

                        QuickActionButton(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Badge,
                            title = "Staff Panel",
                            tag = "employees_shortcut",
                            badgeText = "${metrics.totalEmployees}",
                            color = MarigoldGold,
                            onClick = onNavigateToEmployees
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickActionButton(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.AccountBalance,
                            title = "Accounts",
                            tag = "accounts_shortcut",
                            badgeText = null,
                            color = PeacockTeal,
                            onClick = onNavigateToAccounts
                        )

                        QuickActionButton(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Assessment,
                            title = "DPR Report",
                            tag = "reports_shortcut",
                            badgeText = if (metrics.dprSubmittedToday) "Done" else "Pending",
                            color = if (metrics.dprSubmittedToday) MehendiGreen else KumkumCrimson,
                            onClick = onNavigateToReports
                        )

                        QuickActionButton(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Campaign,
                            title = "CEO Alert",
                            tag = "broadcast_shortcut",
                            badgeText = null,
                            color = IndianSaffron,
                            onClick = onNavigateToBroadcast
                        )
                    }
                }
            }

            // Urgent Guest Dues Action List ("Contract no. so SMS alert of dues before payment date")
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Payment Dues & SMS Reminders",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.bodySmall,
                        color = IndianSaffron,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onNavigateToGuests() }
                    )
                }
            }

            val guestsList = (guestsUiState as? UiState.Success)?.data ?: emptyList()
            val duesGuests = guestsList.filter { it.paymentStatus != "PAID" }.take(3)

            if (duesGuests.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Box(
                            modifier = Modifier.padding(20.dp).fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✨ All guest accounts are clear! No pending rent dues.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MehendiGreen,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            } else {
                items(duesGuests) { guest ->
                    DuesGuestItemCard(
                        guest = guest,
                        onSendAlert = { viewModel.sendDuesAlert(guest) },
                        onCollect = onNavigateToPayments
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun DailyReportCutoffBanner(
    isSubmitted: Boolean,
    onClickSubmit: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSubmitted) MehendiContainer else MarigoldContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (isSubmitted) MehendiGreen else MarigoldGold,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSubmitted) Icons.Default.Assessment else Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isSubmitted) "DPR Submitted (Before Cutoff)" else "Daily Progress Report Due!",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isSubmitted) MehendiGreen else OnMarigoldContainer
                    )
                    Text(
                        text = if (isSubmitted) "Today's occupancy & operations logged" else "Must submit before 8:00 PM till time",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isSubmitted) MehendiGreen.copy(alpha = 0.8f) else OnMarigoldContainer.copy(alpha = 0.8f)
                    )
                }
            }

            Button(
                onClick = onClickSubmit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSubmitted) MehendiGreen else MarigoldGold
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isSubmitted) "View" else "Submit", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun CeoAnnouncementCard(
    title: String,
    message: String,
    priority: String,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = IndianSaffronContainer.copy(alpha = 0.7f)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = null,
                        tint = IndianSaffron,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "CEO Pre-Informed Notice",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                        color = IndianSaffron
                    )
                }
                Box(
                    modifier = Modifier
                        .background(IndianSaffron, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = priority,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subtitle: String,
    containerColor: Color,
    contentColor: Color,
    progress: Float?
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor.copy(alpha = 0.9f),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = contentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.8f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (progress != null) {
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                    color = contentColor,
                    trackColor = contentColor.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    tag: String,
    badgeText: String?,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .testTag(tag)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(color.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                if (badgeText != null) {
                    Box(
                        modifier = Modifier
                            .background(color, CircleShape)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun DuesGuestItemCard(
    guest: Guest,
    onSendAlert: () -> Unit,
    onCollect: () -> Unit
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = guest.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Room ${guest.roomNo} • Bed ${guest.bedNo} • Contract #${guest.contractNo}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            if (guest.paymentStatus == "OVERDUE") KumkumContainer else MarigoldContainer,
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = guest.paymentStatus,
                        color = if (guest.paymentStatus == "OVERDUE") KumkumCrimson else OnMarigoldContainer,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Due Amount: ${currencyFormat.format(guest.monthlyRent)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = KumkumCrimson
                    )
                    Text(
                        text = "Payment Date: ${guest.nextDueDate.ifBlank { "Immediate" }}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onSendAlert,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SMS Alert", fontSize = 11.sp)
                    }

                    Button(
                        onClick = onCollect,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PeacockTeal)
                    ) {
                        Text("Pay & Receipt", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
