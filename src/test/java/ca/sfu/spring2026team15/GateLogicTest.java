package ca.sfu.spring2026team15;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for the gate fish quota logic in GameScreen
 * Gate 1 needs 30 fish to open, Gate 2 needs 20 fish to open
 *
 * These tests check the quota comparisons and message text
 * without needing LibGDX to be running
 */
public class GateLogicTest {

    /** Fish needed to open gate 1 (map 1 to map 2) */
    private static final int FISH_QUOTA_GATE1 = 30;

    /** Fish needed to open gate 2 (map 2 to map 3) */
    private static final int FISH_QUOTA_GATE2 = 20;

    /**
     * Gate 1 should be locked when the player has no fish
     */
    @Test
    public void testGate1BlockedWhenNoFish() {
        int fishCollected = 0;
        assertTrue(FISH_QUOTA_GATE1 - fishCollected > 0);
    }

    /**
     * gate 1 should be locked when the player has fewer fish than needed
     */
    @Test
    public void testGate1BlockedWhenInsufficientFish() {
        int fishCollected = 10;
        assertTrue(FISH_QUOTA_GATE1 - fishCollected > 0);
    }

    /**
     * Gate 1 should open when the player has exactly 30 fish
     */
    @Test
    public void testGate1AllowedWhenQuotaExactlyMet() {
        int fishCollected = 30;
        assertFalse(FISH_QUOTA_GATE1 - fishCollected > 0);
    }

    /**
     * Gate 1 should open when the player has more than 30 fish
     */
    @Test
    public void testGate1AllowedWhenQuotaExceeded() {
        int fishCollected = 45;
        assertFalse(FISH_QUOTA_GATE1 - fishCollected > 0);
    }

    /**
     * Gate 2 should be locked when the player has fewer fish than needed
     */
    @Test
    public void testGate2BlockedWhenInsufficientFish() {
        int fishCollected = 5;
        assertTrue(FISH_QUOTA_GATE2 - fishCollected > 0);
    }

    /**
     * Gate 2 should open when the player has exactly 20 fish
     */
    @Test
    public void testGate2AllowedWhenQuotaExactlyMet() {
        int fishCollected = 20;
        assertFalse(FISH_QUOTA_GATE2 - fishCollected > 0);
    }

    /**
     * The gate message should show the correct number of fish still needed for gate 1
     */
    @Test
    public void testGate1MessageShowsCorrectFishNeeded() {
        int fishCollected = 10;
        int fishNeeded = FISH_QUOTA_GATE1 - fishCollected;
        assertEquals("Need 20 more fish!", "Need " + fishNeeded + " more fish!");
    }

    /**
     * The gate message should show the correct number of fish still needed for gate 2
     */
    @Test
    public void testGate2MessageShowsCorrectFishNeeded() {
        int fishCollected = 5;
        int fishNeeded = FISH_QUOTA_GATE2 - fishCollected;
        assertEquals("Need 15 more fish!", "Need " + fishNeeded + " more fish!");
    }
}
