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


public class SettingsScreen implements Screen {

    private final Main game;
    private SpriteBatch batch;
    private ExtendViewport viewport;
    private Texture settingsOnTexture;
    private Texture settingsOffTexture;

    // Persists across screen visits so the toggle state is remembered
    static boolean soundOn = true;

    // "Toggle Sound" text hit zone (world coords, Y from bottom)
    private static final float TOGGLE_X1 = 500f, TOGGLE_X2 = 750f;
    private static final float TOGGLE_Y1 = 345f, TOGGLE_Y2 = 445f;

    private Sound toggleSound;
    private Sound escSound;

    public SettingsScreen(Main game) {
        this.game = game;
        batch    = new SpriteBatch();
        viewport = new ExtendViewport(1280f, 720f);
    }

    @Override
    public void show() {
        settingsOnTexture  = new Texture(Gdx.files.internal("StartScreen/settings_on.png"));
        settingsOffTexture = new Texture(Gdx.files.internal("StartScreen/settings_off.png"));
        toggleSound = Gdx.audio.newSound(Gdx.files.internal("audio/toggleSound.mp3"));        
        escSound = Gdx.audio.newSound(Gdx.files.internal("audio/escSound.mp3"));
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
        Texture current = soundOn ? settingsOnTexture : settingsOffTexture;
        batch.draw(current, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (SettingsScreen.soundOn) {
                escSound.play(0.8f);
            }
            goBack();
        } else if (Gdx.input.justTouched()) {
            Vector2 touch = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            if (touch.x >= TOGGLE_X1 && touch.x <= TOGGLE_X2 &&
                    touch.y >= TOGGLE_Y1 && touch.y <= TOGGLE_Y2) {
                soundOn = !soundOn;
                if(soundOn){
                    toggleSound.play(0.8f);
                }
            }
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
        settingsOnTexture.dispose();
        settingsOffTexture.dispose();
        toggleSound.dispose();
        escSound.dispose();
        batch.dispose();
    }
}
