package ca.sfu.spring2026team15;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
//import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.Screen;
import java.util.ArrayList;
import java.util.List;

public class GameScreen implements Screen {
    // World constants
    static final float MAP_WIDTH   = 5446f;
    static final float MAP_HEIGHT  = 902f;
    static final float VIEW_WIDTH  = 1280f;
    static final float VIEW_HEIGHT = 720f;

    private Texture mapTexture;
    private SpriteBatch batch;
//    private FitViewport viewport;
    private ExtendViewport viewport;
    private GameCamera gameCamera;
    private Player player;
    private List<PoliceEnemy> police;

    private final Main game;
    private float elapsedTime = 0f;

    // constructor
    public GameScreen(Main game) {
        this.game = game;
    }

    // Equivalent to the create method, this is the "Screen" interface equivalent
    @Override
    public void show() {
        mapTexture = new Texture(Gdx.files.internal("map/map part1.png"));
        batch      = new SpriteBatch();
        gameCamera = new GameCamera(VIEW_WIDTH, VIEW_HEIGHT, MAP_WIDTH, MAP_HEIGHT);
//        viewport   = new FitViewport(VIEW_WIDTH, VIEW_HEIGHT, gameCamera.getCamera());
        viewport   = new ExtendViewport(VIEW_WIDTH, VIEW_HEIGHT, gameCamera.getCamera());
        player     = new Player(300f, 400f);

        police = new ArrayList<>();
        police.add(new PoliceEnemy(800f, 400));
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void render(float delta) {
        elapsedTime += delta;
        player.update(delta);
        gameCamera.update(player.getCenterX(), player.getCenterY());

        ScreenUtils.clear(Color.BLACK);
        batch.setProjectionMatrix(gameCamera.getCamera().combined);
        batch.begin();
        batch.draw(mapTexture, -75, 0, MAP_WIDTH, MAP_HEIGHT);
        player.render(batch);
        for (PoliceEnemy enemy : police) {
            enemy.update(delta, player.getCenterX(), player.getCenterY());
            enemy.render(batch);
        }
        batch.end();

        for (PoliceEnemy enemy : police) {
            if (enemy.isCatching(player.getCenterX(), player.getCenterY())) {
                dispose();
                game.setScreen(new EndScreen(game, (int) elapsedTime));
                return;
            }
        }
    }

    // required methods for Screen interface
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        mapTexture.dispose();
        batch.dispose();
        player.dispose();
        for (PoliceEnemy enemy : police) {
            enemy.dispose();
        }
    }
}
