package ca.sfu.spring2026team15;


import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Tests for Fish.
 * Covers: Count fish, Player + fish interaction.
 */
public class FishTest extends GdxTestSetup {

    // --- single feature: Count fish ---

    @Test
    public void fishStartsUncollected() {
        Fish fish = new Fish(100f, 100f, true);
        assertFalse(fish.isCollected());
    }

    @Test
    public void collectMarksFishAsCollected() {
        Fish fish = new Fish(100f, 100f, true);
        fish.collect();
        assertTrue(fish.isCollected());
    }

    @Test
    public void fishPositionStoredCorrectly() {
        Fish fish = new Fish(250f, 350f, true);
        assertEquals(250f, fish.getCenterX(), 0.01f);
        assertEquals(350f, fish.getCenterY(), 0.01f);
    }

    // --- interaction: Player + fish ---

    @Test
    public void collectCalledTwiceStaysCollected() {
        Fish fish = new Fish(0f, 0f, true);
        fish.collect();
        fish.collect();
        assertTrue(fish.isCollected());
    }

    @Test
    public void multipleFishAreIndependent() {
        Fish f1 = new Fish(0f, 0f, true);
        Fish f2 = new Fish(500f, 500f, true);
        f1.collect();
        assertFalse(f2.isCollected());
    }

    // --- getCenterX/getCenterY for various values ---

    @Test
    public void fishAtOriginHasCorrectCenter() {
        Fish fish = new Fish(0f, 0f, true);
        assertEquals(0f, fish.getCenterX(), 0.01f);
        assertEquals(0f, fish.getCenterY(), 0.01f);
    }

    @Test
    public void fishAtLargeCoordinatesHasCorrectCenter() {
        Fish fish = new Fish(5000f, 2500f, true);
        assertEquals(5000f, fish.getCenterX(), 0.01f);
        assertEquals(2500f, fish.getCenterY(), 0.01f);
    }

    @Test
    public void fishAtNegativeCoordinatesHasCorrectCenter() {
        Fish fish = new Fish(-100f, -200f, true);
        assertEquals(-100f, fish.getCenterX(), 0.01f);
        assertEquals(-200f, fish.getCenterY(), 0.01f);
    }

    @Test
    public void fishAtFractionalCoordinatesHasCorrectCenter() {
        Fish fish = new Fish(33.33f, 66.66f, true);
        assertEquals(33.33f, fish.getCenterX(), 0.01f);
        assertEquals(66.66f, fish.getCenterY(), 0.01f);
    }

    @Test
    public void fishCenterXMatchesExactConstructorValue() {
        float x = 1234.5678f;
        float y = 9876.5432f;
        Fish fish = new Fish(x, y, true);
        assertEquals(x, fish.getCenterX(), 0.001f);
        assertEquals(y, fish.getCenterY(), 0.001f);
    }

    // --- multiple fish independent collection ---

    @Test
    public void threeFishIndependentCollection() {
        Fish f1 = new Fish(100f, 100f, true);
        Fish f2 = new Fish(200f, 200f, true);
        Fish f3 = new Fish(300f, 300f, true);

        f2.collect();

        assertFalse(f1.isCollected());
        assertTrue(f2.isCollected());
        assertFalse(f3.isCollected());
    }

    @Test
    public void collectingAllFishMarksAllAsCollected() {
        Fish f1 = new Fish(100f, 100f, true);
        Fish f2 = new Fish(200f, 200f, true);
        Fish f3 = new Fish(300f, 300f, true);

        f1.collect();
        f2.collect();
        f3.collect();

        assertTrue(f1.isCollected());
        assertTrue(f2.isCollected());
        assertTrue(f3.isCollected());
    }

    @Test
    public void fishCollectionOrderDoesNotMatter() {
        Fish f1 = new Fish(0f, 0f, true);
        Fish f2 = new Fish(50f, 50f, true);

        // Collect second before first
        f2.collect();
        assertFalse(f1.isCollected());
        assertTrue(f2.isCollected());

        f1.collect();
        assertTrue(f1.isCollected());
        assertTrue(f2.isCollected());
    }

    // --- render() ---

    @Test
    public void renderSkipsDrawWhenTextureIsNull() {
        // Testing constructor sets texture = null; render() should be a no-op
        Fish fish = new Fish(100f, 100f, true);
        SpriteBatch batch = mock(SpriteBatch.class);
        fish.render(batch);
        verify(batch, never()).draw(nullable(Texture.class), anyFloat(), anyFloat(), anyFloat(), anyFloat());
    }

    @Test
    public void renderCollectedFishSkipsDraw() {
        Fish fish = new Fish(100f, 100f, true);
        fish.collect();
        SpriteBatch batch = mock(SpriteBatch.class);
        fish.render(batch);
        verify(batch, never()).draw(nullable(Texture.class), anyFloat(), anyFloat(), anyFloat(), anyFloat());
    }

    // --- dispose() ---

    @Test
    public void disposeCallsTextureDispose() {
        Fish fish = new Fish(0f, 0f, true);
        Texture mockTex = mock(Texture.class);
        setField(fish, "texture", mockTex);
        fish.dispose();
        verify(mockTex, times(1)).dispose();
    }

    @Test
    public void disposeDoesNotThrowWhenTextureIsNull() {
        // Testing constructor with true flag sets texture = null
        // dispose() should handle null gracefully (if (texture != null) check)
        Fish fish = new Fish(0f, 0f, true);
        fish.dispose();
        // If no exception is thrown, test passes
    }

    // --- render() with non-null texture ---

    @Test
    public void renderDrawsWhenTextureNonNullAndNotCollected() {
        // Create a fish and inject a mock texture via setField
        Fish fish = new Fish(100f, 200f, true);
        Texture mockTex = mock(Texture.class);
        setField(fish, "texture", mockTex);

        SpriteBatch batch = mock(SpriteBatch.class);
        fish.render(batch);

        // Verify batch.draw() was called with correct coordinates
        // drawX = centerX - SIZE/2 = 100 - 25 = 75
        // drawY = centerY - SIZE/2 = 200 - 25 = 175
        verify(batch, times(1)).draw(mockTex, 75f, 175f, 50f, 50f);
    }

    @Test
    public void renderDrawsAtCorrectCoordinates() {
        // Test with different coordinates to verify offset calculation
        Fish fish = new Fish(300f, 400f, true);
        Texture mockTex = mock(Texture.class);
        setField(fish, "texture", mockTex);

        SpriteBatch batch = mock(SpriteBatch.class);
        fish.render(batch);

        // drawX = 300 - 25 = 275
        // drawY = 400 - 25 = 375
        verify(batch, times(1)).draw(mockTex, 275f, 375f, 50f, 50f);
    }

    @Test
    public void renderDrawsAtOrigin() {
        // Test rendering at origin (0, 0)
        Fish fish = new Fish(0f, 0f, true);
        Texture mockTex = mock(Texture.class);
        setField(fish, "texture", mockTex);

        SpriteBatch batch = mock(SpriteBatch.class);
        fish.render(batch);

        // drawX = 0 - 25 = -25
        // drawY = 0 - 25 = -25
        verify(batch, times(1)).draw(mockTex, -25f, -25f, 50f, 50f);
    }

    @Test
    public void renderDoesNotDrawAfterCollect() {
        // Even with a non-null texture, collected fish should not draw
        Fish fish = new Fish(100f, 100f, true);
        Texture mockTex = mock(Texture.class);
        setField(fish, "texture", mockTex);

        fish.collect();

        SpriteBatch batch = mock(SpriteBatch.class);
        fish.render(batch);

        // Verify draw was never called (collected flag takes precedence)
        verify(batch, never()).draw(any(Texture.class), anyFloat(), anyFloat(), anyFloat(), anyFloat());
    }
}
