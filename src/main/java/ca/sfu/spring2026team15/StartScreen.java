package ca.sfu.spring2026team15;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

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
    float btnWidth  = 200f;
    float btnHeight = 125f;

    // Title
    float titleWidth  = 700f;
    float titleHeight = 350f;

    // Instruction / Settings icon buttons
    private static final float ICON_W = 80f;
    private static final float ICON_H = 80f;

    private Music backgroundMusic;
    private Sound startButtonSound;

    public StartScreen(Main game) {
        this.game = game;
        batch = new SpriteBatch();
        // viewport = new FitViewport(1280f, 720f);
        viewport = new ExtendViewport(1280f, 720f);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        // button coordinates
        float btnX = (viewport.getWorldWidth()  / 2) - (btnWidth  / 2);
        float btnY = (viewport.getWorldHeight() / 3.75f) - (btnHeight / 2);

        // title cooridnates
        float titleX = (viewport.getWorldWidth()  / 2) - (titleWidth  / 2);
        float titleY = (viewport.getWorldHeight() / 1.8f) - (titleHeight / 2);

        // Instruction / Settings button positions
        float newBtnY = btnY - 100f;
        float instrX  = (viewport.getWorldWidth() / 2f) - 50f - ICON_W;
        float settX   = (viewport.getWorldWidth() / 2f) + 50f;

        batch.begin();
        batch.draw(startTexture, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        batch.draw(buttonTexture, btnX, btnY, btnWidth, btnHeight);
        batch.draw(titleTexture, titleX, titleY, titleWidth, titleHeight);
        batch.draw(instructionBtnTexture, instrX, newBtnY, ICON_W, ICON_H);
        batch.draw(settingsBtnTexture,    settX,  newBtnY, ICON_W, ICON_H);
        batch.end();

        if (Gdx.input.justTouched()) {
            // convert screen coordinates to world coordinates
            Vector2 touch = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));

            if (touch.x >= btnX && touch.x <= btnX + btnWidth &&
                    touch.y >= btnY && touch.y <= btnY + btnHeight) {
                if (SettingsScreen.soundOn) {
                    startButtonSound.play(1.0f);
                }
                game.setScreen(new GameScreen(game));
                dispose();
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

    // Screen interface required implementations
    @Override public void resize(int width, int height) {
        viewport.update(width, height, true);
    }
    @Override public void show() {
        startTexture          = new Texture(Gdx.files.internal("StartScreen/startScreenBackground.png"));
        buttonTexture         = new Texture(Gdx.files.internal("StartScreen/playButton.PNG"));
        titleTexture          = new Texture(Gdx.files.internal("StartScreen/newTitle.png"));
        instructionBtnTexture = new Texture(Gdx.files.internal("StartScreen/instructionButton.PNG"));
        settingsBtnTexture    = new Texture(Gdx.files.internal("StartScreen/settingButton.PNG"));

        // Load and play background music
        if (SettingsScreen.soundOn) {
            backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/backgroundMusic.mp3"));
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(0.5f);
            backgroundMusic.play();
        } else {
            backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/backgroundMusic.mp3"));
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(0.5f);
        }

        // audio
        startButtonSound = Gdx.audio.newSound(Gdx.files.internal("audio/startButton.mp3"));
    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

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
    }
}
