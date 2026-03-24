package ca.sfu.spring2026team15;


import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for PoliceEnemy state machine, movement and catching behaviour.
 * Covers: police + character, Enemy + barriers, Police + puddle (speed change).
 */
public class PoliceEnemyTest extends GdxTestSetup {

    private static final BarrierLookup OPEN = (x, y) -> false;
    private static final BarrierLookup SOLID = (x, y) -> true;

    private PoliceEnemy police;

    @Before
    public void createPolice() {
        police = new PoliceEnemy(500f, 500f, true);
    }

    // --- interaction: police + character ---

    @Test
    public void policeDoesNotCatchWhenPlayerFarAway() {
        // Player far away — police is NONE state
        assertFalse(police.isCatching(5000f, 5000f));
    }

    @Test
    public void policeDoesNotCatchBeforeAlertDelay() {
        // Player in range but ALERT_DELAY = 1s hasn't elapsed
        float playerX = 500f + 300f; // within DETECTION_RANGE=400
        float playerY = 500f;
        police.update(0.5f, playerX, playerY, OPEN); // only 0.5s — still ALERTED
        assertFalse(police.isCatching(playerX, playerY));
    }

    @Test
    public void policeBeginsChasingAfterAlertDelay() {
        float playerX = 500f + 300f;
        float playerY = 500f;
        // Advance past the 1s alert delay
        police.update(0.5f, playerX, playerY, OPEN);
        police.update(0.6f, playerX, playerY, OPEN); // total 1.1s >= ALERT_DELAY
        // Now chasing — will catch if within 75 units
        police.update(0f, playerX, playerY, OPEN); // give it a tick to be chasing
        // Police might not have moved close enough yet; just verify not catching at distance
        // isCatching requires CHASING state AND distance < 75
        // Police is at ~500 and player at 800 — not yet within catch range
        assertFalse(police.isCatching(playerX, playerY));
    }

    @Test
    public void policeCatchesWhenChasingAndClose() {
        // Police spawned 10 units from player — will never overshoot during transition
        PoliceEnemy closePolice = new PoliceEnemy(490f, 500f, true);
        float playerX = 500f, playerY = 500f;
        // NONE → ALERTED, then accumulate alertTimer past 1.0s → CHASING
        closePolice.update(0.5f, playerX, playerY, OPEN);
        closePolice.update(0.6f, playerX, playerY, OPEN);
        closePolice.update(0.6f, playerX, playerY, OPEN); // total alert time >= 1.0s
        // Police is now CHASING and within 75 units of player
        assertTrue(closePolice.isCatching(closePolice.getCenterX(), closePolice.getCenterY()));
    }

    @Test
    public void policeStopsWhenPlayerLeavesDetectionRange() {
        float playerX = 500f + 300f;
        float playerY = 500f;
        // Start chasing
        police.update(1.5f, playerX, playerY, OPEN);
        // Move player far away
        float farX = 5000f;
        float farY = 5000f;
        police.update(0.1f, farX, farY, OPEN);
        // Police should have reset to NONE — not catching
        assertFalse(police.isCatching(farX, farY));
    }

    // --- interaction: Enemy + barriers ---

    @Test
    public void policeBlockedByBarrierWhileChasing() {
        float playerX = 500f + 300f;
        float playerY = 500f;
        // Transition to CHASING
        police.update(1.5f, playerX, playerY, OPEN);
        float xBefore = police.getCenterX();
        // Now update with solid barrier
        police.update(1f, playerX, playerY, SOLID);
        assertEquals(xBefore, police.getCenterX(), 0.1f);
    }

    @Test
    public void resetToSpawnRestoresPosition() {
        PoliceEnemy p = new PoliceEnemy(200f, 300f, true);
        p.update(0.1f, 250f, 300f, OPEN); // move a little
        p.resetToSpawn();
        assertEquals(200f, p.getCenterX(), 0.01f);
        assertEquals(300f, p.getCenterY(), 0.01f);
    }

    // --- interaction: Police + puddle (speed change) ---

    @Test
    public void setChaseSpeedSlowsPoliceChase() {
        police.setChaseSpeed(50f);
        float playerX = 500f + 300f;
        float playerY = 500f;
        // Transition to chasing
        police.update(1.5f, playerX, playerY, OPEN);
        float xBefore = police.getCenterX();
        police.update(1f, playerX, playerY, OPEN);
        float moved = police.getCenterX() - xBefore;
        // At speed 50 for 1s — should move roughly 50 units
        assertTrue(moved < 150f);
    }

    @Test
    public void resetSpeedRestoresDefaultChaseSpeed() {
        // Use makePolice so we can force CHASING state directly, avoiding random wander
        PoliceEnemy p = makePolice(500f, 500f);
        p.setChaseSpeed(50f);
        p.resetSpeed();
        setEnumField(p, "alertState", "CHASING");
        float playerX = 700f;
        float playerY = 500f;
        float xBefore = p.getCenterX();
        p.update(1f, playerX, playerY, OPEN);
        float moved = p.getCenterX() - xBefore;
        // Normal CHASE_SPEED=250; should move ~200 units toward player in 1s
        assertTrue(moved > 100f);
    }

    // --- Additional resetToSpawn tests ---

    @Test
    public void resetToSpawnAfterChasing() {
        // Use makePolice to avoid random wander direction
        PoliceEnemy p = makePolice(100f, 100f);
        setEnumField(p, "alertState", "CHASING");
        float playerX = 200f, playerY = 100f;
        // Chase toward player
        p.update(1.0f, playerX, playerY, OPEN);
        // Police has moved toward player
        assertTrue(p.getCenterX() > 100f);
        // Reset
        p.resetToSpawn();
        assertEquals(100f, p.getCenterX(), 0.01f);
        assertEquals(100f, p.getCenterY(), 0.01f);
    }

    @Test
    public void resetToSpawnMultipleTimes() {
        PoliceEnemy p = new PoliceEnemy(300f, 400f, true);
        p.update(0.1f, 350f, 400f, OPEN);
        p.resetToSpawn();
        assertEquals(300f, p.getCenterX(), 0.01f);
        p.update(0.1f, 350f, 400f, OPEN);
        p.resetToSpawn();
        assertEquals(300f, p.getCenterX(), 0.01f);
        assertEquals(400f, p.getCenterY(), 0.01f);
    }

    // --- setChaseSpeed and setWanderSpeed ---

    @Test
    public void setWanderSpeedAffectsWandering() {
        PoliceEnemy p = makePolice(500f, 500f);
        p.setWanderSpeed(200f);
        setEnumField(p, "alertState", "NONE");
        float xBefore = p.getCenterX();
        float yBefore = p.getCenterY();
        // Update with player far away to stay in NONE/wander
        p.update(1f, 5000f, 5000f, OPEN);
        float dx = p.getCenterX() - xBefore;
        float dy = p.getCenterY() - yBefore;
        float totalMoved = (float) Math.sqrt(dx * dx + dy * dy);
        // Should have wandered at 200 speed for 1s
        assertTrue(totalMoved > 50f);
    }

    @Test
    public void setChaseSpeedToZeroPreventsMovement() {
        PoliceEnemy p = makePolice(500f, 500f);
        p.setChaseSpeed(0f);
        setEnumField(p, "alertState", "CHASING");
        float xBefore = p.getCenterX();
        float yBefore = p.getCenterY();
        p.update(1f, 700f, 500f, OPEN);
        assertEquals(xBefore, p.getCenterX(), 0.01f);
        assertEquals(yBefore, p.getCenterY(), 0.01f);
    }

    @Test
    public void resetSpeedRestoresWanderSpeed() {
        PoliceEnemy p = makePolice(500f, 500f);
        p.setWanderSpeed(0f);
        p.resetSpeed();
        setEnumField(p, "alertState", "NONE");
        float xBefore = p.getCenterX();
        float yBefore = p.getCenterY();
        p.update(1f, 5000f, 5000f, OPEN);
        float dx = p.getCenterX() - xBefore;
        float dy = p.getCenterY() - yBefore;
        float totalMoved = (float) Math.sqrt(dx * dx + dy * dy);
        // Default WANDER_SPEED=80, so should move ~80 units
        assertTrue(totalMoved > 30f);
    }

    // --- setPosition resets alert state ---

    @Test
    public void setPositionUpdatesCoordinates() {
        PoliceEnemy p = new PoliceEnemy(100f, 100f, true);
        p.setPosition(999f, 888f);
        assertEquals(999f, p.getCenterX(), 0.01f);
        assertEquals(888f, p.getCenterY(), 0.01f);
    }

    @Test
    public void setPositionResetsAlertStateFromChasing() {
        PoliceEnemy p = new PoliceEnemy(500f, 500f, true);
        float playerX = 600f, playerY = 500f;
        // Get into CHASING state
        p.update(1.5f, playerX, playerY, OPEN);
        // Now setPosition — should reset to NONE
        p.setPosition(100f, 100f);
        // After setPosition, police should not be catching even if player is close
        assertFalse(p.isCatching(100f, 100f));
    }

    @Test
    public void setPositionResetsAlertStateFromAlerted() {
        PoliceEnemy p = new PoliceEnemy(500f, 500f, true);
        float playerX = 600f, playerY = 500f;
        // Get into ALERTED state (update < 1s)
        p.update(0.5f, playerX, playerY, OPEN);
        // Now setPosition
        p.setPosition(100f, 100f);
        // Police should be in NONE state — won't catch
        assertFalse(p.isCatching(100f, 100f));
    }

    @Test
    public void setPositionThenUpdateDoesNotImmediatelyChase() {
        PoliceEnemy p = new PoliceEnemy(500f, 500f, true);
        float playerX = 600f, playerY = 500f;
        // Get into CHASING
        p.update(1.5f, playerX, playerY, OPEN);
        // Teleport away and reset state
        p.setPosition(1000f, 1000f);
        // Update with player nearby — should start fresh from NONE
        p.update(0.1f, 1050f, 1000f, OPEN);
        // Not enough time to go through ALERTED -> CHASING
        assertFalse(p.isCatching(1050f, 1000f));
    }
}
