package com.example

import androidx.compose.ui.graphics.Color
import com.example.model.Particle
import com.example.model.ParticleType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying particle data model and particle type attributes.
 */
class ParticleSystemTest {

    @Test
    fun particle_creationAndPhysicsLifecycle() {
        val particle = Particle(
            x = 100f,
            y = 100f,
            vx = 50f,
            vy = -50f,
            color = Color(0xFF00E5FF),
            size = 8f,
            maxLife = 1.0f,
            life = 1.0f,
            type = ParticleType.STAR_SPARKLE,
            rotation = 45f,
            vRot = 180f
        )

        assertEquals(ParticleType.STAR_SPARKLE, particle.type)
        assertEquals(100f, particle.x)
        assertEquals(100f, particle.y)
        assertEquals(1.0f, particle.alpha)

        // Simulate 0.5s dt update
        val dt = 0.5f
        particle.life -= dt
        particle.x += particle.vx * dt
        particle.y += particle.vy * dt
        particle.rotation += particle.vRot * dt
        particle.alpha = particle.life / particle.maxLife

        assertEquals(125f, particle.x)
        assertEquals(75f, particle.y)
        assertEquals(135f, particle.rotation)
        assertEquals(0.5f, particle.alpha, 0.001f)
        assertTrue(particle.life > 0)
    }

    @Test
    fun particleType_allTypesSupported() {
        val types = ParticleType.values()
        assertTrue(types.contains(ParticleType.NEON_CIRCLE))
        assertTrue(types.contains(ParticleType.STAR_SPARKLE))
        assertTrue(types.contains(ParticleType.NEON_RING))
        assertTrue(types.contains(ParticleType.LIGHT_STREAK))
    }
}
