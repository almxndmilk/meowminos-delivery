package ca.sfu.spring2026team15;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class GameScreen implements Screen {
    // World constants
    static final float MAP_WIDTH   = 5446f;
    static final float MAP_HEIGHT  = 902f;
    static final float VIEW_WIDTH  = 1280f;
    static final float VIEW_HEIGHT = 720f;

    private static final int TARGET_FISH_COUNT = 20;
    // Barrier image x-offset: map is drawn at worldX = -75, so pixelX = worldX + 75
    private static final int BARRIER_X_OFFSET = 75;

    private Texture mapTexture;
    private SpriteBatch batch;
    private ExtendViewport viewport;
    private GameCamera gameCamera;
    private Player player;
    private List<PoliceEnemy> police;
    private List<PuddleEnemy> puddles;
    private List<Fish> fishList;

    // HUD
    private SpriteBatch hudBatch;
    private OrthographicCamera hudCamera;
    private BitmapFont hudFont;
    private Texture fishHudTexture;
    private int fishCollected = 0;

    private final Main game;
    private float elapsedTime = 0f;

    // barriers
    private ArrayList<Rectangle> barriers;

    public GameScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        mapTexture = new Texture(Gdx.files.internal("map/map part1.png"));
        batch      = new SpriteBatch();
        gameCamera = new GameCamera(VIEW_WIDTH, VIEW_HEIGHT, MAP_WIDTH, MAP_HEIGHT);
        viewport   = new ExtendViewport(VIEW_WIDTH, VIEW_HEIGHT, gameCamera.getCamera());
        player     = new Player(300f, 400f);

        police = new ArrayList<>();
        police.add(new PoliceEnemy(800f, 400));
        puddles = new ArrayList<>();
        puddles.add(new PuddleEnemy(1500f, 100f));

        fishHudTexture = new Texture(Gdx.files.internal("small assets/fish.png"));

        hudBatch  = new SpriteBatch();
        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, VIEW_WIDTH, VIEW_HEIGHT);
        hudFont   = new BitmapFont();
        hudFont.getData().setScale(2f);

        spawnFish();
    }

    /** Samples the barriers Pixmap to place fish only on drivable road pixels. */
    private void spawnFish() {
        fishList = new ArrayList<>();
        Pixmap barrierPixmap = new Pixmap(Gdx.files.internal("map/map part1 barriers.png"));
        int imgH = barrierPixmap.getHeight();

        int attempts = 0;
        while (fishList.size() < TARGET_FISH_COUNT && attempts < 10000) {
            attempts++;
            float worldX = 50f + (float) Math.random() * (5300f - 50f);
            float worldY = 50f + (float) Math.random() * (850f - 50f);

            int pixelX = (int) (worldX + BARRIER_X_OFFSET);
            int pixelY = imgH - 1 - (int) worldY;

            // clamp to image bounds
            if (pixelX < 0 || pixelX >= barrierPixmap.getWidth() || pixelY < 0 || pixelY >= imgH) {
                continue;
            }

            int pixel = barrierPixmap.getPixel(pixelX, pixelY);
            int a = pixel & 0xFF;
            // Transparent pixels (a=0) = road; opaque green pixels = off-road
            boolean isRoad = a < 128;
            if (isRoad) {
                fishList.add(new Fish(worldX, worldY));
            }
        }
        barrierPixmap.dispose();

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
        elapsedTime += delta;
        player.update(delta, barriers);
        gameCamera.update(player.getCenterX(), player.getCenterY());

        if (checkFishCollection()) {
            return; // Exit render if switching to EndScreen
        }

        ScreenUtils.clear(Color.BLACK);
        batch.setProjectionMatrix(gameCamera.getCamera().combined);
        batch.begin();
        batch.draw(mapTexture, -75, 0, MAP_WIDTH, MAP_HEIGHT);
        player.resetSpeed(); // reset before checking
        for (PuddleEnemy puddle : puddles) {
            if (puddle.onPuddle(player.getCenterX(), player.getCenterY())) {
                player.setSpeed(70f);
            }
            puddle.render(batch);
        }
        for (Fish fish : fishList) {
            fish.render(batch);
        }
        player.render(batch);
        for (PoliceEnemy enemy : police) {
            enemy.update(delta, player.getCenterX(), player.getCenterY(), barriers);
            enemy.render(batch);
        }
        batch.end();

        renderHud();
    }

    private boolean checkFishCollection() {
        float collectRadius = 60f;
        float rSquared = collectRadius * collectRadius;
        for (Fish fish : fishList) {
            if (fish.isCollected()) continue;
            float dx = fish.getCenterX() - player.getCenterX();
            float dy = fish.getCenterY() - player.getCenterY();
            if (dx * dx + dy * dy < rSquared) {
                fish.collect();
                fishCollected++;
            }
        }

        for (PoliceEnemy enemy : police) {
            if (enemy.isCatching(player.getCenterX(), player.getCenterY())) {
                game.setScreen(new EndScreen(game, (int) elapsedTime));
                return true; // Signal to stop rendering
            }
        }
        return false;
    }

    private void renderHud() {
        hudBatch.setProjectionMatrix(hudCamera.combined);
        hudBatch.begin();

        float iconSize = 48f;
        float iconX = VIEW_WIDTH - 120f;
        float iconY = VIEW_HEIGHT - 70f;

        // Fish icon rotated 45° (pointing top-right)
        hudBatch.draw(
            fishHudTexture,
            iconX, iconY,
            iconSize / 2f, iconSize / 2f,  // rotation origin at center
            iconSize, iconSize,
            1f, 1f,
            45f,
            0, 0,
            fishHudTexture.getWidth(), fishHudTexture.getHeight(),
            false, false
        );

        hudFont.draw(hudBatch, "x " + fishCollected, iconX + iconSize + 4f, iconY + iconSize - 4f);

        hudBatch.end();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        mapTexture.dispose();
        batch.dispose();
        player.dispose();
        hudBatch.dispose();
        hudFont.dispose();
        fishHudTexture.dispose();
        for (PoliceEnemy enemy : police) {
            enemy.dispose();
        }
        for (PuddleEnemy puddle : puddles) {
            puddle.dispose();
        }
        for (Fish fish : fishList) {
            fish.dispose();
        }
    }
}
