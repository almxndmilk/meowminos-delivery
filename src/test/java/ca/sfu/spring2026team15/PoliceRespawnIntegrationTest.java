package ca.sfu.spring2026team15;

import com.badlogic.gdx.math.Vector2;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Integration test for interaction between PoliceEnemy, Respawn, Player, and Timer.
 * Tests the complete sequence: police detection → catch → respawn effects on player position,
 * fish count, timer, and police reset.
 *
 * Uses makePlayer() and makePolice() factory methods from GdxTestSetup to construct
 * entities without calling constructors. Pure Java Timer and Respawn classes require
 * no LibGDX rendering context.
 */
public class PoliceRespawnIntegrationTest extends GdxTestSetup {

    private static final float[] MAP_Y_OFFSETS = {0f, 902f, 1804f};

    private Player player;
    private PoliceEnemy police;
    private Timer timer;
    private List<PoliceEnemy> policeList;

    @Before
    public void setUp() {
        // Player starts in map 1 starting area
        player = makePlayer(500f, 500f);

        // Police spawned 50 units away (within catch range of 75)
        police = makePolice(550f, 500f);

        // Transition police to CHASING state for catch tests
        setEnumField(police, "alertState", "CHASING");

        // Fresh timer
        timer = new Timer();

        // Collect into list for respawn call
        policeList = Arrays.asList(police);
    }

    // --- Test 1: Police catching detection ---

    @Test
    public void testPoliceCatchesPlayerInRange() {
        // Police in CHASING state, 50 units from player (< 75 catch range)
        assertTrue("Police should catch player when in range and CHASING",
                police.isCatching(player.getCenterX(), player.getCenterY()));
    }

    @Test
    public void testPoliceNotCatchingWhenFarAway() {
        // Create police at far distance
        PoliceEnemy farPolice = makePolice(1000f, 1000f);
        setEnumField(farPolice, "alertState", "CHASING");

        // Player at 500, 500 → distance is ~707 units, >> 75
        assertFalse("Police should not catch player when far away",
                farPolice.isCatching(player.getCenterX(), player.getCenterY()));
    }

    @Test
    public void testPoliceNotCatchingWhenNotChasingEvenIfClose() {
        // Police is at default NONE state (not CHASING)
        PoliceEnemy noChasePolice = makePolice(550f, 500f);
        // Don't set alertState to CHASING

        // Even though 50 units away, should not catch because not CHASING
        assertFalse("Police should not catch when not in CHASING state",
                noChasePolice.isCatching(player.getCenterX(), player.getCenterY()));
    }

    // --- Test 2: Respawn reduces fish count ---

    @Test
    public void testRespawnReducesFishCount() {
        int result = Respawn.respawn(player, policeList, timer, 0, 50, MAP_Y_OFFSETS, 10, 30f);
        assertEquals("Fish should be reduced by penalty", 40, result);
    }

    @Test
    public void testRespawnReducesFishCountFloorAtZero() {
        int result = Respawn.respawn(player, policeList, timer, 0, 5, MAP_Y_OFFSETS, 10, 30f);
        assertEquals("Fish count should never go below zero", 0, result);
    }

    @Test
    public void testRespawnWithZeroFishAndPenalty() {
        int result = Respawn.respawn(player, policeList, timer, 0, 0, MAP_Y_OFFSETS, 10, 30f);
        assertEquals("Fish count at zero with penalty should stay at zero", 0, result);
    }

    @Test
    public void testRespawnWithLargePenaltyExceedsFish() {
        int result = Respawn.respawn(player, policeList, timer, 0, 15, MAP_Y_OFFSETS, 50, 30f);
        assertEquals("Large penalty should floor at zero, not go negative", 0, result);
    }

    // --- Test 3: Respawn subtracts time from timer ---

    @Test
    public void testRespawnSubtractsTimeFromTimer() {
        timer.start();
        timer.update(0f); // Ensure timer is running
        int result = Respawn.respawn(player, policeList, timer, 0, 50, MAP_Y_OFFSETS, 10, 30f);
        assertEquals("Timer should subtract penalty", 270, timer.getRemainingSeconds());
    }

    @Test
    public void testRespawnTimerDoesNotGoNegative() {
        timer.start();
        timer.update(0f);
        Respawn.respawn(player, policeList, timer, 0, 50, MAP_Y_OFFSETS, 10, 350f);
        assertEquals("Timer should floor at zero, not go negative", 0, timer.getRemainingSeconds());
    }

    @Test
    public void testRespawnWithZeroTimePenalty() {
        timer.start();
        timer.update(0f);
        Respawn.respawn(player, policeList, timer, 0, 50, MAP_Y_OFFSETS, 10, 0f);
        assertEquals("Timer should remain unchanged with zero penalty", 300, timer.getRemainingSeconds());
    }

    // --- Test 4: Respawn repositions player ---

    @Test
    public void testRespawnRepositionsPlayerToMap0Start() {
        Respawn.respawn(player, policeList, timer, 0, 50, MAP_Y_OFFSETS, 10, 30f);
        assertEquals("Player X should be 300 for map 0", 300f, player.getCenterX(), 0.01f);
        assertEquals("Player Y should be 300 for map 0", 300f, player.getCenterY(), 0.01f);
    }

    @Test
    public void testRespawnRepositionsPlayerToMap1Start() {
        Respawn.respawn(player, policeList, timer, 1, 50, MAP_Y_OFFSETS, 10, 30f);
        assertEquals("Player X should be 5600 for map 1", 5600f, player.getCenterX(), 0.01f);
        assertEquals("Player Y should be 902 + 100 = 1002 for map 1", MAP_Y_OFFSETS[1] + 100f, player.getCenterY(), 0.01f);
    }

    @Test
    public void testRespawnRepositionsPlayerToMap2Start() {
        Respawn.respawn(player, policeList, timer, 2, 50, MAP_Y_OFFSETS, 10, 30f);
        assertEquals("Player X should be 1250 for map 2", 1250f, player.getCenterX(), 0.01f);
        assertEquals("Player Y should be 1804 + 100 = 1904 for map 2", MAP_Y_OFFSETS[2] + 100f, player.getCenterY(), 0.01f);
    }

    // --- Test 5: Respawn resets police to spawn ---

    @Test
    public void testRespawnResetsPoliceToSpawn() {
        // Police is at (550, 500), respawn should reset to spawn
        Respawn.respawn(player, policeList, timer, 0, 50, MAP_Y_OFFSETS, 10, 30f);
        assertEquals("Police should return to spawn X", 550f, police.getCenterX(), 0.01f);
        assertEquals("Police should return to spawn Y", 500f, police.getCenterY(), 0.01f);
    }

    @Test
    public void testRespawnResetsMultiplePolice() {
        PoliceEnemy police2 = makePolice(600f, 400f);
        PoliceEnemy police3 = makePolice(700f, 600f);
        List<PoliceEnemy> multiPolice = Arrays.asList(police, police2, police3);

        Respawn.respawn(player, multiPolice, timer, 0, 50, MAP_Y_OFFSETS, 10, 30f);

        assertEquals("Police 1 X should be at spawn", 550f, police.getCenterX(), 0.01f);
        assertEquals("Police 1 Y should be at spawn", 500f, police.getCenterY(), 0.01f);
        assertEquals("Police 2 X should be at spawn", 600f, police2.getCenterX(), 0.01f);
        assertEquals("Police 2 Y should be at spawn", 400f, police2.getCenterY(), 0.01f);
        assertEquals("Police 3 X should be at spawn", 700f, police3.getCenterX(), 0.01f);
        assertEquals("Police 3 Y should be at spawn", 600f, police3.getCenterY(), 0.01f);
    }

    @Test
    public void testRespawnWithEmptyPoliceList() {
        // Should handle empty list gracefully
        int result = Respawn.respawn(player, Arrays.asList(), timer, 0, 50, MAP_Y_OFFSETS, 10, 30f);
        assertEquals("Fish should be reduced even with no police", 40, result);
        assertEquals("Player should be repositioned", 300f, player.getCenterX(), 0.01f);
    }

    // --- Test 6: Full catch and respawn sequence ---

    @Test
    public void testFullCatchAndRespawnSequence() {
        // Verify initial state
        assertEquals("Player starts at 500, 500", 500f, player.getCenterX(), 0.01f);
        assertEquals("Player starts at 500, 500", 500f, player.getCenterY(), 0.01f);
        assertEquals("Timer starts at 300s", 300, timer.getRemainingSeconds());

        // Verify police can catch
        assertTrue("Police should catch player", police.isCatching(player.getCenterX(), player.getCenterY()));

        // Execute respawn with penalties
        int newFish = Respawn.respawn(player, policeList, timer, 0, 50, MAP_Y_OFFSETS, 10, 30f);

        // Verify all effects applied
        assertEquals("Fish reduced: 50 - 10 = 40", 40, newFish);
        assertEquals("Timer reduced: 300 - 30 = 270", 270, timer.getRemainingSeconds());
        assertEquals("Player moved to map 0 start X", 300f, player.getCenterX(), 0.01f);
        assertEquals("Player moved to map 0 start Y", 300f, player.getCenterY(), 0.01f);
        assertEquals("Police reset to spawn X", 550f, police.getCenterX(), 0.01f);
        assertEquals("Police reset to spawn Y", 500f, police.getCenterY(), 0.01f);
    }

    @Test
    public void testSequenceAcrossMultipleMaps() {
        // Simulate respawn on map 2
        int newFish = Respawn.respawn(player, policeList, timer, 2, 80, MAP_Y_OFFSETS, 20, 45f);

        assertEquals("Fish reduced: 80 - 20 = 60", 60, newFish);
        assertEquals("Timer reduced: 300 - 45 = 255", 255, timer.getRemainingSeconds());
        assertEquals("Player moved to map 2 start X", 1250f, player.getCenterX(), 0.01f);
        assertEquals("Player moved to map 2 start Y", 1804f + 100f, player.getCenterY(), 0.01f);
        assertEquals("Police reset to original spawn", 550f, police.getCenterX(), 0.01f);
    }

    @Test
    public void testMultipleRespawnsConsecutive() {
        // First respawn
        int fish1 = Respawn.respawn(player, policeList, timer, 0, 100, MAP_Y_OFFSETS, 20, 30f);
        assertEquals("First respawn: 100 - 20 = 80", 80, fish1);
        assertEquals("After first respawn: 300 - 30 = 270", 270, timer.getRemainingSeconds());

        // Second respawn (starting from new state)
        int fish2 = Respawn.respawn(player, policeList, timer, 0, 80, MAP_Y_OFFSETS, 15, 25f);
        assertEquals("Second respawn: 80 - 15 = 65", 65, fish2);
        assertEquals("After second respawn: 270 - 25 = 245", 245, timer.getRemainingSeconds());

        // Both should still be at map 0 start
        assertEquals("Player still at map 0 X", 300f, player.getCenterX(), 0.01f);
        assertEquals("Player still at map 0 Y", 300f, player.getCenterY(), 0.01f);
        assertEquals("Police still reset to spawn", 550f, police.getCenterX(), 0.01f);
    }

    // --- Test 7: Edge cases with catching state ---

    @Test
    public void testCatchingDetectionDependsOnAlertState() {
        PoliceEnemy testPolice = makePolice(510f, 500f);  // 10 units away
        setEnumField(testPolice, "alertState", "NONE");

        assertFalse("Should not catch in NONE state even if close",
                testPolice.isCatching(500f, 500f));

        setEnumField(testPolice, "alertState", "ALERTED");
        assertFalse("Should not catch in ALERTED state even if close",
                testPolice.isCatching(500f, 500f));

        setEnumField(testPolice, "alertState", "CHASING");
        assertTrue("Should catch in CHASING state if close",
                testPolice.isCatching(500f, 500f));
    }

    @Test
    public void testCatchRangeExactBoundary() {
        // Police at exactly 75 units away
        PoliceEnemy boundaryPolice = makePolice(575f, 500f);  // distance = 75
        setEnumField(boundaryPolice, "alertState", "CHASING");

        // isCatching uses < 75, not <= 75, so at exactly 75 should not catch
        assertFalse("Should not catch at exactly 75 unit distance",
                boundaryPolice.isCatching(500f, 500f));

        // 74.9 units away should catch
        PoliceEnemy closePolice = makePolice(574.9f, 500f);
        setEnumField(closePolice, "alertState", "CHASING");
        assertTrue("Should catch at 74.9 unit distance",
                closePolice.isCatching(500f, 500f));
    }
}
