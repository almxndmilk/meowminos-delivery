package ca.sfu.spring2026team15;

import org.junit.Test;
import static org.junit.Assert.*;

public class TimerTest {

    /**
     * Tests that newly created timer starts at 0
     */
    @Test
    public void testTimerStartsAtZero() {
        Timer t = new Timer();
        assertEquals(0, t.getElapsedSeconds());
    }

    /**
     * Check that time accumalates correctly after starting
     */
    @Test
    public void testTimerAccumulatesTime() {
        Timer t = new Timer();
        t.start();
        t.update(5.0f);
        assertEquals(5, t.getElapsedSeconds());
    }

    /**
     * Test that timer stops after being paused
     */
    @Test
    public void testTimerPauseStopsAccumulation() {
        Timer t = new Timer();
        t.start();
        t.update(3.0f);
        t.pause();
        t.update(2.0f); // should not count
        assertEquals(3, t.getElapsedSeconds());
    }
}