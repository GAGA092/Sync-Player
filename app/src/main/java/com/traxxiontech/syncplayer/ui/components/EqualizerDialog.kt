package com.traxxiontech.syncplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.traxxiontech.syncplayer.ui.theme.LocalGlassmorphicColors

data class EqualizerPreset(
    val name: String,
    val gains: List<Float> // 5 bands in dB (-12f to +12f)
)

val DEFAULT_EQ_PRESETS = listOf(
    EqualizerPreset("Flat", listOf(0f, 0f, 0f, 0f, 0f)),
    EqualizerPreset("Bass Boost", listOf(9f, 6f, 2f, 0f, -1f)),
    EqualizerPreset("Rock", listOf(5f, 3f, -1f, 3f, 6f)),
    EqualizerPreset("Pop", listOf(-2f, 2f, 5f, 2f, -2f)),
    EqualizerPreset("Jazz", listOf(4f, 2f, -2f, 2f, 5f)),
    EqualizerPreset("Electronic", listOf(7f, 5f, 0f, 3f, 7f)),
    EqualizerPreset("Hip Hop", listOf(8f, 5f, 0f, 2f, 4f)),
    EqualizerPreset("Classical", listOf(5f, 4f, -2f, 3f, 5f)),
    EqualizerPreset("Vocal Booster", listOf(-3f, 0f, 6f, 5f, 1f))
)

@Composable
fun EqualizerDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onBassBoostChanged: (Short) -> Unit,
    onBandsChanged: (List<Float>) -> Unit,
    initialBassBoost: Short = 0,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return
    val glassColors = LocalGlassmorphicColors.current

    var eqEnabled by remember { mutableStateOf(true) }
    var bassBoostPercent by remember { mutableFloatStateOf(initialBassBoost.toFloat() / 10f) } // 0 to 100
    var selectedPresetName by remember { mutableStateOf("Bass Boost") }

    val bandFrequencies = listOf("60 Hz", "230 Hz", "910 Hz", "3.6 kHz", "14 kHz")
    val bandGains = remember {
        mutableStateListOf(6f, 4f, 1f, 2f, 4f)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("eq_apply_button")
            ) {
                Text("Apply & Close", color = glassColors.glowColor, fontWeight = FontWeight.Bold)
            }
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Equalizer,
                        contentDescription = "Equalizer",
                        tint = glassColors.glowColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Hardware Audio EQ",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Switch(
                    checked = eqEnabled,
                    onCheckedChange = { eqEnabled = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = glassColors.glowColor,
                        checkedTrackColor = glassColors.glowColor.copy(alpha = 0.4f),
                        uncheckedThumbColor = Color.Gray,
                        uncheckedTrackColor = Color.DarkGray
                    )
                )
            }
        },
        text = {
            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Acoustic Tuning Presets",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(DEFAULT_EQ_PRESETS) { preset ->
                        val isSelected = preset.name == selectedPresetName
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) glassColors.glowColor.copy(alpha = 0.25f) else glassColors.surfaceGlassSecondary)
                                .border(
                                    1.dp,
                                    if (isSelected) glassColors.glowColor else glassColors.borderGlass.copy(alpha = 0.3f),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable {
                                    selectedPresetName = preset.name
                                    for (i in 0 until 5) {
                                        bandGains[i] = preset.gains[i]
                                    }
                                    onBandsChanged(bandGains.toList())
                                }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Text(
                                text = preset.name,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) glassColors.glowColor else Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 5-Band Slider Controls
                Text(
                    text = "5-Band Master Frequency Sliders (±12 dB)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))

                bandFrequencies.forEachIndexed { index, freq ->
                    val gain = bandGains[index]
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = freq, fontSize = 12.sp, color = Color.White)
                            Text(
                                text = "%+.1f dB".format(gain),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (gain > 0) glassColors.glowColor else Color.White.copy(alpha = 0.7f)
                            )
                        }
                        Slider(
                            value = gain,
                            onValueChange = { newGain ->
                                bandGains[index] = newGain
                                selectedPresetName = "Custom"
                                onBandsChanged(bandGains.toList())
                            },
                            valueRange = -12f..12f,
                            enabled = eqEnabled,
                            colors = SliderDefaults.colors(
                                thumbColor = glassColors.glowColor,
                                activeTrackColor = glassColors.glowColor,
                                inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                            ),
                            modifier = Modifier.height(30.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bass Booster
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Deep Bass Boost", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "${bassBoostPercent.toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = glassColors.glowColor
                    )
                }
                Slider(
                    value = bassBoostPercent,
                    onValueChange = {
                        bassBoostPercent = it
                        onBassBoostChanged((it * 10).toInt().toShort())
                    },
                    valueRange = 0f..100f,
                    enabled = eqEnabled,
                    colors = SliderDefaults.colors(
                        thumbColor = Color(0xFFFF007F),
                        activeTrackColor = Color(0xFFFF007F),
                        inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.height(30.dp)
                )
            }
        },
        containerColor = Color(0xFF131722)
    )
}
