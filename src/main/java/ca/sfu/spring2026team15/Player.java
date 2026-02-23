package ca.sfu.spring2026team15;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Player {
    private static final float SIZE = 128f;
    private static final float SPEED = 300f;

    private final Texture left1;
    private final Texture left2;
    private Texture currentFrame;
    private float animTimer = 0f;
    private float frameDuration = 0.15f;
    private boolean showFrame1 = true;
    private boolean isMoving = false;


    private final Vector2 position;

    public Player(float startX, float startY) {
        left1 = new Texture(Gdx.files.internal("catOffBike/catOffBike1.png"));
        left2 = new Texture(Gdx.files.internal("catOffBike/catOffBike2.png"));
        currentFrame = left1;
        position = new Vector2(startX, startY);
    }

    public void update(float delta) {
        isMoving = false;

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            position.y += SPEED * delta;
            isMoving = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            position.y -= SPEED * delta;
            isMoving = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            position.x += SPEED * delta;
            isMoving = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            position.x -= SPEED * delta;
            isMoving = true;
        }


        if (isMoving) {
            animTimer += delta;
            if (animTimer >= frameDuration) {
                animTimer = 0f;
                showFrame1 = !showFrame1;
            }
        } else {
            showFrame1 = true;
            animTimer = 0f;
        }
        currentFrame = (isMoving && !showFrame1) ? left2 : left1;  // was just "currentFrame;"
    }

    public void render(SpriteBatch batch) {
        batch.draw(currentFrame, position.x - SIZE / 2f, position.y - SIZE / 2f, SIZE, SIZE);
    }

    // Returns the player's center position, used by GameCamera to follow.
    public float getCenterX() { return position.x; }
    public float getCenterY() { return position.y; }

    public void dispose() {

        left1.dispose();
        left2.dispose();
    }
}
