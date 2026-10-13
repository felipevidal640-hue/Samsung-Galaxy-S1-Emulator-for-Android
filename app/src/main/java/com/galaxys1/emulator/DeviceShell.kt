package com.galaxys1.emulator

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * The physical Galaxy S GT-I9000 wrapped around the emulated screen:
 * glossy black body, chrome trim, earpiece, SAMSUNG wordmark, front camera,
 * capacitive Menu / Back keys, the physical Home key, a side power key and
 * the volume rocker. Every hardware control is wired to the firmware.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeviceShell(
    state: EmulatorState,
    onPowerClick: () -> Unit,
    onPowerLongClick: () -> Unit,
    onMenuClick: () -> Unit,
    onHomeClick: () -> Unit,
    onHomeLongClick: () -> Unit,
    onBackClick: () -> Unit,
    onVolumeUp: () -> Unit,
    onVolumeDown: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF10182E), Color(0xFF070A14), Color(0xFF03040A))
                )
            )
    ) {
        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // Galaxy S body ratio: 64.2 x 122.4 mm  ->  w/h = 0.524
            val maxDeviceH = maxHeight * 0.90f
            val maxDeviceW = maxWidth * 0.96f
            val deviceW = minOf(maxDeviceW, maxDeviceH * 0.524f)
            val deviceH = deviceW / 0.524f
            val screenW = deviceW * 0.905f
            val screenH = deviceH * 0.775f

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .width(deviceW)
                        .height(deviceH)
                ) {
                    // ---- chrome trim + glossy black body ----
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(34.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF3A3F4A), Color(0xFF101216), Color(0xFF050608))
                                )
                            )
                            .border(1.dp, Color(0xFF9AA3B2), RoundedCornerShape(34.dp))
                    )

                    // ---- side volume rocker (left) ----
                    Column(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .offset(x = (-4).dp, y = (-deviceH * 0.16f))
                    ) {
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(40.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF565D6B))
                                .clickable { onVolumeUp() }
                        )
                        Box(
                            modifier = Modifier
                                .width(6.dp)
                                .height(40.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFF454B58))
                                .clickable { onVolumeDown() }
                        )
                    }

                    // ---- side power key (right) ----
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .offset(x = 4.dp, y = (-deviceH * 0.20f))
                            .width(6.dp)
                            .height(52.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF6A7280))
                            .combinedClickable(
                                onClick = onPowerClick,
                                onLongClick = onPowerLongClick
                            )
                    )

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // ---- top bezel: earpiece, camera, logo ----
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(deviceH * 0.095f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF232830))
                                            .border(1.dp, Color(0xFF4A5261), CircleShape)
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(56.dp)
                                            .height(7.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF1A1E26))
                                            .border(1.dp, Color(0xFF454C5A), RoundedCornerShape(4.dp))
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(9.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.radialGradient(
                                                    listOf(Color(0xFF3E6B8F), Color(0xFF0B1420))
                                                )
                                            )
                                            .border(1.dp, Color(0xFF4A5261), CircleShape)
                                    )
                                }
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    "S A M S U N G",
                                    color = Color(0xFFC7CCD6),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // ---- 4.0" Super AMOLED 480 x 800 ----
                        Box(
                            modifier = Modifier
                                .width(screenW)
                                .height(screenH)
                                .clip(RoundedCornerShape(5.dp))
                                .background(Color.Black)
                                .border(1.dp, Color(0xFF2A2F3A), RoundedCornerShape(5.dp))
                        ) {
                            content()

                            // emulated brightness
                            if ((state.power == PowerState.ON || state.power == PowerState.LOCK) && state.screenAwake) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = (1f - state.brightness) * 0.62f))
                                )
                            }

                            // glass reflection
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.linearGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.07f),
                                                Color.Transparent,
                                                Color.Transparent,
                                                Color.White.copy(alpha = 0.03f)
                                            )
                                        )
                                    )
                            )
                        }

                        // ---- bottom bezel: Menu | Home | Back ----
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val keyTint = if (state.screenAwake && state.power != PowerState.OFF)
                                Color(0xFFE8ECF3) else Color(0xFF4A505C)
                            Text(
                                "≣",
                                color = keyTint,
                                fontSize = 24.sp,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .clickable { onMenuClick() }
                            )
                            Box(
                                modifier = Modifier
                                    .width(78.dp)
                                    .height(30.dp)
                                    .clip(RoundedCornerShape(15.dp))
                                    .background(Color(0xFF0B0D11))
                                    .border(1.dp, Color(0xFF8A93A3), RoundedCornerShape(15.dp))
                                    .combinedClickable(
                                        onClick = onHomeClick,
                                        onLongClick = onHomeLongClick
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("⌂", color = keyTint, fontSize = 17.sp)
                            }
                            Text(
                                "↩",
                                color = keyTint,
                                fontSize = 22.sp,
                                modifier = Modifier
                                    .padding(12.dp)
                                    .clickable { onBackClick() }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                Text(
                    "SAMSUNG Galaxy S  ·  GT-I9000",
                    color = Color(0xFFD6DCEA),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "4.0″ Super AMOLED 480×800 · Android 2.3.6 · TouchWiz 3.0",
                    color = Color(0xFF7E8AA3),
                    fontSize = 10.5.sp
                )
                Text(
                    if (state.power == PowerState.OFF) "Press the power key on the right edge to switch on"
                    else "Power key: tap = sleep/wake · hold = phone options · hold Home = Task manager",
                    color = Color(0xFF5B6A86),
                    fontSize = 9.5.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
