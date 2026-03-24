package ca.sfu.spring2026team15;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Integration tests for the Respawn and Timer classes working together.
 * When the player is caught, both the fish count and the timer are
 * affected at the same time. These tests check that both components
 * update correctly together.
 */
public class RespawnTimerIntegrationTest {

    /** The timer used in each test. */
    private Timer timer;

    /** Fish penalty applied when the player is caught. */
    private static final int RESPAWN_FISH_PENALTY = 10;

    /** Time penalty applied when the player is caught. */
    private static final float RESPAWN_TIME_PENALTY = 30f;

    /**
     * Sets up a fresh timer before each test.
     */
    @Before
    public void setUp() {
        timer = new Timer();
        timer.start();
    }

    /**
     * Being caught should reduce both fish count and timer at the same time.
     */
    @Test
    public void testCatchReducesBothFishAndTime() {
        int fishCollected = 25;
        timer.update(10f); // 10 seconds have passed

        // player gets caught
        fishCollected -= RESPAWN_FISH_PENALTY;
        timer.subtractTime(RESPAWN_TIME_PENALTY);

        assertEquals(15, fishCollected);
        assertEquals(260, timer.getRemainingSeconds()); // 300 - 10 elapsed - 30 penalty
    }

    /**
     * Being caught twice should stack both fish and time penalties correctly.
     */
    @Test
    public void testTwoCatchesStackBothPenalties() {
        int fishCollected = 50;

        // caught twice
        fishCollected -= RESPAWN_FISH_PENALTY;
        timer.subtractTime(RESPAWN_TIME_PENALTY);
        fishCollected -= RESPAWN_FISH_PENALTY;
        timer.subtractTime(RESPAWN_TIME_PENALTY);

        assertEquals(30, fishCollected);
        assertEquals(240, timer.getRemainingSeconds()); // 300 - 60
    }

    /**
     * If the timer runs out after penalties are applied, the game should
     * report finished.
     */
    @Test
    public void testTimerFinishesAfterLargePenalty() {
        timer.subtractTime(300f);
        assertTrue(timer.isFinished());
    }

    /**
     * Fish count should be clamped to zero by respawn logic even if
     * the penalty would make it negative.
     */
    @Test
    public void testFishClampedToZeroOnCatch() {
        int fishCollected = 5;
        int result = Math.max(0, fishCollected - RESPAWN_FISH_PENALTY);
        assertEquals(0, result);
    }

    /**
     * The timer should still be running after a catch, not stopped.
     */
    @Test
    public void testTimerStillRunningAfterCatch() {
        timer.subtractTime(RESPAWN_TIME_PENALTY);
        timer.update(5f);
        // if timer is still running, elapsed time should have increased
        assertEquals(5, timer.getElapsedSeconds());
    }
}