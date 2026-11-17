package com.sophia.assistant.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sophia.assistant.R
import com.sophia.assistant.data.Message
import com.sophia.assistant.ui.components.AssistantState
import com.sophia.assistant.ui.components.VoiceOrb
import com.sophia.assistant.ui.theme.DarkBg
import com.sophia.assistant.ui.theme.ElectricBlue
import com.sophia.assistant.ui.theme.NeonCyan
import com.sophia.assistant.ui.theme.NeonPink
import com.sophia.assistant.ui.theme.NeonPurple
import com.sophia.assistant.ui.theme.SophiaBubbleBg
import com.sophia.assistant.ui.theme.SophiaBubbleBorder
import com.sophia.assistant.ui.theme.SurfaceCard
import com.sophia.assistant.ui.theme.SurfaceCardBorder
import com.sophia.assistant.ui.theme.SurfaceDark
import com.sophia.assistant.ui.theme.TextMuted
import com.sophia.assistant.ui.theme.TextPrimary
import com.sophia.assistant.ui.theme.TextSecondary
import com.sophia.assistant.ui.theme.UserBubbleGradientEnd
import com.sophia.assistant.ui.theme.UserBubbleGradientStart
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    messages: List<Message>,
    assistantState: AssistantState,
    rmsLevel: Float,
    onMicClick: () -> Unit,
    onSendMessage: (String) -> Unit,
    onStopSpeech: () -> Unit,
    onReplaySpeech: (String) -> Unit,
    onClearHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager()

    // Auto-scroll para a última mensagem
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Image(
                                painter = painterResource(id = R.drawable.sophia_avatar),
                                contentDescription = "Sophia Avatar",
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .border(
                                        2.dp,
                                        when (assistantState) {
                                            AssistantState.IDLE -> NeonCyan
                                            AssistantState.LISTENING -> NeonCyan
                                            AssistantState.THINKING -> NeonPurple
                                            AssistantState.SPEAKING -> NeonPink
                                        },
                                        CircleShape
                                    ),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .size(11.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (assistantState) {
                                            AssistantState.IDLE -> NeonCyan
                                            AssistantState.LISTENING -> NeonCyan
                                            AssistantState.THINKING -> NeonPurple
                                            AssistantState.SPEAKING -> NeonPink
                                        }
                                    )
                                    .border(1.5.dp, SurfaceDark, CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SOPHIA",
                                color = TextPrimary,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = when (assistantState) {
                                    AssistantState.IDLE -> "Pronta para conversar"
                                    AssistantState.LISTENING -> "Ouvindo você..."
                                    AssistantState.THINKING -> "Pensando na resposta..."
                                    AssistantState.SPEAKING -> "Falando..."
                                },
                                color = when (assistantState) {
                                    AssistantState.IDLE -> TextSecondary
                                    AssistantState.LISTENING -> NeonCyan
                                    AssistantState.THINKING -> NeonPurple
                                    AssistantState.SPEAKING -> NeonPink
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                },
                actions = {
                    if (assistantState == AssistantState.SPEAKING) {
                        IconButton(onClick = onStopSpeech) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "Parar Fala",
                                tint = NeonPink
                            )
                        }
                    }
                    IconButton(onClick = onClearHistory) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Limpar Conversa",
                            tint = TextSecondary
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configurações",
                            tint = NeonCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark
                )
            )
        },
        containerColor = DarkBg,
        modifier = Modifier.imePadding()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Seção do Orbe Futurista
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                VoiceOrb(
                    state = assistantState,
                    rmsLevel = rmsLevel,
                    onClick = {
                        if (assistantState == AssistantState.SPEAKING) {
                            onStopSpeech()
                        } else {
                            onMicClick()
                        }
                    }
                )
            }

            // Histórico de Conversas (Balões de Chat)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (messages.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(
                                text = "Toque no microfone e fale comigo,",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "ou digite sua mensagem abaixo.",
                                color = TextMuted,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(messages, key = { it.id }) { message ->
                            ChatBubble(
                                message = message,
                                onReplaySpeech = { onReplaySpeech(message.text) }
                            )
                        }
                    }
                }
            }

            // Barra Inferior de Entrada (Microfone + Campo de Texto)
            Surface(
                color = SurfaceDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SurfaceCardBorder, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Campo de Texto Opcional
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = "Pergunte algo para Sophia...",
                                color = TextMuted,
                                fontSize = 14.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = SurfaceCardBorder,
                            focusedContainerColor = SurfaceCard,
                            unfocusedContainerColor = SurfaceCard
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    if (inputText.isNotBlank()) {
                        // Botão de Enviar Texto
                        IconButton(
                            onClick = {
                                val text = inputText.trim()
                                if (text.isNotBlank()) {
                                    onSendMessage(text)
                                    inputText = ""
                                    focusManager.clearFocus()
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(NeonCyan)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Enviar",
                                tint = DarkBg
                            )
                        }
                    } else {
                        // Botão Futurista de Microfone
                        val isListening = assistantState == AssistantState.LISTENING
                        FloatingActionButton(
                            onClick = onMicClick,
                            containerColor = if (isListening) NeonPink else NeonCyan,
                            contentColor = DarkBg,
                            shape = CircleShape,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = if (isListening) "Parar Escuta" else "Falar",
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: Message,
    onReplaySpeech: () -> Unit
) {
    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val formattedTime = remember(message.timestamp) { timeFormatter.format(Date(message.timestamp)) }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (message.isUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(0.85f),
            horizontalAlignment = if (message.isUser) Alignment.End else Alignment.Start
        ) {
            // Identificação do emissor
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                if (!message.isUser) {
                    Image(
                        painter = painterResource(id = R.drawable.sophia_avatar),
                        contentDescription = "Sophia",
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .border(1.dp, NeonCyan, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = if (message.isUser) "Você" else "Sophia",
                    color = if (message.isUser) ElectricBlue else NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Balão
            Box(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (message.isUser) 16.dp else 4.dp,
                            bottomEnd = if (message.isUser) 4.dp else 16.dp
                        )
                    )
                    .background(
                        if (message.isUser) {
                            Brush.linearGradient(
                                listOf(UserBubbleGradientStart, UserBubbleGradientEnd)
                            )
                        } else {
                            Brush.linearGradient(listOf(SophiaBubbleBg, SophiaBubbleBg))
                        }
                    )
                    .then(
                        if (!message.isUser) {
                            Modifier.border(
                                1.dp,
                                SophiaBubbleBorder,
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = 4.dp,
                                    bottomEnd = 16.dp
                                )
                            )
                        } else Modifier
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    Text(
                        text = message.text,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formattedTime,
                            color = TextMuted,
                            fontSize = 10.sp
                        )

                        if (!message.isUser) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Ouvir novamente",
                                tint = NeonCyan.copy(alpha = 0.7f),
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .clickable { onReplaySpeech() }
                            )
                        }
                    }
                }
            }
        }
    }
}
