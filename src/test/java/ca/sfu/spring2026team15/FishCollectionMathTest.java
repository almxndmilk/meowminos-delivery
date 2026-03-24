package ca.sfu.spring2026team15;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Unit tests for the fish collection distance math used in GameScreen.
 * A fish is collected when the player is within 60 pixels of it.
 * These tests check the distance calculation without needing LibGDX.
 */
public class FishCollectionMathTest {

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
     * Checks if a fish position is within collection range of the player.
     */
    private boolean inRange(float playerX, float playerY, float fishX, float fishY) {
        float dx = fishX - playerX;
        float dy = fishY - playerY;
        return dx * dx + dy * dy < COLLECT_RADIUS * COLLECT_RADIUS;
    }

    /**
     * A fish at the exact same position as the player should be collected.
     */
    @Test
    public void testFishAtSamePositionIsCollected() {
        assertTrue(inRange(100f, 100f, 100f, 100f));
    }

    /**
     * A fish 59 pixels away should be within collection range.
     */
    @Test
    public void testFish59pxAwayIsCollected() {
        assertTrue(inRange(100f, 100f, 159f, 100f));
    }

    /**
     * A fish 61 pixels away should be outside collection range.
     */
    @Test
    public void testFish61pxAwayIsNotCollected() {
        assertFalse(inRange(100f, 100f, 161f, 100f));
    }

    /**
     * A fish exactly 60 pixels away should not be collected.
     * The check uses strict less-than, so the boundary itself does not count.
     */
    @Test
    public void testFishExactly60pxAwayIsNotCollected() {
        assertFalse(inRange(100f, 100f, 160f, 100f));
    }

    /**
     * A fish within range diagonally should still be collected.
     */
    @Test
    public void testFishWithinRangeDiagonally() {
        // roughly 42px diagonal distance
        assertTrue(inRange(100f, 100f, 130f, 130f));
    }

    /**
     * A fish outside range diagonally should not be collected.
     */
    @Test
    public void testFishOutsideRangeDiagonally() {
        // roughly 85px diagonal distance
        assertFalse(inRange(100f, 100f, 160f, 160f));
    }

    /**
     * Collection range should work correctly with negative coordinates.
     */
    @Test
    public void testNegativeCoordinatesWorkCorrectly() {
        assertTrue(inRange(-100f, -100f, -100f, -100f));
    }

    /**
     * A fish on the left side of the player should be collected if within range.
     */
    @Test
    public void testFishToTheLeftIsCollected() {
        assertTrue(inRange(100f, 100f, 50f, 100f));
    }

    /**
     * A fish below the player should be collected if within range.
     */
    @Test
    public void testFishBelowIsCollected() {
        assertTrue(inRange(100f, 100f, 100f, 50f));
    }

    /**
     * Collecting a fish should increase the fish count by 1.
     */
    @Test
    public void testCollectingFishIncrementsCount() {
        boolean inRange = inRange(100f, 100f, 120f, 100f);
        if (inRange) fishCollected++;
        assertEquals(1, fishCollected);
    }

    /**
     * A fish out of range should not change the fish count.
     */
    @Test
    public void testMissingFishDoesNotIncrementCount() {
        boolean inRange = inRange(100f, 100f, 200f, 100f);
        if (inRange) fishCollected++;
        assertEquals(0, fishCollected);
    }

    /**
     * Collecting multiple fish should increment the count for each one.
     */
    @Test
    public void testCollectingMultipleFishIncrementsCorrectly() {
        float playerX = 100f, playerY = 100f;
        float[][] fishPositions = {
                {110f, 100f},
                {120f, 100f},
                {130f, 100f}
        };
        for (float[] pos : fishPositions) {
            if (inRange(playerX, playerY, pos[0], pos[1])) fishCollected++;
        }
        assertEquals(3, fishCollected);
    }
}