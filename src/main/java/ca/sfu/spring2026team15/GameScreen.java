package ca.sfu.spring2026team15;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.audio.Sound;

public class GameScreen implements Screen {
    // World constants
    static final float MAP_WIDTH   = 5446f;
    static final float MAP_HEIGHT  = 902f;
    static final float VIEW_WIDTH  = 1280f;
    static final float VIEW_HEIGHT = 720f;

    private static final int TARGET_FISH_COUNT = 20;
    // Barrier image x-offset: map is drawn at worldX = -75, so pixelX = worldX + 75
    private static final int BARRIER_X_OFFSET = 75;

    private boolean wasOnPuddle = false;

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
    private Timer timer;

    private Pixmap barrierPixmap;

    // Pause overlay
    private boolean isPaused = false;
    private Texture pauseTexture;
    private SpriteBatch pauseBatch;
    private ExtendViewport pauseViewport;
    private static final float RESUME_X1  = 460f, RESUME_X2  = 810f, RESUME_Y1  = 430f, RESUME_Y2  = 485f;
    private static final float RESTART_X1 = 460f, RESTART_X2 = 800f, RESTART_Y1 = 345f, RESTART_Y2 = 405f;
    private static final float QUIT_X1    = 500f, QUIT_X2    = 770f, QUIT_Y1    = 260f, QUIT_Y2    = 320f;

    // orders
    private List<House> houses;
    private Texture orderIndicatorTexture;
    private boolean showDeliverPrompt = false;

    // Background music
    private Music backgroundMusic;
    private Sound deliveredSound;

    public GameScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        mapTexture = new Texture(Gdx.files.internal("map/map part1.png"));
        batch      = new SpriteBatch();
        gameCamera = new GameCamera(VIEW_WIDTH, VIEW_HEIGHT, MAP_WIDTH, MAP_HEIGHT);
        viewport   = new ExtendViewport(VIEW_WIDTH, VIEW_HEIGHT, gameCamera.getCamera());
        player     = new Player(300f, 300f);

        police = new ArrayList<>();
        police.add(new PoliceEnemy(800f, 300));
        puddles = new ArrayList<>();
        puddles.add(new PuddleEnemy(1500f, 100f));

        fishHudTexture = new Texture(Gdx.files.internal("small assets/fish.png"));

        hudBatch  = new SpriteBatch();
        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, VIEW_WIDTH, VIEW_HEIGHT);
        hudFont   = new BitmapFont();
        hudFont.getData().setScale(2f);

        barrierPixmap = new Pixmap(Gdx.files.internal("map/map part1 barriers.png"));
        spawnFish();

        pauseTexture  = new Texture(Gdx.files.internal("Pause/Pause_screen.png"));
        pauseBatch    = new SpriteBatch();
        pauseViewport = new ExtendViewport(VIEW_WIDTH, VIEW_HEIGHT);

        timer = new Timer();
        timer.start();

        // orders:
        orderIndicatorTexture = new Texture(Gdx.files.internal("orderTickets/ticket bigHouse.png"));
        houses = new ArrayList<>();
// Add house positions matching your map image
        houses.add(new House(1700f,  275f, orderIndicatorTexture));  // yellow house
        houses.add(new House(2630f,  275f, orderIndicatorTexture));  // blue house
        houses.add(new House(4755f, 275f, orderIndicatorTexture));  // pink house

        // Load and play background music
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/backgroundMusic.mp3"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.5f);
        backgroundMusic.play();

        // audio
        deliveredSound = Gdx.audio.newSound(Gdx.files.internal("audio/deliveredOrder.mp3"));
    }

    /** Samples the barriers Pixmap to place fish only on drivable road pixels. */
    private void spawnFish() {
        fishList = new ArrayList<>();
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
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        pauseViewport.update(width, height, true);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            isPaused = !isPaused;
            if (isPaused) {
                timer.pause();
                backgroundMusic.pause();
            } else {
                timer.resume();
                backgroundMusic.play();
            }
        }

        if (!isPaused) {
            timer.update(delta);
            if (timer.isFinished()) {
                game.setScreen(new EndScreen(game, timer.getElapsedSeconds()));
                return;
            }
            player.update(delta, barrierPixmap);
            gameCamera.update(player.getCenterX(), player.getCenterY());
            if (checkFishCollection()) return;
        }

        checkFishCollection();

        // orders
        showDeliverPrompt = false;
        for (House house : houses) {
            house.update(delta);
            if (house.hasOrder() && house.isPlayerInRange(player.getCenterX(), player.getCenterY())) {
                showDeliverPrompt = true;
                if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                    deliveredSound.play(1.0f);
                    if (house.tryDeliver(player.getCenterX(), player.getCenterY())) {
                        fishCollected += 10; // reward fish points
                    }
                }
            }
        }

        ScreenUtils.clear(Color.BLACK);
        batch.setProjectionMatrix(gameCamera.getCamera().combined);
        batch.begin();
        batch.draw(mapTexture, -75, 0, MAP_WIDTH, MAP_HEIGHT);

        for (House house : houses) {
            house.render(batch);
        }

        player.resetSpeed();
        boolean isOnPuddle = false;
        for (PuddleEnemy puddle : puddles) {
            if (puddle.onPuddle(player.getCenterX(), player.getCenterY())) {
                isOnPuddle = true;
                player.setSpeed(70f);
                if (!wasOnPuddle && fishCollected > 0) {
                    fishCollected -= 1;
                }
            }
            puddle.render(batch);
        }
        wasOnPuddle = isOnPuddle;

        for (Fish fish : fishList) {
            fish.render(batch);
        }
        player.render(batch);
        for (PoliceEnemy enemy : police) {
            if (!isPaused) enemy.update(delta, player.getCenterX(), player.getCenterY(), barrierPixmap);
            enemy.render(batch);
        }
        batch.end();

        renderHud();

        if (isPaused) {
            renderPause();
        }
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
                timer.stop();
                game.setScreen(new EndScreen(game, timer.getElapsedSeconds() + 1));
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

        timer.render(hudBatch, hudFont);

        //orders
        if (showDeliverPrompt) {
            hudFont.draw(hudBatch, "Press E to deliver!", VIEW_WIDTH / 2 - 100f, 80f);
        }

        hudBatch.end();
    }

    private void renderPause() {
        pauseViewport.apply();
        pauseBatch.setProjectionMatrix(pauseViewport.getCamera().combined);
        pauseBatch.begin();
        pauseBatch.draw(pauseTexture, 0, 0, pauseViewport.getWorldWidth(), pauseViewport.getWorldHeight());
        pauseBatch.end();

        if (Gdx.input.justTouched()) {
            Vector2 touch = pauseViewport.unproject(new Vector2(Gdx.input.getX(), Gdx.input.getY()));
            if (touch.x >= RESUME_X1 && touch.x <= RESUME_X2 && touch.y >= RESUME_Y1 && touch.y <= RESUME_Y2) {
                isPaused = false;
                timer.resume();
            } else if (touch.x >= RESTART_X1 && touch.x <= RESTART_X2 && touch.y >= RESTART_Y1 && touch.y <= RESTART_Y2) {
                dispose();
                game.setScreen(new GameScreen(game));
            } else if (touch.x >= QUIT_X1 && touch.x <= QUIT_X2 && touch.y >= QUIT_Y1 && touch.y <= QUIT_Y2) {
                dispose();
                Gdx.app.exit();
            }
        }
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        mapTexture.dispose();
        barrierPixmap.dispose();
        batch.dispose();
        pauseTexture.dispose();
        pauseBatch.dispose();
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
        orderIndicatorTexture.dispose();
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.dispose();
            deliveredSound.dispose();
        }
    }
}
