package com.example.ui

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.example.engine.GameEngine
import com.example.model.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GameCanvas(
    engine: GameEngine,
    tick: Long = 0L,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()

    Canvas(modifier = modifier.fillMaxSize()) {
        @Suppress("UNUSED_VARIABLE")
        val currentTick = tick
        if (size.width <= 1f || size.height <= 1f) return@Canvas

        try {
            engine.viewWidth = size.width
            engine.viewHeight = size.height

            val rawCamX = engine.cameraX
            val rawCamY = engine.cameraY
            val camX = if (rawCamX.isNaN() || rawCamX.isInfinite()) 500f else rawCamX
            val camY = if (rawCamY.isNaN() || rawCamY.isInfinite()) 500f else rawCamY

            val rawShake = engine.cameraShake
            val safeShake = if (rawShake.isNaN() || rawShake <= 0f) 0f else rawShake.coerceIn(0f, 40f)
            val shakeX = if (safeShake > 0) (kotlin.random.Random.nextFloat() - 0.5f) * safeShake else 0f
            val shakeY = if (safeShake > 0) (kotlin.random.Random.nextFloat() - 0.5f) * safeShake else 0f

            // Screen center offset for camera
            val screenCenterX = size.width / 2f + shakeX
            val screenCenterY = size.height / 2f + shakeY

            // 1. Draw Multi-Layered Parallax Background
            drawParallaxBackground(engine, camX, camY)

            // 2. Draw World Platforms
            val platforms = engine.currentArena.getPlatforms()
            for (plat in platforms) {
                val sx = plat.bounds.left - camX + screenCenterX
                val sy = plat.bounds.top - camY + screenCenterY
                val sw = plat.bounds.width
                val sh = plat.bounds.height

                // Culling: check if on screen
                if (!sx.isNaN() && !sy.isNaN() && sx + sw >= 0 && sx <= size.width && sy + sh >= 0 && sy <= size.height) {
                    drawPlatform(this, plat, sx, sy, sw, sh)
                }
            }

            // 3. Draw Pickups
            for (pickup in engine.pickups) {
                if (pickup.isAvailable) {
                    val px = pickup.x - camX + screenCenterX
                    val py = pickup.y - camY + screenCenterY
                    if (!px.isNaN() && !py.isNaN() && px in -60f..(size.width + 60f) && py in -60f..(size.height + 60f)) {
                        drawPickupItem(this, pickup, px, py, textMeasurer)
                    }
                }
            }

            // 4. Draw Particles (Jetpack, explosions, sparks)
            for (part in engine.particles) {
                val partX = part.x - camX + screenCenterX
                val partY = part.y - camY + screenCenterY
                if (!partX.isNaN() && !partY.isNaN() && partX in 0f..size.width && partY in 0f..size.height) {
                    val rawAlpha = if (part.maxLifeTimeMs > 0) 1f - part.lifeTimeMs.toFloat() / part.maxLifeTimeMs else 0f
                    val alpha = if (rawAlpha.isNaN()) 0f else rawAlpha.coerceIn(0f, 1f)
                    val safeRadius = if (part.radius.isNaN() || part.radius < 0f) 2f else part.radius
                    drawCircle(
                        color = part.color.copy(alpha = alpha),
                        radius = safeRadius,
                        center = Offset(partX, partY)
                    )
                }
            }

            // 5. Draw Projectiles (Arrows, bullets, rockets, plasma)
            for (proj in engine.projectiles) {
                val projX = proj.x - camX + screenCenterX
                val projY = proj.y - camY + screenCenterY
                if (!projX.isNaN() && !projY.isNaN() && projX in -50f..(size.width + 50f) && projY in -50f..(size.height + 50f)) {
                    drawProjectile(this, proj, projX, projY)
                }
            }

            // 6. Draw Characters (Local Player, Bots, LAN Players)
            for (player in engine.players) {
                if (!player.isDead) {
                    val charX = player.x - camX + screenCenterX
                    val charY = player.y - camY + screenCenterY
                    if (!charX.isNaN() && !charY.isNaN() && charX in -100f..(size.width + 100f) && charY in -100f..(size.height + 100f)) {
                        drawWarriorCharacter(this, player, charX, charY, textMeasurer)
                    }
                }
            }

            // 7. Draw Floating Damage Numbers
            for (pop in engine.damagePopups) {
                val popX = pop.x - camX + screenCenterX
                val popY = pop.y - camY + screenCenterY
                if (!popX.isNaN() && !popY.isNaN()) {
                    val rawAlpha = (pop.lifeTimeMs.toFloat() / 600f)
                    val alpha = if (rawAlpha.isNaN()) 0.5f else rawAlpha.coerceIn(0.2f, 1f)
                    try {
                        drawText(
                            textMeasurer = textMeasurer,
                            text = pop.text,
                            topLeft = Offset(popX, popY),
                            style = TextStyle(
                                color = pop.color.copy(alpha = alpha),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    } catch (_: Throwable) {
                        // Safe fallback on measurer edge cases
                    }
                }
            }
        } catch (t: Throwable) {
            Log.e("GameCanvas", "Defensive recovery during draw pass", t)
        }
    }
}

private fun DrawScope.drawParallaxBackground(engine: GameEngine, camX: Float, camY: Float) {
    val arena = engine.currentArena
    // Sky gradient
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(arena.skyColorTop, arena.skyColorBottom)
        ),
        size = size
    )

    // Parallax Mountain Silhouettes (Distant Layer)
    val distantScroll = camX * 0.15f
    val pathDistant = Path().apply {
        moveTo(0f, size.height)
        var x = -100f
        var iters = 0
        while (x < size.width + 200f && iters++ < 60) {
            val wave = sin((x + distantScroll) * 0.003f) * 120f + 250f
            lineTo(x, size.height - wave)
            x += 80f
        }
        lineTo(size.width, size.height)
        close()
    }
    drawPath(pathDistant, color = Color(0x33000000))

    // Parallax Ancient Pillars / Peaks (Mid Layer)
    val midScroll = camX * 0.35f
    val pathMid = Path().apply {
        moveTo(0f, size.height)
        var x = -50f
        var iters = 0
        while (x < size.width + 100f && iters++ < 60) {
            val peakH = cos((x + midScroll) * 0.005f) * 80f + 180f
            lineTo(x, size.height - peakH)
            x += 120f
        }
        lineTo(size.width, size.height)
        close()
    }
    drawPath(pathMid, color = Color(0x4D000000))
}

private fun drawPlatform(
    scope: DrawScope,
    plat: ArenaPlatform,
    x: Float,
    y: Float,
    w: Float,
    h: Float
) {
    val safeW = if (w.isNaN() || w <= 0f) 50f else w
    val safeH = if (h.isNaN() || h <= 0f) 20f else h

    when (plat.type) {
        PlatformType.ANCIENT_ROCK -> {
            scope.drawRoundRect(
                color = Color(0xFF2E382E),
                topLeft = Offset(x, y),
                size = Size(safeW, safeH),
                cornerRadius = CornerRadius(8f, 8f)
            )
            scope.drawRoundRect(
                color = Color(0xFF15803D),
                topLeft = Offset(x, y),
                size = Size(safeW, 8f),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }
        PlatformType.LALIBELA_STONE -> {
            scope.drawRoundRect(
                color = Color(0xFF78350F),
                topLeft = Offset(x, y),
                size = Size(safeW, safeH),
                cornerRadius = CornerRadius(6f, 6f)
            )
            scope.drawRoundRect(
                color = Color(0xFFB45309),
                topLeft = Offset(x, y),
                size = Size(safeW, 6f),
                cornerRadius = CornerRadius(3f, 3f)
            )
            if (safeW > 100f) {
                scope.drawLine(
                    color = Color(0x33FDE68A),
                    start = Offset(x + safeW / 2f, y + 4f),
                    end = Offset(x + safeW / 2f, y + safeH - 4f),
                    strokeWidth = 3f
                )
            }
        }
        PlatformType.CASTLE_BRICK -> {
            scope.drawRoundRect(
                color = Color(0xFF334155),
                topLeft = Offset(x, y),
                size = Size(safeW, safeH),
                cornerRadius = CornerRadius(4f, 4f)
            )
            scope.drawRoundRect(
                color = Color(0xFF64748B),
                topLeft = Offset(x, y),
                size = Size(safeW, 6f),
                cornerRadius = CornerRadius(2f, 2f)
            )
        }
        PlatformType.WOODEN_BRIDGE -> {
            scope.drawRoundRect(
                color = Color(0xFF92400E),
                topLeft = Offset(x, y),
                size = Size(safeW, safeH),
                cornerRadius = CornerRadius(5f, 5f)
            )
            var plankX = x + 20f
            var iters = 0
            while (plankX < x + safeW - 10f && iters++ < 60) {
                scope.drawLine(
                    color = Color(0xFF451A03),
                    start = Offset(plankX, y),
                    end = Offset(plankX, y + safeH),
                    strokeWidth = 2f
                )
                plankX += 24f
            }
        }
        PlatformType.FLOATING_ISLAND -> {
            scope.drawRoundRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(x, y),
                size = Size(safeW, safeH),
                cornerRadius = CornerRadius(10f, 10f)
            )
            scope.drawRoundRect(
                color = Color(0xFF10B981),
                topLeft = Offset(x, y),
                size = Size(safeW, 7f),
                cornerRadius = CornerRadius(5f, 5f)
            )
        }
    }

    // Border outline
    scope.drawRoundRect(
        color = Color(0x33FFFFFF),
        topLeft = Offset(x, y),
        size = Size(safeW, safeH),
        cornerRadius = CornerRadius(6f, 6f),
        style = Stroke(width = 1.5f)
    )
}

private fun drawPickupItem(
    scope: DrawScope,
    pickup: PickupEntity,
    x: Float,
    y: Float,
    measurer: TextMeasurer
) {
    scope.drawCircle(
        color = pickup.type.color.copy(alpha = 0.35f),
        radius = 24f,
        center = Offset(x, y)
    )
    scope.drawCircle(
        color = pickup.type.color,
        radius = 16f,
        center = Offset(x, y)
    )

    when (pickup.type) {
        PickupType.HEALTH_TENADAM -> {
            scope.drawLine(Color.White, Offset(x - 8f, y), Offset(x + 8f, y), strokeWidth = 4f)
            scope.drawLine(Color.White, Offset(x, y - 8f), Offset(x, y + 8f), strokeWidth = 4f)
        }
        PickupType.SHIELD_GASHAA -> {
            val p = Path().apply {
                moveTo(x, y - 9f)
                lineTo(x + 9f, y)
                lineTo(x, y + 10f)
                lineTo(x - 9f, y)
                close()
            }
            scope.drawPath(p, Color.White)
        }
        PickupType.AMMO_CRATE -> {
            scope.drawRect(Color.White, Offset(x - 6f, y - 7f), Size(12f, 14f))
        }
        PickupType.SUPER_JETPACK -> {
            val p = Path().apply {
                moveTo(x, y - 10f)
                lineTo(x + 7f, y + 8f)
                lineTo(x, y + 4f)
                lineTo(x - 7f, y + 8f)
                close()
            }
            scope.drawPath(p, Color.White)
        }
        PickupType.WEAPON_DROP -> {
            scope.drawCircle(Color.White, radius = 6f, center = Offset(x, y))
        }
    }
}

private fun drawProjectile(
    scope: DrawScope,
    proj: ProjectileEntity,
    x: Float,
    y: Float
) {
    val angleRad = kotlin.math.atan2(proj.vy, proj.vx)

    when (proj.weaponType) {
        WeaponType.SPARROW_BOW -> {
            val length = 22f
            val tailX = x - cos(angleRad) * length
            val tailY = y - sin(angleRad) * length
            scope.drawLine(
                color = Color(0xFFD97706),
                start = Offset(tailX, tailY),
                end = Offset(x, y),
                strokeWidth = 2.5f
            )
            scope.drawCircle(
                color = Color(0xFFE2E8F0),
                radius = 3.5f,
                center = Offset(x, y)
            )
            scope.drawCircle(
                color = Color(0xFFEF4444),
                radius = 2.5f,
                center = Offset(tailX, tailY)
            )
        }
        WeaponType.WAR_SPEAR -> {
            val length = 38f
            val tailX = x - cos(angleRad) * length
            val tailY = y - sin(angleRad) * length
            scope.drawLine(
                color = Color(0xFF92400E),
                start = Offset(tailX, tailY),
                end = Offset(x, y),
                strokeWidth = 4f
            )
            scope.drawCircle(
                color = Color(0xFFFBBF24),
                radius = 5.5f,
                center = Offset(x, y)
            )
        }
        WeaponType.RPG7_ROCKET -> {
            val length = 26f
            val tailX = x - cos(angleRad) * length
            val tailY = y - sin(angleRad) * length
            scope.drawLine(
                color = Color(0xFF475569),
                start = Offset(tailX, tailY),
                end = Offset(x, y),
                strokeWidth = 6f
            )
            scope.drawCircle(
                color = Color(0xFFDC2626),
                radius = 7f,
                center = Offset(x, y)
            )
            scope.drawCircle(
                color = Color(0xFFFF6D00),
                radius = 5f,
                center = Offset(tailX, tailY)
            )
        }
        WeaponType.PLASMA_RIFLE -> {
            scope.drawCircle(
                color = Color(0x8038BDF8),
                radius = 10f,
                center = Offset(x, y)
            )
            scope.drawCircle(
                color = Color(0xFFFFFFFF),
                radius = 4f,
                center = Offset(x, y)
            )
        }
        else -> {
            val length = 12f
            val tailX = x - cos(angleRad) * length
            val tailY = y - sin(angleRad) * length
            scope.drawLine(
                color = Color(0xFFFDE68A),
                start = Offset(tailX, tailY),
                end = Offset(x, y),
                strokeWidth = 3f
            )
            scope.drawCircle(
                color = Color(0xFFFFD600),
                radius = 3f,
                center = Offset(x, y)
            )
        }
    }
}

private fun drawWarriorCharacter(
    scope: DrawScope,
    player: PlayerState,
    x: Float,
    y: Float,
    measurer: TextMeasurer
) {
    val facingSign = if (player.isFacingRight) 1f else -1f
    val hero = player.hero

    // 1. Invulnerability blinking halo
    if (player.isInvulnerableMs > 0 && (player.isInvulnerableMs / 100) % 2 == 0L) {
        scope.drawCircle(
            color = Color(0x6638BDF8),
            radius = 42f,
            center = Offset(x, y)
        )
    }

    // 2. Jetpack Thruster Boots on Feet (Kumeta Riding Cuffs & Thrusters)
    val footY = y + 26f
    // Kumeta knee-length riding trousers cuffs
    scope.drawRoundRect(
        color = Color(0xFF1E293B),
        topLeft = Offset(x - 16f, footY - 10f),
        size = Size(32f, 10f),
        cornerRadius = CornerRadius(3f, 3f)
    )
    // Kumeta golden embroidered cuff fringe
    scope.drawLine(
        color = Color(0xFFF59E0B),
        start = Offset(x - 16f, footY - 1f),
        end = Offset(x + 16f, footY - 1f),
        strokeWidth = 2.5f
    )

    // Thruster Boots
    scope.drawRoundRect(
        color = Color(0xFF334155),
        topLeft = Offset(x - 15f, footY),
        size = Size(11f, 12f),
        cornerRadius = CornerRadius(3f, 3f)
    )
    scope.drawRoundRect(
        color = Color(0xFF334155),
        topLeft = Offset(x + 4f, footY),
        size = Size(11f, 12f),
        cornerRadius = CornerRadius(3f, 3f)
    )
    // Brass boot buckles
    scope.drawRect(Color(0xFFFDE68A), Offset(x - 13f, footY + 4f), Size(7f, 3f))
    scope.drawRect(Color(0xFFFDE68A), Offset(x + 6f, footY + 4f), Size(7f, 3f))

    if (player.isJetpacking) {
        val hasTurbo = player.superJetpackTimerMs > 0L
        val flameColor = if (hasTurbo) Color(0xFF38BDF8) else Color(0xFFFF6D00)
        scope.drawCircle(
            color = flameColor,
            radius = 7.5f,
            center = Offset(x - 10f, footY + 14f)
        )
        scope.drawCircle(
            color = Color.White,
            radius = 3.5f,
            center = Offset(x - 10f, footY + 12f)
        )
        scope.drawCircle(
            color = flameColor,
            radius = 7.5f,
            center = Offset(x + 10f, footY + 14f)
        )
        scope.drawCircle(
            color = Color.White,
            radius = 3.5f,
            center = Offset(x + 10f, footY + 12f)
        )
    }

    // 3. Dynamic Flowing Jano & Kuta Cape (ኩታና ጃኖ ካባ) with Royal Red & Gold Tibeb Border
    val cloakEndX = x - facingSign * 28f
    val cloakPath = Path().apply {
        moveTo(x, y - 8f)
        lineTo(cloakEndX, y + 16f)
        lineTo(cloakEndX + 4f, y + 28f)
        lineTo(x, y + 14f)
        close()
    }
    // High-contrast dark outline behind cape
    scope.drawPath(cloakPath, color = Color(0x99000000), style = Stroke(width = 3.5f))
    // Cape Body (Authentic handspun natural white cotton Kuta fabric)
    scope.drawPath(cloakPath, color = Color(0xFFF8FAFC))

    // Iconic Jano Royal Border (ጃኖ ጥበብ): Red banner & Gold trim at cape hem
    val janoBorderPath = Path().apply {
        moveTo(cloakEndX, y + 16f)
        lineTo(cloakEndX + 4f, y + 28f)
        lineTo(cloakEndX + 10f, y + 24f)
        lineTo(cloakEndX + 6f, y + 14f)
        close()
    }
    scope.drawPath(janoBorderPath, color = Color(0xFFDC2626)) // Imperial Crimson Red
    scope.drawLine(
        color = Color(0xFFF59E0B), // Ethiopian Gold
        start = Offset(cloakEndX + 2f, y + 22f),
        end = Offset(cloakEndX + 8f, y + 26f),
        strokeWidth = 2.5f
    )

    // 4. Warrior Torso: Qemisa / Kumeta with High-Contrast Dark Outline
    // Dark silhouette outline
    scope.drawRoundRect(
        color = Color(0xDD090D16),
        topLeft = Offset(x - 16f, y - 11f),
        size = Size(32f, 32f),
        cornerRadius = CornerRadius(8f, 8f)
    )
    // Tunic Base (Hero Primary Color)
    scope.drawRoundRect(
        color = hero.primaryColor,
        topLeft = Offset(x - 14f, y - 9f),
        size = Size(28f, 28f),
        cornerRadius = CornerRadius(6f, 6f)
    )

    // Kuta Shoulder Wrap (የተደረበ ነጭ ኩታ)
    scope.drawRoundRect(
        color = Color(0xFFF1F5F9),
        topLeft = Offset(x - 15f, y - 10f),
        size = Size(30f, 13f),
        cornerRadius = CornerRadius(6f, 6f)
    )
    // Kuta golden neckband embroidery
    scope.drawLine(
        color = Color(0xFFF59E0B),
        start = Offset(x - 8f, y - 5f),
        end = Offset(x + 8f, y - 5f),
        strokeWidth = 2f
    )

    // Jano Red-and-Gold Bandolier across chest (የጃኖ ድግ)
    scope.drawLine(
        color = Color(0xFFDC2626),
        start = Offset(x - 12f, y - 7f),
        end = Offset(x + 12f, y + 15f),
        strokeWidth = 5f
    )
    scope.drawLine(
        color = Color(0xFFFDE68A),
        start = Offset(x - 12f, y - 7f),
        end = Offset(x + 12f, y + 15f),
        strokeWidth = 1.5f
    )

    // 5. Warrior Head with High-Contrast Outline & Features
    val headCenter = Offset(x, y - 22f)
    // Head outline
    scope.drawCircle(
        color = Color(0xDD090D16),
        radius = 15f,
        center = headCenter
    )
    // Skin Tone
    scope.drawCircle(
        color = Color(0xFF8D5B4C),
        radius = 13f,
        center = headCenter
    )
    // Eye facing aiming direction
    val eyeX = x + facingSign * 5f
    val eyeY = y - 23f
    scope.drawCircle(Color.White, radius = 2.5f, center = Offset(eyeX, eyeY))
    scope.drawCircle(Color(0xFF0F172A), radius = 1.2f, center = Offset(eyeX + facingSign * 0.8f, eyeY))

    // 6. Cultural Headgear (የራስ ቁርና ጎፈሬ)
    when (hero.headgearType) {
        "LION_MANE" -> {
            // Arbegna Lion Mane Headdress (ጎፈሬ) with golden mane ring
            scope.drawCircle(
                color = Color(0xFFB45309),
                radius = 18f,
                center = Offset(x, y - 24f),
                style = Stroke(width = 6f)
            )
            scope.drawCircle(
                color = Color(0xFFF59E0B),
                radius = 17f,
                center = Offset(x, y - 24f),
                style = Stroke(width = 2.5f)
            )
        }
        "IMPERIAL_HELM" -> {
            // Gold Imperial Guard Helmet with Royal Cross Crest
            val crest = Path().apply {
                moveTo(x, y - 40f)
                lineTo(x + 8f, y - 30f)
                lineTo(x - 8f, y - 30f)
                close()
            }
            scope.drawPath(crest, color = Color(0xFFF59E0B))
            scope.drawCircle(Color(0xFFDC2626), radius = 4f, center = Offset(x, y - 40f))
        }
        "KALLACHA" -> {
            // Oromo Gadaa Kallacha Forehead Crown
            scope.drawLine(
                color = Color(0xFFFDE68A),
                start = Offset(x, y - 28f),
                end = Offset(x + facingSign * 14f, y - 30f),
                strokeWidth = 4f
            )
            scope.drawCircle(Color(0xFFD97706), radius = 3.5f, center = Offset(x + facingSign * 14f, y - 30f))
        }
        "DESERT_TURBAN" -> {
            // Layered Afar Desert Turban Wrap
            scope.drawRoundRect(
                color = Color(0xFFFEF08A),
                topLeft = Offset(x - 15f, y - 34f),
                size = Size(30f, 13f),
                cornerRadius = CornerRadius(5f, 5f)
            )
            scope.drawLine(Color(0xFFD97706), Offset(x - 14f, y - 28f), Offset(x + 14f, y - 28f), strokeWidth = 2f)
        }
        "EAGLE_HOOD" -> {
            scope.drawCircle(
                color = Color(0xFF047857),
                radius = 16f,
                center = Offset(x, y - 24f),
                style = Stroke(width = 4.5f)
            )
        }
        "TACTICAL_HELMET" -> {
            scope.drawRoundRect(
                color = Color(0xFF1E293B),
                topLeft = Offset(x - 15f, y - 34f),
                size = Size(30f, 13f),
                cornerRadius = CornerRadius(6f, 6f)
            )
            scope.drawRoundRect(
                color = Color(0xFF10B981),
                topLeft = Offset(x - 10f, y - 27f),
                size = Size(20f, 4f),
                cornerRadius = CornerRadius(2f, 2f)
            )
        }
    }

    // 7. Aimable Weapon Arm & Weapon
    val safeAimDeg = if (player.aimAngleDeg.isNaN()) 0f else player.aimAngleDeg
    val aimRad = safeAimDeg * (PI / 180.0).toFloat()
    val shoulderX = x + facingSign * 5f
    val shoulderY = y - 4f
    val weaponLen = 32f
    val muzzleX = shoulderX + cos(aimRad) * weaponLen
    val muzzleY = shoulderY + sin(aimRad) * weaponLen

    drawEquippedWeapon(scope, player.currentWeapon, shoulderX, shoulderY, muzzleX, muzzleY, aimRad)

    if (player.currentWeapon == WeaponType.SNIPER_DRAGUNOV) {
        val laserEnd = Offset(shoulderX + cos(aimRad) * 600f, shoulderY + sin(aimRad) * 600f)
        scope.drawLine(
            color = Color(0x99EF4444),
            start = Offset(muzzleX, muzzleY),
            end = laserEnd,
            strokeWidth = 1.5f
        )
    }

    // 8. Player Overhead HUD (Name, Health Bar, Shield Bar)
    val barW = 52f
    val barH = 6f
    val barX = x - barW / 2f
    val barY = y - 46f

    try {
        scope.drawText(
            textMeasurer = measurer,
            text = player.name,
            topLeft = Offset(barX, barY - 15f),
            style = TextStyle(
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        )
    } catch (_: Throwable) {}

    // Health Bar with border
    scope.drawRect(Color(0xFF0F172A), Offset(barX - 1f, barY - 1f), Size(barW + 2f, barH + 2f))
    scope.drawRect(Color(0xFF334155), Offset(barX, barY), Size(barW, barH))
    val rawHpRatio = if (player.maxHealth > 0) player.health.toFloat() / player.maxHealth else 0f
    val hpRatio = if (rawHpRatio.isNaN()) 0f else rawHpRatio.coerceIn(0f, 1f)
    val hpColor = if (hpRatio > 0.4f) Color(0xFF10B981) else if (hpRatio > 0.2f) Color(0xFFF59E0B) else Color(0xFFEF4444)
    scope.drawRect(hpColor, Offset(barX, barY), Size(barW * hpRatio, barH))

    if (player.armor > 0) {
        val rawArmRatio = if (player.maxArmor > 0) player.armor.toFloat() / player.maxArmor else 0f
        val armRatio = if (rawArmRatio.isNaN()) 0f else rawArmRatio.coerceIn(0f, 1f)
        scope.drawRect(Color(0xFF0284C7), Offset(barX, barY - 5f), Size(barW * armRatio, 3.5f))
    }
}

private fun drawEquippedWeapon(
    scope: DrawScope,
    weapon: WeaponType,
    shoulderX: Float,
    shoulderY: Float,
    muzzleX: Float,
    muzzleY: Float,
    aimRad: Float
) {
    when (weapon) {
        WeaponType.SPARROW_BOW -> {
            val perpRad = aimRad + (PI / 2.0).toFloat()
            val bowTip1 = Offset(muzzleX + cos(perpRad) * 16f, muzzleY + sin(perpRad) * 16f)
            val bowTip2 = Offset(muzzleX - cos(perpRad) * 16f, muzzleY - sin(perpRad) * 16f)
            val bowPath = Path().apply {
                moveTo(bowTip1.x, bowTip1.y)
                quadraticTo(shoulderX, shoulderY, bowTip2.x, bowTip2.y)
            }
            scope.drawPath(bowPath, color = Color(0xFFD97706), style = Stroke(width = 3.5f))
            scope.drawLine(Color(0xFFE2E8F0), bowTip1, bowTip2, strokeWidth = 1f)
        }
        WeaponType.WAR_SPEAR -> {
            scope.drawLine(Color(0xFF92400E), Offset(shoulderX, shoulderY), Offset(muzzleX + cos(aimRad) * 14f, muzzleY + sin(aimRad) * 14f), strokeWidth = 3.5f)
            scope.drawCircle(Color(0xFFFBBF24), radius = 4f, center = Offset(muzzleX + cos(aimRad) * 14f, muzzleY + sin(aimRad) * 14f))
        }
        WeaponType.SHOTEL_BLADE -> {
            val bladeEnd = Offset(shoulderX + cos(aimRad) * 32f, shoulderY + sin(aimRad) * 32f)
            scope.drawLine(Color(0xFFEF4444), Offset(shoulderX, shoulderY), bladeEnd, strokeWidth = 4f)
        }
        WeaponType.RPG7_ROCKET -> {
            scope.drawLine(Color(0xFF334155), Offset(shoulderX, shoulderY), Offset(muzzleX, muzzleY), strokeWidth = 7f)
            scope.drawCircle(Color(0xFFDC2626), radius = 5.5f, center = Offset(muzzleX, muzzleY))
        }
        WeaponType.AK47_KALASHNIKOV -> {
            scope.drawLine(Color(0xFF1E293B), Offset(shoulderX, shoulderY), Offset(muzzleX, muzzleY), strokeWidth = 4.5f)
            val magX = shoulderX + cos(aimRad) * 10f
            val magY = shoulderY + sin(aimRad) * 10f
            scope.drawLine(Color(0xFFD97706), Offset(magX, magY), Offset(magX + sin(aimRad) * 8f, magY - cos(aimRad) * 8f), strokeWidth = 3f)
        }
        WeaponType.PLASMA_RIFLE -> {
            scope.drawLine(Color(0xFF38BDF8), Offset(shoulderX, shoulderY), Offset(muzzleX, muzzleY), strokeWidth = 5f)
            scope.drawCircle(Color(0xFF60A5FA), radius = 4f, center = Offset(muzzleX, muzzleY))
        }
        else -> {
            scope.drawLine(Color(0xFF451A03), Offset(shoulderX, shoulderY), Offset(muzzleX, muzzleY), strokeWidth = 4f)
        }
    }
}
