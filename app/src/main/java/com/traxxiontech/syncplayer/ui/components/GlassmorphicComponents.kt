package com.traxxiontech.syncplayer.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.traxxiontech.syncplayer.model.SongItem
import com.traxxiontech.syncplayer.ui.theme.LocalGlassmorphicColors
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FrostedGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(20.dp),
    borderAlpha: Float = 0.25f,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val glassColors = LocalGlassmorphicColors.current
    val clickableModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Surface(
        modifier = clickableModifier
            .clip(shape)
            .border(
                BorderStroke(
                    1.dp,
                    Brush.verticalGradient(
                        colors = listOf(
                            glassColors.skeuoHighlight.copy(alpha = borderAlpha),
                            glassColors.borderGlass.copy(alpha = borderAlpha * 0.4f)
                        )
                    )
                ),
                shape = shape
            ),
        shape = shape,
        color = glassColors.surfaceGlass,
        tonalElevation = 8.dp,
        shadowElevation = 10.dp
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            glassColors.surfaceGlass,
                            glassColors.surfaceGlassSecondary
                        ),
                        center = Offset(100f, 100f),
                        radius = 800f
                    )
                )
        ) {
            content()
        }
    }
}

@Composable
fun SkeuomorphicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    contentColor: Color = Color.White,
    glowing: Boolean = false,
    content: @Composable () -> Unit
) {
    val glassColors = LocalGlassmorphicColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val topHighlight = if (isPressed) glassColors.skeuoShadow else glassColors.skeuoHighlight
    val bottomShadow = if (isPressed) glassColors.skeuoHighlight else glassColors.skeuoShadow

    Box(
        modifier = modifier
            .clip(shape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .border(
                BorderStroke(
                    1.2.dp,
                    Brush.verticalGradient(
                        colors = listOf(topHighlight, bottomShadow)
                    )
                ),
                shape = shape
            )
            .background(
                Brush.verticalGradient(
                    colors = if (isPressed) {
                        listOf(
                            glassColors.surfaceGlassSecondary.copy(alpha = 0.4f),
                            glassColors.surfaceGlass.copy(alpha = 0.6f)
                        )
                    } else if (glowing) {
                        listOf(
                            glassColors.glowColor.copy(alpha = 0.4f),
                            glassColors.surfaceGlass.copy(alpha = 0.7f)
                        )
                    } else {
                        listOf(
                            glassColors.surfaceGlass.copy(alpha = 0.8f),
                            glassColors.surfaceGlassSecondary.copy(alpha = 0.5f)
                        )
                    }
                )
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun SkeuomorphicKnob(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 88.dp,
    label: String = ""
) {
    val glassColors = LocalGlassmorphicColors.current
    var currentAngle by remember(value) {
        mutableFloatStateOf(-135f + value * 270f)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        change.consume()
                        val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
                        val touchPoint = change.position
                        val rad = atan2(touchPoint.y - center.y, touchPoint.x - center.x)
                        var deg = Math.toDegrees(rad.toDouble()).toFloat() + 90f
                        if (deg < 0) deg += 360f

                        if (deg in 45f..315f) {
                            val norm = ((deg - 45f) / 270f).coerceIn(0f, 1f)
                            currentAngle = -135f + norm * 270f
                            onValueChange(norm)
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(this.size.width / 2f, this.size.height / 2f)
                val radius = this.size.minDimension / 2f - 8f

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(glassColors.skeuoShadow, glassColors.surfaceGlassSecondary),
                        center = center,
                        radius = radius + 6f
                    ),
                    radius = radius + 4f,
                    center = center
                )

                drawArc(
                    color = glassColors.skeuoShadow.copy(alpha = 0.8f),
                    startAngle = 135f,
                    sweepAngle = 270f,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = 8f, cap = StrokeCap.Round)
                )

                val activeSweep = value * 270f
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(glassColors.glowColor.copy(alpha = 0.5f), glassColors.glowColor),
                        center = center
                    ),
                    startAngle = 135f,
                    sweepAngle = activeSweep,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                    style = Stroke(width = 8f, cap = StrokeCap.Round)
                )

                val innerRadius = radius - 14f
                drawCircle(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            glassColors.skeuoHighlight.copy(alpha = 0.7f),
                            glassColors.surfaceGlass,
                            glassColors.skeuoShadow
                        ),
                        startY = center.y - innerRadius,
                        endY = center.y + innerRadius
                    ),
                    radius = innerRadius,
                    center = center
                )

                val angleRad = Math.toRadians((currentAngle - 90f).toDouble())
                val notchStart = Offset(
                    (center.x + (innerRadius - 20f) * cos(angleRad)).toFloat(),
                    (center.y + (innerRadius - 20f) * sin(angleRad)).toFloat()
                )
                val notchEnd = Offset(
                    (center.x + (innerRadius - 4f) * cos(angleRad)).toFloat(),
                    (center.y + (innerRadius - 4f) * sin(angleRad)).toFloat()
                )
                drawLine(
                    color = glassColors.glowColor,
                    start = notchStart,
                    end = notchEnd,
                    strokeWidth = 6f,
                    cap = StrokeCap.Round
                )
            }
        }
        if (label.isNotEmpty()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun SkeuomorphicSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 26.dp,
    showThumbGlow: Boolean = true
) {
    val glassColors = LocalGlassmorphicColors.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    val norm = (change.position.x / size.width.toFloat()).coerceIn(0f, 1f)
                    onValueChange(norm)
                }
            }
            .clip(RoundedCornerShape(height / 2))
    ) {
        val totalWidth = constraints.maxWidth.toFloat()
        val thumbRadiusPx = (height.value * 0.8f)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val trackHeight = 10.dp.toPx()
            val yOffset = (size.height - trackHeight) / 2f

            drawRoundRect(
                color = glassColors.skeuoShadow.copy(alpha = 0.9f),
                topLeft = Offset(0f, yOffset),
                size = Size(size.width, trackHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2f)
            )

            val activeWidth = (totalWidth * value).coerceIn(0f, totalWidth)
            if (activeWidth > 0f) {
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(glassColors.glowColor.copy(alpha = 0.6f), glassColors.glowColor),
                        startX = 0f,
                        endX = activeWidth
                    ),
                    topLeft = Offset(0f, yOffset),
                    size = Size(activeWidth, trackHeight),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2f)
                )
            }

            val thumbCenterX = (totalWidth * value).coerceIn(thumbRadiusPx, totalWidth - thumbRadiusPx)
            val thumbCenterY = size.height / 2f

            if (showThumbGlow) {
                drawCircle(
                    color = glassColors.glowColor.copy(alpha = 0.35f),
                    radius = thumbRadiusPx + 8f,
                    center = Offset(thumbCenterX, thumbCenterY)
                )
            }

            drawCircle(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        glassColors.skeuoHighlight,
                        Color.White,
                        glassColors.skeuoShadow
                    ),
                    startY = thumbCenterY - thumbRadiusPx,
                    endY = thumbCenterY + thumbRadiusPx
                ),
                radius = thumbRadiusPx,
                center = Offset(thumbCenterX, thumbCenterY)
            )

            drawCircle(
                color = glassColors.glowColor,
                radius = thumbRadiusPx * 0.4f,
                center = Offset(thumbCenterX, thumbCenterY)
            )
        }
    }
}

@Composable
fun WaveformVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 32
) {
    val glassColors = LocalGlassmorphicColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "wave")

    val anim1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "w1"
    )

    val anim2 by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(650, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "w2"
    )

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val barWidth = (width / barCount) * 0.65f
        val gap = (width - (barWidth * barCount)) / (barCount - 1).coerceAtLeast(1)

        for (i in 0 until barCount) {
            val factor = if (!isPlaying) {
                0.1f + 0.08f * sin(i * 0.5f)
            } else {
                val cycle = (i % 4)
                when (cycle) {
                    0 -> anim1 * (0.6f + 0.4f * sin(i * 0.3f))
                    1 -> anim2 * (0.7f + 0.3f * cos(i * 0.4f))
                    2 -> ((anim1 + anim2) / 2f) * (0.5f + 0.5f * sin(i * 0.2f))
                    else -> (1f - anim1) * (0.8f + 0.2f * cos(i * 0.5f))
                }.coerceIn(0.1f, 1f)
            }

            val barHeight = height * factor
            val x = i * (barWidth + gap)
            val y = height - barHeight

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(glassColors.glowColor, glassColors.waveColor.copy(alpha = 0.4f)),
                    startY = y,
                    endY = height
                ),
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(barWidth / 2f)
            )
        }
    }
}

@Composable
fun MiniPlayerBar(
    currentSong: SongItem?,
    isPlaying: Boolean,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onBarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (currentSong == null) return
    val glassColors = LocalGlassmorphicColors.current

    FrostedGlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("mini_player_bar"),
        shape = RoundedCornerShape(24.dp),
        onClick = onBarClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(glassColors.surfaceGlassSecondary)
                    .border(1.dp, glassColors.glowColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
            ) {
                AsyncImage(
                    model = currentSong.artworkUri,
                    contentDescription = "Cover Art",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = currentSong.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = currentSong.artist,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            WaveformVisualizer(
                isPlaying = isPlaying,
                barCount = 10,
                modifier = Modifier
                    .width(40.dp)
                    .height(24.dp)
                    .padding(horizontal = 4.dp)
            )

            IconButton(
                onClick = onPlayPause,
                modifier = Modifier.testTag("mini_play_pause_button")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = glassColors.glowColor
                )
            }

            IconButton(
                onClick = onNext,
                modifier = Modifier.testTag("mini_skip_next_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.SkipNext,
                    contentDescription = "Next",
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}
