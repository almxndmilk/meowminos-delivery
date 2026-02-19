package ca.sfu.spring2026team15;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Main game class for the 2D game.
 * Extends ApplicationAdapter and implements the game loop.
 */
public class Main extends ApplicationAdapter {
    private SpriteBatch batch;

    /**
     * Called when the game is first created.
     * Initializes game resources.
     */
    @Override
    public void create() {
        batch = new SpriteBatch();
    }

    /**
     * Called every frame to render the game.
     * Clears the screen and draws game content.
     */
    @Override
    public void render() {
        // Clear screen with black color
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        // Draw your game here
        batch.end();
    }

    /**
     * Called when the game is disposed.
     * Cleans up resources.
     */
    @Override
    public void dispose() {
        batch.dispose();
    }
}