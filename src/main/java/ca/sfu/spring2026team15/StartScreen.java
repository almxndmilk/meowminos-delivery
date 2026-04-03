package ca.sfu.spring2026team15;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

/**
 * The main-menu screen for "Meowmino's Delivery".
 *
 * <p>Displays the title card, a Play button, and icon buttons for Instructions and Settings.
 * Clicking Play triggers an iris-wipe closing animation (using the OpenGL stencil buffer)
 * before transitioning to {@link GameScreen}.
 * Background music plays while this screen is active (respecting the global sound toggle).
 */
public class StartScreen implements Screen {

    private final Main game;
    private SpriteBatch batch;
    private ExtendViewport viewport;

    private Texture startTexture;
    private Texture buttonTexture;
    private Texture titleTexture;
    private Texture instructionBtnTexture;
    private Texture settingsBtnTexture;

    // Start Button
    float btnWidth  = 225f;
    float btnHeight = 125f;

    // Title
    float titleWidth  = 700f;
    float titleHeight = 350f;

    // Instruction / Settings icon buttons
    private static final float ICON_W = 130f;
    private static final float ICON_H = 75f;

    private Music backgroundMusic;
    private Sound startButtonSound;

    private ShapeRenderer shapeRenderer;
    private Texture blackTexture;
    private boolean transitionActive = false;
    private float transitionTimer = 0f;
    private static final float TRANSITION_DURATION = 1.2f;

    /**
     * Creates the start screen. Initialises the {@link SpriteBatch} and viewport;
     * heavy asset loading is deferred to {@link #show()}.
     *
     * @param game the application controller used to switch screens
     */
    public StartScreen(Main game) {
        this.game = game;
        batch = new SpriteBatch();
        // viewport = new FitViewport(1280f, 720f);
        viewport = new ExtendViewport(1280f, 720f);
    }

    /**
     * Draws the menu and handles button input each frame.
     * During an active transition, advances the iris-wipe timer and switches to
     * {@link GameScreen} once the iris closes fully.
     * Touch coordinates are unprojected from screen space to world space before hit-testing.
     *
     * @param delta time in seconds since the last frame
     */
    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        // button coordinates
        float btnX = (viewport.getWorldWidth()  / 2) - (btnWidth  / 2);
        float btnY = (viewport.getWorldHeight() / 3.5f) - (btnHeight / 2);

        // title cooridnates
        float titleX = (viewport.getWorldWidth()  / 2) - (titleWidth  / 2);
        float titleY = (viewport.getWorldHeight() / 1.8f) - (titleHeight / 2);

        // Instruction / Settings button positions
        float newBtnY = btnY - 60f;
        float instrX  = (viewport.getWorldWidth() / 2f) - ICON_W ;
        float settX   = (viewport.getWorldWidth() / 2f);

        batch.begin();
        batch.draw(startTexture, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        batch.draw(buttonTexture, btnX, btnY, btnWidth, btnHeight);
        batch.draw(titleTexture, titleX, titleY, titleWidth, titleHeight);
        batch.draw(instructionBtnTexture, instrX, newBtnY, ICON_W, ICON_H);
        batch.draw(settingsBtnTexture,    settX,  newBtnY, ICON_W, ICON_H);
        batch.end();

        if (transitionActive) {
            transitionTimer += delta;
            float w = viewport.getWorldWidth();
            float h = viewport.getWorldHeight();
            float halfDiag = (float) Math.sqrt(w * w + h * h) / 2f + 50f;
            float t = transitionTimer / TRANSITION_DURATION;
            float irisRadius = halfDiag * (1f - t); // shrinks to 0
            renderIris(Math.max(0f, irisRadius));

            if (transitionTimer >= TRANSITION_DURATION) {
                game.setScreen(new GameScreen(game));
                dispose();
            }
            return;
        }

        if (Gdx.input.justTouched()) {
            // convert screen coordinates to world coordinates
            Vector2 touch = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));

            if (touch.x >= btnX && touch.x <= btnX + btnWidth &&
                    touch.y >= btnY && touch.y <= btnY + btnHeight) {
                if (SettingsScreen.soundOn) {
                    startButtonSound.play(1.0f);
                }
                transitionActive = true;
                transitionTimer = 0f;
            }
            if (touch.x >= instrX && touch.x <= instrX + ICON_W &&
                    touch.y >= newBtnY && touch.y <= newBtnY + ICON_H) {
                dispose();
                game.setScreen(new InstructionsScreen(game));
            }
            if (touch.x >= settX && touch.x <= settX + ICON_W &&
                    touch.y >= newBtnY && touch.y <= newBtnY + ICON_H) {
                dispose();
                game.setScreen(new SettingsScreen(game));
            }
        }
    }

    /**
     * Renders the iris-wipe effect using the OpenGL stencil buffer.
     * The circle shrinks from full-screen to zero as the transition progresses,
     * creating the classic iris-close look used to enter the game.
     *
     * @param irisRadius radius of the transparent circle in world/screen units;
     *                   0 = fully black, &ge; half-diagonal = fully open
     */
    private void renderIris(float irisRadius) {
        float w = viewport.getWorldWidth();
        float h = viewport.getWorldHeight();

        // Step 1: write iris circle into stencil only (no color output)
        Gdx.gl.glClear(GL20.GL_STENCIL_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_STENCIL_TEST);
        Gdx.gl.glStencilFunc(GL20.GL_ALWAYS, 1, 0xFF);
        Gdx.gl.glStencilOp(GL20.GL_KEEP, GL20.GL_KEEP, GL20.GL_REPLACE);
        Gdx.gl.glColorMask(false, false, false, false);

        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(w / 2f, h / 2f, irisRadius, 64);
        shapeRenderer.end();

        Gdx.gl.glColorMask(true, true, true, true);

        // Step 2: draw black everywhere stencil == 0 (outside the circle)
        Gdx.gl.glStencilFunc(GL20.GL_EQUAL, 0, 0xFF);
        Gdx.gl.glStencilOp(GL20.GL_KEEP, GL20.GL_KEEP, GL20.GL_KEEP);

        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        batch.setColor(Color.BLACK);
        batch.draw(blackTexture, 0, 0, w, h);
        batch.setColor(Color.WHITE);
        batch.end();

        Gdx.gl.glDisable(GL20.GL_STENCIL_TEST);
    }

    // Screen interface required implementations
    /**
     * Called by LibGDX when the window is resized. Updates the viewport.
     * @param width  new screen width in pixels
     * @param height new screen height in pixels
     */
    @Override public void resize(int width, int height) {
        viewport.update(width, height, true);
    }
    /**
     * Called by LibGDX when this screen becomes active.
     * Loads all textures, starts background music, creates the shape renderer and
     * 1×1 black texture required for the iris-wipe effect.
     */
    @Override public void show() {
        startTexture          = new Texture(Gdx.files.internal("StartScreen/startScreenBackground.png"));
        buttonTexture         = new Texture(Gdx.files.internal("StartScreen/playButton.PNG"));
        titleTexture          = new Texture(Gdx.files.internal("StartScreen/newTitle.png"));
        instructionBtnTexture = new Texture(Gdx.files.internal("StartScreen/instructionButton.PNG"));
        settingsBtnTexture    = new Texture(Gdx.files.internal("StartScreen/settingButton.PNG"));

        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/backgroundMusic.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.5f);
        if (SettingsScreen.soundOn) backgroundMusic.play();

        // audio
        startButtonSound = Gdx.audio.newSound(Gdx.files.internal("audio/startButton.mp3"));

        shapeRenderer = new ShapeRenderer();
        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.BLACK);
        pm.fill();
        blackTexture = new Texture(pm);
        pm.dispose();
    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    /** Releases all textures, audio, shape renderer, and batch owned by this screen. */
    @Override
    public void dispose() {
        startTexture.dispose();
        buttonTexture.dispose();
        titleTexture.dispose();
        instructionBtnTexture.dispose();
        settingsBtnTexture.dispose();
        batch.dispose();
        backgroundMusic.dispose();
        startButtonSound.dispose();
        shapeRenderer.dispose();
        blackTexture.dispose();
    }
}
