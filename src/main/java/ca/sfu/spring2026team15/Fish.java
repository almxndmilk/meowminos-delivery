package ca.sfu.spring2026team15;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Fish {
    private static final float SIZE = 50f;

    private final Texture texture;
    private final Vector2 position;
    private boolean collected = false;

    public Fish(float x, float y) {
        texture = new Texture(Gdx.files.internal("small assets/fish.png"));
        position = new Vector2(x, y);
    }

    public Fish(float x, float y, boolean testing) {
        texture = null;
        position = new Vector2(x, y);
    }

    public void render(SpriteBatch batch) {
        if (collected || texture == null) return;
        float drawX = position.x - SIZE / 2f;
        float drawY = position.y - SIZE / 2f;
        batch.draw(texture, drawX, drawY, SIZE, SIZE);
    }

    public boolean isCollected() { return collected; }
    public void collect() { collected = true; }

    public float getCenterX() { return position.x; }
    public float getCenterY() { return position.y; }

    public void dispose() {
        if (texture != null) texture.dispose();
    }
}
