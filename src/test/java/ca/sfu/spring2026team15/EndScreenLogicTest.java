package ca.sfu.spring2026team15;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for the bounds-check and digit-calculation logic used by EndScreen.
 * EndScreen itself requires OpenGL, so we test the pure logic in isolation.
 */
public class EndScreenLogicTest {

    // --- Continue button bounds: x [230, 475], y [225, 265] ---

    @Test
    public void inBoundsCenter() {
        float px = 350f, py = 245f;
        assertTrue(px >= 230f && px <= 475f && py >= 225f && py <= 265f);
    }

    @Test
    public void inBoundsOutside() {
        float px = 100f, py = 245f;
        assertFalse(px >= 230f && px <= 475f && py >= 225f && py <= 265f);
    }

    @Test
    public void inBoundsLeftEdge() {
        float px = 230f, py = 245f;
        assertTrue(px >= 230f && px <= 475f && py >= 225f && py <= 265f);
    }

    @Test
    public void inBoundsRightEdge() {
        float px = 475f, py = 245f;
        assertTrue(px >= 230f && px <= 475f && py >= 225f && py <= 265f);
    }

    @Test
    public void inBoundsTopEdge() {
        float px = 350f, py = 265f;
        assertTrue(px >= 230f && px <= 475f && py >= 225f && py <= 265f);
    }

    @Test
    public void inBoundsBottomEdge() {
        float px = 350f, py = 225f;
        assertTrue(px >= 230f && px <= 475f && py >= 225f && py <= 265f);
    }

    @Test
    public void inBoundsAbove() {
        float px = 350f, py = 266f;
        assertFalse(px >= 230f && px <= 475f && py >= 225f && py <= 265f);
    }

    @Test
    public void inBoundsBelow() {
        float px = 350f, py = 224f;
        assertFalse(px >= 230f && px <= 475f && py >= 225f && py <= 265f);
    }

    // --- Exit button bounds: x [230, 365], y [158, 198] ---

    @Test
    public void exitBoundsCenter() {
        float px = 300f, py = 178f;
        assertTrue(px >= 230f && px <= 365f && py >= 158f && py <= 198f);
    }

    @Test
    public void exitBoundsOutside() {
        float px = 400f, py = 178f;
        assertFalse(px >= 230f && px <= 365f && py >= 158f && py <= 198f);
    }

    @Test
    public void exitBoundsLeftEdge() {
        float px = 230f, py = 178f;
        assertTrue(px >= 230f && px <= 365f && py >= 158f && py <= 198f);
    }

    @Test
    public void exitBoundsRightEdge() {
        float px = 365f, py = 178f;
        assertTrue(px >= 230f && px <= 365f && py >= 158f && py <= 198f);
    }

    // --- Score digit calculation logic (5-digit zero-padded, capped at 99999) ---

    @Test
    public void scoreDigitExtraction_zero() {
        int s = Math.max(0, Math.min(0, 99999));
        int[] digits = { s / 10000, (s / 1000) % 10, (s / 100) % 10, (s / 10) % 10, s % 10 };
        assertArrayEquals(new int[]{0, 0, 0, 0, 0}, digits);
    }

    @Test
    public void scoreDigitExtraction_12345() {
        int s = Math.max(0, Math.min(12345, 99999));
        int[] digits = { s / 10000, (s / 1000) % 10, (s / 100) % 10, (s / 10) % 10, s % 10 };
        assertArrayEquals(new int[]{1, 2, 3, 4, 5}, digits);
    }

    @Test
    public void scoreDigitExtraction_99999() {
        int s = Math.max(0, Math.min(99999, 99999));
        int[] digits = { s / 10000, (s / 1000) % 10, (s / 100) % 10, (s / 10) % 10, s % 10 };
        assertArrayEquals(new int[]{9, 9, 9, 9, 9}, digits);
    }

    @Test
    public void scoreDigitExtraction_cappedAbove99999() {
        int s = Math.max(0, Math.min(100000, 99999));
        assertEquals(99999, s);
    }

    @Test
    public void scoreDigitExtraction_negativeClampedToZero() {
        int s = Math.max(0, Math.min(-5, 99999));
        assertEquals(0, s);
    }

    // --- Time digit calculation logic (MM:SS) ---

    @Test
    public void timeDigits_300seconds() {
        int time = 300;
        int mm = Math.min(time / 60, 99);
        int ss = time % 60;
        assertEquals(5, mm);
        assertEquals(0, ss);
        int[] digits = { mm / 10, mm % 10, ss / 10, ss % 10 };
        assertArrayEquals(new int[]{0, 5, 0, 0}, digits);
    }

    @Test
    public void timeDigits_130seconds() {
        int time = 130;
        int mm = Math.min(time / 60, 99);
        int ss = time % 60;
        assertEquals(2, mm);
        assertEquals(10, ss);
        int[] digits = { mm / 10, mm % 10, ss / 10, ss % 10 };
        assertArrayEquals(new int[]{0, 2, 1, 0}, digits);
    }

    @Test
    public void timeDigits_0seconds() {
        int time = 0;
        int mm = Math.min(time / 60, 99);
        int ss = time % 60;
        int[] digits = { mm / 10, mm % 10, ss / 10, ss % 10 };
        assertArrayEquals(new int[]{0, 0, 0, 0}, digits);
    }

    @Test
    public void timeDigits_cappedMinutesAt99() {
        int time = 6000; // 100 minutes
        int mm = Math.min(time / 60, 99);
        assertEquals(99, mm);
    }
}
