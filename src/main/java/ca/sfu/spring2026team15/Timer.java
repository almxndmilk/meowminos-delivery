package ca.sfu.spring2026team15;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Timer {
    private static final float TOTAL_TIME = 300f; // 5 minutes
    private float timeRemaining = TOTAL_TIME;
    private float elapsedTime = 0f;
    private boolean running = false;

    public void start()  { running = true; }
    public void pause()  { running = false; }
    public void resume() { running = true; }
    public void stop()   { running = false; }
    public void reset()  { timeRemaining = TOTAL_TIME; elapsedTime = 0f; running = false; }

    public void update(float delta) {
        if (!running) return;
        timeRemaining -= delta;
        elapsedTime   += delta;
        if (timeRemaining <= 0f) {
            timeRemaining = 0f;
            running = false;
        }
    }

    public void subtractTime(float seconds) {
        timeRemaining -= seconds;
        if (timeRemaining < 0f) {
            timeRemaining = 0f;
        }
    }

    public boolean isFinished()    { return timeRemaining <= 0f; }
    public int getRemainingSeconds() { return (int) timeRemaining; }
    public int getElapsedSeconds()   { return (int) elapsedTime; }

    public String getFormatted() {
        int total = (int) timeRemaining;
        int mm = total / 60;
        int ss = total % 60;
        return String.format("%02d:%02d", mm, ss);
    }

    public void render(SpriteBatch hudBatch, BitmapFont hudFont) {
        hudFont.draw(hudBatch, getFormatted(), 20f, 50f);
    }
}