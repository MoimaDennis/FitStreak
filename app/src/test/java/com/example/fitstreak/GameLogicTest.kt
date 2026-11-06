package com.example.fitstreak

import com.example.fitstreak.util.GameLogic
import org.junit.Assert.assertEquals
import org.junit.Test

class GameLogicTest {
    @Test fun levelStartsAtOneAndRisesEvery500Xp() {
        assertEquals(1, GameLogic.levelFor(0))
        assertEquals(1, GameLogic.levelFor(499))
        assertEquals(2, GameLogic.levelFor(500))
        assertEquals(3, GameLogic.levelFor(1000))
    }

    @Test fun negativeXpIsTreatedAsZero() = assertEquals(1, GameLogic.levelFor(-50))

    @Test fun levelProgressPercent() {
        assertEquals(0, GameLogic.levelProgressPercent(500))
        assertEquals(50, GameLogic.levelProgressPercent(250))
    }

    @Test fun streakProgressIsCappedAt100() {
        assertEquals(50, GameLogic.streakProgressPercent(15))
        assertEquals(100, GameLogic.streakProgressPercent(60))
    }

    @Test fun xpForWorkoutUsesDifficultyMultiplier() {
        assertEquals(100, GameLogic.xpForWorkout(600, "easy"))
        assertEquals(150, GameLogic.xpForWorkout(600, "Medium"))
        assertEquals(200, GameLogic.xpForWorkout(600, "HARD"))
        assertEquals(150, GameLogic.xpForWorkout(600, "something-else"))
    }

    @Test fun xpHasAMinimumOf10() = assertEquals(10, GameLogic.xpForWorkout(30, "easy"))

    @Test fun timerFormatting() {
        assertEquals("01:05", GameLogic.formatTimer(65))
        assertEquals("00:00", GameLogic.formatTimer(-5))
    }
}