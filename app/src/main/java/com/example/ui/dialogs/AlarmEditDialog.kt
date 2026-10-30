package com.example.ui.dialogs

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.AlarmEntity
import com.example.ui.theme.DawnOrange
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MidnightBg
import com.example.ui.theme.MidnightCardBorder
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.MidnightSurfaceVariant
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun AlarmEditDialog(
    initialAlarm: AlarmEntity?,
    onDismiss: () -> Unit,
    onSave: (hour: Int, minute: Int, daysOfWeek: String, label: String, vibrate: Boolean) -> Unit
) {
    val initialH = initialAlarm?.hour ?: 7
    val initialM = initialAlarm?.minute ?: 0

    var isPm by remember { mutableStateOf(initialH >= 12) }
    var displayHour by remember {
        val h12 = if (initialH == 0) 12 else if (initialH > 12) initialH - 12 else initialH
        mutableIntStateOf(h12)
    }
    var selectedMinute by remember { mutableIntStateOf(initialM) }
    var labelText by remember { mutableStateOf(initialAlarm?.label ?: "Wake Up Sajib") }
    var vibrateEnabled by remember { mutableStateOf(initialAlarm?.vibrate ?: true) }

    // Repeat days: 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat, 7=Sun
    val selectedDays = remember {
        val list = mutableStateListOf<Int>()
        initialAlarm?.getDaysList()?.let { list.addAll(it) }
        list
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("alarm_edit_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MidnightSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MidnightCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialAlarm == null) "Set Alarm" else "Edit Alarm",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable Time Wheel Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(MidnightBg)
                        .border(1.dp, MidnightCardBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Scrollable Hour Wheel (1 to 12)
                        ScrollableWheelTimePicker(
                            selectedValue = displayHour,
                            range = (1..12).toList(),
                            onValueChange = { displayHour = it },
                            label = "HOUR",
                            testTagPrefix = "hour_wheel"
                        )

                        Text(
                            text = ":",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = DawnOrange,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        // Scrollable Minute Wheel (00 to 59)
                        ScrollableWheelTimePicker(
                            selectedValue = selectedMinute,
                            range = (0..59).toList(),
                            onValueChange = { selectedMinute = it },
                            label = "MINUTE",
                            isMinute = true,
                            testTagPrefix = "minute_wheel"
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        // AM / PM Toggle Buttons
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AmPmButton(
                                text = "AM",
                                isSelected = !isPm,
                                onClick = { isPm = false },
                                modifier = Modifier.testTag("am_button")
                            )
                            AmPmButton(
                                text = "PM",
                                isSelected = isPm,
                                onClick = { isPm = true },
                                modifier = Modifier.testTag("pm_button")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Repeat Days Selection
                Text(
                    text = "REPEAT DAYS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                val dayLabels = listOf(
                    1 to "M",
                    2 to "T",
                    3 to "W",
                    4 to "T",
                    5 to "F",
                    6 to "S",
                    7 to "S"
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dayLabels.forEach { (dayIndex, dayLabel) ->
                        val isSelected = selectedDays.contains(dayIndex)
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) DawnOrange else MidnightSurfaceVariant)
                                .border(
                                    1.dp,
                                    if (isSelected) DawnOrange else MidnightCardBorder,
                                    CircleShape
                                )
                                .clickable {
                                    if (isSelected) selectedDays.remove(dayIndex)
                                    else selectedDays.add(dayIndex)
                                }
                                .testTag("day_button_$dayIndex"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayLabel,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Alarm Label Input
                OutlinedTextField(
                    value = labelText,
                    onValueChange = { labelText = it },
                    label = { Text("Alarm Label") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DawnOrange,
                        unfocusedBorderColor = MidnightCardBorder,
                        focusedLabelColor = DawnOrange,
                        unfocusedLabelColor = TextMuted,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("alarm_label_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Vibrate Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Vibrate on Alarm",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Pulsating wake-up vibration",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }

                    Switch(
                        checked = vibrateEnabled,
                        onCheckedChange = { vibrateEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = DawnOrange,
                            uncheckedThumbColor = TextMuted,
                            uncheckedTrackColor = MidnightSurfaceVariant
                        ),
                        modifier = Modifier.testTag("vibrate_switch")
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons (Cancel / Save)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("cancel_alarm_button"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextSecondary
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MidnightCardBorder)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val final24Hour = when {
                                isPm && displayHour < 12 -> displayHour + 12
                                !isPm && displayHour == 12 -> 0
                                else -> displayHour
                            }
                            val daysCsv = selectedDays.sorted().joinToString(",")
                            onSave(final24Hour, selectedMinute, daysCsv, labelText, vibrateEnabled)
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_alarm_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DawnOrange,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ScrollableWheelTimePicker(
    selectedValue: Int,
    range: List<Int>,
    onValueChange: (Int) -> Unit,
    label: String,
    isMinute: Boolean = false,
    testTagPrefix: String = "wheel"
) {
    val coroutineScope = rememberCoroutineScope()
    val initialIndex = range.indexOf(selectedValue).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = initialIndex)

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val centerIndex = (listState.firstVisibleItemIndex + if (listState.firstVisibleItemScrollOffset > 40) 1 else 0)
                .coerceIn(0, range.size - 1)
            onValueChange(range[centerIndex])
            listState.animateScrollToItem(centerIndex)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(76.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Box(
            modifier = Modifier
                .height(144.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Selected item background highlight bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MidnightSurfaceVariant)
                    .border(1.dp, DawnOrange.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("${testTagPrefix}_scroll_wheel"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top spacing so first item can be centered
                item {
                    Spacer(modifier = Modifier.height(48.dp))
                }

                itemsIndexed(range) { index, itemValue ->
                    val isSelected = itemValue == selectedValue
                    val text = if (isMinute) String.format("%02d", itemValue) else itemValue.toString()

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clickable {
                                onValueChange(itemValue)
                                coroutineScope.launch {
                                    listState.animateScrollToItem(index)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = text,
                            fontSize = if (isSelected) 30.sp else 18.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Normal,
                            color = if (isSelected) DawnOrange else TextSecondary.copy(alpha = 0.45f)
                        )
                    }
                }

                // Bottom spacing so last item can be centered
                item {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}

@Composable
private fun AmPmButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(width = 50.dp, height = 36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) ElectricCyan else MidnightSurfaceVariant)
            .border(
                1.dp,
                if (isSelected) ElectricCyan else MidnightCardBorder,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) Color.Black else TextSecondary
        )
    }
}
