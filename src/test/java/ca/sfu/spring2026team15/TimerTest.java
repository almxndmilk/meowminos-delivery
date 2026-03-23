package ca.sfu.spring2026team15;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for the Timer class
 * Tests timer state management, time accumulation,
 * pause/resume behaviour, subtraction, reset and formatted output
 */
public class TimerTest {

    /** The Timer being tested */
    private Timer timer;

    /**
     * Makes a new Timer before each test
     */
    @Before
    public void setUp() {
        timer = new Timer();
    }

    /**
     * Tests that the newly created timer starts at 0
     */
    @Test
    public void testTimerStartsAtZero() {
        assertEquals(0, timer.getElapsedSeconds());
    }

    /**
     * Verifies that the timer is made with 300 second (5-minute) countdown
     */
    @Test
    public void testTimerStartsWithFullTime() {
        assertEquals(300, timer.getRemainingSeconds());
    }

    /**
     * Updating the timer before starting it should not change elapsed time
     */
    @Test
    public void testTimerNotRunningByDefault() {
        timer.update(5.0f);
        assertEquals(0, timer.getElapsedSeconds());
    }

    /**
     * Check that time accumalates correctly after starting
     */
    @Test
    public void testTimerAccumulatesTime() {
        timer.start();
        timer.update(5.0f);
        assertEquals(5, timer.getElapsedSeconds());
    }

    /**
     * Test that timer stops after being paused
     */
    @Test
    public void testTimerPauseStopsAccumulation() {
        timer.start();
        timer.update(3.0f);
        timer.pause();
        timer.update(2.0f); // should not count
        assertEquals(3, timer.getElapsedSeconds());
    }

    /**
     * Elapsed time should continue increasing after the timer is resumed
     */
    @Test
    public void testTimerResumeAfterPause() {
        timer.start();
        timer.update(3.0f);
        timer.pause();
        timer.resume();
        timer.update(2.0f);
        assertEquals(5, timer.getElapsedSeconds());
    }

    /**
     * Remaining time should decrease by the same amount as elapsed time increases
     */
    @Test
    public void testTimerDecrementsRemaining() {
        timer.start();
        timer.update(10.0f);
        assertEquals(290, timer.getRemainingSeconds());
    }

    /**
     * The timer should report finished after all 300 seconds have passed
     */
    @Test
    public void testTimerIsFinishedWhenTimeRunsOut() {
        timer.start();
        timer.update(300.0f);
        assertTrue(timer.isFinished());
    }

    /**
     * The timer should not report finished when time is still remaining
     */
    @Test
    public void testTimerNotFinishedBeforeTimeRunsOut() {
        timer.start();
        timer.update(299.0f);
        assertFalse(timer.isFinished());
    }

    /**
     * Remaining time should never go below zero, even with a large update
     */
    @Test
    public void testTimerStopsAtZeroNotNegative() {
        timer.start();
        timer.update(999.0f);
        assertEquals(0, timer.getRemainingSeconds());
    }

    /**
     * Subtracting time should reduce the remaining seconds correctly
     */
    @Test
    public void testSubtractTimeReducesRemaining() {
        timer.subtractTime(30f);
        assertEquals(270, timer.getRemainingSeconds());
    }

    /**
     * Subtracting more time than remains should clamp to zero, not go negative
     */
    @Test
    public void testSubtractTimeDoesNotGoBelowZero() {
        timer.subtractTime(9999f);
        assertEquals(0, timer.getRemainingSeconds());
    }

    /**
     * Resetting the timer should restore full time and clear elapsed time
     */
    @Test
    public void testResetRestoresFullTime() {
        timer.start();
        timer.update(100f);
        timer.reset();
        assertEquals(300, timer.getRemainingSeconds());
        assertEquals(0, timer.getElapsedSeconds());
    }

    /**
     * A full timer should display "05:00"
     */
    @Test
    public void testFormattedOutputFullTime() {
        assertEquals("05:00", timer.getFormatted());
    }

    /**
     * After 65 seconds, the display should show "03:55"
     */
    @Test
    public void testFormattedOutputAfterUpdate() {
        timer.start();
        timer.update(65f);
        assertEquals("03:55", timer.getFormatted());
    }

}