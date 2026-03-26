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


public class InstructionsScreen implements Screen {

    private final Main game;
    private SpriteBatch batch;
    private ExtendViewport viewport;
    private Texture background;

    private Sound escSound;
    private Sound toggleSound;



    public InstructionsScreen(Main game) {
        this.game = game;
        batch    = new SpriteBatch();
        viewport = new ExtendViewport(1280f, 720f);
    }

    @Override
    public void show() {
        background = new Texture(Gdx.files.internal("StartScreen/instructions.png"));
        escSound = Gdx.audio.newSound(Gdx.files.internal("audio/escSound.mp3"));
        toggleSound = Gdx.audio.newSound(Gdx.files.internal("audio/toggleSound.mp3"));                
        if (SettingsScreen.soundOn) {
            toggleSound.play(0.8f);
        }
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        batch.draw(background, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (SettingsScreen.soundOn) {
                escSound.play(0.8f);
            }
            goBack();
        }
    }
    

    private void goBack() {
        dispose();
        game.setScreen(new StartScreen(game));
    }

    @Override public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        background.dispose();
        batch.dispose();
    }
}
