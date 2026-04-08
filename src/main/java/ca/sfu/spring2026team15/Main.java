package ca.sfu.spring2026team15;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;

/**
 * LibGDX application entry point for "Meowmino's Delivery".
 * Extends {@link com.badlogic.gdx.Game} to gain built-in screen management.
 * On creation, immediately transitions to the {@link StartScreen}.
 */
public class Main extends Game {
    /** Creates the LibGDX application instance; called by the framework. */
    public Main() {}

    /**
     * Called once by LibGDX after the OpenGL context is ready.
     * Sets {@link StartScreen} as the initial active screen.
     */
    @Override
    public void create() {
        setScreen(new StartScreen(this));
    }
}
