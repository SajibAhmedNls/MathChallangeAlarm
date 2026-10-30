package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alarm.ActiveAlarmData
import com.example.ui.MathChallengeState
import com.example.ui.components.MathChallengeCard
import com.example.ui.components.NumericKeypad
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
fun AlarmRingingScreen(
    activeAlarm: ActiveAlarmData?,
    mathState: MathChallengeState,
    onKeypadDigit: (String) -> Unit,
    onKeypadBackspace: () -> Unit,
    onKeypadSubmit: () -> Unit,
    onFinishDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Intercept back button to strictly prevent dismissing while ringing without solving!
    BackHandler(enabled = !mathState.isSolved) {
        // Strict mode: Cannot dismiss with back button!
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alarmPulse"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MidnightBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("alarm_ringing_screen")
    ) {
        if (mathState.isSolved) {
            // Success Wake-up Celebration Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(WakeupGreen.copy(alpha = 0.2f))
                        .border(2.dp, WakeupGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = WakeupGreen,
                        modifier = Modifier.size(56.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Good Morning, Sajib!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "3/3 math problems solved correctly!\nAlarm has stopped ringing. Have an awesome day!",
                    fontSize = 15.sp,
                    color = ElectricCyan,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(36.dp))

                Button(
                    onClick = onFinishDismiss,
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(54.dp)
                        .testTag("dismiss_success_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WakeupGreen,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "I'm Awake! Return to Home",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        } else {
            // Active Ringing & Math Challenge Layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Header Banner: Strict Lock Mode
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = AlertCrimson.copy(alpha = 0.15f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AlertCrimson.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = AlertCrimson,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = " STRICT MODE: Solve 3 math problems to turn off alarm",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AlertCrimson,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Pulsing Alarm Clock Widget
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .scale(pulseScale)
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(DawnOrange.copy(alpha = 0.2f))
                            .border(1.5.dp, DawnOrange, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = "Ringing Alarm",
                            tint = DawnOrange,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(modifier = Modifier.padding(start = 14.dp)) {
                        Text(
                            text = activeAlarm?.timeFormatted ?: "07:00 AM",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Text(
                            text = activeAlarm?.label ?: "Wake Up Sajib!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = DawnOrange
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Math Challenge Card
                MathChallengeCard(
                    mathState = mathState,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Numeric Keypad
                NumericKeypad(
                    onDigitClick = onKeypadDigit,
                    onBackspaceClick = onKeypadBackspace,
                    onSubmitClick = onKeypadSubmit,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
