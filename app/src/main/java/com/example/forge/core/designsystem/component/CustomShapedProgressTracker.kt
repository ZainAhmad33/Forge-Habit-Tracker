package com.example.forge.core.designsystem.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun CustomShapeProgress(
    progress: Float,
    shape: Shape,
    modifier: Modifier = Modifier,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceContainerHighest,
    shapeColor: Color = Color.Transparent,
    trackerSize: Dp = 100.dp,
    strokeWidth: Dp = 6.dp,
    startAngle: Float = 0f, // 0f = Top, 90f = Right, 180f = Bottom, -90f = Left
    showCheckMark: Boolean = false,
    label: String = "${(progress * 100).toInt()}%"
) {
    val isInspectionMode = LocalInspectionMode.current
    var animateToValue by remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    
    // In normal execution, animate from 0 to the progress percentage.
    // In inspection mode (Preview), look directly at the progress parameter so the preview inspector can animate it from 0 to progress.
    val targetValue = if (isInspectionMode) progress.coerceIn(0f, 1f) else animateToValue

    androidx.compose.runtime.LaunchedEffect(progress) {
        if (!isInspectionMode) {
            // Force reset to 0 before animating to the new progress percentage
            animateToValue = 0f
            kotlinx.coroutines.delay(10)
            animateToValue = progress.coerceIn(0f, 1f)
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = targetValue,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "ProgressAnimation"
    )

    val isCompleted = animatedProgress >= 0.999f

    val checkmarkStrokeProgress by animateFloatAsState(
        targetValue = if (isCompleted) 1f else 0f,
        animationSpec = tween(durationMillis = 400, delayMillis = 200, easing = FastOutSlowInEasing),
        label = "CheckmarkStrokeAnimation"
    )

    val dynamicLabelColor by animateColorAsState(
        targetValue = if (isCompleted) progressColor else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(durationMillis = 300),
        label = "LabelColorAnimation"
    )

    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val checkColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)

    var containerSize by remember { mutableStateOf(Size.Zero) }

    val startPoint = remember(containerSize, shape, startAngle, layoutDirection, density) {
        if (containerSize == Size.Zero) return@remember Offset.Zero
        val fullPath = Path()
        val outline = shape.createOutline(containerSize, layoutDirection, density)
        when (outline) {
            is Outline.Generic -> fullPath.addPath(outline.path)
            is Outline.Rectangle -> fullPath.addRect(outline.rect)
            is Outline.Rounded -> fullPath.addRoundRect(outline.roundRect)
        }
        val measure = PathMeasure()
        measure.setPath(fullPath, false)
        if (measure.length > 0f) {
            rotatePoint(measure.getPosition(0f), containerSize.center, startAngle)
        } else Offset.Zero
    }

    val badgeSize = if (trackerSize < 75.dp) 20.dp else 26.dp
    val badgeRadius = badgeSize / 2

    Box(
        modifier = modifier
            .padding(badgeRadius)
            .size(trackerSize)
            .aspectRatio(1f)
            .onSizeChanged { containerSize = it.toSize() },
        contentAlignment = Alignment.Center
    ) {
        // 1. Background Shape Track (Rotated to align with progress start)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(strokeWidth / 2)
                .rotate(startAngle)
                .clip(shape)
                .background(shapeColor)
        )

        // 2. Active Shape Progress Bar with Full Track Line
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(strokeWidth / 5)
        ) {
            val fullPath = Path()
            val segmentPath = Path()
            val pathMeasure = PathMeasure()

            val outline = shape.createOutline(size, layoutDirection, density)
            when (outline) {
                is Outline.Generic -> fullPath.addPath(outline.path)
                is Outline.Rectangle -> fullPath.addRect(outline.rect)
                is Outline.Rounded -> fullPath.addRoundRect(outline.roundRect)
            }

            pathMeasure.setPath(fullPath, false)
            val pathLength = pathMeasure.length

            if (pathLength > 0f) {
                rotate(degrees = startAngle, pivot = center) {
                    // A. DRAW THE TRACK BAR (Full inactive outline)
                    drawPath(
                        path = fullPath,
                        color = trackColor,
                        style = Stroke(
                            width = strokeWidth.toPx(),
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )

                    // B. DRAW THE PROGRESS BAR (Active segment on top)
                    if (animatedProgress > 0f) {
                        pathMeasure.getSegment(
                            startDistance = 0f,
                            stopDistance = pathLength * animatedProgress,
                            destination = segmentPath,
                            startWithMoveTo = true
                        )

                        drawPath(
                            path = segmentPath,
                            color = progressColor,
                            style = Stroke(
                                width = strokeWidth.toPx(),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }
        }

        // 3. Center Text Label (Normal / Upright - No Rotation)
        Text(
            text = label,
            color = dynamicLabelColor,
            fontWeight = FontWeight.Bold,
            fontSize = if (trackerSize < 75.dp) 14.sp else 18.sp
        )

        // 4. Tick Badge at rotated position (Icon stays upright - No Rotation)
        if(showCheckMark){
            AnimatedVisibility(
                visible = isCompleted && startPoint != Offset.Zero,
                enter = fadeIn() + scaleIn(
                    initialScale = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                ),
                exit = fadeOut() + scaleOut(targetScale = 0f),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset {
                        IntOffset(
                            x = (startPoint.x + with(density) { (strokeWidth / 2).toPx() - badgeRadius.toPx() }).roundToInt(),
                            y = (startPoint.y + with(density) { (strokeWidth / 2).toPx() - badgeRadius.toPx() }).roundToInt()
                        )
                    }
            ) {
                Surface(
                    shape = CircleShape,
                    color = progressColor,
                    shadowElevation = 3.dp,
                    modifier = Modifier.size(badgeSize)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        // Checkmark Canvas (Unrotated)
                        Canvas(
                            modifier = Modifier.size(if (trackerSize < 75.dp) 12.dp else 16.dp)
                        ) {
                            val path = Path().apply {
                                moveTo(size.width * 0.15f, size.height * 0.5f)
                                lineTo(size.width * 0.42f, size.height * 0.78f)
                                lineTo(size.width * 0.85f, size.height * 0.25f)
                            }

                            val measure = PathMeasure()
                            measure.setPath(path, false)
                            val segmentPath = Path()
                            measure.getSegment(0f, measure.length * checkmarkStrokeProgress, segmentPath, true)

                            drawPath(
                                path = segmentPath,
                                color = checkColor,
                                style = Stroke(
                                    width = 3.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }
                }
            }
        }

    }
}

/** Rotation matrix helper */
private fun rotatePoint(point: Offset, center: Offset, angleDegrees: Float): Offset {
    val radians = Math.toRadians(angleDegrees.toDouble())
    val cos = cos(radians).toFloat()
    val sin = sin(radians).toFloat()

    val dx = point.x - center.x
    val dy = point.y - center.y

    return Offset(
        x = center.x + (dx * cos - dy * sin),
        y = center.y + (dx * sin + dy * cos)
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
@Preview
private fun PreviewCustomProgressTracker(){
    CustomShapeProgress(
        progress = 1.0f,
        shape = MaterialShapes.Circle.toShape(), // Material Expressive shape
        trackerSize = 90.dp,
        startAngle = 40f,
        showCheckMark = true
    )
}
