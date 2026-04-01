package ca.sfu.spring2026team15;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Disposable;

/**
 * Draws cinematic letterbox bars that slide in from the top and bottom edges of the screen.
 * Call show() to start a cutscene, hide() to end it.
 * render() must be called with an already-open SpriteBatch using the HUD projection matrix.
 */
public class CinematicBars implements Disposable {

    private static final float BAR_HEIGHT    = 100f;
    private static final float ANIM_DURATION = 0.5f;

    private enum State { HIDDEN, APPEARING, VISIBLE, DISAPPEARING }
    private State state = State.HIDDEN;
    private float timer = 0f;
    private float currentHeight = 0f;

    private final Texture blackTexture;

    public CinematicBars() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();
        blackTexture = new Texture(pixmap);
        pixmap.dispose();
    }

    public void show() {
        if (state == State.HIDDEN || state == State.DISAPPEARING) {
            // Resume from current position so mid-reversal feels smooth
            timer = (currentHeight / BAR_HEIGHT) * ANIM_DURATION;
            state = State.APPEARING;
        }
    }

    public void hide() {
        if (state == State.VISIBLE || state == State.APPEARING) {
            timer = ANIM_DURATION * (1f - currentHeight / BAR_HEIGHT);
            state = State.DISAPPEARING;
        }
    }

    public void update(float delta) {
        if (state == State.HIDDEN || state == State.VISIBLE) return;

        timer += delta;

        if (state == State.APPEARING) {
            currentHeight = Math.min(BAR_HEIGHT, (timer / ANIM_DURATION) * BAR_HEIGHT);
            if (timer >= ANIM_DURATION) {
                currentHeight = BAR_HEIGHT;
                state = State.VISIBLE;
            }
        } else { // DISAPPEARING
            currentHeight = Math.max(0f, BAR_HEIGHT * (1f - timer / ANIM_DURATION));
            if (timer >= ANIM_DURATION) {
                currentHeight = 0f;
                state = State.HIDDEN;
            }
        }
    }

    /** Call with an open SpriteBatch already using the HUD projection matrix. */
    public void render(SpriteBatch batch) {
        if (state == State.HIDDEN || currentHeight <= 0f) return;

        batch.setColor(Color.BLACK);
        batch.draw(blackTexture, 0, 0, GameScreen.VIEW_WIDTH, currentHeight);
        batch.draw(blackTexture, 0, GameScreen.VIEW_HEIGHT - currentHeight, GameScreen.VIEW_WIDTH, currentHeight);
        batch.setColor(Color.WHITE);
    }

    public boolean isFullyVisible() {
        return state == State.VISIBLE;
    }

    @Override
    public void dispose() {
        blackTexture.dispose();
    }
}
