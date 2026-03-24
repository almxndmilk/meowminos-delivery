package ca.sfu.spring2026team15;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class PuddleEnemy {
    private static final float SIZE = 100f;

    private final Texture puddle;
    private final Vector2 position;

    public PuddleEnemy(float spawnX, float spawnY) {
        puddle = new Texture(Gdx.files.internal("small assets/puddle.png"));
        position = new Vector2(spawnX, spawnY);
    }

    public PuddleEnemy(float spawnX, float spawnY, boolean testing) {
        puddle = null;
        position = new Vector2(spawnX, spawnY);
    }

    public void render(SpriteBatch batch) {
        if (puddle == null) return;
        float x = position.x - SIZE / 2f;
        float y = position.y - SIZE / 2f;
        batch.draw(puddle, x, y, SIZE, SIZE);
    }

    public boolean onPuddle(float playerX, float playerY) {
        float puddleBottomY = position.y - SIZE / 75f;
        return playerY > puddleBottomY && Vector2.dst(position.x, position.y + 5f, playerX, playerY) < 67f;
    }

    public void dispose() {
        if (puddle != null) puddle.dispose();
    }
}