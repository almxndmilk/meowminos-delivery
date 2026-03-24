package ca.sfu.spring2026team15;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Tests for Player movement, collision, speed changes, and mount position.
 * Covers: Collision system, WASD + character movement, Character + barriers,
 *         puddle + character (speed change), mount/dismount position.
 */
public class PlayerCollisionTest extends GdxTestSetup {

    private Player player;

    @Before
    public void createPlayer() {
        player = new Player(500f, 500f, true);
        // Default: no keys pressed
        when(Gdx.input.isKeyPressed(anyInt())).thenReturn(false);
    }

    // --- single feature: Collision system / WASD + character movement ---

    @Test
    public void playerDoesNotMoveWhenNoKeysPressed() {
        BarrierLookup open = (x, y) -> false;
        player.update(0.1f, open);
        assertEquals(500f, player.getCenterX(), 0.1f);
        assertEquals(500f, player.getCenterY(), 0.1f);
    }

    @Test
    public void playerMovesLeftWhenAPressed() {
        when(Gdx.input.isKeyPressed(Input.Keys.A)).thenReturn(true);
        BarrierLookup open = (x, y) -> false;
        player.update(0.1f, open);
        assertTrue(player.getCenterX() < 500f);
    }

    @Test
    public void playerMovesRightWhenDPressed() {
        when(Gdx.input.isKeyPressed(Input.Keys.D)).thenReturn(true);
        BarrierLookup open = (x, y) -> false;
        player.update(0.1f, open);
        assertTrue(player.getCenterX() > 500f);
    }

    @Test
    public void playerMovesUpWhenWPressed() {
        when(Gdx.input.isKeyPressed(Input.Keys.W)).thenReturn(true);
        BarrierLookup open = (x, y) -> false;
        player.update(0.1f, open);
        assertTrue(player.getCenterY() > 500f);
    }

    @Test
    public void playerMovesDownWhenSPressed() {
        when(Gdx.input.isKeyPressed(Input.Keys.S)).thenReturn(true);
        BarrierLookup open = (x, y) -> false;
        player.update(0.1f, open);
        assertTrue(player.getCenterY() < 500f);
    }

    // --- interaction: Character + barriers ---

    @Test
    public void playerBlockedByBarrierOnLeft() {
        when(Gdx.input.isKeyPressed(Input.Keys.A)).thenReturn(true);
        BarrierLookup solid = (x, y) -> true;
        player.update(0.1f, solid);
        assertEquals(500f, player.getCenterX(), 0.1f);
    }

    @Test
    public void playerBlockedByBarrierOnRight() {
        when(Gdx.input.isKeyPressed(Input.Keys.D)).thenReturn(true);
        BarrierLookup solid = (x, y) -> true;
        player.update(0.1f, solid);
        assertEquals(500f, player.getCenterX(), 0.1f);
    }

    @Test
    public void playerBlockedByBarrierAbove() {
        when(Gdx.input.isKeyPressed(Input.Keys.W)).thenReturn(true);
        BarrierLookup solid = (x, y) -> true;
        player.update(0.1f, solid);
        assertEquals(500f, player.getCenterY(), 0.1f);
    }

    @Test
    public void playerBlockedByBarrierBelow() {
        when(Gdx.input.isKeyPressed(Input.Keys.S)).thenReturn(true);
        BarrierLookup solid = (x, y) -> true;
        player.update(0.1f, solid);
        assertEquals(500f, player.getCenterY(), 0.1f);
    }

    // --- interaction: puddle + character (speed) ---

    @Test
    public void setSpeedSlowsPlayer() {
        player.setSpeed(70f);
        when(Gdx.input.isKeyPressed(Input.Keys.D)).thenReturn(true);
        BarrierLookup open = (x, y) -> false;
        player.update(1f, open);
        // At speed 70 for 1s — moved ~70 units, not 400
        assertTrue(player.getCenterX() < 500f + 200f);
    }

    @Test
    public void resetSpeedRestoresNormalMovement() {
        player.setSpeed(70f);
        player.resetSpeed();
        when(Gdx.input.isKeyPressed(Input.Keys.D)).thenReturn(true);
        BarrierLookup open = (x, y) -> false;
        player.update(1f, open);
        // Normal speed 400 — should be around 900
        assertTrue(player.getCenterX() > 800f);
    }

    // --- mount/dismount position ---

    @Test
    public void setPositionRepositionsPlayer() {
        player.setPosition(200f, 300f);
        assertEquals(200f, player.getCenterX(), 0.01f);
        assertEquals(300f, player.getCenterY(), 0.01f);
    }
}
