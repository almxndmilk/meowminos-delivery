package ca.sfu.spring2026team15;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

/**
 * A collectible fish sprite placed on road tiles across all three map sections.
 * Once collected it becomes invisible and is no longer included in proximity checks.
 */
public class Fish {
    private static final float SIZE = 50f;

    private final Texture texture;
    private final Vector2 position;
    private boolean collected = false;

    /**
     * Creates a fish with its texture loaded.
     * @param x world-centre X
     * @param y world-centre Y
     */
    public Fish(float x, float y) {
        texture = new Texture(Gdx.files.internal("small assets/fish.png"));
        position = new Vector2(x, y);
    }

    /**
     * Testing constructor: skips texture loading for headless unit tests.
     * @param x       world-centre X
     * @param y       world-centre Y
     * @param testing unused flag distinguishing this overload from the production constructor
     */
    public Fish(float x, float y, boolean testing) {
        texture = null;
        position = new Vector2(x, y);
    }

    /**
     * Draws the fish sprite in world space. Does nothing if already collected.
     * @param batch the active {@link SpriteBatch} (must be begun)
     */
    public void render(SpriteBatch batch) {
        if (collected || texture == null) return;
        float drawX = position.x - SIZE / 2f;
        float drawY = position.y - SIZE / 2f;
        batch.draw(texture, drawX, drawY, SIZE, SIZE);
    }

    /**
     * Returns whether this fish has already been collected.
     * @return {@code true} if this fish has already been collected
     */
    public boolean isCollected() { return collected; }

    /** Marks this fish as collected; it will no longer render or be collected again. */
    public void collect() { collected = true; }

    /**
     * Returns the world-centre X of this fish.
     * @return world-centre X
     */
    public float getCenterX() { return position.x; }

    /**
     * Returns the world-centre Y of this fish.
     * @return world-centre Y
     */
    public float getCenterY() { return position.y; }

    /** Releases the fish texture if one was loaded. */
    public void dispose() {
        if (texture != null) texture.dispose();
    }
}
