package ca.sfu.spring2026team15;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

/**
 * The player-controlled cat-on-bike entity.
 *
 * <p>Responds to WASD keyboard input, applies axis-separated barrier collision against
 * the current map's barrier pixmap, and cycles a directional sprite animation
 * (left/right share the same side sprites, mirrored horizontally for right).
 * Speed can be overridden externally (e.g. by a {@link PuddleEnemy}) and reset each frame.
 */
public class Player {
    private static final float SIZE = 125f;
    private static final float SPEED = 400f;
    private float currentSpeed = SPEED;

    /**
     * Overrides the movement speed for this frame (e.g. when the player is on a puddle).
     * @param speed new speed in world-units per second
     */
    public void setSpeed(float speed) { this.currentSpeed = speed; }

    /** Restores movement speed to the default {@value #SPEED} world-units per second. */
    public void resetSpeed() { this.currentSpeed = SPEED; }

    private final Texture left1, left2;
    private final Texture up1, up2;
    private final Texture down1, down2;



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
     * Creates a fully initialised player with all textures loaded.
     *
     * @param startX initial world-centre X
     * @param startY initial world-centre Y
     */
    public Player(float startX, float startY) {
        left1 = new Texture(Gdx.files.internal("catOnBike/catOnBike_Down.png"));
        left2 = new Texture(Gdx.files.internal("catOnBike/catOnBike_Up.png"));
        up1 = new Texture(Gdx.files.internal("catOnBike/catBikeBack_down.png"));
        up2 = new Texture(Gdx.files.internal("catOnBike/catBikeBack_up.png"));
        down1 = new Texture(Gdx.files.internal("catOnBike/catOnBikeFront_down.png"));
        down2 = new Texture(Gdx.files.internal("catOnBike/catOnBikeFront_up.png"));
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
    public Player(float startX, float startY, boolean testing) {
        position = new Vector2(startX, startY);
        left1 = null; left2 = null;
        up1 = null; up2 = null;
        down1 = null; down2 = null;
    }

    /**
     * Reads WASD input, moves the player, applies barrier collision, and advances
     * the directional sprite animation each frame.
     * X and Y axes are resolved independently so the player slides along walls.
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
            // Idle animation
            animTimer += delta;
            if (animTimer >= FRAME_DURATION) {
                animTimer = 0f;
                currentFrameIndex = (currentFrameIndex + 1) % 2; // cycle through 2 idle frames)
            }
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
                break;
            case DOWN:
                if (currentFrameIndex == 0){
                    currentFrame = down1;
                }
                else if (currentFrameIndex == 1) {
                    currentFrame = down2;
                }
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

    /** @return the X coordinate of the player's centre in world space */
    public float getCenterX() { return position.x; }

    /** @return the Y coordinate of the player's centre in world space */
    public float getCenterY() { return position.y; }

    /** @return the X coordinate of the player's centre (alias for {@link #getCenterX()}) */
    public float getX() { return position.x; }

    /**
     * Teleports the player to a new position.
     * @param x new world-centre X
     * @param y new world-centre Y
     */
    public void setPosition(float x, float y) { position.x = x; position.y = y; }

    /** Releases all textures held by this player. */
    public void dispose() {
        left1.dispose(); left2.dispose();
        up1.dispose(); up2.dispose();
        down1.dispose(); down2.dispose();
    }
}
