package ca.sfu.spring2026team15;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Player {
    private static final float SIZE = 64f;
    private static final float SPEED = 300f;

    private final Texture texture;
    private final Vector2 position;

    public Player(float startX, float startY) {
        texture = new Texture(Gdx.files.internal("cataseprite1.png"));
        position = new Vector2(startX, startY);
    }

    public void update(float delta) {
        if (Gdx.input.isKeyPressed(Input.Keys.W)) position.y += SPEED * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) position.y -= SPEED * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) position.x += SPEED * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) position.x -= SPEED * delta;
    }

    // Draws the player into an already-open SpriteBatch.
    public void render(SpriteBatch batch) {
        batch.draw(texture, position.x - SIZE / 2f, position.y - SIZE / 2f, SIZE, SIZE);
    }

    // Returns the player's center position, used by GameCamera to follow.
    public float getCenterX() { return position.x; }
    public float getCenterY() { return position.y; }

    public void dispose() {
        texture.dispose();
    }
}
