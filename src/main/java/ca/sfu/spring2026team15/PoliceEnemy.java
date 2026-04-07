package ca.sfu.spring2026team15;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import java.util.Random;
import com.badlogic.gdx.audio.Sound;

/**
 * An AI-controlled police enemy that patrols the map and chases the player on detection.
 *
 * <p>The enemy transitions through three alert states:
 * <ol>
 *   <li><b>NONE</b> — wanders randomly, changing direction every {@value #WANDER_CHANGE_TIME} seconds</li>
 *   <li><b>ALERTED</b> — player entered detection range; shows a {@code ?} indicator and
 *       waits {@value #ALERT_DELAY} second(s) before committing to a chase</li>
 *   <li><b>CHASING</b> — moves directly toward the player at chase speed; shows a {@code !}
 *       indicator and plays the "HEY" sound on first entry</li>
 * </ol>
 * The enemy returns to NONE if the player moves outside {@value #DETECTION_RANGE} pixels.
 * Barrier-pixel collision is applied on both axes independently so the enemy can slide
 * along walls rather than stopping dead.
 */
public class PoliceEnemy {
    private static final float SIZE = 150f;
    private static final float CHASE_SPEED = 250f;
    private float VAR_CHASE_SPEED = CHASE_SPEED;
    private static final float WANDER_SPEED = 80f;
    private float VAR_WANDER_SPEED = WANDER_SPEED;
    private static final float DETECTION_RANGE = 400f;
    private static final float FRAME_DURATION = 0.15f;
    private static final float WANDER_CHANGE_TIME = 2f;
    private static final float ALERT_DELAY = 1f; // seconds of ? before !

    //sprites
    private final Texture side1, side2;
    private final Texture back1, back2;
    private final Texture front1, front2;
    private final Texture exclamation;
    private final Texture question;
    private Texture currentFrame;

    //animation
    private float animTimer = 0f;
    private int currentFrameIndex = 0;

    //direction
    private enum Direction {LEFT, RIGHT, UP, DOWN}
    private Direction lastDirection = Direction.DOWN;

    //state
    private enum AlertState {NONE, ALERTED, CHASING}
    private AlertState alertState = AlertState.NONE;
    private float alertTimer = 0f;

    //movement
    private final Vector2 position;
    private float wanderTimer = 0f;
    private final Random random = new Random();
    private float spawnX;
    private float spawnY;


    //sound
    private final Sound heySound;

    /**
     * Creates a fully initialised police enemy with textures and audio loaded.
     *
     * @param spawnX world X of the enemy's spawn position (used as the centre X)
     * @param spawnY world Y of the enemy's spawn position (used as the centre Y)
     */
    public PoliceEnemy(float spawnX, float spawnY) {
        side1 = new Texture(Gdx.files.internal("policeSide/policeSide1.png"));
        side2 = new Texture(Gdx.files.internal("policeSide/policeSide2.png"));
        back1 = new Texture(Gdx.files.internal("policeBack/policeBack1.png"));
        back2 = new Texture(Gdx.files.internal("policeBack/policeBack2.png"));
        front1 = new Texture(Gdx.files.internal("policeWalk/policeWalk1.png"));
        front2 = new Texture(Gdx.files.internal("policeWalk/policeWalk2.png"));
        exclamation = new Texture(Gdx.files.internal("policeSupplementary/exclamationMark.png"));
        question= new Texture(Gdx.files.internal("policeSupplementary/questionMark.png"));

        heySound = Gdx.audio.newSound(Gdx.files.internal("audio/HEY.mp3"));

        currentFrame = front1;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        position = new Vector2(spawnX, spawnY);
    }

    /**
     * Testing constructor: skips texture and audio loading so the class can be instantiated
     * in headless unit tests without an active OpenGL context.
     *
     * @param spawnX  world X spawn position
     * @param spawnY  world Y spawn position
     * @param testing unused flag that distinguishes this overload from the production constructor
     */
    public PoliceEnemy(float spawnX, float spawnY, boolean testing) {
        side1 = null; side2 = null;
        back1 = null; back2 = null;
        front1 = null; front2 = null;
        exclamation = null; question = null;
        heySound = null;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
        position = new Vector2(spawnX, spawnY);
    }

    /**
     * Overrides the chase speed for this frame (e.g. slowed by a puddle).
     * @param speed new chase speed in world-units per second
     */
    public void setChaseSpeed(float speed) { this.VAR_CHASE_SPEED = speed; }

    /**
     * Overrides the wander speed for this frame (e.g. slowed by a puddle).
     * @param speed new wander speed in world-units per second
     */
    public void setWanderSpeed(float speed) { this.VAR_WANDER_SPEED = speed; }

    /** Restores both chase and wander speeds to their default constants. */
    public void resetSpeed() { this.VAR_CHASE_SPEED = CHASE_SPEED; this.VAR_WANDER_SPEED = WANDER_SPEED; }

    /** @return the X coordinate of the enemy's centre in world space */
    public float getCenterX() { return position.x; }

    /** @return the Y coordinate of the enemy's centre in world space */
    public float getCenterY() { return position.y; }

    /** Teleport the enemy to a new position and reset its AI state. */
    public void setPosition(float x, float y) {
        position.set(x, y);
        alertState = AlertState.NONE;
        alertTimer = 0f;
        wanderTimer = 0f;
    }

    /**
     * Ticks the enemy's AI, movement, and animation for one frame.
     *
     * @param delta   time in seconds since the last frame
     * @param playerX player's world-centre X (used for detection range and chase direction)
     * @param playerY player's world-centre Y
     * @param lookup  barrier lookup used to prevent movement through solid tiles
     */
    public void update(float delta, float playerX, float playerY, BarrierLookup lookup) {
        float dist = Vector2.dst(position.x, position.y, playerX, playerY);
        updateAlertState(delta, dist);
        updateMovement(delta, playerX, playerY, lookup);
        updateAnimation(delta);
    }

    /**
     * Advances the AI alert-state machine based on distance to the player.
     * NONE→ALERTED when player enters range; ALERTED→CHASING after the alert delay elapses;
     * either ALERTED or CHASING → NONE when player leaves range.
     *
     * @param delta time in seconds since the last frame
     * @param dist  current distance between this enemy and the player
     */
    private void updateAlertState(float delta, float dist) {
        boolean inRange = dist <= DETECTION_RANGE;
        switch (alertState) {
            case NONE:
                if (inRange) {
                    alertState = AlertState.ALERTED;
                    alertTimer = 0f;
                }
                break;
            case ALERTED:
                if (!inRange) {
                    alertState = AlertState.NONE;
                    alertTimer = 0f;
                } else {
                    alertTimer += delta;
                    if (alertTimer >= ALERT_DELAY) {
                        alertState = AlertState.CHASING;
                        if (SettingsScreen.soundOn) {
                            heySound.play(1.0f);
                        }
                    }
                }
                break;
            case CHASING:
                if (!inRange) {
                    alertState = AlertState.NONE;
                    alertTimer = 0f;
                }
                break;
        }
    }

    /**
     * Moves the enemy for one frame based on alert state.
     * While CHASING, moves directly toward the player at chase speed.
     * Otherwise, wanders in the current random direction, picking a new direction
     * every {@value #WANDER_CHANGE_TIME} seconds. Barrier collision is applied via
     * {@link #applyBarrierCollision} on both axes independently.
     *
     * @param delta   time in seconds since the last frame
     * @param playerX player's world-centre X (chase target)
     * @param playerY player's world-centre Y (chase target)
     * @param lookup  barrier lookup for wall collision
     */
    private void updateMovement(float delta, float playerX, float playerY, BarrierLookup lookup) {
        if (alertState == AlertState.CHASING) {
            Vector2 dir = new Vector2(playerX - position.x, playerY - position.y).nor();
            float newX = position.x + dir.x * VAR_CHASE_SPEED * delta;
            float newY = position.y + dir.y * VAR_CHASE_SPEED * delta;
            applyBarrierCollision(newX, newY, lookup);

            float dx = playerX - position.x;
            float dy = playerY - position.y;
            if (Math.abs(dx) > Math.abs(dy)) {
                lastDirection = (dx > 0) ? Direction.RIGHT : Direction.LEFT;
            } else {
                lastDirection = (dy > 0) ? Direction.UP : Direction.DOWN;
            }
        } else {
            wanderTimer -= delta;
            if (wanderTimer <= 0f) {
                wanderTimer = WANDER_CHANGE_TIME;
                lastDirection = Direction.values()[random.nextInt(4)];
            }

            float newX = position.x;
            float newY = position.y;

            switch (lastDirection) {
                case LEFT: newX -= VAR_WANDER_SPEED * delta; break;
                case RIGHT: newX += VAR_WANDER_SPEED * delta; break;
                case UP: newY += VAR_WANDER_SPEED * delta; break;
                case DOWN: newY -= VAR_WANDER_SPEED * delta; break;
            }
            applyBarrierCollision(newX, newY, lookup);
        }
    }

    /**
     * Cycles the sprite animation frame and selects the correct directional texture set
     * (side, back, or front) based on the enemy's last movement direction.
     *
     * @param delta time in seconds since the last frame
     */
    private void updateAnimation(float delta) {
        animTimer += delta;
        if (animTimer >= FRAME_DURATION) {
            animTimer = 0f;
            currentFrameIndex = (currentFrameIndex + 1) % 2;
        }
        switch (lastDirection) {
            case LEFT:
            case RIGHT:
                currentFrame = (currentFrameIndex == 0) ? side1 : side2;
                break;
            case UP:
                currentFrame = (currentFrameIndex == 0) ? back1 : back2;
                break;
            case DOWN:
                currentFrame = (currentFrameIndex == 0) ? front1 : front2;
                break;
        }
    }

    /**
     * Applies axis-separated barrier collision for the proposed new position.
     * Tests four hit-zone corners for each axis; only applies movement along an axis
     * if none of its corners land on a barrier pixel.
     *
     * @param newX   proposed new X centre position
     * @param newY   proposed new Y centre position
     * @param lookup barrier lookup for the current map
     */
    private void applyBarrierCollision(float newX, float newY, BarrierLookup lookup) {
        float hitW = SIZE * 0.3f;
        float hitTop = SIZE * 0.3f;

        boolean blockedX =
            lookup.isBarrier(newX - hitW, position.y - SIZE / 2f) ||
            lookup.isBarrier(newX + hitW, position.y - SIZE / 2f) ||
            lookup.isBarrier(newX - hitW, position.y - hitTop) ||
            lookup.isBarrier(newX + hitW, position.y - hitTop);
        if (!blockedX) position.x = newX;

        boolean blockedY =
            lookup.isBarrier(position.x - hitW, newY - SIZE / 2f) ||
            lookup.isBarrier(position.x + hitW, newY - SIZE / 2f) ||
            lookup.isBarrier(position.x - hitW, newY - hitTop) ||
            lookup.isBarrier(position.x + hitW, newY - hitTop);
        if (!blockedY) position.y = newY;
    }

    /**
     * Draws the enemy sprite and its alert indicator (? or !) in world space.
     *
     * @param batch the active {@link SpriteBatch} to draw into (must be begun)
     */
    public void render(SpriteBatch batch) {
        renderSprite(batch);
        renderAlertIndicator(batch);
    }

    /**
     * Draws the directional enemy sprite, mirroring horizontally when facing right.
     *
     * @param batch the active {@link SpriteBatch}
     */
    private void renderSprite(SpriteBatch batch) {
        float x = position.x - SIZE / 2f;
        float y = position.y - SIZE / 2f;

        if (lastDirection == Direction.RIGHT) {
            batch.draw(currentFrame, x + SIZE, y, -SIZE, SIZE);
        } else {
            batch.draw(currentFrame, x, y, SIZE, SIZE);
        }
    }

    /**
     * Draws the {@code ?} or {@code !} icon above the enemy based on current alert state.
     * Nothing is drawn when the state is NONE.
     *
     * @param batch the active {@link SpriteBatch}
     */
    private void renderAlertIndicator(SpriteBatch batch) {
        if (alertState == AlertState.ALERTED) {
            batch.draw(question, position.x + 5, position.y + SIZE / 4f, 30f, 30f);
        } else if (alertState == AlertState.CHASING) {
            batch.draw(exclamation, position.x + 5, position.y + SIZE / 3.8f, 25f, 25f);
        }
    }

    /**
     * Returns {@code true} when this enemy is actively chasing and within catch distance.
     * Used by {@link GameScreen} to trigger the respawn mechanic.
     *
     * @param playerX player's world-centre X
     * @param playerY player's world-centre Y
     * @return {@code true} if the enemy is CHASING and within 75 units of the player
     */
    public boolean isCatching(float playerX, float playerY) {
        return alertState == AlertState.CHASING
            && Vector2.dst(position.x, position.y, playerX, playerY) < 75f;
    }

    /**
     * Teleports the enemy back to its original spawn point without resetting AI state.
     * Used by the respawn system to reposition enemies after catching the player.
     */
    public void resetToSpawn() {
        position.x = spawnX;
        position.y = spawnY;
    }


    /**
     * Releases all textures and audio resources held by this enemy.
     * Should be called when the enemy is no longer needed to avoid GPU memory leaks.
     */
    public void dispose() {
        side1.dispose();  side2.dispose();
        back1.dispose();  back2.dispose();
        front1.dispose(); front2.dispose();
        exclamation.dispose();
        question.dispose();
        heySound.dispose();
    }
}