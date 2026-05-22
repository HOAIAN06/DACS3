package com.fastdash.app.ui.payment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fastdash.app.utils.CurrencyUtils

private val FastDashRed = Color(0xFFC8102E)
private val SurfaceWhite = Color.White
private val BackgroundGrey = Color(0xFFF8F8F8)
private val TextGrey = Color(0xFF6B7280)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    amount: Double,
    onBack: () -> Unit,
    onConfirmPayment: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thanh toan", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lai")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceWhite)
            )
        },
        containerColor = BackgroundGrey
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SurfaceWhite
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Don hang da san sang!", fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                    Text(
                        "Vui long thanh toan so tien ben duoi khi nhan hang (COD).",
                        color = TextGrey,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("TONG THANH TOAN", fontSize = 12.sp, color = TextGrey, fontWeight = FontWeight.Bold)
                    Text(
                        CurrencyUtils.formatVnd(amount),
                        fontSize = 28.sp,
                        color = FastDashRed,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Button(
                onClick = onConfirmPayment,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FastDashRed)
            ) {
                Text("HOAN TAT", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            }
        }
    }
}
