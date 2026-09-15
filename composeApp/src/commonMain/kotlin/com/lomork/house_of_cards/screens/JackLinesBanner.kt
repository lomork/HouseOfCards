package com.lomork.house_of_cards.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.GraphicsLayerScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lomork.house_of_cards.theme.Hoc
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/**
 * High-fidelity "Jack's Lines" laser animation per spec:
 * - Cyan/blue chip icon ABOVE the centered title
 * - White/cyan laser beam (4px) sweeps across TOP of text
 * - SUCCESS: beam crosses full width → lime-green horizontal bloom behind text + confetti burst
 * - BLOCKED: beam stops at center (blocked by chip) → crimson radial glow behind text, no confetti
 * - Timing: sweep 0.28s, glow flare 0.1s/0.5s decay, confetti 1.2s gravity, 1.5s idle loop
 */

// Confetti particle data class (moved to top-level for visibility)
data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val size: Float,
    val rotation: Float,
    val rotSpeed: Float,
    val birthTime: Long,
)

@Composable
fun JackLinesBanner(gameTitle: String) {
    var cycle by remember { mutableStateOf(0) }

    // Per-cycle random outcome
    val isBlocked = remember(cycle) { Random.nextBoolean() }
    val sweepAnim = remember { Animatable(0f) }
    val glowAnim = remember { Animatable(0f) }
    val particles = remember { mutableStateListOf<Particle>() }

    // Colors
    val laserColor = Color(0xFFFFFFFF) // pure white core
    val laserGlowColor = Color(0xFF00FFFF) // cyan glow
    val successGlow = Color(0xFF00FF44) // lime green
    val dangerGlow = Color(0xFFFF0033) // crimson
    val chipColor = Color(0xFF00E5FF) // cyan/blue chip
    val textColor = Hoc.Gold
    val confettiColors = listOf(
        Color(0xFFFFEB3B), // yellow
        Color(0xFFE91E63), // magenta
        Color(0xFF00BCD4), // cyan
        Color(0xFF3F51B5), // dark blue
        Color(0xFFFF9800), // orange
    )

    // Laser sweep parameters
    val SWEEP_DURATION = 280 // ms
    val GLOW_RISE = 100 // ms
    val GLOW_DECAY = 500 // ms
    val CONFETTI_LIFESPAN = 1200 // ms
    val IDLE_DELAY = 1500 // ms

    // Main animation loop
    LaunchedEffect(cycle) {
        particles.clear()
        sweepAnim.snapTo(0f)
        glowAnim.snapTo(0f)

        // 1. SWEEP: fast horizontal beam across text width
        sweepAnim.animateTo(1f, tween(SWEEP_DURATION, easing = FastOutSlowInEasing))

        // 2. GLOW FLARE at completion/collision
        glowAnim.animateTo(1f, tween(GLOW_RISE))

        if (!isBlocked) {
            // SUCCESS: spawn confetti at center line
            val centerX = 0.5f // normalized
            val centerY = 0f // at text baseline
            val now = System.currentTimeMillis()
            repeat(35) {
                val angle = (-90 + Random.nextFloat() * 60 - 30) * PI / 180 // upward spread
                val speed = 150f + Random.nextFloat() * 200f // px/s
                particles.add(Particle(
                    x = centerX,
                    y = centerY,
                    vx = (cos(angle) * speed).toFloat(),
                    vy = (sin(angle) * speed - 50f).toFloat(), // initial upward pop
                    color = confettiColors.random(),
                    size = 6f + Random.nextFloat() * 6f,
                    rotation = Random.nextFloat() * 360f,
                    rotSpeed = (-180f + Random.nextFloat() * 360f),
                    birthTime = now,
                ))
            }
        }

        // 3. HOLD glow briefly, then decay
        delay(GLOW_DECAY.toLong())
        glowAnim.animateTo(0f, tween(GLOW_DECAY))

        // 4. Let confetti fall
        delay((CONFETTI_LIFESPAN - GLOW_DECAY).toLong())

        // 5. Retract sweep line
        sweepAnim.animateTo(0f, tween(200, easing = FastOutSlowInEasing))

        // 6. Idle before next cycle
        delay(IDLE_DELAY.toLong())
        cycle++
    }

    // Confetti physics update (60fps)
    LaunchedEffect(Unit) {
        while (true) {
            delay(16)
            val now = System.currentTimeMillis()
            val toRemove = mutableListOf<Particle>()
            particles.forEach { p ->
                val dt = 0.016f
                val age = (now - p.birthTime) / 1000f
                if (age > CONFETTI_LIFESPAN / 1000f) {
                    toRemove.add(p)
                } else {
                    // gravity
                    val newVy = p.vy + 400f * dt // px/s^2
                    val newX = p.x + p.vx * dt / 300f // normalized by canvas width ~300dp
                    val newY = p.y + newVy * dt / 300f
                    val newRot = p.rotation + p.rotSpeed * dt
                    val index = particles.indexOf(p)
                    if (index >= 0) {
                        particles[index] = p.copy(
                            x = newX,
                            y = newY,
                            vy = newVy,
                            rotation = newRot,
                        )
                    }
                }
            }
            toRemove.forEach { particles.remove(it) }
        }
    }

    // Glow pulse animation for success (horizontal bloom behind text)
    val successGlowAlpha by animateFloatAsState(
        if (!isBlocked) glowAnim.value else 0f,
        tween(0)
    )
    val dangerGlowAlpha by animateFloatAsState(
        if (isBlocked) glowAnim.value else 0f,
        tween(0)
    )

    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        // === CHIP ICON (ABOVE TITLE) ===
        Box(
            Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(chipColor)
                .graphicsLayer {
                    // subtle pulse on success
                    val pulse = 1f + 0.15f * sin(System.currentTimeMillis() / 200f)
                    scaleX = pulse
                    scaleY = pulse
                },
            contentAlignment = Alignment.Center,
        ) {
            // Chip inner detail
            Box(
                Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
            )
        }

        Spacer(Modifier.height(8.dp))

        // === TITLE + LASER + GLOWS + CONFETTI ===
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .wrapContentSize(Alignment.Center),
            contentAlignment = Alignment.Center,
        ) {
            // 1. GLOW LAYERS (behind text)
            // Success: horizontal lime bloom spanning text width
            if (successGlowAlpha > 0f) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .graphicsLayer { alpha = successGlowAlpha },
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val cy = h / 2f
                        // Horizontal bloom: radial gradient stretched horizontally
                        val path = Path().apply {
                            addRect(Rect(0f, cy - 40f, w, cy + 40f))
                        }
                        drawPath(
                            path,
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    successGlow.copy(alpha = 0.6f),
                                    successGlow.copy(alpha = 0.8f),
                                    successGlow.copy(alpha = 0.6f),
                                    Color.Transparent,
                                ),
                                startX = 0f,
                                endX = w,
                            ),
                                                    )
                    }
                }
            }

            // Blocked: crimson radial pulse at center
            if (dangerGlowAlpha > 0f) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .graphicsLayer { alpha = dangerGlowAlpha },
                ) {
                    Canvas(Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height / 2f
                        val radius = max(size.width, size.height) * 0.6f
                        drawCircle(
                            color = dangerGlow.copy(alpha = 0.5f),
                            center = Offset(cx, cy),
                            radius = radius,
                        )
                        drawCircle(
                            color = dangerGlow.copy(alpha = 0.3f),
                            center = Offset(cx, cy),
                            radius = radius * 1.5f,
                        )
                    }
                }
            }

            // 2. TITLE TEXT (on top of glows)
            Text(
                text = gameTitle.uppercase(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = textColor,
                letterSpacing = 2.sp,
            )

            // 3. LASER BEAM (on TOP of text)
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .graphicsLayer {
                        // Shake on blocked collision
                        if (isBlocked && sweepAnim.value >= 0.5f && sweepAnim.value < 1f) {
                            translationX = (sin(System.currentTimeMillis() / 20f) * 3f).toFloat()
                        }
                    }
            ) {
                val w = size.width
                val h = size.height
                val cy = h / 2f

                // Beam sweep progress (0 to 1)
                val progress = sweepAnim.value
                val beamEndX = w * progress

                // Beam core (white, 4px)
                val beamHeight = 4f

                // Outer glow (cyan, 12px)
                val glowHeight = 12f

                if (progress > 0f) {
                    // Outer glow first
                    drawRect(
                        color = laserGlowColor.copy(alpha = 0.4f * (1f - abs(progress - 0.5f) * 2f)),
                        topLeft = Offset(0f, cy - glowHeight / 2f),
                        size = Size(beamEndX, glowHeight),
                    )

                    // Core beam
                    drawRect(
                        color = laserColor,
                        topLeft = Offset(0f, cy - beamHeight / 2f),
                        size = Size(beamEndX, beamHeight),
                    )

                    // Tip highlight
                    if (progress < 1f || !isBlocked) {
                        drawCircle(
                            color = laserColor,
                            center = Offset(beamEndX, cy),
                            radius = beamHeight,
                        )
                    }
                }

                // BLOCKED: show chip collision at center when beam hits it
                if (isBlocked && progress >= 0.5f) {
                    val chipX = w * 0.5f
                    val chipY = cy
                    // Collision spark
                    val sparkAlpha = 1f - (progress - 0.5f) * 2f
                    if (sparkAlpha > 0f) {
                        repeat(6) { i ->
                            val angle = (i * 60 + System.currentTimeMillis() / 50 % 360) * PI / 180
                            val r = 15f + Random.nextFloat() * 10f
                            val path = Path().apply {
                                moveTo(chipX, chipY)
                                lineTo(
                                    chipX + (cos(angle) * r).toFloat(),
                                    chipY + (sin(angle) * r).toFloat()
                                )
                            }
                            drawPath(
                                path,
                                color = dangerGlow.copy(alpha = sparkAlpha * 0.8f),
                            )
                        }
                    }
                }
            }

            // 4. CONFETTI PARTICLES (on top of everything)
            if (particles.isNotEmpty()) {
                Canvas(Modifier.fillMaxSize()) {
                    particles.forEach { p ->
                        val px = p.x * size.width
                        val py = size.height / 2f + p.y * size.height
                        val s = p.size * (1f - (System.currentTimeMillis() - p.birthTime) / CONFETTI_LIFESPAN.toFloat())
                        if (s > 0f) {
                            drawRect(
                                color = p.color.copy(alpha = 1f - (System.currentTimeMillis() - p.birthTime) / CONFETTI_LIFESPAN.toFloat()),
                                topLeft = Offset(px - s / 2, py - s / 2),
                                size = Size(s, s * 0.6f),
                            )
                        }
                    }
                }
            }
        }
    }
}

// Custom easing: fast out, slow in
val FastOutSlowInEasing = object : Easing {
    override fun transform(fraction: Float): Float {
        return when {
            fraction < 0.5f -> 2f * fraction * fraction
            else -> 1f - 2f * (1f - fraction) * (1f - fraction)
        }
    }
}