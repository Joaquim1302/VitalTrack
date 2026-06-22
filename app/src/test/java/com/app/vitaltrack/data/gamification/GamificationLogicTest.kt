package com.app.vitaltrack.data.gamification

import org.junit.Assert.assertEquals
import org.junit.Test

class GamificationLogicTest {

    @Test
    fun `level calculation should be correct`() {
        assertEquals(1, GamificationRules.calculateLevel(0))
        assertEquals(1, GamificationRules.calculateLevel(99))
        assertEquals(2, GamificationRules.calculateLevel(100))
        assertEquals(2, GamificationRules.calculateLevel(249))
        assertEquals(3, GamificationRules.calculateLevel(250))
        assertEquals(4, GamificationRules.calculateLevel(500))
        assertEquals(5, GamificationRules.calculateLevel(1000))
    }

    @Test
    fun `level progress calculation should be correct`() {
        // Level 1: 0 to 100
        assertEquals(0f, GamificationRules.calculateLevelProgress(0))
        assertEquals(0.5f, GamificationRules.calculateLevelProgress(50))
        
        // Level 2: 100 to 250 (range 150)
        assertEquals(0f, GamificationRules.calculateLevelProgress(100))
        assertEquals(1/3f, GamificationRules.calculateLevelProgress(150), 0.01f)
        
        // Level 5: Max
        assertEquals(1f, GamificationRules.calculateLevelProgress(1000))
    }

    @Test
    fun `level names should be correct`() {
        assertEquals("Iniciante", GamificationRules.getLevelName(1))
        assertEquals("Em movimento", GamificationRules.getLevelName(2))
        assertEquals("Focado", GamificationRules.getLevelName(3))
        assertEquals("Consistente", GamificationRules.getLevelName(4))
        assertEquals("Atleta VitalTrack", GamificationRules.getLevelName(5))
    }
}
