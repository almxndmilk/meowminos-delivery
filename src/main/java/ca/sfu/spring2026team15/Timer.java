package ca.sfu.spring2026team15;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Timer {
    private float elapsedTime = 0f;
    private boolean running = false;

    public void start() { running = true; }
    public void pause() { running = false; }
    public void resume() { running = true; }
    public void stop() { running = false; }
    public void reset() { elapsedTime = 0f; running = false; }

    public void update(float delta) {
        if (running) elapsedTime += delta;
    }

    public int getSeconds() { return (int) elapsedTime; }

    public String getFormatted() {
        int total = (int) elapsedTime;
        int mm = total / 60;
        int ss = total % 60;
        return String.format("%02d:%02d", mm, ss);
    }

    public void render(SpriteBatch hudBatch, BitmapFont hudFont) {
        hudFont.draw(hudBatch, getFormatted(), 20f, 50f);
    }
}