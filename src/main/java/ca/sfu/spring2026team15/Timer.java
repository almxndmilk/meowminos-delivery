package ca.sfu.spring2026team15;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * A countdown game timer with a fixed total duration of {@value #TOTAL_TIME} seconds (5 minutes).
 * Tracks both remaining time and total elapsed time; can be started, paused, resumed, and stopped.
 * Time can also be deducted as a penalty (e.g. on player respawn).
 */
public class Timer {
    private static final float TOTAL_TIME = 300f; // 5 minutes
    private float timeRemaining = TOTAL_TIME;
    private float elapsedTime = 0f;
    private boolean running = false;

    /** Starts the timer (same effect as {@link #resume()}). */
    public void start()  { running = true; }

    /** Pauses the timer without resetting it. */
    public void pause()  { running = false; }

    /** Resumes a paused timer. */
    public void resume() { running = true; }

    /** Stops the timer without resetting it (same effect as {@link #pause()}). */
    public void stop()   { running = false; }

    /** Resets the timer to full duration and zero elapsed time in a stopped state. */
    public void reset()  { timeRemaining = TOTAL_TIME; elapsedTime = 0f; running = false; }

    /**
     * Advances the timer by one frame if it is running.
     * Stops automatically when remaining time reaches zero.
     *
     * @param delta time in seconds since the last frame
     */
    public void update(float delta) {
        if (!running) return;
        timeRemaining -= delta;
        elapsedTime   += delta;
        if (timeRemaining <= 0f) {
            timeRemaining = 0f;
            running = false;
        }
    }

    /**
     * Deducts time from the remaining countdown as a penalty.
     * Clamps remaining time to zero if the deduction would go negative.
     *
     * @param seconds the penalty amount in seconds
     */
    public void subtractTime(float seconds) {
        timeRemaining -= seconds;
        if (timeRemaining < 0f) {
            timeRemaining = 0f;
        }
    }

    /** @return {@code true} if the countdown has reached zero */
    public boolean isFinished()    { return timeRemaining <= 0f; }

    /** @return whole seconds remaining, truncated toward zero */
    public int getRemainingSeconds() { return (int) timeRemaining; }

    /** @return whole seconds elapsed since the timer was started, truncated toward zero */
    public int getElapsedSeconds()   { return (int) elapsedTime; }

    /**
     * Returns the remaining time formatted as {@code MM:SS}.
     * @return formatted time string, e.g. {@code "04:37"}
     */
    public String getFormatted() {
        int total = (int) timeRemaining;
        int mm = total / 60;
        int ss = total % 60;
        return String.format("%02d:%02d", mm, ss);
    }

    /**
     * Draws the formatted remaining time in the top-left corner of the HUD.
     *
     * @param hudBatch the HUD {@link SpriteBatch} (must be begun)
     * @param hudFont  the font to use for rendering
     */
    public void render(SpriteBatch hudBatch, BitmapFont hudFont) {
        hudFont.draw(hudBatch, getFormatted(), 20f, 50f);
    }
}