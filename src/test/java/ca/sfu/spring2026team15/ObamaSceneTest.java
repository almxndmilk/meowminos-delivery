package ca.sfu.spring2026team15;


import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for Obama scene behaviour using CatOffBike.walkToward() —
 * the same math used in ObamaScreen.updatePlayerWalk().
 *
 * Covers: Obama scene + character.
 */
public class ObamaSceneTest extends GdxTestSetup {

    // --- interaction: Obama scene + character ---

    @Test
    public void catWalksTowardObamaFromLeft() {
        CatOffBike cat = new CatOffBike(0f, 400f, true);
        float obamaX = 640f;
        float obamaY = 400f;
        cat.walkToward(obamaX, obamaY, 0.5f, 200f);
        assertTrue(cat.getCenterX() > 0f);
        assertTrue(cat.getCenterX() <= obamaX);
    }

    @Test
    public void catReachesObamaAfterEnoughTime() {
        CatOffBike cat = new CatOffBike(0f, 400f, true);
        float obamaX = 100f;
        float obamaY = 400f;
        // At speed 200 for 2s — covers 400 units; target is only 100 away
        // walkToward stops when dist < 2; multiple small steps
        for (int i = 0; i < 20; i++) {
            cat.walkToward(obamaX, obamaY, 0.1f, 200f);
        }
        assertEquals(obamaX, cat.getCenterX(), 3f);
    }

    @Test
    public void catDoesNotOvershotTarget() {
        CatOffBike cat = new CatOffBike(0f, 400f, true);
        float obamaX = 50f;
        float obamaY = 400f;
        // Large delta — walkToward uses Math.min(speed*delta, dist) so no overshoot
        cat.walkToward(obamaX, obamaY, 10f, 200f);
        assertEquals(obamaX, cat.getCenterX(), 3f);
    }

    @Test
    public void catDoesNotMoveWhenAlreadyAtTarget() {
        CatOffBike cat = new CatOffBike(100f, 400f, true);
        // within 2 units — walkToward should return immediately
        cat.walkToward(101f, 400f, 1f, 200f);
        assertEquals(100f, cat.getCenterX(), 0.01f);
    }

    @Test
    public void catWalksInYDirectionTowardObama() {
        CatOffBike cat = new CatOffBike(400f, 0f, true);
        float obamaY = 300f;
        cat.walkToward(400f, obamaY, 0.5f, 200f);
        assertTrue(cat.getCenterY() > 0f);
    }
}
