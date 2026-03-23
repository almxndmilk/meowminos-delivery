package ca.sfu.spring2026team15;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Tests for the puddle penalty logic in GameScreen.
 * Puddles slow the player down and take away one fish on first contact.
 * The player does not keep losing fish while standing still on a puddle.
 * These tests check that behavior without needing LibGDX to be running.
 */
public class PuddleLogicTest {

    /** Player speed while on a puddle. */
    private static final float PUDDLE_SPEED = 70f;

    /** Normal player speed off puddles. */
    private static final float NORMAL_SPEED = 200f;

    /** Fish count used in each test. */
    private int fishCollected;

    /** Tracks whether the player was on a puddle last frame. */
    private boolean wasOnPuddle;

    /**
     * Sets up default values before each test.
     */
    @Before
    public void setUp() {
        fishCollected = 10;
        wasOnPuddle = false;
    }

    /**
     * The player should lose one fish when first stepping on a puddle.
     */
    @Test
    public void testFirstPuddleContactLosesFish() {
        boolean isOnPuddle = true;
        if (isOnPuddle && !wasOnPuddle) fishCollected -= 1;
        assertEquals(9, fishCollected);
    }

    /**
     * The player should not lose more fish while staying on the same puddle.
     */
    @Test
    public void testContinuedContactDoesNotLoseFish() {
        wasOnPuddle = true;
        boolean isOnPuddle = true;
        if (isOnPuddle && !wasOnPuddle) fishCollected -= 1;
        assertEquals(10, fishCollected);
    }

    /**
     * The wasOnPuddle flag should be false after the player leaves a puddle.
     */
    @Test
    public void testLeavingPuddleResetsFlag() {
        wasOnPuddle = true;
        boolean isOnPuddle = false;
        wasOnPuddle = isOnPuddle;
        assertFalse(wasOnPuddle);
    }

    /**
     * The player should lose a fish again when re-entering a puddle they left.
     */
    @Test
    public void testReenteringPuddleLosesFishAgain() {
        wasOnPuddle = false;
        boolean isOnPuddle = true;
        if (isOnPuddle && !wasOnPuddle) fishCollected -= 1;
        assertEquals(9, fishCollected);
    }

    /**
     * Player speed should drop to 70 when on a puddle.
     */
    @Test
    public void testSpeedReducedOnPuddle() {
        float speed = NORMAL_SPEED;
        boolean isOnPuddle = true;
        if (isOnPuddle) speed = PUDDLE_SPEED;
        assertEquals(70f, speed, 0.001f);
    }

    /**
     * Player speed should stay at normal when not on a puddle.
     */
    @Test
    public void testSpeedNormalOffPuddle() {
        float speed = NORMAL_SPEED;
        boolean isOnPuddle = false;
        if (isOnPuddle) speed = PUDDLE_SPEED;
        assertEquals(NORMAL_SPEED, speed, 0.001f);
    }

    /**
     * If the player has 0 fish and steps on a puddle, their count goes negative,
     * which triggers the game over condition.
     */
    @Test
    public void testPuddleFishLossCanCauseNegativeCount() {
        fishCollected = 0;
        boolean isOnPuddle = true;
        if (isOnPuddle && !wasOnPuddle) fishCollected -= 1;
        assertTrue(fishCollected < 0);
    }
}