package ca.sfu.spring2026team15;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

/**
 * Launcher class for the game.
 * Initializes LibGDX with LWJGL3 backend and starts the game.
 */
public class GameLauncher {
    /**
     * Main entry point for the game.
     *
     * @param args Command line arguments (unused)
     */
    public static void main(String[] args) {
        // Create LibGDX configuration
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("2D Game - CMPT 276");
        config.setWindowedMode(800, 600);
        config.setForegroundFPS(60);

        // Create and start the game
        new Lwjgl3Application(new Main(), config);
    }
}