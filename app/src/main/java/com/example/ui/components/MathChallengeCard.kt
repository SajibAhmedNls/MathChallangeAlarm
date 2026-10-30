package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MathChallengeState
import com.example.ui.theme.extendedColors

@Composable
fun MathChallengeCard(
    mathState: MathChallengeState,
    modifier: Modifier = Modifier
) {
    // Horizontal shake on a wrong answer
    val shakeOffset = remember { Animatable(0f) }
    LaunchedEffect(mathState.isShaking) {
        if (mathState.isShaking) {
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    -24f at 50
                    24f at 120
                    -16f at 190
                    16f at 260
                    -6f at 330
                }
            )
        }
    }

    val borderColor by animateColorAsState(
        if (mathState.isShaking) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outlineVariant,
        label = "cardBorder"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { translationX = shakeOffset.value }
            .testTag("math_challenge_card"),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        border = BorderStroke(if (mathState.isShaking) 2.dp else 1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val problemNumber = (mathState.streak + 1).coerceAtMost(mathState.targetStreak)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Problem $problemNumber of ${mathState.targetStreak}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${mathState.streak}/${mathState.targetStreak} solved",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Segmented progress bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "${mathState.streak} of ${mathState.targetStreak} problems solved"
                    },
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (i in 0 until mathState.targetStreak) {
                    val segmentColor by animateColorAsState(
                        when {
                            i < mathState.streak -> MaterialTheme.extendedColors.success
                            i == mathState.streak -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.surfaceContainerHighest
                        },
                        label = "segment$i"
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(segmentColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "${mathState.currentProblem.expression} = ?",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("math_expression_text")
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Answer field
            val answerBorder by animateColorAsState(
                if (mathState.currentInput.isEmpty()) MaterialTheme.colorScheme.outline
                else MaterialTheme.colorScheme.primary,
                label = "answerBorder"
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(2.dp, answerBorder, MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                if (mathState.currentInput.isEmpty()) {
                    Text(
                        text = "Type your answer",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = mathState.currentInput,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 2.sp,
                        modifier = Modifier.testTag("math_current_input")
                    )
                }
            }

            AnimatedVisibility(
                visible = mathState.errorMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Text(
                    text = mathState.errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .semantics { liveRegion = LiveRegionMode.Assertive }
                        .testTag("math_error_message")
                )
            }
        }
    }
}
