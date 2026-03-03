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

public class SettingsScreen implements Screen {

    private final Main game;
    private SpriteBatch batch;
    private ExtendViewport viewport;
    private Texture settingsOnTexture;
    private Texture settingsOffTexture;

    // Persists across screen visits so the toggle state is remembered
    static boolean soundOn = true;

    // "Toggle Sound" text hit zone (world coords, Y from bottom)
    private static final float TOGGLE_X1 = 400f, TOGGLE_X2 = 900f;
    private static final float TOGGLE_Y1 = 470f, TOGGLE_Y2 = 530f;

    public SettingsScreen(Main game) {
        this.game = game;
        batch    = new SpriteBatch();
        viewport = new ExtendViewport(1280f, 720f);
    }

    @Override
    public void show() {
        settingsOnTexture  = new Texture(Gdx.files.internal("StartScreen/settings_on.png"));
        settingsOffTexture = new Texture(Gdx.files.internal("StartScreen/settings_off.png"));
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
            goBack();
        } else if (Gdx.input.justTouched()) {
            Vector2 touch = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            if (touch.x >= TOGGLE_X1 && touch.x <= TOGGLE_X2 &&
                    touch.y >= TOGGLE_Y1 && touch.y <= TOGGLE_Y2) {
                soundOn = !soundOn;
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
        batch.dispose();
    }
}
