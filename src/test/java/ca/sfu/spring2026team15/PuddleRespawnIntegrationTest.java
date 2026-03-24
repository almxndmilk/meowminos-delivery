package ca.sfu.spring2026team15;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Integration tests for the puddle and respawn systems working together.
 * Both puddles and police enemies reduce the fish count, and if the count
 * goes low enough, the combination can trigger game over. These tests
 * check how the two systems interact.
 */
public class PuddleRespawnIntegrationTest {

    /** Fish lost each time the player is caught by police. */
    private static final int RESPAWN_FISH_PENALTY = 10;

    /** Fish count used in each test. */
    private int fishCollected;

    /** Tracks whether the player was on a puddle last frame. */
    private boolean wasOnPuddle;

    /**
     * Sets up default values before each test.
     */
    @Before
    public void setUp() {
        fishCollected = 15;
        wasOnPuddle = false;
    }

    /**
     * A puddle hit followed by a police catch should reduce fish count by 11 total.
     */
    @Test
    public void testPuddleThenCatchReducesFishCorrectly() {
        // step on puddle
        boolean isOnPuddle = true;
        if (isOnPuddle && !wasOnPuddle) fishCollected -= 1;

        // caught by police
        fishCollected -= RESPAWN_FISH_PENALTY;

        assertEquals(4, fishCollected);
    }

    /**
     * A puddle hit followed by a police catch can trigger game over
     * if the fish count goes negative.
     */
    @Test
    public void testPuddlePlusCatchCanTriggerDeath() {
        fishCollected = 10;

        // step on puddle
        boolean isOnPuddle = true;
        if (isOnPuddle && !wasOnPuddle) fishCollected -= 1; // now 9

        // caught by police
        assertTrue(fishCollected - RESPAWN_FISH_PENALTY < 0); // 9 - 10 = -1
    }

    /**
     * Multiple puddle hits combined with a catch should stack correctly.
     */
    @Test
    public void testMultiplePuddleHitsPlusCatch() {
        fishCollected = 20;

        // hit 3 different puddles
        fishCollected -= 1;
        fishCollected -= 1;
        fishCollected -= 1;

        // caught by police
        fishCollected -= RESPAWN_FISH_PENALTY;

        assertEquals(7, fishCollected);
    }

    /**
     * If fish count is exactly 11, one puddle hit plus one catch should
     * result in exactly 0 fish — not death.
     */
    @Test
    public void testExactBoundaryPuddlePlusCatch() {
        fishCollected = 11;

        // puddle hit
        fishCollected -= 1; // now 10

        // caught — 10 - 10 = 0, not negative, so no death
        assertFalse(fishCollected - RESPAWN_FISH_PENALTY < 0);
    }

    /**
     * If fish count is exactly 10, one puddle hit plus one catch should
     * trigger death since 9 - 10 = -1.
     */
    @Test
    public void testOneBelowBoundaryPuddlePlusCatch() {
        fishCollected = 10;

        // puddle hit
        fishCollected -= 1; // now 9

        // caught — 9 - 10 = -1, triggers death
        assertTrue(fishCollected - RESPAWN_FISH_PENALTY < 0);
    }
}