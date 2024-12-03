package com.google.ar.core.examples.kotlin.ui.theme.screens

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat.startActivity
import com.example.augment_ed.ui.theme.AugmentEDTheme
import com.google.ar.core.examples.kotlin.helloar.HelloArActivity
import com.google.ar.core.examples.kotlin.helloar.R
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AugmentEDTheme {
                MainScreen(
                    isArSupported = true,
                    sensorX = 0f,
                    sensorY = 0f
                )
            }
        }
    }
}

val MinecraftFontFamily = FontFamily(
    Font(R.font.minecraftregular, FontWeight.Normal),
    Font(R.font.minecraftbold, FontWeight.Bold)
)

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    isArSupported: Boolean,
    sensorX: Float,
    sensorY: Float
) {
    val context = LocalContext.current

    val infiniteTransition = rememberInfiniteTransition()
    val color1 by infiniteTransition.animateColor(
        initialValue = Color(0xFF0D1B2A),
        targetValue = Color(0xFF1B263B),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val color2 by infiniteTransition.animateColor(
        initialValue = Color(0xFF1B263B),
        targetValue = Color(0xFF415A77),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )
    val color3 by infiniteTransition.animateColor(
        initialValue = Color(0xFF415A77),
        targetValue = Color(0xFF778DA9),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(color1, color2, color3)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Augment-ED",
                fontSize = 45.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFD4AF37),
                fontFamily = MinecraftFontFamily
            )
            Text(
                text = "Welcome!",
                fontSize = 24.sp,
                color = Color.White,
                fontFamily = MinecraftFontFamily,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            AnimatedMaterialIconButton(
                text = "Scan",
                icon = Icons.Filled.QrCodeScanner,
                onClick = {
                    // Trigger AR Scan
                    val intent = Intent(context, HelloArActivity::class.java)
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            AnimatedMaterialIconButton(
                text = "Practice",
                icon = Icons.Filled.School,
                onClick = {
                    // Handle Practice button click
                }
            )
        }
    }
}

@Composable
fun AnimatedMaterialIconButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier.size(120.dp),
        colors = IconButtonDefaults.filledTonalIconButtonColors(
            containerColor = Color(0xFFD4AF37)
        )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = text, tint = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = text, color = Color.White)
        }
    }
}