package com.vigizoomato.customer.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.vigizoomato.customer.data.models.ChatMessage
import com.vigizoomato.customer.data.models.SenderType
import com.vigizoomato.customer.ui.components.AppHeader
import com.vigizoomato.customer.ui.theme.*

@Composable
fun OrderChatScreen(
    subOrderId: String,
    restaurantName: String,
    onBackClick: () -> Unit,
    viewModel: ChatViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    LaunchedEffect(subOrderId, restaurantName) {
        viewModel.initialize(subOrderId, restaurantName)
    }

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            AppHeader(
                title = "Chat: $restaurantName",
                subtitle = "Sub-Order #$subOrderId • Kitchen Staff Online",
                onBackClick = onBackClick
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceWhite,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.inputText,
                        onValueChange = { viewModel.onInputChanged(it) },
                        placeholder = { Text("Type special prep instructions or questions...") },
                        maxLines = 3,
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = { viewModel.sendMessage() },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(PrimaryOrange)
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = "Send", tint = SurfaceWhite)
                    }
                }
            }
        },
        containerColor = BackgroundLight,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(uiState.messages) { msg ->
                ChatBubble(msg = msg)
            }
        }
    }
}

@Composable
fun ChatBubble(msg: ChatMessage) {
    val isCustomer = msg.senderType == SenderType.CUSTOMER
    val isSystem = msg.senderType == SenderType.SYSTEM

    if (isSystem) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(BorderLight)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = msg.messageText,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isCustomer) Arrangement.End else Arrangement.Start
        ) {
            Column(
                horizontalAlignment = if (isCustomer) Alignment.End else Alignment.Start,
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Text(
                    text = msg.senderName,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                )

                Box(
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                topStart = 14.dp,
                                topEnd = 14.dp,
                                bottomStart = if (isCustomer) 14.dp else 2.dp,
                                bottomEnd = if (isCustomer) 2.dp else 14.dp
                            )
                        )
                        .background(if (isCustomer) PrimaryOrange else SurfaceWhite)
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = msg.messageText,
                            color = if (isCustomer) SurfaceWhite else TextPrimary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = msg.timestamp,
                            color = if (isCustomer) SurfaceWhite.copy(alpha = 0.7f) else TextTertiary,
                            fontSize = 10.sp,
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }
        }
    }
}
