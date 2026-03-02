package ca.sfu.spring2026team15;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class EndScreen implements Screen {

    private final Main game;
    private final int score; // seconds survived

    private SpriteBatch batch;
    private ExtendViewport viewport;

    private Texture background;
    private Texture[] digits; // digits[0] = 0.png, ..., digits[9] = 9.png

    // Score box: 5 large digits filling the empty white box at the top.
    // Box interior world coords: x [242, 913], y [430, 629] (670x199 world units).
    // 5 digits at DIGIT_W each + 4 gaps = 670, so gap = (670 - 5*115) / 4 = 24.
    private static final float DIGIT_W   = 115f;
    private static final float DIGIT_H   = 185f;
    private static final float DIGIT_X   = 242f;  // x of first digit (left interior edge)
    private static final float DIGIT_Y   = 437f;  // y bottom (interior bottom + 7 margin)
    private static final float DIGIT_GAP = 139f;  // step = DIGIT_W + 24 gap

    // Time row: 4 smaller digits overlaid on the baked-in "02:10" placeholder.
    // endingScene.png has "Time : 02:10" pixel art at world y≈238-264; digits start at x≈437.
    private static final float TIME_W        = 40f;
    private static final float TIME_H        = 45f;
    private static final float TIME_X        = 500f; // x of first time digit (M tens)
    private static final float TIME_Y        = 340f; // y bottom, centres on time row
    private static final float TIME_GAP      = 35f;  // step between digits within MM or SS
    private static final float TIME_COLON_W  = 22f;  // extra gap to skip the baked-in colon

    // Hit zones for buttons (world coords, origin = bottom-left)
    private static final float CONTINUE_X1 = 230f, CONTINUE_X2 = 475f;
    private static final float CONTINUE_Y1 = 225f, CONTINUE_Y2 = 265f;
    private static final float EXIT_X1     = 230f, EXIT_X2     = 365f;
    private static final float EXIT_Y1     = 158f, EXIT_Y2     = 198f;

    public EndScreen(Main game, int score) {
        this.game  = game;
        this.score = score;
        batch      = new SpriteBatch();
        viewport   = new ExtendViewport(1280f, 720f);
    }

    @Override
    public void show() {
        background = new Texture(Gdx.files.internal("End/endingScene.png"));
        digits = new Texture[10];
        for (int i = 0; i < 10; i++) {
            digits[i] = new Texture(Gdx.files.internal("End/" + i + ".png"));
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
        int s = Math.min(score, 99999);
        int[] scoreDigits = {
            s / 10000,
            (s / 1000) % 10,
            (s / 100)  % 10,
            (s / 10)   % 10,
             s         % 10
        };
        for (int i = 0; i < 5; i++) {
            batch.draw(digits[scoreDigits[i]], DIGIT_X + i * DIGIT_GAP, DIGIT_Y, DIGIT_W, DIGIT_H);
        }

        // Time: MM:SS (colon is baked into the background image)
        int mm = Math.min(score / 60, 99);
        int ss = score % 60;
        int[] timeDigits = { mm / 10, mm % 10, ss / 10, ss % 10 };
        for (int i = 0; i < 4; i++) {
            float offset = (i < 2) ? i * TIME_GAP : i * TIME_GAP + TIME_COLON_W;
            batch.draw(digits[timeDigits[i]], TIME_X + offset, TIME_Y, TIME_W, TIME_H);
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

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        background.dispose();
        for (Texture t : digits) {
            t.dispose();
        }
        batch.dispose();
    }
}
