package com.example.ui.reports

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyReport
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyProgressReportScreen(
    viewModel: EzyStayViewModel,
    onNavigateBack: () -> Unit
) {
    BackHandler { onNavigateBack() }

    val metrics by viewModel.metricsState.collectAsState()
    val dailyReportsState by viewModel.dailyReportsState.collectAsState()

    var showSubmitDialog by remember { mutableStateOf(false) }

    val now = Calendar.getInstance()
    val isAfter8Pm = now.get(Calendar.HOUR_OF_DAY) >= 20
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Daily Progress Reports (DPR)",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Mandatory Operational Report Update Before Till Time",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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

                // Cutoff Status Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (metrics.dprSubmittedToday) MehendiContainer else if (isAfter8Pm) KumkumContainer else MarigoldContainer
                    )
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
                                        .size(38.dp)
                                        .background(
                                            if (metrics.dprSubmittedToday) MehendiGreen else if (isAfter8Pm) KumkumCrimson else MarigoldGold,
                                            CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (metrics.dprSubmittedToday) Icons.Default.CheckCircle else Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (metrics.dprSubmittedToday) "TODAY'S DPR SUBMITTED" else if (isAfter8Pm) "CUTOFF TIME PASSED (8:00 PM)" else "CUTOFF TIME: 8:00 PM TODAY",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = if (metrics.dprSubmittedToday) MehendiGreen else if (isAfter8Pm) KumkumCrimson else OnMarigoldContainer
                                    )
                                    Text(
                                        text = if (metrics.dprSubmittedToday) "Uploaded before till time and synced to CEO review" else "Hostel management policy mandates update before 8 PM",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (metrics.dprSubmittedToday) MehendiGreen.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { showSubmitDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("submit_dpr_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (metrics.dprSubmittedToday) MehendiGreen else IndianSaffron
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (metrics.dprSubmittedToday) "Update Today's Progress Report" else "Submit Today's DPR Before Cutoff",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Real-time Aggregated Snapshot for Today's DPR
            item {
                Text(
                    text = "Today's Live Aggregated Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            DprStatItem(title = "Beds Occupied", value = "${metrics.occupiedBeds} / ${metrics.totalBeds}", color = PeacockTeal)
                            DprStatItem(title = "Occupancy Rate", value = "${metrics.occupancyRate.toInt()}%", color = PeacockTeal)
                            DprStatItem(title = "Collections Today", value = currencyFormat.format(metrics.totalCollectedThisMonth), color = MehendiGreen)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            DprStatItem(title = "Pending Dues", value = currencyFormat.format(metrics.totalPendingDues), color = KumkumCrimson)
                            DprStatItem(title = "Overdue Residents", value = "${metrics.overdueCount} Alerts", color = KumkumCrimson)
                            DprStatItem(title = "Staff on Duty", value = "${metrics.totalEmployees} Active", color = MarigoldGold)
                        }
                    }
                }
            }

            // Past DPR Submissions History
            item {
                Text(
                    text = "Previous DPR Submission History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            when (val state = dailyReportsState) {
                is UiState.Loading -> {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = IndianSaffron)
                        }
                    }
                }
                is UiState.Error -> {
                    item {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is UiState.Success -> {
                    val reports = state.data
                    if (reports.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 30.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No prior daily progress reports found.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(reports, key = { it.id }) { report ->
                            DailyReportHistoryCard(report = report)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showSubmitDialog) {
        SubmitDprDialog(
            metrics = metrics,
            onDismiss = { showSubmitDialog = false },
            onSubmit = { remarks, messStatus ->
                viewModel.submitDailyProgressReport(remarks, messStatus) {
                    showSubmitDialog = false
                }
            }
        )
    }
}

@Composable
fun DprStatItem(
    title: String,
    value: String,
    color: Color
) {
    Column {
        Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
fun DailyReportHistoryCard(report: DailyReport) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale("en", "IN")) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EventNote, contentDescription = null, tint = IndianSaffron, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Report Date: ${report.reportDate}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            if (report.submittedBeforeCutoff) MehendiContainer else KumkumContainer,
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (report.submittedBeforeCutoff) "ON TIME (BEFORE 8 PM)" else "POST CUTOFF",
                        color = if (report.submittedBeforeCutoff) MehendiGreen else KumkumCrimson,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Occupancy: ${report.occupancyRate.toInt()}% • Collections: ${currencyFormat.format(report.collectionsToday)} • Submitted by: ${report.submittedBy}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Mess Status: ${report.messStatus}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Staff Remarks: ${report.summaryRemarks}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun SubmitDprDialog(
    metrics: com.example.ui.viewmodel.CrmMetrics,
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    var remarks by remember {
        mutableStateOf("Daily progress report updated before till time. All rooms 101-106 inspected, night security briefed.")
    }
    var messStatus by remember {
        mutableStateOf("Dinner served on schedule. Water purifiers & kitchen hygiene verified.")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Submit Daily Progress Report") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Occupancy: ${metrics.occupancyRate.toInt()}% (${metrics.occupiedBeds} beds occupied)\nToday's Total Collections: ₹${metrics.totalCollectedThisMonth.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = PeacockTeal
                )

                OutlinedTextField(
                    value = messStatus,
                    onValueChange = { messStatus = it },
                    label = { Text("Mess / Food Hygiene & Status") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Operations Handover & Night Shift Remarks") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(remarks, messStatus) },
                colors = ButtonDefaults.buttonColors(containerColor = IndianSaffron)
            ) {
                Text("Confirm & Submit DPR")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
