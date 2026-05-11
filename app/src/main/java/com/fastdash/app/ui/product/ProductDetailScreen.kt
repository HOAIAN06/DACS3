package com.fastdash.app.ui.product

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.fastdash.app.data.model.response.ProductResponse
import com.fastdash.app.data.model.response.ProductSizeResponse
import com.fastdash.app.data.model.response.ToppingResponse
import com.fastdash.app.utils.CurrencyUtils
import com.fastdash.app.utils.ImageUtils

private val PizzaHutRed = Color(0xFFC8102E)
private val PrimaryBlack = Color(0xFF1C1C1C)
private val LightGrey = Color(0xFFF4F4F4)
private val SurfaceWhite = Color.White

@Composable
fun ProductDetailScreen(
    product: ProductResponse,
    sizes: List<ProductSizeResponse> = emptyList(),
    toppings: List<ToppingResponse> = emptyList(),
    onBack: () -> Unit,
    onAddToCart: (productId: Long, quantity: Int, sizeName: String?, toppingIds: List<Long>) -> Unit
) {
    var selectedSize by remember { mutableStateOf<String?>(sizes.firstOrNull()?.sizeName) }
    var quantity by remember { mutableIntStateOf(1) }
    val selectedToppings = remember { mutableStateListOf<Long>() }

    val sizePrice = sizes.firstOrNull { it.sizeName == selectedSize }?.price ?: product.basePrice
    val toppingsPrice = toppings.filter { selectedToppings.contains(it.id) }.sumOf { it.price }
    val unitPrice = sizePrice + toppingsPrice
    val totalPrice = unitPrice * quantity

    Box(modifier = Modifier.fillMaxSize().background(LightGrey)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Product Image Header
            item {
                Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {
                    AsyncImage(
                        model = ImageUtils.formatImageUrl(product.imageUrl),
                        contentDescription = product.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    // Gradient overlay could be added here
                }
            }

            // Product Info Section
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                    color = SurfaceWhite,
                    tonalElevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            text = product.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = PrimaryBlack
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = product.description ?: "Không có mô tả cho sản phẩm này.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            lineHeight = 20.sp
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = CurrencyUtils.formatVnd(sizePrice),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = PizzaHutRed
                        )
                    }
                }
            }

            // Selection Sections
            if (sizes.isNotEmpty()) {
                item {
                    SectionDivider("CHỌN KÍCH THƯỚC")
                    Surface(modifier = Modifier.fillMaxWidth(), color = SurfaceWhite) {
                        Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
                            sizes.forEach { size ->
                                SizeSelectionItem(
                                    size = size,
                                    isSelected = selectedSize == size.sizeName,
                                    onClick = { selectedSize = size.sizeName }
                                )
                            }
                        }
                    }
                }
            }

            if (toppings.isNotEmpty()) {
                item {
                    SectionDivider("THÊM TOPPING")
                }
                items(toppings) { topping ->
                    ToppingSelectionItem(
                        topping = topping,
                        isSelected = selectedToppings.contains(topping.id),
                        onToggle = {
                            if (selectedToppings.contains(topping.id)) {
                                selectedToppings.remove(topping.id)
                            } else {
                                selectedToppings.add(topping.id)
                            }
                        }
                    )
                }
            }
        }

        // Floating Back Button
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .padding(16.dp)
                .size(40.dp)
                .background(Color.Black.copy(alpha = 0.3f), CircleShape)
        ) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
        }

        // Sticky Bottom Bar
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            shadowElevation = 8.dp,
            color = SurfaceWhite
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Quantity Selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(LightGrey, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    IconButton(onClick = { if (quantity > 1) quantity-- }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Remove, contentDescription = null, tint = PrimaryBlack)
                    }
                    Text(
                        text = quantity.toString(),
                        modifier = Modifier.padding(horizontal = 12.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    IconButton(onClick = { quantity++ }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryBlack)
                    }
                }

                // Add to Cart Button
                Button(
                    onClick = { onAddToCart(product.id, quantity, selectedSize, selectedToppings.toList()) },
                    modifier = Modifier.weight(1f).height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PizzaHutRed),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "THÊM VÀO GIỎ - ${CurrencyUtils.formatVnd(totalPrice)}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionDivider(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp)
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
    }
}

@Composable
private fun SizeSelectionItem(
    size: ProductSizeResponse,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(selectedColor = PizzaHutRed)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = size.sizeName,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = CurrencyUtils.formatVnd(size.price),
            fontSize = 14.sp,
            color = if (isSelected) PizzaHutRed else Color.Black
        )
    }
}

@Composable
private fun ToppingSelectionItem(
    topping: ToppingResponse,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onToggle() },
        color = SurfaceWhite
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = PizzaHutRed)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = topping.name,
                fontSize = 16.sp,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "+ ${CurrencyUtils.formatVnd(topping.price)}",
                fontSize = 14.sp,
                color = PizzaHutRed
            )
        }
    }
}
