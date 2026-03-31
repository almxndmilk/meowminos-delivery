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

    // Score box: 5 large digits filling the empty white box at the top.
    // Box interior world coords: x [242, 913], y [430, 629] (670x199 world units).
    // 5 digits at DIGIT_W each + 4 gaps = 670, so gap = (670 - 5*115) / 4 = 24.
    private static final DigitLayout SCORE_LAYOUT = new DigitLayout(348f, 460f, 150f, 140f, 139f);

    // Time row: 4 smaller digits overlaid on the baked-in "02:10" placeholder.
    // endingScene.png has "Time : 02:10" pixel art at world y≈238-264; digits start at x≈437.
    private static final DigitLayout TIME_LAYOUT  = new DigitLayout(450, 328,  50f,  50f,  35f);

    // For the time row colon in between the minute and seconds
    private static final float TIME_COLON_W  = 25f;  // extra gap to skip the baked-in colon

    // Method that helps reduces DATA CLUMP
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

    public EndScreen(Main game, int score, int time, boolean fromObamaScene) {
        this.game  = game;
        this.score = score;
        this.time  = time;
        this.fromObamaScene = fromObamaScene;
        batch      = new SpriteBatch();
        viewport   = new ExtendViewport(1280f, 720f);
    }

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
        int[] timeDigits = { mm / 10, mm % 10, ss / 10, ss % 10 };
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

    private boolean inBounds(Vector2 p, float x1, float y1, float x2, float y2) {
        return p.x >= x1 && p.x <= x2 && p.y >= y1 && p.y <= y2;
    }

    // CODE DUPLICATION helper method, and reduced it to line 110
    private int[] digitsOf(int value, int count) {
        int[] d = new int[count];
        for (int i = count - 1; i >= 0; i--) {
            d[i] = value % 10;
            value /= 10;
        }
        return d;
    }

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
