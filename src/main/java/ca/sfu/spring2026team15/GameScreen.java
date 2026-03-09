package ca.sfu.spring2026team15;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import java.util.ArrayList;
import java.util.List;

public class GameScreen implements Screen {
    // World constants
    static final float MAP_WIDTH           = 5446f;
    static final float MAP_HEIGHT_PER_PART = 902f;
    static final float MAP_HEIGHT          = MAP_HEIGHT_PER_PART * 3; // 2706
    static final float VIEW_WIDTH          = 1280f;
    static final float VIEW_HEIGHT         = 720f;

    private static final int TARGET_FISH_COUNT = 20;
    // Barrier image x-offset: map drawn at worldX = -75, so pixelX = worldX + 75
    private static final int BARRIER_X_OFFSET = 75;

    // Gate 2 (map2 → map3): left-side corridor
    private static final float GATE2_X_MIN = 0f;
    private static final float GATE2_X_MAX = 700f;

    // Part 1 → Part 2 iris transition
    private enum TransitionState { NONE, FADE_OUT, FADE_IN }
    private TransitionState transitionState = TransitionState.NONE;
    private float transitionTimer = 0f;
    private static final float TRANSITION_DURATION = 1.2f;

    // Protrude corridor trigger (world coords) — horizontal band across the corridor
    private static final float PROTRUDE_X_MIN     = 3500f;
    private static final float PROTRUDE_X_MAX     = 4200f;
    private static final float PROTRUDE_TRIGGER_Y =  660f;

    // Fish quota required to advance from map 1 → map 2
    private static final int FISH_QUOTA = 50;

    // Spawn points for each map transition (world coords)
    private static final float PART2_SPAWN_X = 5600f;
    private static final float PART2_SPAWN_Y = MAP_HEIGHT_PER_PART + 100f; // = 1002
    private static final float PART3_SPAWN_X = 350f;
    private static final float PART3_SPAWN_Y = MAP_HEIGHT_PER_PART * 2 + 100f; // = 1904

    // Iris rendering resources
    private ShapeRenderer shapeRenderer;
    private Texture blackTexture;

    // Tracks the highest map part the player has unlocked (0 = only map 1 rendered)
    private int currentMapIndex = 0;
    // Which map we're transitioning into; set before starting FADE_OUT
    private int pendingMapIndex = 1;

    private final Texture[] mapTextures    = new Texture[3];
    private final float[]   mapWidths      = new float[3];
    private final float[]   mapHeights     = new float[3];
    private final float[]   mapYOffsets    = new float[3]; // world Y of each map's bottom edge
    private final Pixmap[]  barrierPixmaps = new Pixmap[3];
    private BarrierLookup barrierLookup;

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

    // Pause overlay
    private boolean isPaused = false;
    private Texture pauseTexture;
    private SpriteBatch pauseBatch;
    private ExtendViewport pauseViewport;
    private static final float RESUME_X1  = 460f, RESUME_X2  = 810f, RESUME_Y1  = 430f, RESUME_Y2  = 485f;
    private static final float RESTART_X1 = 460f, RESTART_X2 = 800f, RESTART_Y1 = 345f, RESTART_Y2 = 405f;
    private static final float QUIT_X1    = 500f, QUIT_X2    = 770f, QUIT_Y1    = 260f, QUIT_Y2    = 320f;

    // Orders — per-map house lists for gate checking, flat list for update/render
    private final List<List<House>> housesByMap = new ArrayList<>();
    private List<House> houses; // flat view used in update/render loops
    private Texture orderIndicatorTexture;
    private boolean showDeliverPrompt = false;

    // Gate message
    private String gateMessage = null;
    private float gateMessageTimer = 0f;

    public GameScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        // Load all three map textures and barrier pixmaps
        mapTextures[0]    = new Texture(Gdx.files.internal("map/map part1.png"));
        mapTextures[1]    = new Texture(Gdx.files.internal("map/map part2.png"));
        mapTextures[2]    = new Texture(Gdx.files.internal("map/map part3.png"));
        for (int i = 0; i < 3; i++) {
            mapWidths[i]  = mapTextures[i].getWidth();
            mapHeights[i] = mapTextures[i].getHeight();
        }
        mapYOffsets[0] = 0f;
        mapYOffsets[1] = mapHeights[0];
        mapYOffsets[2] = mapHeights[0] + mapHeights[1];
        barrierPixmaps[0] = new Pixmap(Gdx.files.internal("map/map part1 barriers.png"));
        barrierPixmaps[1] = new Pixmap(Gdx.files.internal("map/map part2 barriers.png"));
        barrierPixmaps[2] = new Pixmap(Gdx.files.internal("map/map part3 barriers.png"));

        // Pick the right barrier pixmap by world Y using actual per-map Y offsets
        barrierLookup = (worldX, worldY) -> {
            if (worldY < 0) return true;
            int idx = 0;
            for (int i = 2; i >= 0; i--) {
                if (worldY >= mapYOffsets[i]) { idx = i; break; }
            }
            Pixmap pm = barrierPixmaps[idx];
            float localY = worldY - mapYOffsets[idx];
            int px = (int)(worldX + BARRIER_X_OFFSET);
            int py = pm.getHeight() - 1 - (int)localY;
            if (px < 0 || px >= pm.getWidth() || py < 0 || py >= pm.getHeight()) return true;
            return (pm.getPixel(px, py) & 0xFF) >= 128;
        };

        batch      = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        Pixmap blackPm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        blackPm.setColor(Color.BLACK);
        blackPm.fill();
        blackTexture = new Texture(blackPm);
        blackPm.dispose();

        // Camera bounds scoped to map 1; updated on each map transition (raw map edges, halfViewH applied internally).
        // X uses mapWidth - BARRIER_X_OFFSET because the map is drawn at worldX = -75, so the right world-edge is mapWidth - 75.
        gameCamera = new GameCamera(VIEW_WIDTH, VIEW_HEIGHT,
            mapWidths[0] - BARRIER_X_OFFSET,
            0f,
            mapHeights[0]);
        viewport   = new ExtendViewport(VIEW_WIDTH, VIEW_HEIGHT, gameCamera.getCamera());
        player     = new Player(300f, 300f);

        police = new ArrayList<>();
        police.add(new PoliceEnemy(800f, 300f));
        puddles = new ArrayList<>();
        puddles.add(new PuddleEnemy(1500f, 100f));

        fishHudTexture = new Texture(Gdx.files.internal("small assets/fish.png"));
        hudBatch  = new SpriteBatch();
        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, VIEW_WIDTH, VIEW_HEIGHT);
        hudFont   = new BitmapFont();
        hudFont.getData().setScale(2f);

        pauseTexture  = new Texture(Gdx.files.internal("Pause/Pause_screen.png"));
        pauseBatch    = new SpriteBatch();
        pauseViewport = new ExtendViewport(VIEW_WIDTH, VIEW_HEIGHT);

        timer = new Timer();
        timer.start();

        orderIndicatorTexture = new Texture(Gdx.files.internal("orderTickets/ticket bigHouse.png"));

        // Map 1 houses (world Y 0–902)
        List<House> map1Houses = new ArrayList<>();
        map1Houses.add(new House(1700f, 275f, orderIndicatorTexture));
        map1Houses.add(new House(2630f, 275f, orderIndicatorTexture));
        map1Houses.add(new House(4755f, 275f, orderIndicatorTexture));
        housesByMap.add(map1Houses);

        // Map 2 houses (world Y 902–1804) — positions are placeholders, tune against map art
        List<House> map2Houses = new ArrayList<>();
        map2Houses.add(new House(1500f, MAP_HEIGHT_PER_PART + 300f, orderIndicatorTexture));
        map2Houses.add(new House(3000f, MAP_HEIGHT_PER_PART + 250f, orderIndicatorTexture));
        map2Houses.add(new House(4500f, MAP_HEIGHT_PER_PART + 350f, orderIndicatorTexture));
        housesByMap.add(map2Houses);

        // Map 3 houses (world Y 1804–2706) — positions are placeholders, tune against map art
        List<House> map3Houses = new ArrayList<>();
        map3Houses.add(new House(1000f, MAP_HEIGHT_PER_PART * 2 + 300f, orderIndicatorTexture));
        map3Houses.add(new House(2800f, MAP_HEIGHT_PER_PART * 2 + 300f, orderIndicatorTexture));
        housesByMap.add(map3Houses);

        // Flat list for update/render loops
        houses = new ArrayList<>();
        for (List<House> perMap : housesByMap) houses.addAll(perMap);

        spawnFish();
    }

    /** Spawns fish on drivable road tiles across all three maps. */
    private void spawnFish() {
        fishList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            spawnFishForMap(i);
        }
    }

    private void spawnFishForMap(int mapIndex) {
        float yOffset = mapYOffsets[mapIndex]; // use actual stacked Y, not assumed-uniform height
        Pixmap pm = barrierPixmaps[mapIndex];
        int imgH = pm.getHeight();
        float mapW = mapWidths[mapIndex]; // use actual map width, not hardcoded 5300

        int attempts = 0;
        int spawned  = 0;
        while (spawned < TARGET_FISH_COUNT && attempts < 10000) {
            attempts++;
            float worldX = 50f + (float) Math.random() * (mapW - 100f);
            float localY = 50f + (float) Math.random() * (imgH - 100f);

            int pixelX = (int)(worldX + BARRIER_X_OFFSET);
            int pixelY = imgH - 1 - (int)localY;

            if (pixelX < 0 || pixelX >= pm.getWidth() || pixelY < 0 || pixelY >= imgH) continue;

            int pixel = pm.getPixel(pixelX, pixelY);
            boolean isRoad = (pixel & 0xFF) < 128;
            if (isRoad) {
                fishList.add(new Fish(worldX, localY + yOffset));
                spawned++;
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
            if (isPaused) timer.pause();
            else timer.resume();
        }

        if (!isPaused) {
            timer.update(delta);
            if (timer.isFinished()) {
                game.setScreen(new EndScreen(game, timer.getElapsedSeconds()));
                return;
            }
            if (transitionState == TransitionState.NONE) {
                player.update(delta, barrierLookup);
                checkGates();
            }
            updateTransition(delta);
            gameCamera.update(player.getCenterX(), player.getCenterY());
            if (checkFishCollection()) return;
            gateMessageTimer -= delta;
        }

        // Delivery prompt + interaction
        showDeliverPrompt = false;
        for (House house : houses) {
            house.update(delta);
            if (house.hasOrder() && house.isPlayerInRange(player.getCenterX(), player.getCenterY())) {
                showDeliverPrompt = true;
                if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                    if (house.tryDeliver(player.getCenterX(), player.getCenterY())) {
                        fishCollected += 10;
                    }
                }
            }
        }

        ScreenUtils.clear(Color.BLACK);
        batch.setProjectionMatrix(gameCamera.getCamera().combined);
        batch.begin();

        // Only draw the current map at its actual pixel dimensions
        batch.draw(mapTextures[currentMapIndex], -75, mapYOffsets[currentMapIndex],
            mapWidths[currentMapIndex], mapHeights[currentMapIndex]);

        for (House house : houses) {
            house.render(batch);
        }

        player.resetSpeed();
        for (PuddleEnemy puddle : puddles) {
            if (puddle.onPuddle(player.getCenterX(), player.getCenterY())) {
                player.setSpeed(70f);
                if (fishCollected > 0) fishCollected -= 1;
            }
            puddle.render(batch);
        }
        for (Fish fish : fishList) {
            fish.render(batch);
        }
        player.render(batch);
        for (PoliceEnemy enemy : police) {
            if (!isPaused) enemy.update(delta, player.getCenterX(), player.getCenterY(), barrierLookup);
            enemy.render(batch);
        }
        batch.end();

        renderHud();

        if (transitionState != TransitionState.NONE) {
            float halfDiag = (float) Math.sqrt(
                (VIEW_WIDTH / 2f) * (VIEW_WIDTH / 2f) + (VIEW_HEIGHT / 2f) * (VIEW_HEIGHT / 2f)
            ) + 50f;
            float t = transitionTimer / TRANSITION_DURATION;
            float irisRadius = (transitionState == TransitionState.FADE_OUT)
                ? halfDiag * (1f - t)
                : halfDiag * t;
            renderIris(Math.max(0f, irisRadius));
        }

        if (isPaused) {
            renderPause();
        }
    }

    /** Check gate triggers — no delta needed; called only when transition is inactive. */
    private void checkGates() {
        float px = player.getCenterX();
        float py = player.getCenterY();

        // Gate 1: protrude corridor trigger band (X 3500–4200, Y ≥ 660)
        if (currentMapIndex == 0 && px >= PROTRUDE_X_MIN && px <= PROTRUDE_X_MAX && py >= PROTRUDE_TRIGGER_Y) {
            int fishNeeded = FISH_QUOTA - fishCollected;
            if (fishNeeded > 0) {
                player.setPosition(px, PROTRUDE_TRIGGER_Y - 20f);
                setGateMessage("Collect " + fishNeeded + " more fish to advance!");
            } else {
                pendingMapIndex  = 1;
                transitionState  = TransitionState.FADE_OUT;
                transitionTimer  = 0f;
            }
        }

        // Hard boundary: prevent entering map 2 at any X without the iris transition
        if (py > MAP_HEIGHT_PER_PART && currentMapIndex < 1) {
            player.setPosition(px, MAP_HEIGHT_PER_PART - 80f);
        }

        // Gate 2: map 2 → map 3 via left-side corridor
        if (py > mapYOffsets[2] && px > GATE2_X_MIN && px < GATE2_X_MAX) {
            int needed = deliveriesNeeded(1);
            if (needed > 0) {
                player.setPosition(px, mapYOffsets[2] - 80f);
                setGateMessage("Deliver to " + needed + " more house(s) to advance!");
            } else {
                pendingMapIndex  = 2;
                transitionState  = TransitionState.FADE_OUT;
                transitionTimer  = 0f;
            }
        }
    }

    private void updateTransition(float delta) {
        if (transitionState == TransitionState.NONE) return;
        transitionTimer += delta;
        if (transitionState == TransitionState.FADE_OUT && transitionTimer >= TRANSITION_DURATION) {
            activateMap(pendingMapIndex);
            transitionTimer = 0f;
            transitionState = TransitionState.FADE_IN;
        } else if (transitionState == TransitionState.FADE_IN && transitionTimer >= TRANSITION_DURATION) {
            transitionTimer = 0f;
            transitionState = TransitionState.NONE;
        }
    }

    private void activateMap(int index) {
        float[] spawnX = { 0f, PART2_SPAWN_X, PART3_SPAWN_X };
        float[] spawnY = { 0f, PART2_SPAWN_Y, PART3_SPAWN_Y };
        player.setPosition(spawnX[index], spawnY[index]);
        currentMapIndex = Math.max(currentMapIndex, index);
        gameCamera.setMaxX(mapWidths[index] - BARRIER_X_OFFSET);
        gameCamera.setYBounds(mapYOffsets[index], mapYOffsets[index] + mapHeights[index]);
    }

    private void renderIris(float irisRadius) {
        // Step 1: write iris circle into stencil only (no color output)
        Gdx.gl.glClear(GL20.GL_STENCIL_BUFFER_BIT);
        Gdx.gl.glEnable(GL20.GL_STENCIL_TEST);
        Gdx.gl.glStencilFunc(GL20.GL_ALWAYS, 1, 0xFF);
        Gdx.gl.glStencilOp(GL20.GL_KEEP, GL20.GL_KEEP, GL20.GL_REPLACE);
        Gdx.gl.glColorMask(false, false, false, false);

        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeType.Filled);
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.circle(VIEW_WIDTH / 2f, VIEW_HEIGHT / 2f, irisRadius, 64);
        shapeRenderer.end();

        Gdx.gl.glColorMask(true, true, true, true);

        // Step 2: draw black everywhere stencil == 0 (outside the circle)
        Gdx.gl.glStencilFunc(GL20.GL_EQUAL, 0, 0xFF);
        Gdx.gl.glStencilOp(GL20.GL_KEEP, GL20.GL_KEEP, GL20.GL_KEEP);

        hudBatch.setProjectionMatrix(hudCamera.combined);
        hudBatch.begin();
        hudBatch.setColor(Color.BLACK);
        hudBatch.draw(blackTexture, 0, 0, VIEW_WIDTH, VIEW_HEIGHT);
        hudBatch.setColor(Color.WHITE);
        hudBatch.end();

        Gdx.gl.glDisable(GL20.GL_STENCIL_TEST);
    }

    private int deliveriesNeeded(int mapIndex) {
        int count = 0;
        for (House h : housesByMap.get(mapIndex)) {
            if (!h.wasDelivered()) count++;
        }
        return count;
    }

    private void setGateMessage(String msg) {
        gateMessage = msg;
        gateMessageTimer = 3f;
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

        hudBatch.draw(
            fishHudTexture,
            iconX, iconY,
            iconSize / 2f, iconSize / 2f,
            iconSize, iconSize,
            1f, 1f,
            45f,
            0, 0,
            fishHudTexture.getWidth(), fishHudTexture.getHeight(),
            false, false
        );

        hudFont.draw(hudBatch, "x " + fishCollected, iconX + iconSize + 4f, iconY + iconSize - 4f);

        timer.render(hudBatch, hudFont);

        if (showDeliverPrompt) {
            hudFont.draw(hudBatch, "Press E to deliver!", VIEW_WIDTH / 2 - 100f, 80f);
        }

        if (gateMessageTimer > 0) {
            hudFont.draw(hudBatch, gateMessage, VIEW_WIDTH / 2 - 200f, VIEW_HEIGHT / 2f);
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
        for (Texture t : mapTextures) if (t != null) t.dispose();
        for (Pixmap p : barrierPixmaps) if (p != null) p.dispose();
        batch.dispose();
        pauseTexture.dispose();
        pauseBatch.dispose();
        player.dispose();
        hudBatch.dispose();
        hudFont.dispose();
        fishHudTexture.dispose();
        for (PoliceEnemy enemy : police) enemy.dispose();
        for (PuddleEnemy puddle : puddles) puddle.dispose();
        for (Fish fish : fishList) fish.dispose();
        orderIndicatorTexture.dispose();
        shapeRenderer.dispose();
        blackTexture.dispose();
    }
}
