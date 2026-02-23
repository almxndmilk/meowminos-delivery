package ca.sfu.spring2026team15;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
//import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.math.Vector2;

public class StartScreen implements Screen {

    private final Main game;
    private SpriteBatch batch;
//    private FitViewport viewport;
    private ExtendViewport viewport;

    private Texture startTexture;
    private Texture buttonTexture;
    private Texture titleTexture;

    // Start Button
    float btnWidth  = 200f;
    float btnHeight = 125f;

    // Title
    float titleWidth  = 700f;
    float titleHeight = 350f;

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

        batch.begin();
        batch.draw(startTexture, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        batch.draw(buttonTexture, btnX, btnY, btnWidth, btnHeight);
        batch.draw(titleTexture, titleX, titleY, titleWidth, titleHeight);
        batch.end();

        if (Gdx.input.justTouched()) {
            // convert screen coordinates to world coordinates
            Vector2 touch = viewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));

            if (touch.x >= btnX && touch.x <= btnX + btnWidth &&
                    touch.y >= btnY && touch.y <= btnY + btnHeight) {
                game.setScreen(new GameScreen(game));
                dispose();
            }
        }
    }

    // Screen interface required implementations
    @Override public void resize(int width, int height) {
        viewport.update(width, height, true);
    }
    @Override public void show() {
        startTexture = new Texture(Gdx.files.internal("StartScreen/startScreenBackground.png"));
        buttonTexture = new Texture(Gdx.files.internal("StartScreen/playButton.PNG"));
        titleTexture = new Texture(Gdx.files.internal("StartScreen/newTitle.png"));
    }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        startTexture.dispose();
        buttonTexture.dispose();
        titleTexture.dispose();
        batch.dispose();
    }
}
