package ca.sfu.spring2026team15;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Player {
    private static final float SIZE = 150f;
    private static final float SPEED = 400f;

    private final Texture left1, left2;
    private final Texture up1, up2, up3;
    private final Texture down1, down2, down3;



    private Texture currentFrame;
    private static final float FRAME_DURATION = 0.12f;
    private float animTimer = 0f;
    private int currentFrameIndex = 0;
    private boolean isMoving = false;

    private enum Direction { LEFT, RIGHT, UP, DOWN }
    private Direction lastDirection = Direction.DOWN;
    private Direction prevDirection = Direction.DOWN;

    private final Vector2 position;

    public Player(float startX, float startY) {
        left1 = new Texture(Gdx.files.internal("catOffBike/catOffBike1.png"));
        left2 = new Texture(Gdx.files.internal("catOffBike/catOffBike2.png"));
        up1 = new Texture(Gdx.files.internal("catBack/catBack1.png"));
        up2 = new Texture(Gdx.files.internal("catBack/catBack2.png"));
        up3 = new Texture(Gdx.files.internal("catBack/catBack3.png"));
        down1 = new Texture(Gdx.files.internal("catFront/catFront1.png"));
        down2 = new Texture(Gdx.files.internal("catFront/catFront2.png"));
        down3 = new Texture(Gdx.files.internal("catFront/catFront3.png"));
        currentFrame = down1;
        position = new Vector2(startX, startY);
    }

    public void update(float delta) {
        isMoving = false;

        boolean left = Gdx.input.isKeyPressed(Input.Keys.A);
        boolean right = Gdx.input.isKeyPressed(Input.Keys.D);
        boolean up = Gdx.input.isKeyPressed(Input.Keys.W);
        boolean down = Gdx.input.isKeyPressed(Input.Keys.S);

        if (left && !right) { position.x -= SPEED * delta; isMoving = true; lastDirection = Direction.LEFT; }
        if (right && !left) { position.x += SPEED * delta; isMoving = true; lastDirection = Direction.RIGHT; }
        if (up && !down) { position.y += SPEED * delta; isMoving = true; lastDirection = Direction.UP; }
        if (down && !up) { position.y -= SPEED * delta; isMoving = true; lastDirection = Direction.DOWN; }


        if (isMoving) {
            if (lastDirection != prevDirection) {
                currentFrameIndex = 0;
                animTimer = 0f;
            }
            animTimer += delta;
            if (animTimer >= FRAME_DURATION) {
                animTimer = 0f;
                int frameCount = (lastDirection == Direction.LEFT || lastDirection == Direction.RIGHT) ? 2 : 3;
                currentFrameIndex = (currentFrameIndex + 1) % frameCount;
            }
        } else {
            currentFrameIndex = 0;
            animTimer = 0f;
        }
        prevDirection = lastDirection;

        switch (lastDirection) {
            case LEFT:
            case RIGHT:
                currentFrame = (currentFrameIndex == 0) ? left1 : left2;
                break;
            case UP:
                if (currentFrameIndex == 0) {
                    currentFrame = up1;
                }
                else if (currentFrameIndex == 1){
                    currentFrame = up2;
                }
                else{
                    currentFrame = up3;
                }
                break;
            case DOWN:
                if (currentFrameIndex == 0){
                    currentFrame = down1;
                }
                else if (currentFrameIndex == 1) {
                    currentFrame = down2;
                }
                else{
                    currentFrame = down3;
                }
                break;
        }
    }
    public void render(SpriteBatch batch) {
        float x = position.x - SIZE / 2f;
        float y = position.y - SIZE / 2f;

        if (lastDirection == Direction.RIGHT) {
            batch.draw(currentFrame, x + SIZE, y, -SIZE, SIZE);
        } else {
            batch.draw(currentFrame, x, y, SIZE, SIZE);
        }
    }

    public float getCenterX() { return position.x; }
    public float getCenterY() { return position.y; }

    public void dispose() {
        left1.dispose(); left2.dispose();
        up1.dispose(); up2.dispose(); up3.dispose();
        down1.dispose(); down2.dispose(); down3.dispose();
    }
}
