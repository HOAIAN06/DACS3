package com.fastdash.app.ui.order

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fastdash.app.utils.ImageUtils

private val FastDashReviewRed = Color(0xFFE31837)
private val ReviewBackground = Color(0xFFF7F7F7)
private val ReviewSurface = Color.White
private val ReviewTextPrimary = Color(0xFF1F2937)
private val ReviewTextSecondary = Color(0xFF6B7280)
private val ReviewChipBackground = Color(0xFFFDECEF)
private val ReviewStar = Color(0xFFF4A622)

private val quickReviewChips = listOf(
    "Ngon miệng",
    "Còn nóng",
    "Giao nhanh",
    "Đóng gói tốt",
    "Sẽ đặt lại"
)

data class ReviewEditorUiModel(
    val orderId: Long,
    val productId: Long,
    val productName: String,
    val productImageUrl: String,
    val initialRating: Int = 0,
    val initialComment: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteReviewScreen(
    reviewItem: ReviewEditorUiModel,
    submitting: Boolean,
    submitSuccess: Boolean,
    submitMessage: String?,
    errorMessage: String?,
    onBack: () -> Unit,
    onDismissSuccess: () -> Unit,
    onSubmit: (Long, Long, Int, String) -> Unit
) {
    var rating by remember(reviewItem.orderId, reviewItem.productId) {
        mutableIntStateOf(reviewItem.initialRating.coerceIn(0, 5))
    }
    var comment by remember(reviewItem.orderId, reviewItem.productId) {
        mutableStateOf(reviewItem.initialComment.take(1000))
    }

    fun toggleQuickComment(chip: String) {
        val currentItems = comment
            .split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toMutableList()

        val exists = currentItems.any { it.equals(chip, ignoreCase = true) }

        if (exists) {
            comment = currentItems.filterNot { it.equals(chip, ignoreCase = true) }.joinToString(", ")
        } else {
            val newText = (currentItems + chip).joinToString(", ")
            if (newText.length <= 1000) {
                comment = newText
            }
        }
    }

    if (submitSuccess) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Đánh giá thành công") },
            text = { Text(submitMessage ?: "Cảm ơn bạn đã đánh giá món ăn") },
            confirmButton = {
                Button(
                    onClick = onDismissSuccess,
                    colors = ButtonDefaults.buttonColors(containerColor = FastDashReviewRed)
                ) {
                    Text("Đóng")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Viết đánh giá", fontWeight = FontWeight.Bold, color = ReviewTextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = ReviewTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ReviewSurface)
            )
        },
        containerColor = ReviewBackground,
        bottomBar = {
            Surface(color = ReviewSurface, shadowElevation = 12.dp) {
                Button(
                    onClick = {
                        onSubmit(reviewItem.orderId, reviewItem.productId, rating, comment.trim())
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                    enabled = rating > 0 && !submitting && comment.length <= 1000,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FastDashReviewRed,
                        disabledContainerColor = Color(0xFFF0B9C2)
                    )
                ) {
                    if (submitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Gửi đánh giá", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = ReviewSurface)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ReviewProductImage(imageUrl = reviewItem.productImageUrl, productName = reviewItem.productName)
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(reviewItem.productName, color = ReviewTextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("Hãy chia sẻ trải nghiệm của bạn về món ăn này", color = ReviewTextSecondary, fontSize = 13.sp)
                        }
                    }
                }
            }
            item {
                Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = ReviewSurface)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Đánh giá của bạn", color = ReviewTextPrimary, fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            repeat(5) { index ->
                                val star = index + 1
                                Box(modifier = Modifier.clickable { rating = star }) {
                                    Icon(
                                        imageVector = if (star <= rating) Icons.Filled.Star else Icons.Outlined.StarOutline,
                                        contentDescription = null,
                                        tint = if (star <= rating) ReviewStar else Color(0xFFD1D5DB),
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = ratingLabel(rating),
                            color = if (rating > 0) FastDashReviewRed else ReviewTextSecondary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            item {
                Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = ReviewSurface)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Cảm nhận nhanh", color = ReviewTextPrimary, fontWeight = FontWeight.SemiBold)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(quickReviewChips) { chip ->
                                val selected = comment
                                    .split(",")
                                    .map { it.trim() }
                                    .any { it.equals(chip, ignoreCase = true) }
                                FilterChip(
                                    selected = selected,
                                    onClick = { toggleQuickComment(chip) },
                                    label = { Text(chip) },
                                    border = BorderStroke(1.dp, if (selected) FastDashReviewRed else Color(0xFFE5E7EB)),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = ReviewChipBackground,
                                        selectedLabelColor = FastDashReviewRed,
                                        containerColor = Color.White,
                                        labelColor = ReviewTextPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }
            item {
                Card(shape = RoundedCornerShape(18.dp), colors = CardDefaults.cardColors(containerColor = ReviewSurface)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Bình luận", color = ReviewTextPrimary, fontWeight = FontWeight.SemiBold)
                        OutlinedTextField(
                            value = comment,
                            onValueChange = { newValue ->
                                if (newValue.length <= 1000) {
                                    comment = newValue
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 5,
                            maxLines = 8,
                            placeholder = { Text("Món ăn có hợp khẩu vị của bạn không?") },
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = FastDashReviewRed,
                                focusedLabelColor = FastDashReviewRed,
                                cursorColor = FastDashReviewRed,
                                unfocusedBorderColor = Color(0xFFE5E7EB)
                            )
                        )
                        Text(
                            text = "${comment.length}/1000 ký tự",
                            color = ReviewTextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.End
                        )
                        when {
                            rating == 0 -> Text("Vui lòng chọn số sao trước khi gửi đánh giá", color = ReviewTextSecondary, fontSize = 13.sp)
                            comment.length > 1000 -> Text("Bình luận không được vượt quá 1000 ký tự", color = FastDashReviewRed, fontSize = 13.sp)
                            errorMessage != null -> Text(errorMessage, color = FastDashReviewRed, fontSize = 13.sp)
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun ReviewProductImage(imageUrl: String, productName: String) {
    if (imageUrl.isBlank()) {
        Box(
            modifier = Modifier
                .size(84.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(FastDashReviewRed.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = productName.take(1).uppercase(),
                color = FastDashReviewRed,
                fontWeight = FontWeight.Black,
                fontSize = 28.sp
            )
        }
    } else {
        AsyncImage(
            model = ImageUtils.buildImageRequest(LocalContext.current, imageUrl),
            contentDescription = productName,
            modifier = Modifier
                .size(84.dp)
                .clip(RoundedCornerShape(14.dp)),
            contentScale = ContentScale.Crop
        )
    }
}

private fun ratingLabel(rating: Int): String = when (rating) {
    1 -> "Rất tệ"
    2 -> "Chưa hài lòng"
    3 -> "Tạm ổn"
    4 -> "Rất ngon"
    5 -> "Tuyệt vời"
    else -> "Chọn số sao để đánh giá"
}
