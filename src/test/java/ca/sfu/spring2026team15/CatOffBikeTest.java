package ca.sfu.spring2026team15;


import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for CatOffBike — movement, speed, walkToward.
 * Covers: mount/dismount, WASD + character movement, Obama scene + character,
 *         puddle + character (speed).
 */
public class CatOffBikeTest extends GdxTestSetup {

    // --- single feature: mount/dismount ---

    @Test
    public void catOffBikeInitialPositionCorrect() {
        CatOffBike cat = new CatOffBike(300f, 400f, true);
        assertEquals(300f, cat.getCenterX(), 0.01f);
        assertEquals(400f, cat.getCenterY(), 0.01f);
    }

    @Test
    public void setPositionMovesOffBikeCat() {
        CatOffBike cat = new CatOffBike(0f, 0f, true);
        cat.setPosition(150f, 250f);
        assertEquals(150f, cat.getCenterX(), 0.01f);
        assertEquals(250f, cat.getCenterY(), 0.01f);
    }

    // --- interaction: Obama scene + character (walkToward) ---

    @Test
    public void walkTowardMovesCloserToTarget() {
        CatOffBike cat = new CatOffBike(0f, 0f, true);
        cat.walkToward(100f, 0f, 0.1f, 250f);
        assertTrue(cat.getCenterX() > 0f);
    }

    @Test
    public void walkTowardStopsWhenWithin2Units() {
        CatOffBike cat = new CatOffBike(99f, 0f, true);
        cat.walkToward(100f, 0f, 0.1f, 250f); // distance = 1 < 2 — should not move
        assertEquals(99f, cat.getCenterX(), 0.01f);
    }

    @Test
    public void walkTowardMovesInYDirectionCorrectly() {
        CatOffBike cat = new CatOffBike(0f, 0f, true);
        cat.walkToward(0f, 100f, 0.1f, 250f);
        assertTrue(cat.getCenterY() > 0f);
    }

    // --- puddle + character: speed slowing ---

    @Test
    public void setSpeedSlowsCatOffBike() {
        CatOffBike cat = new CatOffBike(0f, 0f, true);
        cat.setSpeed(70f);
        cat.walkToward(1000f, 0f, 1f, 70f);
        // At 70 speed for 1s — should be close to 70
        assertTrue(cat.getCenterX() <= 70f + 1f);
    }

    @Test
    public void resetSpeedRestoresDefaultSpeed() {
        CatOffBike cat = new CatOffBike(0f, 0f, true);
        cat.setSpeed(70f);
        cat.resetSpeed();
        cat.walkToward(1000f, 0f, 1f, 250f);
        // At 250 speed for 1s — should be around 250
        assertTrue(cat.getCenterX() >= 200f);
    }

    // --- walkToward diagonal movement ---

    @Test
    public void walkTowardDiagonalDxGreaterThanDy() {
        // dx=100, dy=10 => abs(dx) > abs(dy) => direction should be RIGHT
        CatOffBike cat = new CatOffBike(0f, 0f, true);
        cat.walkToward(100f, 10f, 0.1f, 250f);
        assertTrue(cat.getCenterX() > 0f);
        assertTrue(cat.getCenterY() > 0f);
    }

    @Test
    public void walkTowardDiagonalDyGreaterThanDx() {
        // dx=10, dy=100 => abs(dy) > abs(dx) => direction should be UP
        CatOffBike cat = new CatOffBike(0f, 0f, true);
        cat.walkToward(10f, 100f, 0.1f, 250f);
        assertTrue(cat.getCenterX() > 0f);
        assertTrue(cat.getCenterY() > 0f);
    }

    // --- walkToward negative directions ---

    @Test
    public void walkTowardNegativeXDirectionMovesLeft() {
        CatOffBike cat = new CatOffBike(100f, 0f, true);
        cat.walkToward(0f, 0f, 0.1f, 250f);
        assertTrue(cat.getCenterX() < 100f);
    }

    @Test
    public void walkTowardNegativeYDirectionMovesDown() {
        CatOffBike cat = new CatOffBike(0f, 100f, true);
        cat.walkToward(0f, 0f, 0.1f, 250f);
        assertTrue(cat.getCenterY() < 100f);
    }

    @Test
    public void walkTowardDiagonalNegativeBothDirections() {
        CatOffBike cat = new CatOffBike(200f, 200f, true);
        cat.walkToward(0f, 0f, 0.1f, 250f);
        assertTrue(cat.getCenterX() < 200f);
        assertTrue(cat.getCenterY() < 200f);
    }

    // --- multiple walkToward calls simulating animation frames cycling ---

    @Test
    public void multipleWalkTowardCallsProgressTowardTarget() {
        CatOffBike cat = new CatOffBike(0f, 0f, true);
        for (int i = 0; i < 10; i++) {
            cat.walkToward(500f, 0f, 0.05f, 250f);
        }
        // 10 * 0.05s * 250 = 125 units moved
        assertEquals(125f, cat.getCenterX(), 1f);
    }

    @Test
    public void multipleWalkTowardCallsCycleAnimationFrames() {
        CatOffBike cat = new CatOffBike(0f, 0f, true);
        // FRAME_DURATION is 0.12f; calling with 0.13f delta should trigger frame advance each call
        for (int i = 0; i < 5; i++) {
            cat.walkToward(1000f, 0f, 0.13f, 250f);
        }
        // No exception means animation cycling worked
        assertTrue(cat.getCenterX() > 0f);
    }

    @Test
    public void walkTowardDirectionChangeTriggers() {
        CatOffBike cat = new CatOffBike(0f, 0f, true);
        // First walk right
        cat.walkToward(100f, 0f, 0.1f, 250f);
        // Then walk up — direction change should reset animation
        cat.walkToward(cat.getCenterX(), 100f, 0.1f, 250f);
        assertTrue(cat.getCenterY() > 0f);
    }

    // --- walkToward respects maxing out step to distance ---

    @Test
    public void walkTowardStepCappedToDistance() {
        CatOffBike cat = new CatOffBike(0f, 0f, true);
        // Target is 10 units away but speed*delta = 250*1 = 250, should cap to 10
        cat.walkToward(10f, 0f, 1f, 250f);
        assertEquals(10f, cat.getCenterX(), 0.5f);
    }

    @Test
    public void walkTowardDoesNotOvershootTarget() {
        CatOffBike cat = new CatOffBike(0f, 0f, true);
        cat.walkToward(5f, 0f, 1f, 1000f);
        // Should stop at target, not overshoot
        assertEquals(5f, cat.getCenterX(), 0.5f);
    }

    // --- getX() method ---

    @Test
    public void getXReturnsSameAsCenterX() {
        CatOffBike cat = new CatOffBike(42f, 99f, true);
        assertEquals(42f, cat.getX(), 0.01f);
        assertEquals(cat.getCenterX(), cat.getX(), 0.01f);
    }

    @Test
    public void getXAfterSetPosition() {
        CatOffBike cat = new CatOffBike(0f, 0f, true);
        cat.setPosition(777f, 888f);
        assertEquals(777f, cat.getX(), 0.01f);
    }
}
