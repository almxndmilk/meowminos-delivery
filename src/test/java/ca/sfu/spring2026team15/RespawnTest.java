package ca.sfu.spring2026team15;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Tests for the Respawn static utility.
 * Covers: Police + character (respawn on catch), fish penalty, timer penalty.
 *
 * Uses real Player / PoliceEnemy objects (via test constructors) so no
 * Mockito construction mocking is required.
 */
public class RespawnTest extends GdxTestSetup {

    private static final float[] MAP_Y_OFFSETS = {0f, 902f, 1804f};

    // --- fish penalty ---

    @Test
    public void fishCountReducedByPenalty() {
        Player player = new Player(0f, 0f, true);
        Timer timer = new Timer();
        int result = Respawn.respawn(player, Collections.emptyList(), timer,
                0, 20, MAP_Y_OFFSETS, 10, 0f);
        assertEquals(10, result);
    }

    @Test
    public void fishCountNeverGoesBelowZero() {
        Player player = new Player(0f, 0f, true);
        Timer timer = new Timer();
        int result = Respawn.respawn(player, Collections.emptyList(), timer,
                0, 5, MAP_Y_OFFSETS, 10, 0f);
        assertEquals(0, result);
    }

    // --- timer penalty ---

    @Test
    public void timerPenaltyApplied() {
        Player player = new Player(0f, 0f, true);
        Timer timer = new Timer();
        Respawn.respawn(player, Collections.emptyList(), timer,
                0, 20, MAP_Y_OFFSETS, 0, 30f);
        assertEquals(270, timer.getRemainingSeconds());
    }

    // --- player repositioning ---

    @Test
    public void playerRepositionedToMap0Start() {
        Player player = new Player(9999f, 9999f, true);
        Timer timer = new Timer();
        Respawn.respawn(player, Collections.emptyList(), timer,
                0, 20, MAP_Y_OFFSETS, 0, 0f);
        assertEquals(300f, player.getCenterX(), 0.01f);
        assertEquals(300f, player.getCenterY(), 0.01f);
    }

    @Test
    public void playerRepositionedToMap1Start() {
        Player player = new Player(0f, 0f, true);
        Timer timer = new Timer();
        Respawn.respawn(player, Collections.emptyList(), timer,
                1, 20, MAP_Y_OFFSETS, 0, 0f);
        assertEquals(5600f, player.getCenterX(), 0.01f);
        assertEquals(MAP_Y_OFFSETS[1] + 100f, player.getCenterY(), 0.01f);
    }

    @Test
    public void playerRepositionedToMap2Start() {
        Player player = new Player(0f, 0f, true);
        Timer timer = new Timer();
        Respawn.respawn(player, Collections.emptyList(), timer,
                2, 20, MAP_Y_OFFSETS, 0, 0f);
        assertEquals(1250f, player.getCenterX(), 0.01f);
        assertEquals(MAP_Y_OFFSETS[2] + 100f, player.getCenterY(), 0.01f);
    }

    // --- police reset ---

    @Test
    public void allPoliceResetToSpawn() {
        Player player = new Player(0f, 0f, true);
        Timer timer = new Timer();

        // Spawn police at (200,300) and (600,700) then move them away
        PoliceEnemy p1 = new PoliceEnemy(200f, 300f, true);
        PoliceEnemy p2 = new PoliceEnemy(600f, 700f, true);
        p1.setPosition(9000f, 9000f);
        p2.setPosition(9000f, 9000f);

        Respawn.respawn(player, Arrays.asList(p1, p2), timer, 0, 20, MAP_Y_OFFSETS, 0, 0f);

        assertEquals(200f, p1.getCenterX(), 0.01f);
        assertEquals(300f, p1.getCenterY(), 0.01f);
        assertEquals(600f, p2.getCenterX(), 0.01f);
        assertEquals(700f, p2.getCenterY(), 0.01f);
    }
}
