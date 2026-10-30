package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MathChallengeState
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.DawnOrange
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.MidnightBg
import com.example.ui.theme.MidnightCardBorder
import com.example.ui.theme.MidnightSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.WakeupGreen

@Composable
fun MathChallengeCard(
    mathState: MathChallengeState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("math_challenge_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MidnightSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (mathState.isShaking) 2.dp else 1.dp,
            color = if (mathState.isShaking) AlertCrimson else MidnightCardBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with Streak Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SOLVE TO STOP ALARM",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = DawnOrange
                )

                Text(
                    text = "${mathState.streak} / ${mathState.targetStreak} SOLVED",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ElectricCyan
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3 Progress Step Indicator Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until mathState.targetStreak) {
                    val isCompleted = i < mathState.streak
                    val isCurrent = i == mathState.streak

                    Box(
                        modifier = Modifier
                            .size(if (isCurrent) 32.dp else 26.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCompleted -> WakeupGreen
                                    isCurrent -> DawnOrange.copy(alpha = 0.35f)
                                    else -> MidnightBg
                                }
                            )
                            .border(
                                width = if (isCurrent) 2.dp else 1.dp,
                                color = when {
                                    isCompleted -> WakeupGreen
                                    isCurrent -> DawnOrange
                                    else -> MidnightCardBorder
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed step",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = "${i + 1}",
                                fontSize = if (isCurrent) 14.sp else 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isCurrent) DawnOrange else TextMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Math Expression Display (Big and bold)
            Text(
                text = "${mathState.currentProblem.expression} = ?",
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("math_expression_text")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // User Answer Box
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(64.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MidnightBg)
                    .border(
                        width = 1.5.dp,
                        brush = Brush.horizontalGradient(
                            listOf(DawnOrange.copy(alpha = 0.7f), ElectricCyan.copy(alpha = 0.7f))
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (mathState.currentInput.isEmpty()) {
                    Text(
                        text = "Your Answer",
                        fontSize = 18.sp,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )
                } else {
                    Text(
                        text = mathState.currentInput,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = ElectricCyan,
                        letterSpacing = 2.sp,
                        modifier = Modifier.testTag("math_current_input")
                    )
                }
            }

            // Error feedback banner
            AnimatedVisibility(
                visible = mathState.errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = mathState.errorMessage ?: "",
                    color = AlertCrimson,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .testTag("math_error_message")
                )
            }
        }
    }
}
