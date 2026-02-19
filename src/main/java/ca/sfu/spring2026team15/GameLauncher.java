package ca.sfu.spring2026team15;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public class GameLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("2D Game - CMPT 276");
        config.setWindowedMode(1280, 900);
        config.setForegroundFPS(60);
        config.useVsync(true);

        // Create and start the game
        new Lwjgl3Application(new Main(), config);
    }
}