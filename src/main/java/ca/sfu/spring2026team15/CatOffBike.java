package ca.sfu.spring2026team15;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

/**
 * The cat entity used when the player dismounts the bike, and also during cutscenes.
 *
 * <p>Supports two movement modes:
 * <ul>
 *   <li><b>Player-driven</b> ({@link #update}) — responds to WASD keyboard input with
 *       barrier collision, used during normal off-bike gameplay</li>
 *   <li><b>Programmatic</b> ({@link #walkToward}) — moves autonomously toward a target
 *       position, used during the intro cutscene and the Obama cutscene</li>
 * </ul>
 * Sprite animation uses 3-frame cycles for up/down movement and 2-frame cycles for
 * left/right, mirroring the side sprite for rightward movement.
 */
public class CatOffBike {
    private static final float SIZE = 170f;
    private static final float SPEED = 250f;
    private float currentSpeed = SPEED;

    /**
     * Overrides the movement speed for this frame (e.g. when on a puddle).
     * @param speed new speed in world-units per second
     */
    public void setSpeed(float speed) { this.currentSpeed = speed; }

    /** Restores movement speed to the default {@value #SPEED} world-units per second. */
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

    /**
     * Creates a fully initialised off-bike cat with all textures loaded.
     *
     * @param startX initial world-centre X
     * @param startY initial world-centre Y
     */
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

    /**
     * Testing constructor: skips texture loading for headless unit tests.
     *
     * @param startX  initial world-centre X
     * @param startY  initial world-centre Y
     * @param testing unused flag distinguishing this overload from the production constructor
     */
    public CatOffBike(float startX, float startY, boolean testing) {
        position = new Vector2(startX, startY);
        left1 = null; left2 = null;
        up1 = null; up2 = null; up3 = null;
        down1 = null; down2 = null; down3 = null;
    }

    /**
     * Reads WASD input, moves the off-bike cat, applies barrier collision, and advances
     * the directional sprite animation. Resets to the idle frame when not moving.
     *
     * @param delta  time in seconds since the last frame
     * @param lookup barrier lookup for the current map section
     */
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
        updateFrame();
    }

    /**
     * Moves the cat programmatically toward a target position without keyboard input.
     * Used during the intro and Obama cutscenes. Stops within 2 units of the target.
     * Updates directional animation the same way as {@link #update}.
     *
     * @param targetX world X to walk toward
     * @param targetY world Y to walk toward
     * @param delta   time in seconds since the last frame
     * @param speed   movement speed in world-units per second
     */
    public void walkToward(float targetX, float targetY, float delta, float speed) {
        float dx = targetX - position.x;
        float dy = targetY - position.y;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        if (dist < 2f) return;

        if (Math.abs(dy) >= Math.abs(dx)) {
            lastDirection = (dy > 0) ? Direction.UP : Direction.DOWN;
        } else {
            lastDirection = (dx > 0) ? Direction.RIGHT : Direction.LEFT;
        }

        float step = Math.min(speed * delta, dist);
        position.x += (dx / dist) * step;
        position.y += (dy / dist) * step;

        isMoving = true;
        if (lastDirection != prevDirection) { currentFrameIndex = 0; animTimer = 0f; }
        animTimer += delta;
        if (animTimer >= FRAME_DURATION) {
            animTimer = 0f;
            int frameCount = (lastDirection == Direction.LEFT || lastDirection == Direction.RIGHT) ? 2 : 3;
            currentFrameIndex = (currentFrameIndex + 1) % frameCount;
        }
        prevDirection = lastDirection;
        updateFrame();
    }

    /** Selects the correct texture frame based on the current direction and frame index. */
    private void updateFrame() {
        switch (lastDirection) {
            case LEFT:
            case RIGHT:
                currentFrame = (currentFrameIndex == 0) ? left1 : left2;
                break;
            case UP:
                if (currentFrameIndex == 0) currentFrame = up1;
                else if (currentFrameIndex == 1) currentFrame = up2;
                else currentFrame = up3;
                break;
            case DOWN:
                if (currentFrameIndex == 0) currentFrame = down1;
                else if (currentFrameIndex == 1) currentFrame = down2;
                else currentFrame = down3;
                break;
        }
    }
    /**
     * Draws the current animation frame in world space, mirroring horizontally when facing right.
     *
     * @param batch the active {@link SpriteBatch} (must be begun)
     */
    public void render(SpriteBatch batch) {
        float x = position.x - SIZE / 2f;
        float y = position.y - SIZE / 2f;

        if (lastDirection == Direction.RIGHT) {
            batch.draw(currentFrame, x + SIZE, y, -SIZE, SIZE);
        } else {
            batch.draw(currentFrame, x, y, SIZE, SIZE);
        }
    }

    /**
     * Returns the X coordinate of the cat's centre in world space.
     * @return the cat's world-centre X
     */
    public float getCenterX() { return position.x; }

    /**
     * Returns the Y coordinate of the cat's centre in world space.
     * @return the cat's world-centre Y
     */
    public float getCenterY() { return position.y; }

    /**
     * Returns the X coordinate of the cat's centre (alias for {@link #getCenterX()}).
     * @return the cat's world-centre X
     */
    public float getX() { return position.x; }

    /**
     * Teleports the off-bike cat to a new position.
     * @param x new world-centre X
     * @param y new world-centre Y
     */
    public void setPosition(float x, float y) { position.x = x; position.y = y; }

    /** Releases all textures held by this entity. */
    public void dispose() {
        left1.dispose(); left2.dispose();
        up1.dispose(); up2.dispose(); up3.dispose();
        down1.dispose(); down2.dispose(); down3.dispose();
    }
}
