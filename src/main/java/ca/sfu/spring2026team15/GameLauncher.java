package ca.sfu.spring2026team15;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import ca.sfu.spring2026team15.Main;

/**
 * Desktop application launcher for "Meowmino's Delivery".
 *
 * <p>Configures the LWJGL3 backend (window size, FPS cap, VSync, window icon, and back-buffer
 * format with 8-bit stencil required for the iris-wipe effect) then starts the LibGDX
 * application with a {@link Main} instance.
 *
 * <p><b>macOS note:</b> The POM's exec plugin passes {@code -XstartOnFirstThread} automatically;
 * always use {@code mvn exec:exec} rather than {@code mvn exec:java} on macOS.
 */
public class GameLauncher {
    /**
     * Application entry point. Delegates immediately to {@link #createApplication()}.
     * @param args command-line arguments (ignored)
     */
    public static void main(String[] args) {
        createApplication();
    }

    /**
     * Constructs and starts the LWJGL3 application.
     * @return the running {@link Lwjgl3Application} instance
     */
    private static Lwjgl3Application createApplication() {
        return new Lwjgl3Application(new Main(), getDefaultConfiguration());
    }

    /**
     * Builds the LWJGL3 window configuration.
     * Sets title, display mode FPS, 1920×1080 windowed mode, window icon, VSync, and
     * a back-buffer with 8-bit stencil (required by the OpenGL stencil-buffer iris wipe).
     *
     * @return a fully configured {@link Lwjgl3ApplicationConfiguration}
     */
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
