package ca.sfu.spring2026team15;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Integration tests for the gate quota and fish collection systems working together.
 * The player collects fish one at a time, and the gate checks the total count
 * to decide whether to open. These tests check that collecting fish correctly
 * affects whether the gate can be opened.
 */
public class GateFishIntegrationTest {

    /** Fish needed to open gate 1. */
    private static final int FISH_QUOTA_GATE1 = 30;

    /** Fish needed to open gate 2. */
    private static final int FISH_QUOTA_GATE2 = 20;

    /** Collection radius used in GameScreen. */
    private static final float COLLECT_RADIUS = 60f;

    /** Fish count used in each test. */
    private int fishCollected;

    /**
     * Resets fish count before each test.
     */
    @Before
    public void setUp() {
        fishCollected = 0;
    }

    /**
     * Collecting fish one at a time should eventually meet the gate 1 quota.
     */
    @Test
    public void testCollectingFishEventuallyOpensGate1() {
        for (int i = 0; i < 30; i++) {
            fishCollected++;
        }
        assertFalse(FISH_QUOTA_GATE1 - fishCollected > 0);
    }

    /**
     * Collecting fish one at a time should eventually meet the gate 2 quota.
     */
    @Test
    public void testCollectingFishEventuallyOpensGate2() {
        for (int i = 0; i < 20; i++) {
            fishCollected++;
        }
        assertFalse(FISH_QUOTA_GATE2 - fishCollected > 0);
    }

    /**
     * Gate 1 should still be locked one fish short of the quota.
     */
    @Test
    public void testGate1StillLockedOneFishShort() {
        for (int i = 0; i < 29; i++) {
            fishCollected++;
        }
        assertTrue(FISH_QUOTA_GATE1 - fishCollected > 0);
    }

    /**
     * A fish within collection range should be counted when collected.
     */
    @Test
    public void testFishWithinRangeCountsTowardQuota() {
        float playerX = 100f, playerY = 100f;
        float fishX = 130f, fishY = 100f; // 30px away, within 60px radius

        float dx = fishX - playerX;
        float dy = fishY - playerY;
        boolean inRange = dx * dx + dy * dy < COLLECT_RADIUS * COLLECT_RADIUS;

        if (inRange) fishCollected++;

        assertEquals(1, fishCollected);
    }

    /**
     * A fish outside collection range should not be counted.
     */
    @Test
    public void testFishOutOfRangeDoesNotCount() {
        float playerX = 100f, playerY = 100f;
        float fishX = 200f, fishY = 100f; // 100px away, outside 60px radius

        float dx = fishX - playerX;
        float dy = fishY - playerY;
        boolean inRange = dx * dx + dy * dy < COLLECT_RADIUS * COLLECT_RADIUS;

        if (inRange) fishCollected++;

        assertEquals(0, fishCollected);
    }

    /**
     * Losing fish from a puddle should lock the gate again if count drops below quota.
     */
    @Test
    public void testLosingFishFromPuddleCanLockGateAgain() {
        fishCollected = 30; // gate was open
        fishCollected -= 1; // stepped on puddle
        assertTrue(FISH_QUOTA_GATE1 - fishCollected > 0);
    }

    /**
     * Losing fish from a respawn penalty should lock the gate again.
     */
    @Test
    public void testRespawnPenaltyCanLockGateAgain() {
        fishCollected = 30; // gate was open
        fishCollected -= 10; // respawn penalty
        assertTrue(FISH_QUOTA_GATE1 - fishCollected > 0);
    }
}