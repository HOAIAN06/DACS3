package com.fastdash.app.ui.product

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fastdash.app.data.model.response.ProductResponse
import com.fastdash.app.data.model.response.ProductSizeResponse
import com.fastdash.app.data.model.response.ReviewResponse
import com.fastdash.app.data.model.response.ToppingResponse
import com.fastdash.app.utils.CurrencyUtils
import com.fastdash.app.utils.ImageUtils

import java.util.Locale

private val FastDashRed = Color(0xFFD6092F)
private val FastDashRedDark = Color(0xFF9B0622)
private val BackgroundGrey = Color(0xFFFAFAFA)
private val SurfaceWhite = Color.White
private val PrimaryBlack = Color(0xFF1F1F1F)
private val TextGrey = Color(0xFF777777)
private val BorderGrey = Color(0xFFEAEAEA)
private val SoftPink = Color(0xFFFFF3F6)
private val WarmChip = Color(0xFFFFF5DB)

private const val LABEL_BEST_SELLER = "Bán chạy"
private const val LABEL_FLEX = "Tuỳ chọn linh hoạt"
private const val LABEL_BACK = "Quay lại"
private const val LABEL_SHARE = "Chia sẻ"
private const val LABEL_FAVORITE = "Yêu thích"
private const val OPTION_EMPTY_MESSAGE = "Món này hiện chưa có thêm tuỳ chọn. Bạn vẫn có thể đặt nhanh ngay bên dưới."

@Composable
fun ProductDetailScreen(
    product: ProductResponse?,
    sizes: List<ProductSizeResponse> = emptyList(),
    toppings: List<ToppingResponse> = emptyList(),
    initialQuantity: Int = 1,
    initialSizeId: Long? = null,
    initialToppingIds: List<Long> = emptyList(),
    initialSelectedToppingNames: List<String> = emptyList(),
    initialNote: String = "",
    isLoading: Boolean = false,
    reviews: List<ReviewResponse> = emptyList(),
    onBack: () -> Unit,
    onAddToCart: (Long, Int, Long?, List<Long>, String?) -> Unit,
    onBuyNow: (Long, Int, Long?, List<Long>, String?, Double) -> Unit
) {
    if (product == null) {
        ProductEmptyState(onBack)
        return
    }

    var selectedSizeId by remember(product.id, initialSizeId) { mutableStateOf(initialSizeId) }
    var quantity by remember(product.id, initialQuantity) { mutableIntStateOf(initialQuantity.coerceAtLeast(1)) }
    val selectedToppings = remember(product.id, initialToppingIds) {
        mutableStateListOf<Long>().apply { addAll(initialToppingIds.distinct()) }
    }
    val rememberedToppingNames = remember(product.id, initialSelectedToppingNames) {
        initialSelectedToppingNames
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }
    var note by remember(product.id, initialNote) { mutableStateOf(initialNote) }

    LaunchedEffect(product.id, sizes, initialSizeId) {
        if (sizes.isEmpty()) return@LaunchedEffect
        val hasSelectedSize = selectedSizeId != null && sizes.any { it.id == selectedSizeId }
        if (!hasSelectedSize) {
            selectedSizeId = initialSizeId?.takeIf { initial -> sizes.any { it.id == initial } }
                ?: sizes.firstOrNull()?.id
        }
    }

    LaunchedEffect(product.id, toppings) {
        if (toppings.isEmpty()) return@LaunchedEffect
        val validToppingIds = toppings.map { it.id }.toSet()
        val sanitizedIds = selectedToppings.filter { it in validToppingIds }
        if (sanitizedIds.size != selectedToppings.size) {
            selectedToppings.clear()
            selectedToppings.addAll(sanitizedIds)
        }
    }

    val unitPrice = sizes.firstOrNull { it.id == selectedSizeId }?.price ?: product.basePrice
    val resolvedToppingIds = selectedToppings.filter { selectedId -> toppings.any { it.id == selectedId } }
    val resolvedSelectedToppingNames = toppings
        .filter { resolvedToppingIds.contains(it.id) }
        .mapNotNull { it.name?.trim()?.takeIf { name -> name.isNotBlank() } }
    val unavailableSelectedToppingNames = rememberedToppingNames
        .filterNot { selectedName -> resolvedSelectedToppingNames.any { it.equals(selectedName, ignoreCase = true) } }
    val toppingsPrice = toppings.filter { resolvedToppingIds.contains(it.id) }.sumOf { it.price }
    val totalPrice = (unitPrice + toppingsPrice) * quantity
    val badgeLabel = if (product.isCustomizable == 1) LABEL_FLEX else LABEL_BEST_SELLER

    Box(modifier = Modifier.fillMaxSize().background(BackgroundGrey)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 206.dp)
        ) {
            item {
                ProductHeroImage(product = product, isLoading = isLoading) {
                    ProductDetailTopBar(onBack = onBack)
                }
            }
            item {
                ProductInfoCard(
                    product = product,
                    displayPrice = unitPrice,
                    badgeLabel = badgeLabel
                )
            }
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (sizes.isNotEmpty()) {
                        ProductSizeSection(
                            sizes = sizes,
                            selectedSizeId = selectedSizeId,
                            onSelectSize = { selectedSizeId = it }
                        )
                    }
                    if (toppings.isNotEmpty()) {
                        ProductToppingSection(
                            toppings = toppings,
                            selectedToppings = selectedToppings,
                            onToggleTopping = {
                                if (selectedToppings.contains(it)) selectedToppings.remove(it) else selectedToppings.add(it)
                            }
                        )
                    }
                    if (rememberedToppingNames.isNotEmpty()) {
                        SelectedToppingsSummaryCard(
                            selectedNames = resolvedSelectedToppingNames,
                            unavailableNames = unavailableSelectedToppingNames
                        )
                    }
                    ProductNoteSection(note = note, onNoteChanged = { note = it })
                    if (sizes.isEmpty() && toppings.isEmpty()) {
                        EmptyOptionCard()
                    }
                }
            }
            if (reviews.isNotEmpty()) {
                item {
                    ProductReviewSection(reviews = reviews)
                }
            }
        }

        ProductBottomActionBar(
            quantity = quantity,
            totalPrice = totalPrice,
            onDecrease = { if (quantity > 1) quantity-- },
            onIncrease = { quantity++ },
            onBuyNowClick = { onBuyNow(product.id, quantity, selectedSizeId, resolvedToppingIds, note.takeIf { it.isNotBlank() }, totalPrice) },
            onAddToCartClick = { onAddToCart(product.id, quantity, selectedSizeId, resolvedToppingIds, note.takeIf { it.isNotBlank() }) }
        )
    }
}

@Composable
private fun SelectedToppingsSummaryCard(
    selectedNames: List<String>,
    unavailableNames: List<String>
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Topping da chon",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = PrimaryBlack
            )
            if (selectedNames.isNotEmpty()) {
                Text(
                    text = selectedNames.joinToString(", "),
                    fontSize = 13.sp,
                    color = PrimaryBlack,
                    lineHeight = 18.sp
                )
            }
            if (unavailableNames.isNotEmpty()) {
                Text(
                    text = "Khong con ap dung: ${unavailableNames.joinToString(", ")}",
                    fontSize = 12.sp,
                    color = FastDashRed,
                    lineHeight = 17.sp
                )
                Text(
                    text = "Ban can chon lai topping nay truoc khi cap nhat gio hang.",
                    fontSize = 11.sp,
                    color = TextGrey,
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}

@Composable
private fun ProductDetailTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProductTopActionButton(icon = Icons.AutoMirrored.Filled.ArrowBack, description = LABEL_BACK, onClick = onBack)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ProductTopActionButton(icon = Icons.Filled.Share, description = LABEL_SHARE, onClick = {})
            ProductTopActionButton(icon = Icons.Filled.FavoriteBorder, description = LABEL_FAVORITE, onClick = {})
        }
    }
}

@Composable
private fun ProductTopActionButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(48.dp)
            .clickable { onClick() },
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.24f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = description, tint = Color.White)
        }
    }
}

@Composable
private fun ProductHeroImage(product: ProductResponse, isLoading: Boolean, overlayContent: @Composable BoxScope.() -> Unit) {
    Box(modifier = Modifier.fillMaxWidth().height(328.dp)) {
        if (product.imageUrl.isNullOrBlank()) {
            Box(
                modifier = Modifier.fillMaxSize().background(Brush.linearGradient(listOf(FastDashRed, FastDashRedDark))),
                contentAlignment = Alignment.Center
            ) {
                Text(product.categoryName.ifBlank { "FastDash" }, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            }
        } else {
            AsyncImage(
                model = ImageUtils.buildImageRequest(LocalContext.current, product.imageUrl),
                contentDescription = product.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.32f), Color.Transparent, Color.Transparent, BackgroundGrey)
                )
            )
        )
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
        }
        overlayContent()
    }
}

@Composable
private fun ProductInfoCard(product: ProductResponse, displayPrice: Double, badgeLabel: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(top = 0.dp),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ProductBadgeRow(
                badgeLabel = badgeLabel,
                averageRating = product.averageRating ?: 0.0,
                reviewCount = product.reviewCount ?: 0
            )
            Text(
                text = product.name,
                fontSize = 29.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PrimaryBlack,
                lineHeight = 35.sp
            )
            product.description?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    fontSize = 14.sp,
                    color = TextGrey,
                    lineHeight = 21.sp
                )
            }
            Text(
                text = CurrencyUtils.formatVnd(displayPrice),
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = FastDashRed
            )
            product.categoryName.takeIf { it.isNotBlank() }?.let {
                Text(text = it, fontSize = 13.sp, color = TextGrey)
            }
        }
    }
}

@Composable
private fun ProductBadgeRow(badgeLabel: String, averageRating: Double, reviewCount: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(color = SoftPink, shape = RoundedCornerShape(50)) {
            Text(
                text = badgeLabel,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                color = FastDashRed,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
        Surface(color = WarmChip, shape = RoundedCornerShape(50)) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFF4A622), modifier = Modifier.size(14.dp))
                val ratingText = if (reviewCount > 0) String.format(Locale.getDefault(), "%.1f (%d)", averageRating, reviewCount) else "Chưa có đánh giá"
                Text(ratingText, color = PrimaryBlack, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ProductSizeSection(sizes: List<ProductSizeResponse>, selectedSizeId: Long?, onSelectSize: (Long?) -> Unit) {
    OptionSectionCard(title = "Chọn size") {
        sizes.forEach { size ->
            OptionRow(
                isSelected = selectedSizeId == size.id,
                title = size.sizeName?.takeIf { it.isNotBlank() } ?: "Kích thước tiêu chuẩn",
                price = CurrencyUtils.formatVnd(size.price),
                onClick = { onSelectSize(size.id) }
            ) {
                RadioButton(
                    selected = selectedSizeId == size.id,
                    onClick = { onSelectSize(size.id) },
                    colors = RadioButtonDefaults.colors(selectedColor = FastDashRed)
                )
            }
        }
    }
}

@Composable
private fun ProductToppingSection(toppings: List<ToppingResponse>, selectedToppings: List<Long>, onToggleTopping: (Long) -> Unit) {
    OptionSectionCard(title = "Chọn topping") {
        toppings.forEach { topping ->
            ToppingRow(
                isSelected = selectedToppings.contains(topping.id),
                title = topping.name?.takeIf { it.isNotBlank() } ?: "Topping yêu thích",
                price = "+ ${CurrencyUtils.formatVnd(topping.price)}",
                onClick = { onToggleTopping(topping.id) },
                onCheckedChange = { onToggleTopping(topping.id) }
            )
        }
    }
}

@Composable
private fun ProductNoteSection(note: String, onNoteChanged: (String) -> Unit) {
    var expanded by remember(note) { mutableStateOf(note.isNotBlank()) }
    val quickNotes = listOf("Ít cay", "Thêm tương cà", "Không hành", "Cắt nhỏ", "Giao nóng")

    fun appendQuickNote(chip: String) {
        val currentItems = note.split(",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
        val alreadyExists = currentItems.any { it.equals(chip, ignoreCase = true) }
        if (!alreadyExists) {
            onNoteChanged((currentItems + chip).joinToString(", "))
        }
        expanded = true
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = RoundedCornerShape(10.dp),
                        color = FastDashRed.copy(alpha = 0.08f)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Outlined.EditNote, contentDescription = null, tint = FastDashRed, modifier = Modifier.size(17.dp))
                        }
                    }
                    Text("Ghi chú cho cửa hàng", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = PrimaryBlack)
                }
                Icon(
                    imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextGrey
                )
            }

            if (expanded) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(quickNotes) { chip ->
                        val selected = note.split(",").map { it.trim() }.any { it.equals(chip, ignoreCase = true) }
                        Surface(
                            modifier = Modifier.clickable { appendQuickNote(chip) },
                            shape = RoundedCornerShape(50),
                            color = if (selected) SoftPink else Color(0xFFFAFAFA),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) FastDashRed.copy(alpha = 0.25f) else BorderGrey)
                        ) {
                            Text(
                                text = chip,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                color = if (selected) FastDashRed else PrimaryBlack,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = onNoteChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Ví dụ: ít cay, thêm tương cà, không hành...", color = TextGrey) },
                    minLines = 2,
                    maxLines = 3,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = FastDashRed,
                        unfocusedBorderColor = BorderGrey,
                        focusedContainerColor = Color(0xFFFAFAFA),
                        unfocusedContainerColor = Color(0xFFFAFAFA),
                        cursorColor = FastDashRed
                    )
                )
            }
        }
    }
}

@Composable
private fun OptionSectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = PrimaryBlack)
            content()
        }
    }
}

@Composable
private fun OptionRow(
    isSelected: Boolean,
    title: String,
    price: String,
    onClick: () -> Unit,
    selector: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) SoftPink else Color(0xFFFDFDFD),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) FastDashRed.copy(alpha = 0.45f) else BorderGrey)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            selector()
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PrimaryBlack)
            }
            Text(price, color = FastDashRed, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun ToppingRow(
    isSelected: Boolean,
    title: String,
    price: String,
    onClick: () -> Unit,
    onCheckedChange: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) SoftPink else Color(0xFFFDFDFD),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) FastDashRed.copy(alpha = 0.45f) else BorderGrey)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onCheckedChange() },
                colors = CheckboxDefaults.colors(checkedColor = FastDashRed)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = PrimaryBlack)
            }
            Text(price, color = FastDashRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun EmptyOptionCard() {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(modifier = Modifier.size(36.dp), shape = CircleShape, color = FastDashRed.copy(alpha = 0.1f)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = FastDashRed, modifier = Modifier.size(18.dp))
                }
            }
            Text(OPTION_EMPTY_MESSAGE, color = TextGrey, lineHeight = 20.sp)
        }
    }
}

@Composable
private fun BoxScope.ProductBottomActionBar(
    quantity: Int,
    totalPrice: Double,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    onBuyNowClick: () -> Unit,
    onAddToCartClick: () -> Unit
) {
    Surface(
        modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
        color = SurfaceWhite,
        shadowElevation = 18.dp,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp).navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Tổng tạm tính", fontSize = 13.sp, color = TextGrey)
                    Text(CurrencyUtils.formatVnd(totalPrice), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = FastDashRed)
                }
                ProductQuantitySelector(quantity = quantity, onDecrease = onDecrease, onIncrease = onIncrease)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onBuyNowClick,
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlack, contentColor = Color.White)
                ) {
                    Text("Mua ngay", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = onAddToCartClick,
                    modifier = Modifier.weight(1.35f).height(54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FastDashRed, contentColor = Color.White)
                ) {
                    Text("Thêm vào giỏ", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ProductQuantitySelector(quantity: Int, onDecrease: () -> Unit, onIncrease: () -> Unit) {
    Surface(shape = RoundedCornerShape(18.dp), color = Color(0xFFF5F5F5)) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onDecrease, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Filled.Remove, contentDescription = "Giảm số lượng", tint = PrimaryBlack)
            }
            Text(quantity.toString(), modifier = Modifier.padding(horizontal = 12.dp), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = PrimaryBlack)
            IconButton(onClick = onIncrease, modifier = Modifier.size(34.dp)) {
                Icon(Icons.Filled.Add, contentDescription = "Tăng số lượng", tint = FastDashRed)
            }
        }
    }
}

@Composable
private fun ProductEmptyState(onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize().background(BackgroundGrey), contentAlignment = Alignment.Center) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = FastDashRed, modifier = Modifier.size(42.dp))
                Text("Không tìm thấy món ăn", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = PrimaryBlack)
                Text(
                    "Món bạn chọn hiện không khả dụng. Vui lòng quay lại thực đơn để chọn món khác.",
                    color = TextGrey,
                    lineHeight = 20.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(containerColor = FastDashRed, contentColor = Color.White),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text(LABEL_BACK, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ProductReviewSection(reviews: List<ReviewResponse>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Đánh giá từ khách hàng (${reviews.size})",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryBlack
        )
        reviews.forEach { review ->
            ReviewItem(review = review)
            HorizontalDivider(color = BorderGrey.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun ReviewItem(review: ReviewResponse) {
    val displayName = review.userName?.takeIf { it.isNotBlank() } ?: "Khách hàng"
    val displayDate = review.createdAt?.takeIf { it.isNotBlank() }?.take(10).orEmpty()
    val avatarUrl = review.userAvatar ?: "https://ui-avatars.com/api/?name=$displayName&background=random"

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = avatarUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
            Column {
                Text(
                    text = displayName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlack
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (index < review.rating) Color(0xFFF4A622) else Color(0xFFE0E0E0),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                    if (displayDate.isNotBlank()) {
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = displayDate,
                            fontSize = 12.sp,
                            color = TextGrey
                        )
                    }
                }
            }
        }
        review.comment?.let {
            if (it.isNotBlank()) {
                Text(
                    text = it,
                    fontSize = 14.sp,
                    color = PrimaryBlack,
                    lineHeight = 20.sp
                )
            }
        }
        if (review.images.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(review.images) { imageUrl ->
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}





