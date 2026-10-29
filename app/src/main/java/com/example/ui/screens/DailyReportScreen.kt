package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.ai.AiAssistantService
import com.example.data.model.DailyReport
import com.example.data.model.Guest
import com.example.data.model.HostelDetails
import com.example.data.model.Payment
import com.example.data.model.Room
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DailyReportScreen(
    reports: List<DailyReport>,
    rooms: List<Room>,
    guests: List<Guest>,
    payments: List<Payment>,
    aiService: AiAssistantService,
    onSubmitReport: (DailyReport) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val todayDateStr = remember { SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()) }
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    val isBeforeCutoff = currentHour < HostelDetails.DPR_CUTOFF_HOUR

    // Calculated today's metrics
    val totalRooms = rooms.size
    val occupiedRooms = rooms.count { it.status == "occupied" }
    val partialRooms = rooms.count { it.status == "partially_occupied" }
    val vacantRooms = rooms.count { it.status == "vacant" }
    val maintenanceRooms = rooms.count { it.status == "maintenance" }
    val totalBeds = rooms.sumOf { it.totalBeds }
    val occupiedBeds = rooms.sumOf { it.occupiedBeds }
    val occupancyRate = if (totalBeds > 0) (occupiedBeds.toDouble() / totalBeds * 100) else 0.0

    val todayPayments = payments.filter { it.status == "Success" }
    val todayCollectionAmount = todayPayments.sumOf { it.amount }

    val pendingCount = guests.count { it.status == "active" }
    val pendingDuesAmount = guests.filter { it.status == "active" }.sumOf { it.monthlyRent } - todayCollectionAmount

    var submittedByName by remember { mutableStateOf("Warden / Property Manager") }
    var remarksText by remember { mutableStateOf("All room checks complete. No maintenance escalations today.") }
    var aiAnalysisText by remember { mutableStateOf<String?>(null) }
    var isAnalyzingAi by remember { mutableStateOf(false) }

    val alreadySubmittedToday = reports.any { it.reportDate == todayDateStr }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Cutoff Timer Banner
            ElevatedCard(
                modifier = Modifier.fillMaxWidth().testTag("dpr_cutoff_banner"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (alreadySubmittedToday) Color(0xFFDCFCE7)
                    else if (isBeforeCutoff) MaterialTheme.colorScheme.primaryContainer
                    else Color(0xFFFEF2F2)
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(40.dp).background(
                                if (alreadySubmittedToday) Color(0xFF16A34A)
                                else if (isBeforeCutoff) MaterialTheme.colorScheme.primary
                                else Color(0xFFDC2626),
                                CircleShape
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (alreadySubmittedToday) Icons.Default.CheckCircle
                                else if (isBeforeCutoff) Icons.Default.AccessTime
                                else Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (alreadySubmittedToday) "Today's DPR Submitted!"
                                else if (isBeforeCutoff) "DPR Submission Window Open"
                                else "Past 8:00 PM Cutoff Time",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (alreadySubmittedToday) Color(0xFF166534)
                                else if (isBeforeCutoff) MaterialTheme.colorScheme.onPrimaryContainer
                                else Color(0xFF991B1B)
                            )
                            Text(
                                text = "Daily Cutoff Time: 8:00 PM (20:00 hrs)",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Auto-aggregated DPR Form Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("dpr_submission_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Daily Progress Report (Date: $todayDateStr)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    // 4 stat boxes
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatBox("Occupancy", "${occupancyRate.toInt()}%", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                        StatBox("Vacant", "$vacantRooms rooms", Color(0xFF16A34A), Modifier.weight(1f))
                        StatBox("In Repair", "$maintenanceRooms rooms", Color(0xFFD97706), Modifier.weight(1f))
                        StatBox("Collections", "₹ ${todayCollectionAmount.toInt()}", Color(0xFF16A34A), Modifier.weight(1f))
                    }

                    OutlinedTextField(
                        value = submittedByName,
                        onValueChange = { submittedByName = it },
                        label = { Text("Submitting Staff / Employee Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = remarksText,
                        onValueChange = { remarksText = it },
                        label = { Text("Daily Progress Notes & Facility Remarks") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            val report = DailyReport(
                                reportDate = todayDateStr,
                                submittedBy = submittedByName.trim().ifBlank { "Duty Warden" },
                                cutoffMet = isBeforeCutoff,
                                totalRooms = totalRooms,
                                occupiedRooms = occupiedRooms + partialRooms,
                                vacantRooms = vacantRooms,
                                maintenanceRooms = maintenanceRooms,
                                occupancyRate = occupancyRate,
                                todayCollections = todayCollectionAmount,
                                pendingDuesCount = pendingCount,
                                pendingDuesAmount = pendingDuesAmount.coerceAtLeast(0.0),
                                remarks = remarksText.trim()
                            )
                            onSubmitReport(report)
                            Toast.makeText(context, "DPR for $todayDateStr submitted successfully!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (alreadySubmittedToday) "Re-submit / Update DPR" else "Submit Daily Report Before Cutoff")
                    }
                }
            }
        }

        // Gemini AI Insights Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth().testTag("ai_insights_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gemini AI Operations Advisory", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (aiAnalysisText == null) {
                        Text(
                            text = "Generate instant AI executive analysis on today's occupancy patterns, collection trends, and actionable warden advice.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    isAnalyzingAi = true
                                    val result = aiService.generateDailyExecutiveSummary(rooms, reports, todayCollectionAmount)
                                    result.onSuccess { aiAnalysisText = it }
                                    result.onFailure {
                                        Toast.makeText(context, "AI Analysis error: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
                                    }
                                    isAnalyzingAi = false
                                }
                            },
                            enabled = !isAnalyzingAi
                        ) {
                            if (isAnalyzingAi) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Analyzing with Gemini 3.8 Flash...")
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Generate AI Executive Insights")
                            }
                        }
                    } else {
                        Text(
                            text = aiAnalysisText ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp
                        )
                        TextButton(
                            onClick = {
                                scope.launch {
                                    isAnalyzingAi = true
                                    val result = aiService.generateDailyExecutiveSummary(rooms, reports, todayCollectionAmount)
                                    result.onSuccess { aiAnalysisText = it }
                                    isAnalyzingAi = false
                                }
                            }
                        ) {
                            Text("Refresh Analysis")
                        }
                    }
                }
            }
        }

        // Past Reports Section
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "DPR Submission History (${reports.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (reports.isEmpty()) {
            item {
                Text(
                    text = "No previous reports logged yet. Submit today's first DPR above.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(reports, key = { it.id.ifBlank { it.reportDate } }) { r ->
                ReportHistoryCard(report = r)
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }
}

@Composable
private fun StatBox(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = color, fontSize = 10.sp)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun ReportHistoryCard(report: DailyReport) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DPR: ${report.reportDate}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .background(
                            if (report.cutoffMet) Color(0xFFDCFCE7) else Color(0xFFFEF2F2),
                            RoundedCornerShape(10.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (report.cutoffMet) "ON TIME (Before 8 PM)" else "LATE SUBMISSION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (report.cutoffMet) Color(0xFF166534) else Color(0xFF991B1B)
                    )
                }
            }

            Text(
                text = "Submitted by ${report.submittedBy} • Occupancy: ${report.occupancyRate.toInt()}% • Collected: ₹ ${report.todayCollections.toInt()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!report.remarks.isNullOrBlank()) {
                Text(
                    text = "\"${report.remarks}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
