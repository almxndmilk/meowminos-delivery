package ca.sfu.spring2026team15;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main extends ApplicationAdapter {
    // World constants
    static final float MAP_WIDTH  = 5446f;
    static final float MAP_HEIGHT = 902f;
    static final float VIEW_WIDTH  = 1280f;
    static final float VIEW_HEIGHT = 720f;

    private Texture mapTexture;
    private SpriteBatch batch;
    private FitViewport viewport;
    private GameCamera gameCamera;
    private Player player;

    @Override
    public void create() {
        mapTexture = new Texture(Gdx.files.internal("map/map part1.png"));
        batch      = new SpriteBatch();
        gameCamera = new GameCamera(VIEW_WIDTH, VIEW_HEIGHT, MAP_WIDTH, MAP_HEIGHT);
        viewport   = new FitViewport(VIEW_WIDTH, VIEW_HEIGHT, gameCamera.getCamera());
        player     = new Player(300f, 400f);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void render() {
        player.update(Gdx.graphics.getDeltaTime());
        gameCamera.update(player.getCenterX(), player.getCenterY());

        ScreenUtils.clear(Color.BLACK);
        batch.setProjectionMatrix(gameCamera.getCamera().combined);
        batch.begin();
        batch.draw(mapTexture, 0, 0, MAP_WIDTH, MAP_HEIGHT);
        player.render(batch);
        batch.end();
    }

    @Override
    public void dispose() {
        mapTexture.dispose();
        batch.dispose();
        player.dispose();
    }
}
