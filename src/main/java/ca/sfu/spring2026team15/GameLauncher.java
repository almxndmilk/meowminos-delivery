package ca.sfu.spring2026team15;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import ca.sfu.spring2026team15.Main;

public class GameLauncher {
    public static void main(String[] args) {
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new Main(), getDefaultConfiguration());
    }

    private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Meowmino's Delivery");
        config.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate);
        config.setWindowedMode(1920, 1080);
        config.setWindowIcon("catOnBike/catOnBike_Up.png");
//        config.setForegroundFPS(60);
        config.useVsync(true);
        // r, g, b, a=8 each; depth=16; stencil=8 (needed for iris wipe); samples=0
        config.setBackBufferConfig(8, 8, 8, 8, 16, 8, 0);
        return config;
    }
}
