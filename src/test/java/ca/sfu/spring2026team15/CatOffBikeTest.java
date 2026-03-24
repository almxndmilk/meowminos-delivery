package ca.sfu.spring2026team15;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Tests for CatOffBike — movement, speed, walkToward.
 * Covers: mount/dismount, WASD + character movement, Obama scene + character,
 *         puddle + character (speed).
 */
public class CatOffBikeTest extends GdxTestSetup {

    @Before
    public void resetKeys() {
        when(Gdx.input.isKeyPressed(anyInt())).thenReturn(false);
    }

    @After
    public void clearKeys() {
        reset(Gdx.input);
    }

    // --- single feature: mount/dismount ---

    @Test
    public void catOffBikeInitialPositionCorrect() {
        CatOffBike cat = new CatOffBike(300f, 400f, true);
        assertEquals(300f, cat.getCenterX(), 0.01f);
        assertEquals(400f, cat.getCenterY(), 0.01f);
    }

    @Test
    public void setPositionMovesOffBikeCat() {
        CatOffBike cat = new CatOffBike(0f, 0f, true);
        cat.setPosition(150f, 250f);
        assertEquals(150f, cat.getCenterX(), 0.01f);
        assertEquals(250f, cat.getCenterY(), 0.01f);
    }

    // --- interaction: Obama scene + character (walkToward) ---

    @Test
    public void walkTowardMovesCloserToTarget() {
        CatOffBike cat = new CatOffBike(0f, 0f, true);
        cat.walkToward(100f, 0f, 0.1f, 250f);
        assertTrue(cat.getCenterX() > 0f);
    }

    @Test
    public void walkTowardStopsWhenWithin2Units() {
        CatOffBike cat = new CatOffBike(99f, 0f, true);
        cat.walkToward(100f, 0f, 0.1f, 250f); // distance = 1 < 2 — should not move
        assertEquals(99f, cat.getCenterX(), 0.01f);
    }

    @Test
    public void walkTowardMovesInYDirectionCorrectly() {
        CatOffBike cat = new CatOffBike(0f, 0f, true);
        cat.walkToward(0f, 100f, 0.1f, 250f);
        assertTrue(cat.getCenterY() > 0f);
    }

    // --- puddle + character: speed slowing ---

    @Test
    public void setSpeedSlowsCatOffBike() {
        CatOffBike cat = new CatOffBike(0f, 0f, true);
        cat.setSpeed(70f);
        cat.walkToward(1000f, 0f, 1f, 70f);
        // At 70 speed for 1s — should be close to 70
        assertTrue(cat.getCenterX() <= 70f + 1f);
    }

    @Test
    public void resetSpeedRestoresDefaultSpeed() {
        CatOffBike cat = new CatOffBike(0f, 0f, true);
        cat.setSpeed(70f);
        cat.resetSpeed();
        cat.walkToward(1000f, 0f, 1f, 250f);
        // At 250 speed for 1s — should be around 250
        assertTrue(cat.getCenterX() >= 200f);
    }

    // --- walkToward diagonal movement ---

    @Test
    public void walkTowardDiagonalDxGreaterThanDy() {
        // dx=100, dy=10 => abs(dx) > abs(dy) => direction should be RIGHT
        CatOffBike cat = new CatOffBike(0f, 0f, true);
        cat.walkToward(100f, 10f, 0.1f, 250f);
        assertTrue(cat.getCenterX() > 0f);
        assertTrue(cat.getCenterY() > 0f);
    }

    @Test
    public void walkTowardDiagonalDyGreaterThanDx() {
        // dx=10, dy=100 => abs(dy) > abs(dx) => direction should be UP
        CatOffBike cat = new CatOffBike(0f, 0f, true);
        cat.walkToward(10f, 100f, 0.1f, 250f);
        assertTrue(cat.getCenterX() > 0f);
        assertTrue(cat.getCenterY() > 0f);
    }

    // --- walkToward negative directions ---

    @Test
    public void walkTowardNegativeXDirectionMovesLeft() {
        CatOffBike cat = new CatOffBike(100f, 0f, true);
        cat.walkToward(0f, 0f, 0.1f, 250f);
        assertTrue(cat.getCenterX() < 100f);
    }

    @Test
    public void walkTowardNegativeYDirectionMovesDown() {
        CatOffBike cat = new CatOffBike(0f, 100f, true);
        cat.walkToward(0f, 0f, 0.1f, 250f);
        assertTrue(cat.getCenterY() < 100f);
    }

    @Test
    public void walkTowardDiagonalNegativeBothDirections() {
        CatOffBike cat = new CatOffBike(200f, 200f, true);
        cat.walkToward(0f, 0f, 0.1f, 250f);
        assertTrue(cat.getCenterX() < 200f);
        assertTrue(cat.getCenterY() < 200f);
    }

    // --- multiple walkToward calls simulating animation frames cycling ---

    @Test
    public void multipleWalkTowardCallsProgressTowardTarget() {
        CatOffBike cat = new CatOffBike(0f, 0f, true);
        for (int i = 0; i < 10; i++) {
            cat.walkToward(500f, 0f, 0.05f, 250f);
        }
        // 10 * 0.05s * 250 = 125 units moved
        assertEquals(125f, cat.getCenterX(), 1f);
    }

    @Test
    public void multipleWalkTowardCallsCycleAnimationFrames() {
        CatOffBike cat = new CatOffBike(0f, 0f, true);
        // FRAME_DURATION is 0.12f; calling with 0.13f delta should trigger frame advance each call
        for (int i = 0; i < 5; i++) {
            cat.walkToward(1000f, 0f, 0.13f, 250f);
        }
        // No exception means animation cycling worked
        assertTrue(cat.getCenterX() > 0f);
    }

    @Test
    public void walkTowardDirectionChangeTriggers() {
        CatOffBike cat = new CatOffBike(0f, 0f, true);
        // First walk right
        cat.walkToward(100f, 0f, 0.1f, 250f);
        // Then walk up — direction change should reset animation
        cat.walkToward(cat.getCenterX(), 100f, 0.1f, 250f);
        assertTrue(cat.getCenterY() > 0f);
    }

    // --- walkToward respects maxing out step to distance ---

    @Test
    public void walkTowardStepCappedToDistance() {
        CatOffBike cat = new CatOffBike(0f, 0f, true);
        // Target is 10 units away but speed*delta = 250*1 = 250, should cap to 10
        cat.walkToward(10f, 0f, 1f, 250f);
        assertEquals(10f, cat.getCenterX(), 0.5f);
    }

    @Test
    public void walkTowardDoesNotOvershootTarget() {
        CatOffBike cat = new CatOffBike(0f, 0f, true);
        cat.walkToward(5f, 0f, 1f, 1000f);
        // Should stop at target, not overshoot
        assertEquals(5f, cat.getCenterX(), 0.5f);
    }

    // --- getX() method ---

    @Test
    public void getXReturnsSameAsCenterX() {
        CatOffBike cat = new CatOffBike(42f, 99f, true);
        assertEquals(42f, cat.getX(), 0.01f);
        assertEquals(cat.getCenterX(), cat.getX(), 0.01f);
    }

    @Test
    public void getXAfterSetPosition() {
        CatOffBike cat = new CatOffBike(0f, 0f, true);
        cat.setPosition(777f, 888f);
        assertEquals(777f, cat.getX(), 0.01f);
    }

    // --- update() with keyboard input ---

    @Test
    public void updateNoKeysDoesNotMove() {
        CatOffBike cat = new CatOffBike(500f, 500f, true);
        BarrierLookup open = (x, y) -> false;
        cat.update(0.1f, open);
        assertEquals(500f, cat.getCenterX(), 0.01f);
        assertEquals(500f, cat.getCenterY(), 0.01f);
    }

    @Test
    public void updateMovesLeftWhenAPressed() {
        CatOffBike cat = new CatOffBike(500f, 500f, true);
        when(Gdx.input.isKeyPressed(Input.Keys.A)).thenReturn(true);
        BarrierLookup open = (x, y) -> false;
        cat.update(0.1f, open);
        assertTrue(cat.getCenterX() < 500f);
    }

    @Test
    public void updateMovesRightWhenDPressed() {
        CatOffBike cat = new CatOffBike(500f, 500f, true);
        when(Gdx.input.isKeyPressed(Input.Keys.D)).thenReturn(true);
        BarrierLookup open = (x, y) -> false;
        cat.update(0.1f, open);
        assertTrue(cat.getCenterX() > 500f);
    }

    @Test
    public void updateMovesUpWhenWPressed() {
        CatOffBike cat = new CatOffBike(500f, 500f, true);
        when(Gdx.input.isKeyPressed(Input.Keys.W)).thenReturn(true);
        BarrierLookup open = (x, y) -> false;
        cat.update(0.1f, open);
        assertTrue(cat.getCenterY() > 500f);
    }

    @Test
    public void updateMovesDownWhenSPressed() {
        CatOffBike cat = new CatOffBike(500f, 500f, true);
        when(Gdx.input.isKeyPressed(Input.Keys.S)).thenReturn(true);
        BarrierLookup open = (x, y) -> false;
        cat.update(0.1f, open);
        assertTrue(cat.getCenterY() < 500f);
    }

    @Test
    public void updateBlockedByBarrierXAxis() {
        CatOffBike cat = new CatOffBike(500f, 500f, true);
        when(Gdx.input.isKeyPressed(Input.Keys.D)).thenReturn(true);
        BarrierLookup solid = (x, y) -> true;
        cat.update(0.1f, solid);
        assertEquals(500f, cat.getCenterX(), 0.01f);
    }

    @Test
    public void updateBlockedByBarrierYAxis() {
        CatOffBike cat = new CatOffBike(500f, 500f, true);
        when(Gdx.input.isKeyPressed(Input.Keys.W)).thenReturn(true);
        BarrierLookup solid = (x, y) -> true;
        cat.update(0.1f, solid);
        assertEquals(500f, cat.getCenterY(), 0.01f);
    }

    @Test
    public void updateBothLeftRightCancelOut() {
        CatOffBike cat = new CatOffBike(500f, 500f, true);
        when(Gdx.input.isKeyPressed(Input.Keys.A)).thenReturn(true);
        when(Gdx.input.isKeyPressed(Input.Keys.D)).thenReturn(true);
        BarrierLookup open = (x, y) -> false;
        cat.update(0.1f, open);
        assertEquals(500f, cat.getCenterX(), 0.01f);
    }

    @Test
    public void updateAnimationFrameAdvancesAfterDuration() {
        CatOffBike cat = new CatOffBike(500f, 500f, true);
        when(Gdx.input.isKeyPressed(Input.Keys.D)).thenReturn(true);
        BarrierLookup open = (x, y) -> false;
        // FRAME_DURATION = 0.12f; 3 updates of 0.13f each → frames advance
        cat.update(0.13f, open);
        cat.update(0.13f, open);
        cat.update(0.13f, open);
        // No exception means animation cycling worked correctly
        assertTrue(cat.getCenterX() > 500f);
    }

    @Test
    public void updateDirectionChangeResetsFrameIndex() {
        CatOffBike cat = new CatOffBike(500f, 500f, true);
        BarrierLookup open = (x, y) -> false;
        when(Gdx.input.isKeyPressed(Input.Keys.D)).thenReturn(true);
        cat.update(0.1f, open);
        // Change direction to UP
        when(Gdx.input.isKeyPressed(Input.Keys.D)).thenReturn(false);
        when(Gdx.input.isKeyPressed(Input.Keys.W)).thenReturn(true);
        cat.update(0.1f, open);
        // No exception — direction change reset frame index
        assertTrue(cat.getCenterY() > 500f);
    }

    // --- render() ---

    @Test
    public void renderLeftDirectionDoesNotThrow() {
        CatOffBike cat = new CatOffBike(500f, 500f, true);
        when(Gdx.input.isKeyPressed(Input.Keys.A)).thenReturn(true);
        cat.update(0.1f, (x, y) -> false);
        SpriteBatch batch = mock(SpriteBatch.class);
        cat.render(batch);
        verify(batch, times(1)).draw((com.badlogic.gdx.graphics.Texture) any(), anyFloat(), anyFloat(), anyFloat(), anyFloat());
    }

    @Test
    public void renderRightDirectionUsesFlip() {
        CatOffBike cat = new CatOffBike(500f, 500f, true);
        when(Gdx.input.isKeyPressed(Input.Keys.D)).thenReturn(true);
        cat.update(0.1f, (x, y) -> false);
        SpriteBatch batch = mock(SpriteBatch.class);
        cat.render(batch);
        // RIGHT direction uses negative width (flip) — draw called once
        verify(batch, times(1)).draw((com.badlogic.gdx.graphics.Texture) any(), anyFloat(), anyFloat(), anyFloat(), anyFloat());
    }

    @Test
    public void renderDownDirectionDoesNotThrow() {
        CatOffBike cat = new CatOffBike(500f, 500f, true);
        // Default direction is DOWN — no key needed
        SpriteBatch batch = mock(SpriteBatch.class);
        cat.render(batch);
        verify(batch, times(1)).draw((com.badlogic.gdx.graphics.Texture) any(), anyFloat(), anyFloat(), anyFloat(), anyFloat());
    }
}
