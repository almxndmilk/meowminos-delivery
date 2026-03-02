package ca.sfu.spring2026team15;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import java.util.Random;

public class PoliceEnemy {
    private static final float SIZE = 150f;
    private static final float CHASE_SPEED = 250f;
    private static final float WANDER_SPEED = 80f;
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

    //anamation
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

    public PoliceEnemy(float spawnX, float spawnY) {
        side1 = new Texture(Gdx.files.internal("policeSide/policeSide1.png"));
        side2 = new Texture(Gdx.files.internal("policeSide/policeSide2.png"));
        back1 = new Texture(Gdx.files.internal("policeBack/policeBack1.png"));
        back2 = new Texture(Gdx.files.internal("policeBack/policeBack2.png"));
        front1 = new Texture(Gdx.files.internal("policeWalk/policeWalk1.png"));
        front2 = new Texture(Gdx.files.internal("policeWalk/policeWalk2.png"));
        exclamation = new Texture(Gdx.files.internal("policeSupplementary/exclamationMark.png"));
        question= new Texture(Gdx.files.internal("policeSupplementary/questionMark.png"));
        currentFrame = front1;
        position= new Vector2(spawnX, spawnY);
    }

    public void update(float delta, float playerX, float playerY) {
        float dist = Vector2.dst(position.x, position.y, playerX, playerY);
        boolean inRange = dist <= DETECTION_RANGE;

        // state machine
        switch (alertState) {
            case NONE:
                if (inRange) {
                    alertState = AlertState.ALERTED;
                    alertTimer = 0f;
                }
                break;
            case ALERTED:
                if (!inRange) {
                    // player ran away before timer finished, reset
                    alertState = AlertState.NONE;
                    alertTimer = 0f;
                } else {
                    alertTimer += delta;
                    if (alertTimer >= ALERT_DELAY) {
                        alertState = AlertState.CHASING;
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

        // movement
        if (alertState == AlertState.CHASING) {
            Vector2 dir = new Vector2(playerX - position.x, playerY - position.y).nor();
            position.x += dir.x * CHASE_SPEED * delta;
            position.y += dir.y * CHASE_SPEED * delta;

            // still pick dominant axis for sprite direction
            float dx = playerX - position.x;
            float dy = playerY - position.y;
            if (Math.abs(dx) > Math.abs(dy)) {
                lastDirection = (dx > 0) ? Direction.RIGHT : Direction.LEFT;
            } else {
                lastDirection = (dy > 0) ? Direction.UP : Direction.DOWN;
            }
        }
        else {
            //wander
            wanderTimer -= delta;
            if (wanderTimer <= 0f) {
                wanderTimer = WANDER_CHANGE_TIME;
                lastDirection = Direction.values()[random.nextInt(4)];
            }
            switch (lastDirection) {
                case LEFT: position.x -= WANDER_SPEED * delta; break;
                case RIGHT: position.x += WANDER_SPEED * delta; break;
                case UP: position.y += WANDER_SPEED * delta; break;
                case DOWN: position.y -= WANDER_SPEED * delta; break;
            }
        }

        //animation
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

    public void render(SpriteBatch batch) {
        float x = position.x - SIZE / 2f;
        float y = position.y - SIZE / 2f;

        if (lastDirection == Direction.RIGHT) {
            batch.draw(currentFrame, x + SIZE, y, -SIZE, SIZE);
        } else {
            batch.draw(currentFrame, x, y, SIZE, SIZE);
        }

        // draw ? or ! above head depending on state
        if (alertState == AlertState.ALERTED) {
            batch.draw(question, position.x, position.y + SIZE / 3.5f, 30f, 30f);
        } else if (alertState == AlertState.CHASING) {
            batch.draw(exclamation, position.x, position.y + SIZE / 3.5f, 30f, 30f);
        }
    }

    public void dispose() {
        side1.dispose();  side2.dispose();
        back1.dispose();  back2.dispose();
        front1.dispose(); front2.dispose();
        exclamation.dispose();
        question.dispose();
    }
}