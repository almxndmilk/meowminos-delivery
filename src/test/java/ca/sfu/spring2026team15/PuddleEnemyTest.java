package ca.sfu.spring2026team15;


import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for PuddleEnemy.onPuddle detection.
 * Covers: puddle + character, Police + puddle.
 */
public class PuddleEnemyTest extends GdxTestSetup {

    // --- interaction: puddle + character ---

    @Test
    public void playerOnPuddleWhenAtCenter() {
        PuddleEnemy puddle = new PuddleEnemy(500f, 500f, true);
        assertTrue(puddle.onPuddle(500f, 500f));
    }

    @Test
    public void playerNotOnPuddleWhenFarAway() {
        PuddleEnemy puddle = new PuddleEnemy(500f, 500f, true);
        assertFalse(puddle.onPuddle(700f, 700f));
    }

    @Test
    public void playerNotOnPuddleWhenBelowBottomEdge() {
        PuddleEnemy puddle = new PuddleEnemy(500f, 500f, true);
        // puddleBottomY = 500 - 100/75 ≈ 498.67; player Y must be > that
        assertFalse(puddle.onPuddle(500f, 490f));
    }

    @Test
    public void playerOnPuddleWhenWithinRadius() {
        PuddleEnemy puddle = new PuddleEnemy(500f, 500f, true);
        // player at 60 units away — within the 67-unit radius
        assertTrue(puddle.onPuddle(500f + 60f, 500f));
    }

    @Test
    public void playerNotOnPuddleJustOutsideRadius() {
        PuddleEnemy puddle = new PuddleEnemy(500f, 500f, true);
        // player at 70 units away — outside the 67-unit radius
        assertFalse(puddle.onPuddle(500f + 70f, 500f));
    }

    // --- Edge cases around Y-bottom check ---

    @Test
    public void playerJustAboveBottomEdgeIsOnPuddle() {
        PuddleEnemy puddle = new PuddleEnemy(500f, 500f, true);
        // puddleBottomY = 500 - 100/75 = 498.667
        // player at Y=499 is above puddleBottomY and within radius
        assertTrue(puddle.onPuddle(500f, 499f));
    }

    @Test
    public void playerExactlyAtBottomEdgeIsNotOnPuddle() {
        PuddleEnemy puddle = new PuddleEnemy(500f, 500f, true);
        // puddleBottomY = 500 - 100/75 ≈ 498.667
        // player at Y=498 is below puddleBottomY
        assertFalse(puddle.onPuddle(500f, 498f));
    }

    @Test
    public void playerSlightlyBelowPuddleCenterStillOnPuddle() {
        PuddleEnemy puddle = new PuddleEnemy(500f, 500f, true);
        // Y=499 is above 498.67 and close enough (dist < 67)
        assertTrue(puddle.onPuddle(500f, 499f));
    }

    // --- Test with different puddle positions ---

    @Test
    public void puddleAtOriginDetectsPlayerCorrectly() {
        PuddleEnemy puddle = new PuddleEnemy(0f, 0f, true);
        assertTrue(puddle.onPuddle(0f, 0f));
    }

    @Test
    public void puddleAtOriginRejectsDistantPlayer() {
        PuddleEnemy puddle = new PuddleEnemy(0f, 0f, true);
        assertFalse(puddle.onPuddle(100f, 100f));
    }

    @Test
    public void puddleAtLargeCoordinatesDetectsCorrectly() {
        PuddleEnemy puddle = new PuddleEnemy(3000f, 2000f, true);
        assertTrue(puddle.onPuddle(3000f, 2000f));
        assertFalse(puddle.onPuddle(3100f, 2000f));
    }

    @Test
    public void puddleAtNegativeCoordinatesWorksCorrectly() {
        PuddleEnemy puddle = new PuddleEnemy(-200f, -300f, true);
        assertTrue(puddle.onPuddle(-200f, -300f));
        assertFalse(puddle.onPuddle(0f, 0f));
    }

    // --- Test the exact boundary at 67-unit radius ---

    @Test
    public void playerAt66UnitsIsOnPuddle() {
        PuddleEnemy puddle = new PuddleEnemy(500f, 500f, true);
        // Distance check uses puddle center offset by +5 in Y: (500, 505)
        // Player at (566, 505) => distance = 66 < 67, and Y > 498.67
        assertTrue(puddle.onPuddle(566f, 505f));
    }

    @Test
    public void playerAt67UnitsIsNotOnPuddle() {
        PuddleEnemy puddle = new PuddleEnemy(500f, 500f, true);
        // Player at (567, 505) => distance = 67, not < 67
        assertFalse(puddle.onPuddle(567f, 505f));
    }

    @Test
    public void playerAbovePuddleWithinRadius() {
        PuddleEnemy puddle = new PuddleEnemy(500f, 500f, true);
        // Player above puddle center: (500, 560) => dist from (500,505) = 55 < 67
        // Y=560 > 498.67 — on puddle
        assertTrue(puddle.onPuddle(500f, 560f));
    }

    @Test
    public void playerAbovePuddleOutsideRadius() {
        PuddleEnemy puddle = new PuddleEnemy(500f, 500f, true);
        // Player above puddle: (500, 580) => dist from (500,505) = 75 > 67
        assertFalse(puddle.onPuddle(500f, 580f));
    }
}
