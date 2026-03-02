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
import com.badlogic.gdx.math.Rectangle;

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
    private List<PuddleEnemy> puddles;

    private final Main game;

    // barriers
    private ArrayList<Rectangle> barriers;

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
        puddles = new ArrayList<>();
        puddles.add(new PuddleEnemy(1500f, 100f));

        // barriers
        barriers = new ArrayList<>();
        barriers.add(new Rectangle(0, 0, MAP_WIDTH, 10));                    // bottom
        barriers.add(new Rectangle(0, MAP_HEIGHT - 450, MAP_WIDTH / 1.6f, 450));      // top left
        barriers.add(new Rectangle(4100, MAP_HEIGHT - 450, MAP_WIDTH / 1.6f, 450));      // top right
        barriers.add(new Rectangle(0, 0, 10, MAP_HEIGHT));                   // left
        barriers.add(new Rectangle(MAP_WIDTH - 50, 0, 10, MAP_HEIGHT));      // right
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void render(float delta) {
        player.update(delta, barriers);
        gameCamera.update(player.getCenterX(), player.getCenterY());

        ScreenUtils.clear(Color.BLACK);
        batch.setProjectionMatrix(gameCamera.getCamera().combined);
        batch.begin();
        batch.draw(mapTexture, -75, 0, MAP_WIDTH, MAP_HEIGHT);
        for (PuddleEnemy puddle : puddles) {
            puddle.render(batch);
        }
        player.render(batch);
        for (PoliceEnemy enemy : police) {
            enemy.update(delta, player.getCenterX(), player.getCenterY());
            enemy.render(batch);
        }
        batch.end();
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
        for (PuddleEnemy puddle : puddles) {
            puddle.dispose();
        }
    }
}
