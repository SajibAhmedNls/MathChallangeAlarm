package com.example.ui.dialogs

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AlarmEntity
import com.example.ui.DEFAULT_ALARM_LABEL
import com.example.ui.components.DayFullNames
import com.example.ui.components.DayShortLabels
import com.example.ui.components.repeatSummary
import kotlinx.coroutines.launch

private val WheelItemHeight = 52.dp
private const val WheelVisibleItems = 3

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
    var labelText by remember { mutableStateOf(initialAlarm?.label ?: DEFAULT_ALARM_LABEL) }
    var vibrateEnabled by remember { mutableStateOf(initialAlarm?.vibrate ?: true) }

    // Repeat days: 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat, 7=Sun
    val selectedDays = remember {
        val list = mutableStateListOf<Int>()
        initialAlarm?.getDaysList()?.let { list.addAll(it) }
        list
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .testTag("alarm_edit_dialog"),
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialAlarm == null) "New alarm" else "Edit alarm",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Time picker
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.large)
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(vertical = 16.dp, horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ScrollableWheelTimePicker(
                            selectedValue = displayHour,
                            range = (1..12).toList(),
                            onValueChange = { displayHour = it },
                            label = "Hour",
                            testTagPrefix = "hour_wheel"
                        )

                        Text(
                            text = ":",
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 0.dp)
                        )

                        ScrollableWheelTimePicker(
                            selectedValue = selectedMinute,
                            range = (0..59).toList(),
                            onValueChange = { selectedMinute = it },
                            label = "Minute",
                            isMinute = true,
                            testTagPrefix = "minute_wheel"
                        )

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

                Spacer(modifier = Modifier.height(24.dp))

                // Repeat days
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Repeat",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = repeatSummary(selectedDays),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    DayShortLabels.forEachIndexed { index, dayLabel ->
                        val dayIndex = index + 1
                        val isSelected = selectedDays.contains(dayIndex)
                        val bg by animateColorAsState(
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer,
                            label = "dayBg"
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(bg)
                                .clickable {
                                    if (isSelected) selectedDays.remove(dayIndex)
                                    else selectedDays.add(dayIndex)
                                }
                                .semantics {
                                    role = Role.Checkbox
                                    selected = isSelected
                                    contentDescription = DayFullNames[index]
                                }
                                .testTag("day_button_$dayIndex"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayLabel,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = labelText,
                    onValueChange = { labelText = it },
                    label = { Text("Label") },
                    placeholder = { Text(DEFAULT_ALARM_LABEL) },
                    leadingIcon = { Icon(Icons.AutoMirrored.Outlined.Label, contentDescription = null) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("alarm_label_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Vibrate: the whole row toggles, not just the switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.medium)
                        .toggleable(
                            value = vibrateEnabled,
                            role = Role.Switch,
                            onValueChange = { vibrateEnabled = it }
                        )
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Vibration,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp, end = 16.dp)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Vibrate", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "Pulse while the alarm rings",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = vibrateEnabled,
                        onCheckedChange = null,
                        modifier = Modifier.testTag("vibrate_switch")
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("cancel_alarm_button")
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
                            .height(52.dp)
                            .testTag("save_alarm_button")
                    ) {
                        Text("Save alarm")
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
    val itemHeightPx = with(LocalDensity.current) { WheelItemHeight.toPx() }

    // With one item of padding above and below, the first visible item is the centered one.
    // Snap to whichever item is closest to the center once the user lets go.
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val roundUp = listState.firstVisibleItemScrollOffset > itemHeightPx / 2
            val centerIndex = (listState.firstVisibleItemIndex + if (roundUp) 1 else 0)
                .coerceIn(0, range.size - 1)
            onValueChange(range[centerIndex])
            listState.animateScrollToItem(centerIndex)
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(72.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Box(
            modifier = Modifier
                .height(WheelItemHeight * WheelVisibleItems)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            // Selection highlight behind the centered item
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(WheelItemHeight)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            )

            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(vertical = WheelItemHeight),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("${testTagPrefix}_scroll_wheel"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                itemsIndexed(range) { index, itemValue ->
                    val isSelected = itemValue == selectedValue
                    val text = if (isMinute) String.format("%02d", itemValue) else itemValue.toString()

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(WheelItemHeight)
                            .clickable {
                                onValueChange(itemValue)
                                coroutineScope.launch {
                                    listState.animateScrollToItem(index)
                                }
                            }
                            .semantics { contentDescription = "$label $text" },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = text,
                            style = if (isSelected) MaterialTheme.typography.displaySmall
                            else MaterialTheme.typography.titleLarge,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
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
    val bg by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        label = "amPmBg"
    )
    Box(
        modifier = modifier
            .size(width = 56.dp, height = 48.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(bg)
            .clickable { onClick() }
            .semantics {
                role = Role.RadioButton
                selected = isSelected
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
