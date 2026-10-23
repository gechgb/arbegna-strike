package com.example.engine

import androidx.compose.ui.geometry.Rect
import com.example.model.ArenaPlatform
import com.example.model.PlayerState
import kotlin.math.abs

/**
 * 2D Physics and Collision Detection System for character movement.
 * Provides:
 *  - Proportional vertical thrust logic and fuel consumption for the jetpack
 *  - Constant downward gravity and terminal fall velocity
 *  - Jetpack recharge delay and ground fuel regeneration
 *  - Ground friction vs air drag
 *  - AABB bounding box collision resolution against:
 *      * Platform floors (solid & one-way ledges)
 *      * Platform ceilings (solid platforms)
 *      * Lateral walls (solid platforms)
 *      * World arena boundaries
 */
object PhysicsEngine {
    const val GRAVITY = 850f                      // pixels / sec^2 downward acceleration
    const val TERMINAL_FALL_VELOCITY = 650f       // maximum downward velocity
    const val BASE_JETPACK_THRUST = 780f          // upward acceleration during normal jetpack flight
    const val SUPER_JETPACK_THRUST = 1100f        // upward acceleration during turbo boost
    const val MAX_UPWARD_SPEED = -480f            // max upward velocity under normal thrust
    const val BOOSTED_MAX_UPWARD_SPEED = -650f    // max upward velocity under turbo boost
    const val BASE_FUEL_BURN_RATE = 35f           // fuel units burned per second at full thrust
    const val GROUND_FUEL_RECHARGE_RATE = 45f     // fuel units recovered per second when grounded
    const val RECHARGE_DELAY_MS = 250L            // cooldown after thrust before recharge begins
    const val GROUND_FRICTION = 0.82f             // horizontal deceleration when grounded
    const val AIR_DRAG = 0.94f                    // horizontal deceleration in air
    const val CHARACTER_HALF_WIDTH = 20f          // collision box half-width
    const val CHARACTER_HEIGHT = 56f              // collision box height
    const val WORLD_MARGIN_X = 60f
    const val WORLD_MARGIN_TOP = 50f
    const val WORLD_MARGIN_BOTTOM = 80f

    data class CollisionResult(
        val isGrounded: Boolean = false,
        val hitCeiling: Boolean = false,
        val hitWall: Boolean = false
    )

    /**
     * Executes one complete physics and collision step for a 2D character.
     */
    fun step(
        player: PlayerState,
        moveX: Float,
        moveY: Float,
        dt: Float,
        platforms: List<ArenaPlatform>,
        worldWidth: Float,
        worldHeight: Float,
        onJetpackThrust: ((intensity: Float) -> Unit)? = null
    ): CollisionResult {
        // 1. Horizontal input & friction
        applyHorizontalMovement(player, moveX, dt)

        // 2. Vertical thrust & fuel consumption / gravity
        applyJetpackAndGravity(player, moveY, dt, onJetpackThrust)

        // 3. Integrate position
        val prevY = player.y
        player.x += player.vx * dt
        player.y += player.vy * dt

        // 4. Resolve Collisions against platforms and world bounds
        val isDroppingDown = moveY > 0.6f
        val result = resolveCollisions(player, platforms, worldWidth, worldHeight, prevY, dt, isDroppingDown)
        player.isGrounded = result.isGrounded

        // 5. Fuel recharge when grounded (after recharge delay has expired)
        if (result.isGrounded && !player.isJetpacking) {
            if (player.jetpackRechargeDelayMs > 0) {
                player.jetpackRechargeDelayMs = (player.jetpackRechargeDelayMs - (dt * 1000f).toLong()).coerceAtLeast(0L)
            } else {
                val rechargeSpeed = GROUND_FUEL_RECHARGE_RATE * player.hero.jetpackRechargeRate
                player.jetpackFuel = (player.jetpackFuel + rechargeSpeed * dt).coerceAtMost(player.maxJetpackFuel)
            }
        }

        return result
    }

    /**
     * Handles vertical jetpack thrust, fuel consumption, super-jetpack bonus, and gravity.
     */
    fun applyJetpackAndGravity(
        player: PlayerState,
        moveY: Float,
        dt: Float,
        onThrust: ((intensity: Float) -> Unit)? = null
    ) {
        // Decrement turbo booster duration if active
        val hasTurbo = player.superJetpackTimerMs > 0L
        if (hasTurbo) {
            player.superJetpackTimerMs = (player.superJetpackTimerMs - (dt * 1000f).toLong()).coerceAtLeast(0L)
        }

        // Pushing upward on control stick engages thrust (proportional input: 0.0 to 1.0)
        val isTryingToFly = moveY < -0.15f
        val hasFuelOrTurbo = player.jetpackFuel > 0f || hasTurbo

        if (isTryingToFly && hasFuelOrTurbo) {
            val thrustIntensity = (-moveY).coerceIn(0.2f, 1.0f)
            player.isJetpacking = true
            player.jetpackRechargeDelayMs = RECHARGE_DELAY_MS

            // Fuel Consumption: turbo provides free thrust, otherwise burn fuel
            if (!hasTurbo) {
                val burnAmount = BASE_FUEL_BURN_RATE * thrustIntensity * dt
                player.jetpackFuel = (player.jetpackFuel - burnAmount).coerceAtLeast(0f)
            }

            // Apply upward vertical acceleration against gravity
            val thrustAcc = if (hasTurbo) SUPER_JETPACK_THRUST else BASE_JETPACK_THRUST
            player.vy -= thrustAcc * thrustIntensity * dt

            // Clamp max upward flight velocity
            val maxUpwardSpeed = if (hasTurbo) BOOSTED_MAX_UPWARD_SPEED else MAX_UPWARD_SPEED
            if (player.vy < maxUpwardSpeed) {
                player.vy = maxUpwardSpeed
            }

            onThrust?.invoke(thrustIntensity)
        } else {
            // Not thrusting or fuel empty: apply natural downward gravity
            player.isJetpacking = false
            player.vy += GRAVITY * dt
            if (player.vy > TERMINAL_FALL_VELOCITY) {
                player.vy = TERMINAL_FALL_VELOCITY
            }

            // If not thrusting and not grounded, still tick down recharge delay
            if (player.jetpackRechargeDelayMs > 0) {
                player.jetpackRechargeDelayMs = (player.jetpackRechargeDelayMs - (dt * 1000f).toLong()).coerceAtLeast(0L)
            }
        }
    }

    /**
     * Applies constant gravity or counter-gravity jetpack thrust (helper for unit tests).
     */
    fun applyGravity(player: PlayerState, isThrusting: Boolean, dt: Float) {
        if (isThrusting && (player.jetpackFuel > 0f || player.superJetpackTimerMs > 0L)) {
            val thrustAcc = if (player.superJetpackTimerMs > 0L) SUPER_JETPACK_THRUST else BASE_JETPACK_THRUST
            player.vy -= thrustAcc * dt
            val maxSpeed = if (player.superJetpackTimerMs > 0L) BOOSTED_MAX_UPWARD_SPEED else MAX_UPWARD_SPEED
            if (player.vy < maxSpeed) {
                player.vy = maxSpeed
            }
            if (player.superJetpackTimerMs <= 0L) {
                player.jetpackFuel = (player.jetpackFuel - BASE_FUEL_BURN_RATE * dt).coerceAtLeast(0f)
            }
        } else {
            player.vy += GRAVITY * dt
            if (player.vy > TERMINAL_FALL_VELOCITY) {
                player.vy = TERMINAL_FALL_VELOCITY
            }
        }
    }

    /**
     * Applies horizontal movement velocity with ground friction / air drag.
     */
    fun applyHorizontalMovement(player: PlayerState, moveX: Float, dt: Float) {
        val baseSpeed = 400f * player.hero.speedMultiplier
        if (abs(moveX) > 0.15f) {
            player.vx = moveX * baseSpeed
        } else {
            val friction = if (player.isGrounded) GROUND_FRICTION else AIR_DRAG
            player.vx *= friction
            if (abs(player.vx) < 1f) player.vx = 0f
        }
    }

    /**
     * Detects and resolves AABB collisions with platforms and world boundaries.
     */
    fun resolveCollisions(
        player: PlayerState,
        platforms: List<ArenaPlatform>,
        worldWidth: Float,
        worldHeight: Float,
        prevY: Float,
        dt: Float,
        isDropThroughRequested: Boolean = false
    ): CollisionResult {
        var grounded = false
        var hitCeil = false
        var hitWall = false

        val halfW = CHARACTER_HALF_WIDTH
        val halfH = CHARACTER_HEIGHT / 2f
        val playerFootY = player.y + halfH
        val playerHeadY = player.y - halfH
        val prevFootY = prevY + halfH

        // 1. Platform Collisions
        for (plat in platforms) {
            val b = plat.bounds

            // Check if player horizontally overlaps with platform
            val horizontalOverlap = (player.x + halfW > b.left) && (player.x - halfW < b.right)
            if (horizontalOverlap) {
                // Landing on platform floor (from above)
                if (player.vy >= 0f) {
                    val wasAbove = prevFootY <= b.top + 14f
                    val isAtOrBelow = playerFootY >= b.top && playerFootY <= b.bottom

                    val dropThrough = plat.isOneWay && isDropThroughRequested
                    if (wasAbove && isAtOrBelow && !dropThrough) {
                        player.y = b.top - halfH
                        player.vy = 0f
                        grounded = true
                    }
                }

                // Hitting ceiling (from below) - applies only to solid (non-one-way) platforms
                if (!plat.isOneWay && player.vy < 0f) {
                    val wasBelow = (prevY - halfH) >= b.bottom - 10f
                    val isAtOrAbove = playerHeadY <= b.bottom && playerHeadY >= b.top

                    if (wasBelow && isAtOrAbove) {
                        player.y = b.bottom + halfH
                        player.vy = 0f
                        hitCeil = true
                    }
                }
            }

            // Lateral Wall Collisions (Left & Right) - applies only to solid platforms
            if (!plat.isOneWay) {
                val verticalOverlap = (player.y + halfH > b.top + 6f) && (player.y - halfH < b.bottom - 6f)
                if (verticalOverlap) {
                    // Moving right into left wall
                    if (player.vx > 0f && (player.x + halfW >= b.left) && (player.x < b.left)) {
                        player.x = b.left - halfW
                        player.vx = 0f
                        hitWall = true
                    }
                    // Moving left into right wall
                    else if (player.vx < 0f && (player.x - halfW <= b.right) && (player.x > b.right)) {
                        player.x = b.right + halfW
                        player.vx = 0f
                        hitWall = true
                    }
                }
            }
        }

        // 2. World Boundaries Collision
        if (player.x < WORLD_MARGIN_X) {
            player.x = WORLD_MARGIN_X
            player.vx = 0f
            hitWall = true
        } else if (player.x > worldWidth - WORLD_MARGIN_X) {
            player.x = worldWidth - WORLD_MARGIN_X
            player.vx = 0f
            hitWall = true
        }

        if (player.y < WORLD_MARGIN_TOP) {
            player.y = WORLD_MARGIN_TOP
            player.vy = 0f
            hitCeil = true
        } else if (player.y > worldHeight - WORLD_MARGIN_BOTTOM) {
            player.y = worldHeight - WORLD_MARGIN_BOTTOM
            player.vy = 0f
            grounded = true
        }

        return CollisionResult(
            isGrounded = grounded,
            hitCeiling = hitCeil,
            hitWall = hitWall
        )
    }

    /**
     * Helper to get a player's Axis-Aligned Bounding Box (AABB).
     */
    fun getPlayerBounds(player: PlayerState): Rect {
        val halfW = CHARACTER_HALF_WIDTH
        val halfH = CHARACTER_HEIGHT / 2f
        return Rect(
            left = player.x - halfW,
            top = player.y - halfH,
            right = player.x + halfW,
            bottom = player.y + halfH
        )
    }
}
