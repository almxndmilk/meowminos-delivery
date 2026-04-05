package ca.sfu.spring2026team15;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.audio.Sound;


/**
 * Screen that displays the game instructions image.
 * The player can return to {@link StartScreen} by pressing Escape.
 * A toggle sound plays on entry and an ESC sound plays when leaving.
 */
public class InstructionsScreen implements Screen {

    private final Main game;
    private SpriteBatch batch;
    private ExtendViewport viewport;
    private Texture background;

    private Sound escSound;
    private Sound toggleSound;


    /**
     * Creates the instructions screen with a viewport sized to 1280×720.
     * @param game the application controller used to switch back to {@link StartScreen}
     */
    public InstructionsScreen(Main game) {
        this.game = game;
        batch    = new SpriteBatch();
        viewport = new ExtendViewport(1280f, 720f);
    }

    /** Loads the instructions background texture and audio clips. */
    @Override
    public void show() {
        background = new Texture(Gdx.files.internal("StartScreen/instructions.png"));
        toggleSound = ScreenAudioHelper.loadAndPlayEntrySound("audio/toggleSound.mp3");
        escSound    = ScreenAudioHelper.load("audio/escSound.mp3");              
    }

    /**
     * Draws the instructions image and listens for ESC to return to the start screen.
     * @param delta time in seconds since the last frame (unused)
     */
    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        batch.draw(background, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            ScreenAudioHelper.playIfEnabled(escSound);
            goBack();
        }
    }
    

    /** Disposes this screen and returns to {@link StartScreen}. */
    private void goBack() {
        dispose();
        game.setScreen(new StartScreen(game));
    }

    /** @param width new width; @param height new height — updates viewport dimensions */
    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    /** Releases the background texture, batch, and audio. */
    @Override
    public void dispose() {
        background.dispose();
        batch.dispose();
        toggleSound.dispose();
    }
}
