package com.sophia.assistant.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.sophia.assistant.ui.theme.ElectricBlue
import com.sophia.assistant.ui.theme.NeonCyan
import com.sophia.assistant.ui.theme.NeonPink
import com.sophia.assistant.ui.theme.NeonPurple

enum class AssistantState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING
}

@Composable
fun VoiceOrb(
    state: AssistantState,
    rmsLevel: Float = 0f,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "OrbTransition")

    // Rotação suave contínua
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "OrbRotation"
    )

    // Pulsação suave para modo Idle
    val idlePulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "IdlePulse"
    )

    // Expansão de ondas sonoras para quando Sophia está falando
    val speakingWave1 by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SpeakingWave1"
    )

    val speakingWave2 by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.75f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SpeakingWave2"
    )

    // Cores e brilhos dinâmicos baseados no estado
    val primaryColor = when (state) {
        AssistantState.IDLE -> NeonCyan
        AssistantState.LISTENING -> NeonCyan
        AssistantState.THINKING -> NeonPurple
        AssistantState.SPEAKING -> NeonPink
    }

    val secondaryColor = when (state) {
        AssistantState.IDLE -> ElectricBlue
        AssistantState.LISTENING -> ElectricBlue
        AssistantState.THINKING -> NeonCyan
        AssistantState.SPEAKING -> NeonPurple
    }

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(190.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(190.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.minDimension / 4.2f

            // Fator de escala dinâmico
            val scale = when (state) {
                AssistantState.IDLE -> idlePulse
                AssistantState.LISTENING -> 1.0f + (rmsLevel * 0.45f)
                AssistantState.THINKING -> 1.05f
                AssistantState.SPEAKING -> 1.08f + ((speakingWave1 - 1f) * 0.2f)
            }

            // 1. Ondas externas de difusão de som
            if (state == AssistantState.SPEAKING) {
                val alpha1 = ((1.45f - speakingWave1) / 0.45f).coerceIn(0f, 0.7f)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(primaryColor.copy(alpha = alpha1 * 0.4f), Color.Transparent),
                        center = center,
                        radius = baseRadius * speakingWave1
                    ),
                    center = center,
                    radius = baseRadius * speakingWave1
                )
                val alpha2 = ((1.75f - speakingWave2) / 0.75f).coerceIn(0f, 0.5f)
                drawCircle(
                    color = secondaryColor.copy(alpha = alpha2 * 0.35f),
                    center = center,
                    radius = baseRadius * speakingWave2,
                    style = Stroke(width = 3.dp.toPx())
                )
            } else if (state == AssistantState.LISTENING) {
                // Anéis que reagem em tempo real ao volume captado do microfone
                val dynamicRadius = baseRadius * (1.2f + rmsLevel * 0.6f)
                drawCircle(
                    color = NeonCyan.copy(alpha = 0.35f + (rmsLevel * 0.45f)),
                    center = center,
                    radius = dynamicRadius,
                    style = Stroke(width = (2f + rmsLevel * 4f).dp.toPx())
                )
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(NeonCyan.copy(alpha = 0.25f), Color.Transparent),
                        center = center,
                        radius = dynamicRadius * 1.2f
                    ),
                    center = center,
                    radius = dynamicRadius * 1.2f
                )
            }

            // 2. Halo / Brilho Externo do Orbe
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.5f),
                        secondaryColor.copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * scale * 1.55f
                ),
                center = center,
                radius = baseRadius * scale * 1.55f
            )

            // 3. Anel orbital futurista giratório
            val ringRadius = baseRadius * scale * 1.22f
            val ringAngleRad = Math.toRadians(rotation.toDouble())
            val startOffset = Offset(
                x = center.x + (ringRadius * Math.cos(ringAngleRad)).toFloat(),
                y = center.y + (ringRadius * Math.sin(ringAngleRad)).toFloat()
            )
            val endOffset = Offset(
                x = center.x - (ringRadius * Math.cos(ringAngleRad)).toFloat(),
                y = center.y - (ringRadius * Math.sin(ringAngleRad)).toFloat()
            )

            drawCircle(
                brush = Brush.linearGradient(
                    colors = listOf(primaryColor, secondaryColor, Color.Transparent),
                    start = startOffset,
                    end = endOffset
                ),
                center = center,
                radius = ringRadius,
                style = Stroke(width = 2.5.dp.toPx())
            )

            // 4. Esfera Central de Energia Quântica (Núcleo)
            val coreRadius = baseRadius * scale
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        primaryColor,
                        secondaryColor,
                        Color(0xFF04060A)
                    ),
                    center = Offset(center.x - coreRadius * 0.25f, center.y - coreRadius * 0.25f),
                    radius = coreRadius * 1.2f
                ),
                center = center,
                radius = coreRadius
            )

            // 5. Brilho especular no centro (ponto de luz holográfico)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.85f), Color.Transparent),
                    center = Offset(center.x - coreRadius * 0.35f, center.y - coreRadius * 0.35f),
                    radius = coreRadius * 0.35f
                ),
                center = Offset(center.x - coreRadius * 0.35f, center.y - coreRadius * 0.35f),
                radius = coreRadius * 0.35f
            )
        }
    }
}
