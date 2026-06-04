package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TealPrimary
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.TealLight
import com.example.ui.theme.TealDark

enum class LogoVariant {
    ICON_ONLY,          // Compact icon version (just book and star)
    FULL_LOGO,          // Full central branding with slogan (for Splash screen)
    BRANDING,           // High fidelity pulsing branding (for Authentication screen)
    HORIZONTAL_ADMIN,   // Row variant for Admin Console with Admin badging
    COMPACT_DRAWER      // Sleek sidebar layout for Drawer / Wide Sidebar
}

@Composable
fun MyReaderProLogo(
    modifier: Modifier = Modifier,
    scale: Float = 1f,
    sloganVisible: Boolean = true,
    variant: LogoVariant = LogoVariant.FULL_LOGO
) {
    when (variant) {
        LogoVariant.ICON_ONLY -> {
            LogoIconGraphic(scale = scale)
        }
        
        LogoVariant.FULL_LOGO -> {
            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                LogoIconGraphic(scale = scale)
                
                Spacer(modifier = Modifier.height((8 * scale).dp))
                
                // Multi-colored Typography brand header
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My",
                        fontSize = (25 * scale).sp,
                        fontWeight = FontWeight.Black,
                        color = TealPrimary
                    )
                    Text(
                        text = "Reader",
                        fontSize = (25 * scale).sp,
                        fontWeight = FontWeight.Black,
                        color = TealPrimary
                    )
                    Text(
                        text = "Pro",
                        fontSize = (25 * scale).sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = OrangeAccent,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
                
                if (sloganVisible) {
                    Spacer(modifier = Modifier.height((4 * scale).dp))
                    Text(
                        text = "READ MORE • KNOW MORE • GROW MORE",
                        fontSize = (8 * scale).sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.LightGray,
                        letterSpacing = 1.2.sp
                    )
                }
            }
        }
        
        LogoVariant.BRANDING -> {
            // Elegant pulsing animation of the shining star
            val infiniteTransition = rememberInfiniteTransition(label = "shining_star")
            val starScale by infiniteTransition.animateFloat(
                initialValue = 0.9f,
                targetValue = 1.15f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "star_pulse"
            )
            val glowAlpha by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 0.95f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "star_glow"
            )

            Column(
                modifier = modifier,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Glow effect box holding the graphic
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(4.dp)
                ) {
                    // Golden background glow
                    Box(
                        modifier = Modifier
                            .size((68 * scale).dp)
                            .scale(starScale)
                            .alpha(glowAlpha * 0.15f)
                            .background(OrangeAccent, shape = CircleShape)
                    )
                    
                    LogoIconGraphic(scale = scale * 1.05f)
                }
                
                Spacer(modifier = Modifier.height((10 * scale).dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My",
                        fontSize = (28 * scale).sp,
                        fontWeight = FontWeight.Black,
                        color = TealLight
                    )
                    Text(
                        text = "Reader",
                        fontSize = (28 * scale).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Pro",
                        fontSize = (28 * scale).sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = OrangeAccent,
                        modifier = Modifier.padding(start = 3.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height((4 * scale).dp))
                
                Text(
                    text = "PREMIUM DIGITAL READING ENGINE",
                    fontSize = (9 * scale).sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.6f),
                    letterSpacing = 1.6.sp
                )
            }
        }
        
        LogoVariant.HORIZONTAL_ADMIN -> {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size((38 * scale).dp)
                        .background(TealDark.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    LogoIconGraphic(scale = scale * 0.5f)
                }
                
                Spacer(modifier = Modifier.width(10.dp))
                
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "MyReaderPro",
                            fontSize = (15 * scale).sp,
                            fontWeight = FontWeight.Black,
                            color = OrangeAccent
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(OrangeAccent.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "ADMIN",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = OrangeAccent
                            )
                        }
                    }
                    Text(
                        text = "Simulation Command Suite",
                        fontSize = (10 * scale).sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                }
            }
        }
        
        LogoVariant.COMPACT_DRAWER -> {
            Row(
                modifier = modifier,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size((36 * scale).dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(TealPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    LogoIconGraphic(scale = scale * 0.45f)
                }
                
                Spacer(modifier = Modifier.width(10.dp))
                
                Column {
                    Text(
                        text = "MyReaderPro",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = (16 * scale).sp,
                        color = TealPrimary
                    )
                    Text(
                        text = "Enterprise Reader",
                        fontSize = (9 * scale).sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun LogoIconGraphic(
    scale: Float = 1f
) {
    Box(
        modifier = Modifier.size((80 * scale).dp),
        contentAlignment = Alignment.Center
    ) {
        // Star shining at the center-top
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = OrangeAccent,
            modifier = Modifier
                .size((28 * scale).dp)
                .offset(y = (-20 * scale).dp)
        )

        // Canvas drawing the open double-curve book representing pages fanning out
        Canvas(
            modifier = Modifier
                .size((64 * scale).dp)
                .offset(y = (6 * scale).dp)
        ) {
            val width = size.width
            val height = size.height

            // Draw book pages pathway
            val leftWing = Path().apply {
                moveTo(width * 0.1f, height * 0.5f)
                cubicTo(
                    width * 0.2f, height * 0.15f,
                    width * 0.45f, height * 0.2f,
                    width * 0.5f, height * 0.55f
                )
                lineTo(width * 0.5f, height * 0.85f)
                cubicTo(
                    width * 0.45f, height * 0.5f,
                    width * 0.2f, height * 0.45f,
                    width * 0.1f, height * 0.8f
                )
                close()
            }

            val rightWing = Path().apply {
                moveTo(width * 0.9f, height * 0.5f)
                cubicTo(
                    width * 0.8f, height * 0.15f,
                    width * 0.55f, height * 0.2f,
                    width * 0.5f, height * 0.55f
                )
                lineTo(width * 0.5f, height * 0.85f)
                cubicTo(
                    width * 0.55f, height * 0.5f,
                    width * 0.8f, height * 0.45f,
                    width * 0.9f, height * 0.8f
                )
                close()
            }

            // Teal theme colors
            drawPath(
                path = leftWing,
                color = TealPrimary
            )
            drawPath(
                path = rightWing,
                color = TealPrimary.copy(alpha = 0.85f)
            )

            // Gold central page line representing spine/bookmark fanning down
            val spinePath = Path().apply {
                moveTo(width * 0.48f, height * 0.45f)
                lineTo(width * 0.52f, height * 0.45f)
                lineTo(width * 0.52f, height * 0.93f)
                lineTo(width * 0.50f, height * 0.97f)
                lineTo(width * 0.48f, height * 0.93f)
                close()
            }
            drawPath(
                path = spinePath,
                color = OrangeAccent
            )
        }
    }
}
