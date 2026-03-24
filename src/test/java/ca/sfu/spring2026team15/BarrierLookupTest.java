package ca.sfu.spring2026team15;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for BarrierLookup — pure lambda, no LibGDX required.
 * Covers: collision system (barrier detection).
 */
public class BarrierLookupTest {

    // --- single feature: Collision system ---

    @Test
    public void alwaysBarrierLambdaBlocksEverything() {
        BarrierLookup solid = (x, y) -> true;
        assertTrue(solid.isBarrier(0, 0));
        assertTrue(solid.isBarrier(500, 500));
    }

    @Test
    public void neverBarrierLambdaBlocksNothing() {
        BarrierLookup open = (x, y) -> false;
        assertFalse(open.isBarrier(0, 0));
        assertFalse(open.isBarrier(999, 999));
    }

    @Test
    public void regionalBarrierBlocksOnlyInZone() {
        BarrierLookup wall = (x, y) -> x >= 100 && x <= 200;
        assertTrue(wall.isBarrier(150, 0));
        assertFalse(wall.isBarrier(50, 0));
        assertFalse(wall.isBarrier(250, 0));
    }

    @Test
    public void barrierLookupIsFunctionalInterface() {
        // Confirm it can be used as a lambda
        BarrierLookup lookup = (x, y) -> x < 0 || y < 0;
        assertTrue(lookup.isBarrier(-1, 0));
        assertFalse(lookup.isBarrier(10, 10));
    }
}
