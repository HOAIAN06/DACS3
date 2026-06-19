package com.fastdash.app.ui.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val FastDashCanvas = Color(0xFFF7FAFF)
val FastDashSurface = Color(0xFFFFFFFF)
val FastDashSurfaceAlt = Color(0xFFFFFFFF)
val FastDashInk = Color(0xFF18202A)
val FastDashMuted = Color(0xFF6B7280)
val FastDashRed = Color(0xFFD95C34)
val FastDashRedDark = Color(0xFFB3261E)
val FastDashOrange = Color(0xFFE53935)
val FastDashOlive = Color(0xFF48633D)
val FastDashBlue = Color(0xFF1976D2)
val FastDashLine = Color(0xFFE3ECF8)
val FastDashWhite = Color(0xFFFFFFFF)
val FastDashBg = Color(0xFFF7FAFF)
val FastDashBlobA = Color(0xFFEAF2FE)
val FastDashBlobB = Color(0xFFFCEBEC)
val FastDashBlobC = Color(0xFFF2F7FF)
val FastDashBrandLight = Color(0xFF1976D2)
val FastDashBrandGold = Color(0xFF4A90E2)
val FastDashBrandAmber = Color(0xFFE53935)

@Composable
fun FastDashTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: () -> Unit = {},
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            singleLine = true,
            shape = RoundedCornerShape(22.dp),
            placeholder = {
                Text(
                    text = placeholder,
                    color = FastDashMuted,
                    fontSize = 15.sp
                )
            },
            leadingIcon = {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFF4F8FD)
                ) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = FastDashRed,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = onPasswordToggle) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null,
                            tint = FastDashMuted
                        )
                    }
                }
            } else {
                null
            },
            visualTransformation = if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            keyboardOptions = keyboardOptions,
            isError = isError,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = FastDashWhite,
                unfocusedContainerColor = FastDashSurfaceAlt,
                errorContainerColor = FastDashSurfaceAlt,
                focusedBorderColor = FastDashBlue,
                unfocusedBorderColor = FastDashLine,
                errorBorderColor = FastDashRed,
                focusedTextColor = FastDashInk,
                unfocusedTextColor = FastDashInk,
                cursorColor = FastDashRed
            )
        )

        if (isError && !errorMessage.isNullOrBlank()) {
            Text(
                text = errorMessage,
                color = FastDashRedDark,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 10.dp, top = 6.dp)
            )
        }
    }
}

@Composable
fun FastDashPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp)
            .drawBehind {
                drawCircle(
                    color = FastDashOrange.copy(alpha = 0.12f),
                    radius = size.width / 2f,
                    center = center
                )
            },
        shape = RoundedCornerShape(999.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFE53935),
            disabledContainerColor = Color(0xFFE53935).copy(alpha = 0.45f),
            contentColor = FastDashWhite
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = FastDashWhite,
                strokeWidth = 2.5.dp
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.2.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun FastDashSocialButton(
    text: String,
    icon: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.height(54.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = FastDashWhite,
            contentColor = FastDashInk
        ),
        border = BorderStroke(1.dp, FastDashLine)
    ) {
        androidx.compose.foundation.Image(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun FastDashHeroTitle(
    modifier: Modifier = Modifier
) {
    val brandShadow = Shadow(
        color = Color.Black.copy(alpha = 0.42f),
        offset = androidx.compose.ui.geometry.Offset(0f, 7f),
        blurRadius = 24f
    )
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .drawBehind {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                FastDashBlue.copy(alpha = 0.18f),
                                FastDashRed.copy(alpha = 0.10f),
                                Color.Black.copy(alpha = 0.14f),
                                Color.Transparent
                            )
                        ),
                        radius = size.minDimension,
                        center = center
                    )
                }
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.28f),
                            Color.Transparent
                        ),
                        radius = 460f
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(horizontal = 34.dp, vertical = 22.dp)
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(
                        TextStyle(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    FastDashBlue,
                                    Color(0xFF4A90E2),
                                    FastDashRed
                                )
                            ),
                            shadow = brandShadow
                        ).toSpanStyle()
                    ) {
                        append("FastDash")
                    }
                },
                fontSize = 41.sp,
                lineHeight = 43.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.7.sp,
                fontFamily = FontFamily.Serif
            )
        }
    }
}

@Composable
fun AuthBackgroundDecoration(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.drawBehind {
            drawCircle(
                color = FastDashBlobA.copy(alpha = 0.55f),
                radius = size.width * 0.42f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.86f, size.height * 0.68f)
            )
            drawCircle(
                color = FastDashBlobB.copy(alpha = 0.42f),
                radius = size.width * 0.34f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.16f, size.height * 0.82f)
            )
            drawCircle(
                color = FastDashBlobC.copy(alpha = 0.32f),
                radius = size.width * 0.24f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.72f, size.height * 0.24f)
            )
        }
    )
}

@Composable
fun FastDashAuthCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        color = FastDashSurface,
        shadowElevation = 12.dp,
        border = BorderStroke(1.dp, FastDashLine)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            content = content
        )
    }
}

@Composable
fun AuthBackButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = FastDashWhite.copy(alpha = 0.92f),
        shadowElevation = 8.dp
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = FastDashInk
            )
        }
    }
}

@Composable
fun AuthScaffold(
    heroImage: Int,
    logo: Int,
    title: String,
    subtitle: String? = null,
    heroHeight: Dp = 280.dp,
    overlapHeight: Dp = 32.dp,
    titleTopSpacing: Dp = 24.dp,
    chipText: String? = null,
    topBar: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(FastDashBg, Color(0xFFFFF8F1), FastDashBg)
                )
            )
    ) {
        AuthBackgroundDecoration(modifier = Modifier.fillMaxSize())

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(heroHeight)
                .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
        ) {
            androidx.compose.foundation.Image(
                painter = painterResource(id = heroImage),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                FastDashBlue.copy(alpha = 0.20f),
                                FastDashRed.copy(alpha = 0.14f),
                                Color.Black.copy(alpha = 0.10f),
                                FastDashBg.copy(alpha = 0.94f)
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                FastDashWhite.copy(alpha = 0.04f),
                                FastDashWhite.copy(alpha = 0.52f),
                                FastDashSurface
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                topBar?.invoke()

                FastDashHeroTitle(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(top = 18.dp)
                        .padding(horizontal = 24.dp)
                )
            }
        }

        FastDashAuthCard(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxSize()
                .padding(top = heroHeight - overlapHeight)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(titleTopSpacing))
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(FastDashLine)
                )
                Spacer(modifier = Modifier.height(18.dp))
                Text(
                    text = title,
                    color = FastDashInk,
                    fontSize = 27.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Serif
                )
                if (!subtitle.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = subtitle,
                        color = FastDashMuted,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(22.dp))
                content()
                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }
}
