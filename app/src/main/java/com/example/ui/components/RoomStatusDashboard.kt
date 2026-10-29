package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Room
import com.example.ui.theme.StatusMaintenance
import com.example.ui.theme.StatusOccupied
import com.example.ui.theme.StatusPartial
import com.example.ui.theme.StatusVacant

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RoomStatusDashboard(
    rooms: List<Room>,
    onUpdateRoom: (Room) -> Unit,
    onAddRoom: (Room) -> Unit,
    onQuickBookRoom: (Room) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf("All") }
    var selectedFloorFilter by remember { mutableStateOf("All") }
    var showAddRoomDialog by remember { mutableStateOf(false) }
    var roomToEdit by remember { mutableStateOf<Room?>(null) }

    // Aggregate occupancy statistics
    val totalRooms = rooms.size
    val occupiedRooms = rooms.count { it.status == "occupied" }
    val partiallyOccupiedRooms = rooms.count { it.status == "partially_occupied" }
    val vacantRooms = rooms.count { it.status == "vacant" }
    val maintenanceRooms = rooms.count { it.status == "maintenance" }

    val totalBeds = rooms.sumOf { it.totalBeds }
    val occupiedBeds = rooms.sumOf { it.occupiedBeds }
    val availableBeds = (totalBeds - occupiedBeds).coerceAtLeast(0)
    val occupancyRate = if (totalBeds > 0) (occupiedBeds.toFloat() / totalBeds * 100) else 0f

    val floors = remember(rooms) {
        listOf("All") + rooms.map { it.floor }.distinct().sorted()
    }

    // Filter rooms
    val filteredRooms = rooms.filter { room ->
        val matchesSearch = searchQuery.isBlank() ||
                room.roomNumber.contains(searchQuery, ignoreCase = true) ||
                room.roomType.contains(searchQuery, ignoreCase = true)
        val matchesStatus = when (selectedStatusFilter) {
            "All" -> true
            "Vacant" -> room.status == "vacant"
            "Occupied" -> room.status == "occupied"
            "Partially Occupied" -> room.status == "partially_occupied"
            "Maintenance" -> room.status == "maintenance"
            else -> true
        }
        val matchesFloor = selectedFloorFilter == "All" || room.floor.equals(selectedFloorFilter, ignoreCase = true)

        matchesSearch && matchesStatus && matchesFloor
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                // Visual Occupancy Header Card
                OccupancyMetricsCard(
                    totalRooms = totalRooms,
                    totalBeds = totalBeds,
                    occupiedBeds = occupiedBeds,
                    availableBeds = availableBeds,
                    occupancyRate = occupancyRate,
                    vacantRooms = vacantRooms,
                    occupiedRooms = occupiedRooms,
                    partialRooms = partiallyOccupiedRooms,
                    maintenanceRooms = maintenanceRooms
                )
            }

            // Search Bar & Filters
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("room_search_input"),
                        placeholder = { Text("Search by room number or type (e.g. 101, Single)") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Status Filter Row
                    Text(
                        text = "Filter by Occupancy Status:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val statusList = listOf("All", "Vacant", "Occupied", "Partially Occupied", "Maintenance")
                        items(statusList) { status ->
                            val isSelected = selectedStatusFilter == status
                            val chipColor = when (status) {
                                "Vacant" -> StatusVacant
                                "Occupied" -> StatusOccupied
                                "Partially Occupied" -> StatusPartial
                                "Maintenance" -> StatusMaintenance
                                else -> MaterialTheme.colorScheme.primary
                            }

                            SuggestionChip(
                                onClick = { selectedStatusFilter = status },
                                label = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (status != "All") {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .background(chipColor, CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                        }
                                        Text(
                                            text = status,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    }
                                },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = if (isSelected) chipColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                    labelColor = if (isSelected) chipColor else MaterialTheme.colorScheme.onSurface
                                ),
                                border = SuggestionChipDefaults.suggestionChipBorder(
                                    enabled = true,
                                    borderColor = if (isSelected) chipColor else MaterialTheme.colorScheme.outlineVariant
                                )
                            )
                        }
                    }

                    // Floor Filter Row
                    if (floors.size > 2) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(floors) { floor ->
                                val isSelected = selectedFloorFilter == floor
                                SuggestionChip(
                                    onClick = { selectedFloorFilter = floor },
                                    label = { Text(if (floor == "All") "All Floors" else floor) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Section Header & Room Count
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rooms Inventory (${filteredRooms.size} showing)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = { showAddRoomDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Room", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Room")
                    }
                }
            }

            if (filteredRooms.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MeetingRoom,
                                contentDescription = "No rooms",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No rooms match the selected filters",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredRooms, key = { it.id.ifBlank { it.roomNumber } }) { room ->
                    RoomItemCard(
                        room = room,
                        onUpdateRoom = onUpdateRoom,
                        onEditRoom = { roomToEdit = room },
                        onQuickBook = { onQuickBookRoom(room) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddRoomDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("add_room_fab"),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Room")
        }
    }

    // Add Room Dialog
    if (showAddRoomDialog) {
        AddRoomDialog(
            onDismiss = { showAddRoomDialog = false },
            onAdd = { newRoom ->
                onAddRoom(newRoom)
                showAddRoomDialog = false
            }
        )
    }

    // Edit Room Dialog
    roomToEdit?.let { room ->
        EditRoomDialog(
            room = room,
            onDismiss = { roomToEdit = null },
            onSave = { updated ->
                onUpdateRoom(updated)
                roomToEdit = null
            }
        )
    }
}

@Composable
private fun OccupancyMetricsCard(
    totalRooms: Int,
    totalBeds: Int,
    occupiedBeds: Int,
    availableBeds: Int,
    occupancyRate: Float,
    vacantRooms: Int,
    occupiedRooms: Int,
    partialRooms: Int,
    maintenanceRooms: Int
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("occupancy_metrics_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "REAL-TIME OCCUPANCY DASHBOARD",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${occupancyRate.toInt()}% Capacity",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            if (occupancyRate >= 80f) StatusOccupied.copy(alpha = 0.15f)
                            else if (occupancyRate >= 50f) StatusPartial.copy(alpha = 0.15f)
                            else StatusVacant.copy(alpha = 0.15f),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "$occupiedBeds / $totalBeds Beds Filled",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (occupancyRate >= 80f) StatusOccupied else if (occupancyRate >= 50f) StatusPartial else StatusVacant
                    )
                }
            }

            // Visual Progress Bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { (occupancyRate / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "$availableBeds Beds Available for Booking",
                        style = MaterialTheme.typography.labelSmall,
                        color = StatusVacant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "$totalRooms Total Rooms",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Status Counters Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricChip(
                    label = "Vacant",
                    count = vacantRooms,
                    color = StatusVacant,
                    modifier = Modifier.weight(1f)
                )
                MetricChip(
                    label = "Occupied",
                    count = occupiedRooms,
                    color = StatusOccupied,
                    modifier = Modifier.weight(1f)
                )
                MetricChip(
                    label = "Partial",
                    count = partialRooms,
                    color = StatusPartial,
                    modifier = Modifier.weight(1f)
                )
                MetricChip(
                    label = "Under Repair",
                    count = maintenanceRooms,
                    color = StatusMaintenance,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun MetricChip(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontSize = 10.sp,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun RoomItemCard(
    room: Room,
    onUpdateRoom: (Room) -> Unit,
    onEditRoom: () -> Unit,
    onQuickBook: () -> Unit
) {
    var showStatusMenu by remember { mutableStateOf(false) }

    val statusColor = when (room.status) {
        "vacant" -> StatusVacant
        "occupied" -> StatusOccupied
        "partially_occupied" -> StatusPartial
        "maintenance" -> StatusMaintenance
        else -> MaterialTheme.colorScheme.primary
    }

    val statusLabel = when (room.status) {
        "vacant" -> "VACANT"
        "occupied" -> "FULL OCCUPIED"
        "partially_occupied" -> "PARTIAL (${room.occupiedBeds}/${room.totalBeds})"
        "maintenance" -> "MAINTENANCE"
        else -> room.status.uppercase()
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("room_card_${room.roomNumber}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row: Room Number, Floor, Status Chip & Quick Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Room ${room.roomNumber}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${room.floor} • ${room.roomType}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box {
                    Box(
                        modifier = Modifier
                            .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                            .clickable { showStatusMenu = true }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(statusColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = statusLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showStatusMenu,
                        onDismissRequest = { showStatusMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Mark Vacant") },
                            onClick = {
                                onUpdateRoom(room.copy(status = "vacant", occupiedBeds = 0))
                                showStatusMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Mark Partially Occupied") },
                            onClick = {
                                onUpdateRoom(room.copy(status = "partially_occupied", occupiedBeds = 1.coerceAtMost(room.totalBeds)))
                                showStatusMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Mark Fully Occupied") },
                            onClick = {
                                onUpdateRoom(room.copy(status = "occupied", occupiedBeds = room.totalBeds))
                                showStatusMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Mark Under Maintenance") },
                            onClick = {
                                onUpdateRoom(room.copy(status = "maintenance"))
                                showStatusMenu = false
                            }
                        )
                    }
                }
            }

            // Bed Slots Visual Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Beds:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                for (i in 1..room.totalBeds) {
                    val isBedOccupied = i <= room.occupiedBeds && room.status != "vacant"
                    val bedLetter = ('A' + (i - 1)).toString()
                    Box(
                        modifier = Modifier
                            .background(
                                if (room.status == "maintenance") StatusMaintenance.copy(alpha = 0.15f)
                                else if (isBedOccupied) StatusOccupied.copy(alpha = 0.15f)
                                else StatusVacant.copy(alpha = 0.15f),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bed,
                                contentDescription = "Bed $bedLetter",
                                modifier = Modifier.size(12.dp),
                                tint = if (room.status == "maintenance") StatusMaintenance
                                else if (isBedOccupied) StatusOccupied
                                else StatusVacant
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Bed-$bedLetter (${if (room.status == "maintenance") "Repair" else if (isBedOccupied) "Taken" else "Free"})",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (room.status == "maintenance") StatusMaintenance
                                else if (isBedOccupied) StatusOccupied
                                else StatusVacant
                            )
                        }
                    }
                }
            }

            // Maintenance Note Banner if applicable
            if (room.status == "maintenance" && !room.maintenanceNote.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(StatusMaintenance.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = "Maintenance",
                            tint = StatusMaintenance,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = room.maintenanceNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Footer: Rent and Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "₹ ${room.rentPerBed.toInt()} / bed / month",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onEditRoom,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Room", modifier = Modifier.size(18.dp))
                    }

                    if (room.status != "occupied" && room.status != "maintenance") {
                        Button(
                            onClick = onQuickBook,
                            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Book / Assign", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddRoomDialog(
    onDismiss: () -> Unit,
    onAdd: (Room) -> Unit
) {
    var roomNumber by remember { mutableStateOf("") }
    var floor by remember { mutableStateOf("1st Floor") }
    var roomType by remember { mutableStateOf("Double Sharing") }
    var totalBeds by remember { mutableStateOf("2") }
    var rentPerBed by remember { mutableStateOf("7500") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Hostel Room", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = roomNumber,
                    onValueChange = { roomNumber = it },
                    label = { Text("Room Number (e.g. 105, 204)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = floor,
                    onValueChange = { floor = it },
                    label = { Text("Floor (e.g. Ground Floor, 1st Floor)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = roomType,
                    onValueChange = { roomType = it },
                    label = { Text("Room Type (e.g. Single, Double Sharing, Dorm)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = totalBeds,
                        onValueChange = { totalBeds = it },
                        label = { Text("Total Beds") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = rentPerBed,
                        onValueChange = { rentPerBed = it },
                        label = { Text("Rent (₹/bed)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (roomNumber.isNotBlank()) {
                        val beds = totalBeds.toIntOrNull() ?: 2
                        val rent = rentPerBed.toDoubleOrNull() ?: 7000.0
                        onAdd(
                            Room(
                                roomNumber = roomNumber.trim(),
                                floor = floor.trim(),
                                roomType = roomType.trim(),
                                totalBeds = beds,
                                occupiedBeds = 0,
                                status = "vacant",
                                rentPerBed = rent
                            )
                        )
                    }
                },
                enabled = roomNumber.isNotBlank()
            ) {
                Text("Save Room")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun EditRoomDialog(
    room: Room,
    onDismiss: () -> Unit,
    onSave: (Room) -> Unit
) {
    var roomType by remember { mutableStateOf(room.roomType) }
    var totalBeds by remember { mutableStateOf(room.totalBeds.toString()) }
    var occupiedBeds by remember { mutableStateOf(room.occupiedBeds.toString()) }
    var status by remember { mutableStateOf(room.status) }
    var rentPerBed by remember { mutableStateOf(room.rentPerBed.toInt().toString()) }
    var maintenanceNote by remember { mutableStateOf(room.maintenanceNote ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Room ${room.roomNumber}", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = roomType,
                    onValueChange = { roomType = it },
                    label = { Text("Room Type") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = totalBeds,
                        onValueChange = { totalBeds = it },
                        label = { Text("Total Beds") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = occupiedBeds,
                        onValueChange = { occupiedBeds = it },
                        label = { Text("Occupied Beds") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = rentPerBed,
                    onValueChange = { rentPerBed = it },
                    label = { Text("Monthly Rent (₹/bed)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = maintenanceNote,
                    onValueChange = { maintenanceNote = it },
                    label = { Text("Maintenance / Repair Note") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val beds = totalBeds.toIntOrNull() ?: room.totalBeds
                    val occ = occupiedBeds.toIntOrNull() ?: room.occupiedBeds
                    val rent = rentPerBed.toDoubleOrNull() ?: room.rentPerBed
                    val calculatedStatus = if (status == "maintenance") {
                        "maintenance"
                    } else if (occ >= beds) {
                        "occupied"
                    } else if (occ > 0) {
                        "partially_occupied"
                    } else {
                        "vacant"
                    }

                    onSave(
                        room.copy(
                            roomType = roomType.trim(),
                            totalBeds = beds,
                            occupiedBeds = occ.coerceIn(0, beds),
                            status = calculatedStatus,
                            rentPerBed = rent,
                            maintenanceNote = maintenanceNote.ifBlank { null }
                        )
                    )
                }
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
