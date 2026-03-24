package ca.sfu.spring2026team15;


import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for Fish.
 * Covers: Count fish, Player + fish interaction.
 */
public class FishTest extends GdxTestSetup {

    // --- single feature: Count fish ---

    @Test
    public void fishStartsUncollected() {
        Fish fish = new Fish(100f, 100f, true);
        assertFalse(fish.isCollected());
    }

    @Test
    public void collectMarksFishAsCollected() {
        Fish fish = new Fish(100f, 100f, true);
        fish.collect();
        assertTrue(fish.isCollected());
    }

    @Test
    public void fishPositionStoredCorrectly() {
        Fish fish = new Fish(250f, 350f, true);
        assertEquals(250f, fish.getCenterX(), 0.01f);
        assertEquals(350f, fish.getCenterY(), 0.01f);
    }

    // --- interaction: Player + fish ---

    @Test
    public void collectCalledTwiceStaysCollected() {
        Fish fish = new Fish(0f, 0f, true);
        fish.collect();
        fish.collect();
        assertTrue(fish.isCollected());
    }

    @Test
    public void multipleFishAreIndependent() {
        Fish f1 = new Fish(0f, 0f, true);
        Fish f2 = new Fish(500f, 500f, true);
        f1.collect();
        assertFalse(f2.isCollected());
    }
}
