package ca.sfu.spring2026team15;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class CatOffBike {
    private static final float SIZE = 125f;
    private static final float SPEED = 400f;
    private float currentSpeed = SPEED;

    //for puddle speed change
    public void setSpeed(float speed) { this.currentSpeed = speed; }
    public void resetSpeed() { this.currentSpeed = SPEED; }

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

    public CatOffBike(float startX, float startY) {
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

    public void update(float delta, BarrierLookup lookup) {
        isMoving = false;

        boolean left = Gdx.input.isKeyPressed(Input.Keys.A);
        boolean right = Gdx.input.isKeyPressed(Input.Keys.D);
        boolean up = Gdx.input.isKeyPressed(Input.Keys.W);
        boolean down = Gdx.input.isKeyPressed(Input.Keys.S);

        float newX = position.x;
        float newY = position.y;

        if (left && !right) { newX -= currentSpeed * delta; isMoving = true; lastDirection = Direction.LEFT; }
        if (right && !left) { newX += currentSpeed * delta; isMoving = true; lastDirection = Direction.RIGHT; }
        if (up && !down) { newY += currentSpeed * delta; isMoving = true; lastDirection = Direction.UP; }
        if (down && !up) { newY -= currentSpeed * delta; isMoving = true; lastDirection = Direction.DOWN; }

        float hitW = SIZE * 0.3f; // narrower than sprite for forgiveness
        float hitTop = SIZE * 0.3f; // bottom 20%: feet up to position.y - SIZE*0.3

        // Check X — bottom 20% of sprite
        boolean blockedX =
                lookup.isBarrier(newX - hitW, position.y - SIZE / 2f) ||
                        lookup.isBarrier(newX + hitW, position.y - SIZE / 2f) ||
                        lookup.isBarrier(newX - hitW, position.y - hitTop) ||
                        lookup.isBarrier(newX + hitW, position.y - hitTop);
        if (!blockedX) position.x = newX;

        // Check Y — bottom 20% of sprite
        boolean blockedY =
                lookup.isBarrier(position.x - hitW, newY - SIZE / 2f) ||
                        lookup.isBarrier(position.x + hitW, newY - SIZE / 2f) ||
                        lookup.isBarrier(position.x - hitW, newY - hitTop) ||
                        lookup.isBarrier(position.x + hitW, newY - hitTop);
        if (!blockedY) position.y = newY;

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
    public float getX() { return position.x; }
    public void setPosition(float x, float y) { position.x = x; position.y = y; }

    public void dispose() {
        left1.dispose(); left2.dispose();
        up1.dispose(); up2.dispose(); up3.dispose();
        down1.dispose(); down2.dispose(); down3.dispose();
    }
}
