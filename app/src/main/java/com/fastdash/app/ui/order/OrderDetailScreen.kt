package com.fastdash.app.ui.order

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fastdash.app.utils.CurrencyUtils

data class OrderItemUiModel(
    val id: Long,
    val name: String,
    val quantity: Int,
    val unitPrice: Double
)

data class OrderDetailUiModel(
    val id: Long,
    val orderCode: String,
    val status: String,
    val createdAt: String,
    val deliveryAddress: String,
    val shippingFee: Double,
    val items: List<OrderItemUiModel>
)

@Composable
fun OrderDetailScreen(
    order: OrderDetailUiModel,
    onBack: () -> Unit,
    onReorder: (OrderDetailUiModel) -> Unit
) {
    val subtotal = order.items.sumOf { it.unitPrice * it.quantity }
    val total = subtotal + order.shippingFee

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("<- Quay lai")
        }

        Text(
            text = "Chi tiet don ${order.orderCode}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Surface(
            tonalElevation = 1.dp,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Trang thai: ${order.status}")
                Text("Ngay dat: ${order.createdAt}")
                Text("Dia chi giao: ${order.deliveryAddress}")
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(order.items, key = { it.id }) { item ->
                Surface(
                    tonalElevation = 1.dp,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(item.name, fontWeight = FontWeight.Medium)
                            Text("SL: ${item.quantity}")
                        }
                        Text(CurrencyUtils.formatVnd(item.quantity * item.unitPrice))
                    }
                }
            }
        }

        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text("Tam tinh: ${CurrencyUtils.formatVnd(subtotal)}")
                Text("Phi giao hang: ${CurrencyUtils.formatVnd(order.shippingFee)}")
                Text(text = "Tong cong: ${CurrencyUtils.formatVnd(total)}", fontWeight = FontWeight.Bold)
            }
        }

        Button(
            onClick = { onReorder(order) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Text("Dat lai don nay")
        }
    }
}
