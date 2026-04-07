package ca.sfu.spring2026team15;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

/**
 * The end-of-game screen displaying the player's final score (fish collected) and time.
 *
 * <p>Score is rendered as 4 large digit sprites; time is rendered as MM:SS using smaller
 * digit sprites with the colon baked into the background image. Two buttons allow the
 * player to return to {@link StartScreen} or quit the application.
 * Plays a win sound ("yipee") when reached from the {@link ObamaScreen} cutscene,
 * or a loss sound otherwise.
 */
public class EndScreen implements Screen {

    private final Main game;
    private final int score; // fish collected
    private final int time;  // seconds elapsed
    private final boolean fromObamaScene;

    private SpriteBatch batch;
    private ExtendViewport viewport;

    private Texture background;
    private Texture[] digits; // digits[0] = 0.png, ..., digits[9] = 9.png
    private Texture[] timeNumbers;

    /** 
     * Score box: 4 large digits at the middle top part of the endScreen, to represent the score 
     * x = x-cor, y = y-cor, w = width, h = height, gap = spacing between digits 
    */
    private static final DigitLayout SCORE_LAYOUT = new DigitLayout(348f, 460f, 150f, 140f, 139f);

    /** 
     * Time row: 4 smaller digits with different appearance for representation of time
     * x = x-cor, y = y-cor, w = width, h = height, gap = spacing between digits 
    */
    private static final DigitLayout TIME_LAYOUT  = new DigitLayout(450, 328,  50f,  50f,  35f);

    // For the time row colon in between the minute and seconds
    private static final float TIME_COLON_W  = 25f;  // extra gap to skip the baked-in colon

    // Method that helps reduces DATA CLUMP
    /**
     * Holds layout parameters for a row of digit sprites, avoiding repeated constants.
     */
    private static class DigitLayout {
        final float x, y, w, h, gap;
        DigitLayout(float x, float y, float w, float h, float gap) {
            this.x = x; this.y = y; this.w = w;
            this.h = h; this.gap = gap;
        }
    }

    // Hit zones for buttons (world coords, origin = bottom-left)
    private static final float CONTINUE_X1 = 260f, CONTINUE_X2 = 475f;
    private static final float CONTINUE_Y1 = 225f, CONTINUE_Y2 = 265f;
    private static final float EXIT_X1     = 235f, EXIT_X2     = 365f;
    private static final float EXIT_Y1     = 158f, EXIT_Y2     = 198f;

    //music
    private Music backgroundMusic;
    private Music yipeeSound;
    private Music doowopSound;

    /**
     * Creates the end screen.
     *
     * @param game           the application controller used to switch screens
     * @param score          number of fish collected (displayed as the score)
     * @param time           total elapsed time in seconds (displayed as MM:SS)
     * @param fromObamaScene {@code true} when reached via the Obama win cutscene;
     *                       affects which end sound is played
     */
    public EndScreen(Main game, int score, int time, boolean fromObamaScene) {
        this.game  = game;
        this.score = score;
        this.time  = time;
        this.fromObamaScene = fromObamaScene;
        batch      = new SpriteBatch();
        viewport   = new ExtendViewport(1280f, 720f);
    }

    /**
     * Called by LibGDX when this screen becomes active.
     * Loads the background texture, all digit textures (0–9) for both score and time,
     * and starts the appropriate end music based on {@code fromObamaScene}.
     */
    @Override
    public void show() {
        background = new Texture(Gdx.files.internal("End/endingScene2.png"));
        digits = new Texture[10];
        timeNumbers = new Texture[10];
        for (int i = 0; i < 10; i++) {
            digits[i] = new Texture(Gdx.files.internal("End/" + i + ".png"));
            timeNumbers[i] = new Texture(Gdx.files.internal("End/timeNumbers/" + i + ".png"));
        }

        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/endMusic.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.5f);

        if (fromObamaScene) {
            yipeeSound = Gdx.audio.newMusic(Gdx.files.internal("Obama/yipee.mp3"));
            yipeeSound.setVolume(0.8f);
        } else {
            doowopSound = Gdx.audio.newMusic(Gdx.files.internal("audio/boowop.mp3"));
            doowopSound.setVolume(1.5f);
        }

        if (SettingsScreen.soundOn) {
            backgroundMusic.play();
            if (yipeeSound != null) yipeeSound.play();
            if (doowopSound != null) doowopSound.play();
        }
    }

    /**
     * Draws the end screen and handles Continue / Exit button input.
     * Score and time digits are drawn using {@link #digitsOf} to decompose the values
     * into individual digit indices. Touch coordinates are unprojected before hit-testing.
     *
     * @param delta time in seconds since the last frame
     */
    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();

        // Background
        batch.draw(background, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

        // Score: 5-digit zero-padded (capped at 99999 seconds)
        int s = Math.max(0, Math.min(score, 99999));
        int[] scoreDigits =  digitsOf(s, 4);
        for (int i = 0; i < 4; i++) {
            batch.draw(digits[scoreDigits[i]],
                    SCORE_LAYOUT.x + i * SCORE_LAYOUT.gap,
                    SCORE_LAYOUT.y,
                    SCORE_LAYOUT.w,
                    SCORE_LAYOUT.h);
        }

        // Time: MM:SS (colon is baked into the background image)
        int mm = Math.min(time / 60, 99);
        int ss = time % 60;
        int[] timeDigits = digitsOf(mm * 100 + ss, 4);
        for (int i = 0; i < 4; i++) {
            float offset = (i < 2) ? i * TIME_LAYOUT.gap : i * TIME_LAYOUT.gap + TIME_COLON_W;
            batch.draw(timeNumbers[timeDigits[i]],
                    TIME_LAYOUT.x + offset,
                    TIME_LAYOUT.y,
                    TIME_LAYOUT.w,
                    TIME_LAYOUT.h);
        }

        batch.end();

        // Button input
        if (Gdx.input.justTouched()) {
            Vector2 touch = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            if (inBounds(touch, CONTINUE_X1, CONTINUE_Y1, CONTINUE_X2, CONTINUE_Y2)) {
                dispose();
                game.setScreen(new StartScreen(game));
            } else if (inBounds(touch, EXIT_X1, EXIT_Y1, EXIT_X2, EXIT_Y2)) {
                dispose();
                Gdx.app.exit();
            }
        }
    }

    /**
     * Returns {@code true} if the point {@code p} lies within the axis-aligned rectangle
     * defined by {@code (x1,y1)} (bottom-left) and {@code (x2,y2)} (top-right).
     *
     * @param p  point to test (world coordinates)
     * @param x1 left bound
     * @param y1 bottom bound
     * @param x2 right bound
     * @param y2 top bound
     * @return {@code true} if the point is inside the rectangle
     */
    private boolean inBounds(Vector2 p, float x1, float y1, float x2, float y2) {
        return p.x >= x1 && p.x <= x2 && p.y >= y1 && p.y <= y2;
    }

    // CODE DUPLICATION helper method
    // reduces repitiion in line 110 and line 122
    /**
     * Decomposes an integer into its individual decimal digits, zero-padded to {@code count} digits.
     * The most-significant digit is at index 0.
     *
     * @param value the non-negative integer to decompose
     * @param count the number of digits to return (excess leading digits are truncated)
     * @return array of {@code count} digit values in [0, 9]
     */
    private int[] digitsOf(int value, int count) {
        int[] d = new int[count];
        for (int i = count - 1; i >= 0; i--) {
            d[i] = value % 10;
            value /= 10;
        }
        return d;
    }

    /**
     * Called by LibGDX when the window is resized. Updates the viewport.
     * @param width  new screen width in pixels
     * @param height new screen height in pixels
     */
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {
        if (backgroundMusic != null) backgroundMusic.stop();
        if (yipeeSound != null) yipeeSound.stop();
        if (doowopSound != null) doowopSound.stop();
    }

    /** Releases all textures and audio resources owned by this screen. */
    @Override
    public void dispose() {
        background.dispose();
        for (Texture t : digits) {
            t.dispose();
        }
        for (Texture t : timeNumbers) {
            t.dispose();
        }
        if (backgroundMusic != null) backgroundMusic.dispose();
        if (yipeeSound != null) yipeeSound.dispose();
        if (doowopSound != null) doowopSound.dispose();
        batch.dispose();
    }
}
