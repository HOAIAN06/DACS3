package com.fastdash.app.ui.ai

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fastdash.app.data.model.response.AiSuggestedProduct
import com.fastdash.app.utils.CurrencyUtils
import com.fastdash.app.utils.ImageUtils

private val FastDashRed = Color(0xFFD6092F)
private val FastDashBlue = Color(0xFF1E78D7)
private val FastDashBlueDark = Color(0xFF0F4E8A)
private val ScreenBackground = Color(0xFFF5F7FB)
private val TextPrimary = Color(0xFF171A1F)
private val TextSecondary = Color(0xFF667085)
private val BorderColor = Color(0xFFE4EAF2)
private val AiBubbleColor = Color.White
private val UserBubbleStart = Color(0xFFE33C5B)
private val UserBubbleEnd = Color(0xFFC70D33)
private val SoftBlue = Color(0xFFEEF6FF)
private val SoftRed = Color(0xFFFFEFF3)

private data class QuickPromptUi(
    val label: String,
    val message: String
)

private val quickPrompts = listOf(
    QuickPromptUi("Cho 2 người", "Gợi ý món ăn cho 2 người"),
    QuickPromptUi("Dưới 200.000đ", "Gợi ý món ăn dưới 200.000đ"),
    QuickPromptUi("Pizza hải sản", "Tôi thích pizza hải sản, có món nào phù hợp không?"),
    QuickPromptUi("Combo 4 người", "Gợi ý combo cho 4 người"),
    QuickPromptUi("Nhiều phô mai", "Gợi ý món có nhiều phô mai"),
    QuickPromptUi("Cách đặt hàng", "Hướng dẫn đặt hàng trên FastDash"),
    QuickPromptUi("Cách thanh toán", "Hướng dẫn thanh toán trên FastDash")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiAssistantScreen(
    messages: List<ChatMessage>,
    inputText: String,
    isLoading: Boolean,
    errorMessage: String?,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onQuickPromptClick: (String) -> Unit,
    onOpenProduct: (AiSuggestedProduct) -> Unit,
    onBack: () -> Unit,
    onClearError: () -> Unit
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isLoading) {
        val targetIndex = messages.lastIndex + if (isLoading) 1 else 0
        if (targetIndex >= 0) {
            listState.animateScrollToItem(targetIndex)
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            onClearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(1.dp)) {
                        Text(
                            text = "Trợ lý món ăn",
                            color = TextPrimary,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Gợi ý món theo nhu cầu của bạn",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ScreenBackground)
            )
        },
        bottomBar = {
            AiInputBar(
                inputText = inputText,
                isLoading = isLoading,
                onInputChange = onInputChange,
                onSendMessage = onSendMessage
            )
        },
        containerColor = ScreenBackground,
        contentWindowInsets = WindowInsets.navigationBars
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFF9FBFF), ScreenBackground)
                    )
                )
        ) {
            if (messages.isEmpty()) {
                HeroSection(onQuickPromptClick = onQuickPromptClick)
            } else {
                QuickPromptBar(onQuickPromptClick = onQuickPromptClick)
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(messages, key = { it.id }) { message ->
                    ChatBubble(
                        message = message,
                        onOpenProduct = onOpenProduct
                    )
                }

                if (isLoading) {
                    item {
                        LoadingBubble()
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroSection(onQuickPromptClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color.White, Color(0xFFF8FBFF))
                        )
                    )
                    .padding(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(92.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(SoftBlue, Color.Transparent)
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(SoftRed, Color.Transparent)
                            )
                        )
                )

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(42.dp),
                            shape = CircleShape,
                            color = Color.Transparent
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(FastDashBlue, FastDashBlueDark)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Trợ lý FastDash",
                                color = TextPrimary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Gợi ý món phù hợp",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Text(
                        text = "Hôm nay ăn gì?",
                        color = TextPrimary,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = "Nhập nhu cầu để nhận gợi ý món phù hợp.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    Text(
                        text = "Ví dụ: Gợi ý món cho 2 người dưới 300.000đ",
                        color = FastDashBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { onQuickPromptClick("Gợi ý món cho 2 người dưới 300.000đ") },
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = FastDashRed,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text("Tư vấn ngay", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { onQuickPromptClick("Gợi ý món nổi bật trên FastDash") },
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SoftBlue,
                                contentColor = FastDashBlue
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp)
                        ) {
                            Text("Xem gợi ý", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Gợi ý nhanh",
                color = TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
            QuickPromptFlow(onQuickPromptClick = onQuickPromptClick)
        }
    }
}

@Composable
private fun QuickPromptBar(onQuickPromptClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "Gợi ý nhanh",
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        QuickPromptFlow(onQuickPromptClick = onQuickPromptClick)
    }
}

@Composable
private fun QuickPromptFlow(onQuickPromptClick: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        quickPrompts.chunked(2).forEach { rowPrompts ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowPrompts.forEach { prompt ->
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onQuickPromptClick(prompt.message) },
                        shape = RoundedCornerShape(18.dp),
                        color = Color.White,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BorderColor, RoundedCornerShape(18.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier.size(24.dp),
                                shape = CircleShape,
                                color = SoftBlue
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Filled.RestaurantMenu,
                                        contentDescription = null,
                                        tint = FastDashBlue,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Text(
                                text = prompt.label,
                                color = TextPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                if (rowPrompts.size == 1) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(
    message: ChatMessage,
    onOpenProduct: (AiSuggestedProduct) -> Unit
) {
    if (message.isUser) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(0.8f),
                shape = RoundedCornerShape(24.dp, 24.dp, 10.dp, 24.dp),
                color = Color.Transparent
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                colors = listOf(UserBubbleStart, UserBubbleEnd)
                            )
                        )
                        .padding(14.dp)
                ) {
                    Text(
                        text = message.content,
                        color = Color.White,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(0.8f),
                shape = RoundedCornerShape(24.dp, 24.dp, 24.dp, 10.dp),
                color = AiBubbleColor,
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = SoftBlue
                    ) {
                        Text(
                            text = intentBadge(message.intent),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            color = FastDashBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = message.content,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )

                    if (message.suggestedProducts.isNotEmpty()) {
                        SuggestedProductRow(
                            products = message.suggestedProducts,
                            onOpenProduct = onOpenProduct
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SuggestedProductRow(
    products: List<AiSuggestedProduct>,
    onOpenProduct: (AiSuggestedProduct) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(products, key = { it.id }) { product ->
            SuggestedProductCard(product = product, onOpenProduct = onOpenProduct)
        }
    }
}

@Composable
private fun SuggestedProductCard(
    product: AiSuggestedProduct,
    onOpenProduct: (AiSuggestedProduct) -> Unit
) {
    val context = LocalContext.current

    ElevatedCard(
        modifier = Modifier.width(220.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(118.dp)
                    .background(Color(0xFFF4F7FB)),
                contentAlignment = Alignment.Center
            ) {
                if (product.imageUrl.isNullOrBlank()) {
                    Icon(
                        imageVector = Icons.Filled.RestaurantMenu,
                        contentDescription = null,
                        tint = FastDashRed,
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    AsyncImage(
                        model = ImageUtils.buildImageRequest(context, product.imageUrl),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                product.categoryName?.takeIf { it.isNotBlank() }?.let { category ->
                    Surface(
                        color = SoftBlue,
                        shape = RoundedCornerShape(999.dp)
                    ) {
                        Text(
                            text = category,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            color = FastDashBlue,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = product.name,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                product.description?.takeIf { it.isNotBlank() }?.let { description ->
                    Text(
                        text = description,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = CurrencyUtils.formatVnd(product.basePrice),
                        color = FastDashRed,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    TextButton(onClick = { onOpenProduct(product) }) {
                        Text("Xem chi tiết", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingBubble() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.7f),
            shape = RoundedCornerShape(24.dp, 24.dp, 24.dp, 10.dp),
            color = AiBubbleColor,
            shadowElevation = 3.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = FastDashBlue
                )
                Text(
                    text = "Đang tư vấn...",
                    color = TextSecondary,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun AiInputBar(
    inputText: String,
    isLoading: Boolean,
    onInputChange: (String) -> Unit,
    onSendMessage: () -> Unit
) {
    val canSend = inputText.trim().isNotEmpty() && !isLoading

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding(),
        color = Color.Transparent
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            shape = RoundedCornerShape(26.dp),
            color = Color.White,
            shadowElevation = 10.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = onInputChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            text = "Nhập món bạn muốn tìm...",
                            color = TextSecondary
                        )
                    },
                    shape = RoundedCornerShape(22.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FastDashBlue,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color(0xFFF8FBFF),
                        unfocusedContainerColor = Color(0xFFF8FBFF),
                        cursorColor = FastDashBlue
                    )
                )

                Surface(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .clickable(enabled = canSend) { onSendMessage() },
                    color = if (canSend) FastDashRed else Color(0xFFE3E7ED)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Gửi",
                            tint = if (canSend) Color.White else TextSecondary
                        )
                    }
                }
            }
        }
    }
}

private fun intentBadge(intent: String?): String {
    return when (intent.orEmpty().trim().uppercase()) {
        "FOOD_RECOMMENDATION" -> "Gợi ý món"
        "COMBO_RECOMMENDATION" -> "Combo"
        "ORDER_GUIDE" -> "Đặt hàng"
        "PAYMENT_GUIDE" -> "Thanh toán"
        else -> "Trợ lý"
    }
}
