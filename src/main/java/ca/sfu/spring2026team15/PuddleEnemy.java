package ca.sfu.spring2026team15;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

/**
 * A static puddle obstacle placed on the map.
 * Any entity (player or police) whose centre overlaps the puddle has its speed reduced
 * to 70 world-units/second for that frame. Stepping onto a puddle also deducts 1 fish
 * from the player (tracked in {@link GameScreen} via an entry-detection flag).
 */
public class PuddleEnemy {
    private static final float SIZE = 100f;

    private final Texture puddle;
    private final Vector2 position;

    /**
     * Creates a puddle at the given position with its texture loaded.
     * @param spawnX world-centre X of the puddle
     * @param spawnY world-centre Y of the puddle
     */
    public PuddleEnemy(float spawnX, float spawnY) {
        puddle = new Texture(Gdx.files.internal("small assets/puddle.png"));
        position = new Vector2(spawnX, spawnY);
    }

    /**
     * Testing constructor: skips texture loading for headless unit tests.
     * @param spawnX  world-centre X
     * @param spawnY  world-centre Y
     * @param testing unused flag distinguishing this overload from the production constructor
     */
    public PuddleEnemy(float spawnX, float spawnY, boolean testing) {
        puddle = null;
        position = new Vector2(spawnX, spawnY);
    }

    /**
     * Draws the puddle sprite in world space.
     * @param batch the active {@link SpriteBatch} (must be begun)
     */
    public void render(SpriteBatch batch) {
        if (puddle == null) return;
        float x = position.x - SIZE / 2f;
        float y = position.y - SIZE / 2f;
        batch.draw(puddle, x, y, SIZE, SIZE);
    }

    /**
     * Returns {@code true} when the given position overlaps the puddle's effective area.
     * Uses a circular hit zone slightly above the puddle sprite's geometric centre.
     *
     * @param playerX world X of the entity to test
     * @param playerY world Y of the entity to test
     * @return {@code true} if the entity is within the puddle's influence radius
     */
    public boolean onPuddle(float playerX, float playerY) {
        float puddleBottomY = position.y - SIZE / 75f;
        return playerY > puddleBottomY && Vector2.dst(position.x, position.y + 5f, playerX, playerY) < 67f;
    }

    /** Releases the puddle texture if one was loaded. */
    public void dispose() {
        if (puddle != null) puddle.dispose();
    }
}