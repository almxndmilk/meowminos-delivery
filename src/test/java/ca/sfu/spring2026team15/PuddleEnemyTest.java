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
}
