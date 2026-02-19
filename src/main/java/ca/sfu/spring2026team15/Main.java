package ca.sfu.spring2026team15;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Main entry point for Meowmino's Delivery.
 *
 * <p>Bootstraps the LibGDX LWJGL3 application and owns the top-level game loop.
 * Extend this class or delegate to a {@code Game}/{@code Screen} hierarchy as
 * the project grows.
 */
public class Main extends ApplicationAdapter {

    /** Batch used to draw sprites each frame. */
    private SpriteBatch batch;

    /**
     * Application entry point. Configures the window and launches LibGDX.
     *
     * @param args command-line arguments (unused)
     */
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Meowmino's Delivery");
        config.setWindowedMode(800, 600);
        config.useVsync(true);
        new Lwjgl3Application(new Main(), config);
    }

    /**
     * Called once when the application is created.
     * Initialize all resources here.
     */
    @Override
    public void create() {
        batch = new SpriteBatch();
    }

    /**
     * Called every frame to update and draw the game.
     * Clear the screen, then issue all draw calls inside a batch begin/end pair.
     */
    @Override
    public void render() {
        Gdx.gl.glClearColor(0.15f, 0.15f, 0.2f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        // TODO: draw game entities here
        batch.end();
    }

    /**
     * Called when the application is destroyed.
     * Release all {@link com.badlogic.gdx.utils.Disposable} resources.
     */
    @Override
    public void dispose() {
        batch.dispose();
    }
}
