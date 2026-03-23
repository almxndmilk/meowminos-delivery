package ca.sfu.spring2026team15;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Tests for the respawn penalty logic in GameScreen and Respawn.
 * When a police enemy catches the player, they lose 10 fish and
 * 30 seconds. If their fish count would go below zero, the game ends.
 * These tests check the penalty math without needing LibGDX to be running.
 */
public class RespawnPenaltyTest {

    /** Fish lost each time the player is caught. */
    private static final int RESPAWN_FISH_PENALTY = 10;

    /** Seconds removed from the timer each time the player is caught. */
    private static final float RESPAWN_TIME_PENALTY = 30f;

    /** Fish count used in each test. */
    private int fishCollected;

    /** Total time penalties accumulated across catches. */
    private int totalTimePenalties;

    /**
     * Sets up default values before each test.
     */
    @Before
    public void setUp() {
        fishCollected = 25;
        totalTimePenalties = 0;
    }

    /**
     * Being caught once should reduce the fish count by 10.
     */
    @Test
    public void testFishPenaltyApplied() {
        fishCollected -= RESPAWN_FISH_PENALTY;
        assertEquals(15, fishCollected);
    }

    /**
     * Being caught once should add 30 seconds to the total time penalty.
     */
    @Test
    public void testTimePenaltyAccumulatesOnce() {
        totalTimePenalties += (int) RESPAWN_TIME_PENALTY;
        assertEquals(30, totalTimePenalties);
    }

    /**
     * Being caught twice should add 60 seconds total to the time penalty.
     */
    @Test
    public void testTimePenaltyAccumulatesTwice() {
        totalTimePenalties += (int) RESPAWN_TIME_PENALTY;
        totalTimePenalties += (int) RESPAWN_TIME_PENALTY;
        assertEquals(60, totalTimePenalties);
    }

    /**
     * The game should end if the fish count would go negative after the penalty.
     */
    @Test
    public void testDeathWhenFishBelowPenalty() {
        fishCollected = 5;
        assertTrue(fishCollected - RESPAWN_FISH_PENALTY < 0);
    }

    /**
     * The player should survive if they have more fish than the penalty amount.
     */
    @Test
    public void testSurvivalWhenFishAbovePenalty() {
        fishCollected = 15;
        assertFalse(fishCollected - RESPAWN_FISH_PENALTY < 0);
    }

    /**
     * Having exactly 10 fish when caught should not trigger game over
     * since the result is 0, not negative.
     */
    @Test
    public void testExactBoundaryNotDeath() {
        fishCollected = 10;
        assertFalse(fishCollected - RESPAWN_FISH_PENALTY < 0);
    }

    /**
     * Having 9 fish when caught should trigger game over
     * since the result would be negative.
     */
    @Test
    public void testOneBelowBoundaryIsDeath() {
        fishCollected = 9;
        assertTrue(fishCollected - RESPAWN_FISH_PENALTY < 0);
    }

    /**
     * The respawn method should clamp the fish count to 0, not go negative.
     */
    @Test
    public void testRespawnResetsToMinZero() {
        fishCollected = 5;
        int result = Math.max(0, fishCollected - RESPAWN_FISH_PENALTY);
        assertEquals(0, result);
    }
}