package ca.sfu.spring2026team15;

import org.junit.Before;
import org.junit.Test;
import org.objenesis.ObjenesisStd;

import java.lang.reflect.Field;

import static org.junit.Assert.*;

/**
 * Tests for CinematicBars cinematic letterbox animation.
 * CinematicBars creates black bars that slide in from top/bottom (100px each),
 * taking 0.5s for the full transition. Uses a state machine: HIDDEN → APPEARING → VISIBLE → DISAPPEARING.
 * Tests cover state transitions, animation timing, and reversals.
 */
public class CinematicBarsTest extends GdxTestSetup {

    private static final float BAR_HEIGHT = 100f;
    private static final float ANIM_DURATION = 0.5f;
    private static final float DELTA = 0.01f;

    private CinematicBars bars;
    private ObjenesisStd objenesis;

    @Before
    public void setUp() {
        objenesis = new ObjenesisStd();
        bars = objenesis.newInstance(CinematicBars.class);
        // Initialize private fields to known state
        setEnumField(bars, "state", "HIDDEN");
        setField(bars, "timer", 0f);
        setField(bars, "currentHeight", 0f);
    }

    // ---- Reflection helpers ----

    private String getState(CinematicBars b) {
        return getField(b, "state").toString();
    }

    private float getTimer(CinematicBars b) {
        return (float) getField(b, "timer");
    }

    private float getCurrentHeight(CinematicBars b) {
        return (float) getField(b, "currentHeight");
    }

    private Object getField(Object obj, String name) {
        try {
            Field f = findField(obj.getClass(), name);
            f.setAccessible(true);
            return f.get(obj);
        } catch (Exception e) {
            throw new RuntimeException("Could not get field '" + name + "'", e);
        }
    }

    private static Field findField(Class<?> clazz, String name) throws NoSuchFieldException {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }

    // ---- Tests ----

    @Test
    public void freshInstanceStartsHidden() {
        assertEquals("HIDDEN", getState(bars));
        assertFalse(bars.isFullyVisible());
    }

    @Test
    public void freshInstanceHasZeroHeight() {
        assertEquals(0f, getCurrentHeight(bars), DELTA);
    }

    @Test
    public void freshInstanceHasZeroTimer() {
        assertEquals(0f, getTimer(bars), DELTA);
    }

    @Test
    public void showOnHiddenStartsAppearing() {
        bars.show();
        assertEquals("APPEARING", getState(bars));
    }

    @Test
    public void showOnHiddenResetsTimerToZero() {
        bars.show();
        // Initially timer is 0 and currentHeight is 0, so timer should be set to 0
        assertEquals(0f, getTimer(bars), DELTA);
    }

    @Test
    public void showOnDisappearingTransitionsToAppearing() {
        // Set up state as DISAPPEARING with partial height
        setEnumField(bars, "state", "DISAPPEARING");
        setField(bars, "currentHeight", 50f);
        setField(bars, "timer", 0f);

        bars.show();
        assertEquals("APPEARING", getState(bars));
    }

    @Test
    public void showOnDisappearingResumesFromCurrentHeight() {
        // At midway point of disappearing animation
        setEnumField(bars, "state", "DISAPPEARING");
        setField(bars, "currentHeight", 50f);
        setField(bars, "timer", 0f);

        bars.show();
        // timer should be set to resume from current height: (50 / 100) * 0.5 = 0.25
        assertEquals(0.25f, getTimer(bars), DELTA);
    }

    @Test
    public void showOnAppearingIsNoOp() {
        setEnumField(bars, "state", "APPEARING");
        setField(bars, "timer", 0.1f);

        bars.show();
        // Should remain in APPEARING state and timer should not change
        assertEquals("APPEARING", getState(bars));
        assertEquals(0.1f, getTimer(bars), DELTA);
    }

    @Test
    public void showOnVisibleIsNoOp() {
        setEnumField(bars, "state", "VISIBLE");
        setField(bars, "timer", 0.5f);

        bars.show();
        assertEquals("VISIBLE", getState(bars));
        assertEquals(0.5f, getTimer(bars), DELTA);
    }

    @Test
    public void hideOnVisibleStartsDisappearing() {
        setEnumField(bars, "state", "VISIBLE");
        setField(bars, "currentHeight", 100f);

        bars.hide();
        assertEquals("DISAPPEARING", getState(bars));
    }

    @Test
    public void hideOnVisibleResetsTimer() {
        setEnumField(bars, "state", "VISIBLE");
        setField(bars, "currentHeight", 100f);
        setField(bars, "timer", 0.5f);

        bars.hide();
        // timer = 0.5 * (1 - 100/100) = 0
        assertEquals(0f, getTimer(bars), DELTA);
    }

    @Test
    public void hideOnAppearingStartsDisappearing() {
        setEnumField(bars, "state", "APPEARING");
        setField(bars, "currentHeight", 50f);
        setField(bars, "timer", 0.25f);

        bars.hide();
        assertEquals("DISAPPEARING", getState(bars));
    }

    @Test
    public void hideOnAppearingResumesFromCurrentHeight() {
        setEnumField(bars, "state", "APPEARING");
        setField(bars, "currentHeight", 50f);

        bars.hide();
        // timer = 0.5 * (1 - 50/100) = 0.25
        assertEquals(0.25f, getTimer(bars), DELTA);
    }

    @Test
    public void hideOnHiddenIsNoOp() {
        setEnumField(bars, "state", "HIDDEN");
        setField(bars, "timer", 0f);

        bars.hide();
        assertEquals("HIDDEN", getState(bars));
        assertEquals(0f, getTimer(bars), DELTA);
    }

    @Test
    public void hideOnDisappearingIsNoOp() {
        setEnumField(bars, "state", "DISAPPEARING");
        setField(bars, "timer", 0.2f);

        bars.hide();
        assertEquals("DISAPPEARING", getState(bars));
        assertEquals(0.2f, getTimer(bars), DELTA);
    }

    @Test
    public void updateOnHiddenIsNoOp() {
        setEnumField(bars, "state", "HIDDEN");
        setField(bars, "timer", 0f);
        setField(bars, "currentHeight", 0f);

        bars.update(0.1f);
        assertEquals("HIDDEN", getState(bars));
        assertEquals(0f, getTimer(bars), DELTA);
        assertEquals(0f, getCurrentHeight(bars), DELTA);
    }

    @Test
    public void updateOnVisibleIsNoOp() {
        setEnumField(bars, "state", "VISIBLE");
        setField(bars, "timer", 0.5f);
        setField(bars, "currentHeight", 100f);

        bars.update(0.1f);
        assertEquals("VISIBLE", getState(bars));
        assertEquals(0.5f, getTimer(bars), DELTA);
        assertEquals(100f, getCurrentHeight(bars), DELTA);
    }

    @Test
    public void updateAppearingIncreasesHeight() {
        setEnumField(bars, "state", "APPEARING");
        setField(bars, "timer", 0f);
        setField(bars, "currentHeight", 0f);

        bars.update(0.1f);
        // After 0.1s: timer = 0.1, currentHeight = (0.1 / 0.5) * 100 = 20
        assertEquals(0.1f, getTimer(bars), DELTA);
        assertEquals(20f, getCurrentHeight(bars), DELTA);
    }

    @Test
    public void updateAppearingMidway() {
        setEnumField(bars, "state", "APPEARING");
        setField(bars, "timer", 0f);
        setField(bars, "currentHeight", 0f);

        bars.update(0.25f);
        // After 0.25s: timer = 0.25, currentHeight = (0.25 / 0.5) * 100 = 50
        assertEquals(0.25f, getTimer(bars), DELTA);
        assertEquals(50f, getCurrentHeight(bars), DELTA);
        assertEquals("APPEARING", getState(bars));
    }

    @Test
    public void updateAppearingCompletesAfterFullDuration() {
        setEnumField(bars, "state", "APPEARING");
        setField(bars, "timer", 0f);
        setField(bars, "currentHeight", 0f);

        bars.update(0.6f);
        // After 0.6s: timer = 0.6 (exceeds 0.5), state = VISIBLE, currentHeight = 100
        assertEquals("VISIBLE", getState(bars));
        assertEquals(100f, getCurrentHeight(bars), DELTA);
    }

    @Test
    public void updateAppearingStopsAtBarHeight() {
        setEnumField(bars, "state", "APPEARING");
        setField(bars, "timer", 0.4f);
        setField(bars, "currentHeight", 80f);

        bars.update(0.15f); // Total timer = 0.55s, but clamped to BAR_HEIGHT
        assertEquals("VISIBLE", getState(bars));
        assertEquals(100f, getCurrentHeight(bars), DELTA);
    }

    @Test
    public void updateDisappearingDecreasesHeight() {
        setEnumField(bars, "state", "DISAPPEARING");
        setField(bars, "timer", 0f);
        setField(bars, "currentHeight", 100f);

        bars.update(0.1f);
        // After 0.1s: timer = 0.1, currentHeight = 100 * (1 - 0.1/0.5) = 100 * 0.8 = 80
        assertEquals(0.1f, getTimer(bars), DELTA);
        assertEquals(80f, getCurrentHeight(bars), DELTA);
    }

    @Test
    public void updateDisappearingMidway() {
        setEnumField(bars, "state", "DISAPPEARING");
        setField(bars, "timer", 0f);
        setField(bars, "currentHeight", 100f);

        bars.update(0.25f);
        // After 0.25s: timer = 0.25, currentHeight = 100 * (1 - 0.25/0.5) = 50
        assertEquals(0.25f, getTimer(bars), DELTA);
        assertEquals(50f, getCurrentHeight(bars), DELTA);
        assertEquals("DISAPPEARING", getState(bars));
    }

    @Test
    public void updateDisappearingCompletesAfterFullDuration() {
        setEnumField(bars, "state", "DISAPPEARING");
        setField(bars, "timer", 0f);
        setField(bars, "currentHeight", 100f);

        bars.update(0.6f);
        // After 0.6s: timer = 0.6 (exceeds 0.5), state = HIDDEN, currentHeight = 0
        assertEquals("HIDDEN", getState(bars));
        assertEquals(0f, getCurrentHeight(bars), DELTA);
    }

    @Test
    public void updateDisappearingStopsAtZero() {
        setEnumField(bars, "state", "DISAPPEARING");
        setField(bars, "timer", 0.4f);
        setField(bars, "currentHeight", 20f);

        bars.update(0.15f); // Total timer = 0.55s, but clamped to 0
        assertEquals("HIDDEN", getState(bars));
        assertEquals(0f, getCurrentHeight(bars), DELTA);
    }

    @Test
    public void isFullyVisibleTrueOnlyInVisibleState() {
        setEnumField(bars, "state", "VISIBLE");
        assertTrue(bars.isFullyVisible());
    }

    @Test
    public void isFullyVisibleFalseOnHidden() {
        setEnumField(bars, "state", "HIDDEN");
        assertFalse(bars.isFullyVisible());
    }

    @Test
    public void isFullyVisibleFalseOnAppearing() {
        setEnumField(bars, "state", "APPEARING");
        assertFalse(bars.isFullyVisible());
    }

    @Test
    public void isFullyVisibleFalseOnDisappearing() {
        setEnumField(bars, "state", "DISAPPEARING");
        assertFalse(bars.isFullyVisible());
    }

    @Test
    public void fullShowCycleUpdatesAllStates() {
        bars.show();
        assertEquals("APPEARING", getState(bars));
        assertEquals(0f, getCurrentHeight(bars), DELTA);

        bars.update(0.25f);
        assertEquals("APPEARING", getState(bars));
        assertEquals(50f, getCurrentHeight(bars), DELTA);

        bars.update(0.3f);
        assertEquals("VISIBLE", getState(bars));
        assertEquals(100f, getCurrentHeight(bars), DELTA);
        assertTrue(bars.isFullyVisible());
    }

    @Test
    public void fullHideCycleUpdatesAllStates() {
        setEnumField(bars, "state", "VISIBLE");
        setField(bars, "currentHeight", 100f);

        bars.hide();
        assertEquals("DISAPPEARING", getState(bars));
        assertEquals(100f, getCurrentHeight(bars), DELTA);

        bars.update(0.25f);
        assertEquals("DISAPPEARING", getState(bars));
        assertEquals(50f, getCurrentHeight(bars), DELTA);

        bars.update(0.3f);
        assertEquals("HIDDEN", getState(bars));
        assertEquals(0f, getCurrentHeight(bars), DELTA);
        assertFalse(bars.isFullyVisible());
    }

    @Test
    public void reversalFromMidwayAppearingToDisappearing() {
        bars.show();
        bars.update(0.25f);
        assertEquals("APPEARING", getState(bars));
        assertEquals(50f, getCurrentHeight(bars), DELTA);

        bars.hide();
        // timer = ANIM_DURATION * (1 - currentHeight/BAR_HEIGHT) = 0.5 * (1 - 50/100) = 0.25
        assertEquals("DISAPPEARING", getState(bars));
        assertEquals(0.25f, getTimer(bars), DELTA);
        assertEquals(50f, getCurrentHeight(bars), DELTA);

        bars.update(0.2f);
        // timer = 0.25 + 0.2 = 0.45, height = 100 * (1 - 0.45/0.5) = 10
        assertEquals(10f, getCurrentHeight(bars), DELTA);
    }

    @Test
    public void reversalFromMidwayDisappearingToAppearing() {
        setEnumField(bars, "state", "DISAPPEARING");
        setField(bars, "currentHeight", 50f);
        setField(bars, "timer", 0f);

        bars.update(0.125f);
        // After 0.125s: currentHeight = 100 * (1 - 0.125/0.5) = 75
        assertEquals(75f, getCurrentHeight(bars), DELTA);

        bars.show();
        // timer = (75 / 100) * 0.5 = 0.375
        assertEquals("APPEARING", getState(bars));
        assertEquals(0.375f, getTimer(bars), DELTA);

        bars.update(0.15f);
        // timer = 0.375 + 0.15 = 0.525, height = (0.525 / 0.5) * 100, clamped to 100
        assertEquals("VISIBLE", getState(bars));
        assertEquals(100f, getCurrentHeight(bars), DELTA);
    }

    @Test
    public void multipleShowCallsWhileAppearingAreNoOp() {
        bars.show();
        float timerAfterFirst = getTimer(bars);
        float heightAfterFirst = getCurrentHeight(bars);

        bars.update(0.1f);
        bars.show(); // Should be no-op while APPEARING
        float timerAfterSecond = getTimer(bars);
        float heightAfterSecond = getCurrentHeight(bars);

        // Timer and height should have advanced only from update, not reset
        assertTrue(timerAfterSecond > timerAfterFirst);
        assertTrue(heightAfterSecond > heightAfterFirst);
    }

    @Test
    public void accumulatedDeltasProduceCorrectHeight() {
        bars.show();
        bars.update(0.1f);
        assertEquals(20f, getCurrentHeight(bars), DELTA);

        bars.update(0.1f);
        assertEquals(40f, getCurrentHeight(bars), DELTA);

        bars.update(0.1f);
        assertEquals(60f, getCurrentHeight(bars), DELTA);

        bars.update(0.2f);
        assertEquals("VISIBLE", getState(bars));
        assertEquals(100f, getCurrentHeight(bars), DELTA);
    }

    @Test
    public void zeroUpdateDoesNotChangeState() {
        bars.show();
        bars.update(0f);
        assertEquals("APPEARING", getState(bars));
        assertEquals(0f, getCurrentHeight(bars), DELTA);
    }

    @Test
    public void verySmallUpdateAdvancesHeightMinimally() {
        bars.show();
        bars.update(0.01f);
        float height = getCurrentHeight(bars);
        // (0.01 / 0.5) * 100 = 2
        assertEquals(2f, height, DELTA);
    }
}
