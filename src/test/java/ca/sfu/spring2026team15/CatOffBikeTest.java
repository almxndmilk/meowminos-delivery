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
}
