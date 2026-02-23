package ca.sfu.spring2026team15;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class StartScreen implements Screen {
    private final Main game;
    private SpriteBatch batch;
    private BitmapFont font;
    private FitViewport viewport;

    public StartScreen(Main game) {
        this.game = game;
        batch = new SpriteBatch();
        font = new BitmapFont();
        viewport = new FitViewport(800, 500);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        font.draw(batch, "Meowminos Pizza", 350, 300);
        font.draw(batch, "Press ENTER to start", 320, 250);
        batch.end();

        if (Gdx.input.isKeyJustPressed(com.badlogic.gdx.Input.Keys.ENTER)) {
            game.setScreen(new GameScreen(game));
            dispose();
        }
    }

    // Screen interface required implementations
    @Override public void resize(int width, int height) {
        viewport.update(width, height, true);
    }
    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
