package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DawnOrange
import com.example.ui.theme.MidnightCardBorder
import com.example.ui.theme.MidnightSurfaceVariant
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.WakeupGreen

@Composable
fun NumericKeypad(
    onDigitClick: (String) -> Unit,
    onBackspaceClick: () -> Unit,
    onSubmitClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9")
        )

        for (row in rows) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (digit in row) {
                    KeypadButton(
                        text = digit,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("keypad_digit_$digit"),
                        onClick = { onDigitClick(digit) }
                    )
                }
            }
        }

        // Bottom row: Backspace, 0, Submit Checkmark
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Backspace button
            KeypadActionButton(
                icon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Delete",
                        tint = TextPrimary
                    )
                },
                backgroundColor = MidnightSurfaceVariant,
                modifier = Modifier
                    .weight(1f)
                    .testTag("keypad_backspace"),
                onClick = onBackspaceClick
            )

            // 0 button
            KeypadButton(
                text = "0",
                modifier = Modifier
                    .weight(1f)
                    .testTag("keypad_digit_0"),
                onClick = { onDigitClick("0") }
            )

            // Submit button
            KeypadActionButton(
                icon = {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Submit Answer",
                        tint = Color.White
                    )
                },
                backgroundColor = WakeupGreen,
                modifier = Modifier
                    .weight(1f)
                    .testTag("keypad_submit"),
                onClick = onSubmitClick
            )
        }
    }
}

@Composable
private fun KeypadButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .height(60.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = DawnOrange)
            ) { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = MidnightSurfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, MidnightCardBorder)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
    }
}

@Composable
private fun KeypadActionButton(
    icon: @Composable () -> Unit,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .height(60.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = Color.White)
            ) { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = backgroundColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, MidnightCardBorder)
    ) {
        Box(contentAlignment = Alignment.Center) {
            icon()
        }
    }
}
