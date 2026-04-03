package ca.sfu.spring2026team15;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

/**
 * Primary game screen that runs the main gameplay loop for "Meowmino's Delivery".
 *
 * <p>Manages three sequential map sections (map1 → map2 → map3), each gated behind
 * a fish-collection quota. Responsibilities include:
 * <ul>
 *   <li>Rendering tile maps, entities (player, police, fish, houses, puddles), and HUD</li>
 *   <li>Player input: WASD movement, bike mount/dismount (Q), delivery/interaction (E), pause (ESC)</li>
 *   <li>Gate progression: obstacle barriers cleared after collecting enough fish</li>
 *   <li>Iris-wipe transitions between map sections using OpenGL stencil buffer</li>
 *   <li>Intro cutscene (cat walks from door to bike) and map-3 cinematic pan</li>
 *   <li>Delivery order system with per-house timers and HUD notifications</li>
 *   <li>Obama Whitehouse trigger that ends the game with a cutscene</li>
 *   <li>Respawn mechanic: police catch deducts fish and adds time penalty</li>
 * </ul>
 */
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
    private float preRenderTransitionTimer = 0f;
    private TransitionState preRenderTransitionState = TransitionState.NONE;
    private boolean pendingFadeInSwitch = false; // delays FADE_OUT→FADE_IN by one frame to guarantee a full-black frame
    private static final float TRANSITION_DURATION = 1.2f;

    // Map 3 intro cinematic
    private enum CinematicState { NONE, PAN_TO_HOUSES, HOLD, PAN_BACK }
    private CinematicState cinematicState = CinematicState.NONE;
    private float cinematicTimer = 0f;
    private float panStartX, panStartY, panStartZoom;
    private static final float CINEMATIC_PAN_DURATION  = 2.5f;
    private static final float CINEMATIC_HOLD_DURATION = 3.5f;
    // Target zoom computed at runtime: (mapWidth - offset) / VIEW_WIDTH to show full map width

    // Intro cutscene — cat walks from door to bike at game start
    private enum IntroState { FADE_WAIT, DOOR_DELAY, WALK_TO_BIKE, WAIT_MOUNT, MOUNT, DONE }
    private IntroState introState = IntroState.FADE_WAIT;
    private float introTimer = 0f;
    private boolean showIntroMountPrompt = false;
    private static final float INTRO_DOOR_DELAY = 1.0f;
    private static final float INTRO_MOUNT_DURATION = 0.5f;
    private static final float INTRO_WALK_SPEED = 150f;
    private static final float INTRO_ZOOM = 0.5f;
    private static final float INTRO_ZOOM_OUT_SPEED = 1.5f;
    private static final float INTRO_DOOR_X = 361f, INTRO_DOOR_Y = 451f;
    private static final float INTRO_BIKE_X = 325f, INTRO_BIKE_Y = 242f;
    private static final float INTRO_STOP_X = 325f, INTRO_STOP_Y = 322f;

    // Respawn Penalties
    private static final int RESPAWN_FISH_PENALTY = 10;
    private static final float RESPAWN_TIME_PENALTY = 30f;

    private static final float[][] MAP2_POLICE_SPAWNS = {
        {2000f, 1000f},
        {3500f, 1000f}
    };
    private static final float[][] MAP2_PUDDLE_SPAWNS = {
        {2100f, 950f},
        {3000f, 780f}
    };
    private static final float[][] MAP3_POLICE_SPAWNS = {
        { 500f,  850f}, {1300f, 1200f}, {2500f, 1300f}, { 800f, 1450f},
        {1800f, 1600f}, {1380f,  800f}, {2700f, 1900f}, { 300f, 2300f},
        {1600f, 2300f}, {2300f, 2150f}, {2200f,  700f}, { 600f, 2480f},
        {1500f, 1300f}, { 400f, 1400f}, { 920f, 1650f}, {2000f, 1600f},
        {1700f,  900f}, { 300f, 2000f}, {1600f, 2100f}, {1100f, 2250f}
    };
    private static final float[][] MAP3_PUDDLE_SPAWNS = {
        { 550f,  800f}, {1500f, 1000f}, {2500f, 1200f}, { 900f, 1450f},
        {2000f, 1500f}, {1200f, 1750f}, {2100f, 1900f}, { 400f, 2000f},
        {1600f, 2200f}, {2300f, 2250f}, { 900f, 2200f}, { 600f, 2480f},
        {2300f, 2480f}
    };
    private int totalTimePenalties = 0;

    // Gate 1 obstacle — blocks top of map 1 until player has enough fish
    private Texture gate1ObstacleTexture;
    private float   gate1ObstacleX, gate1ObstacleY; // computed in show()
    private float   gate1DrawWidth, gate1DrawHeight; // rendered size (500px wide, proportional height)
    private boolean gate1ObstacleActive = true;     // false once the fade clears it
    private boolean gate1Cleared        = false;    // true after obstacle is fully gone; top edge becomes the trigger

    // Gate 2 obstacle — blocks top of map 2 until player has enough fish
    private Texture gate2ObstacleTexture;
    private float   gate2ObstacleX, gate2ObstacleY; // computed in show()
    private float   gate2DrawWidth, gate2DrawHeight; // rendered size (500px wide, proportional height)
    private boolean gate2ObstacleActive = true;     // false once the fade clears it
    private boolean gate2Cleared        = false;    // true after obstacle is fully gone; top edge becomes the trigger

    // Fish quotas
    private static final int FISH_QUOTA_GATE1 = 30; // map 1 → map 2
    private static final int FISH_QUOTA_GATE2 = 20; // map 2 → map 3

    // Gate 1 obstacle clear: fade screen to black, then back, removing the obstacle
    private enum Gate1FadeState { NONE, FADE_OUT, FADE_IN }
    private Gate1FadeState gate1FadeState = Gate1FadeState.NONE;
    private float gate1FadeTimer = 0f;
    private static final float GATE1_FADE_DURATION = 1.0f;

    // Gate 2 obstacle clear: fade screen to black, then back, removing the obstacle
    private enum Gate2FadeState { NONE, FADE_OUT, FADE_IN }
    private Gate2FadeState gate2FadeState = Gate2FadeState.NONE;
    private float gate2FadeTimer = 0f;
    private static final float GATE2_FADE_DURATION = 1.0f;

    // Iris rendering resources
    private ShapeRenderer shapeRenderer;
    private Texture blackTexture;
    private Texture whiteTexture;

    private CinematicBars cinematicBars;

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

    // Puddle entry tracking — lose 1 fish on entry (not per frame); can go negative → death
    private boolean wasOnPuddle = false;

    // Audio
    private Music backgroundMusic;
    private Sound deliveredSound;
    private Sound fishSound;

    private SpriteBatch batch;
    private ExtendViewport viewport;
    private GameCamera gameCamera;

    // the player
    private Player player;
    private CatOffBike catOffBike;
    private boolean isOnBike = true;
    private float parkedBikeX = -9999f, parkedBikeY = -9999f;
    private boolean bikeParked = false;
    private Texture parkedBikeTexture;


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
    private static final float RESUME_X1  = 520f, RESUME_X2  = 750f, RESUME_Y1  = 345f, RESUME_Y2  = 445f;
    private static final float RESTART_X1 = 520f, RESTART_X2 = 750f, RESTART_Y1 = 240f, RESTART_Y2 = 340f;
    private static final float QUIT_X1    = 540f, QUIT_X2    = 730f, QUIT_Y1    = 135f, QUIT_Y2    = 235f;

    // Orders — per-map house lists for gate checking, update/render, and delivery notifications
    private final List<List<House>> housesByMap = new ArrayList<>();
    private Texture ticketYellow, ticketBlue, ticketPink;                       // map 1 small houses
    private Texture ticketBigBlue, ticketBigBlack, ticketBigRed, ticketBigBrown; // map 2 big houses
    private boolean showDeliverPrompt = false;
    private List<DeliveryNotification> activeNotifications;
    private Sound deliveryAlertSound;
    private int activeDeliveryMap = 0;
    private boolean showObamaPrompt = false;

    // Gate message
    private String gateMessage = null;
    private float gateMessageTimer = 0f;

    // Obama cutscene trigger — fires once when player has 90 fish and reaches the whitehouse
    private boolean obamaTriggered = false;
    private House whitehouse = null; // Reference to the whitehouse on map 3

    /**
     * Creates a new GameScreen bound to the given application controller.
     * Heavy initialization (asset loading, entity creation) is deferred to {@link #show()}.
     *
     * @param game the LibGDX {@link Main} instance used to switch screens
     */
    public GameScreen(Main game) {
        this.game = game;
    }

    /**
     * Called by LibGDX when this screen becomes active. Loads all assets, constructs
     * entities, configures the camera, and starts the intro cutscene iris-wipe.
     *
     * <p>Specifically this method:
     * <ul>
     *   <li>Loads map textures and barrier pixmaps for all three map sections</li>
     *   <li>Builds a {@link BarrierLookup} from the barrier pixmaps for pixel-perfect collision</li>
     *   <li>Creates the player, off-bike cat, police enemies, puddles, gate obstacle textures,
     *       and house delivery targets for each map</li>
     *   <li>Calls {@link #spawnFish()} to randomly place fish on road tiles</li>
     *   <li>Starts background music (respecting the global sound toggle) and loads SFX</li>
     *   <li>Begins the opening iris-wipe (FADE_IN) and zooms the camera in for the intro cutscene</li>
     * </ul>
     */
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

        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE); // Pure white
        pixmap.fill();
        whiteTexture = new Texture(pixmap);
        pixmap.dispose();

        cinematicBars = new CinematicBars(VIEW_WIDTH, VIEW_HEIGHT);

        // Camera bounds scoped to map 1; updated on each map transition (raw map edges, halfViewH applied internally).
        // X uses mapWidth - BARRIER_X_OFFSET because the map is drawn at worldX = -75, so the right world-edge is mapWidth - 75.
        gameCamera = new GameCamera(VIEW_WIDTH, VIEW_HEIGHT,
            mapWidths[0] - BARRIER_X_OFFSET,
            0f,
            mapHeights[0]);
        viewport   = new ExtendViewport(VIEW_WIDTH, VIEW_HEIGHT, gameCamera.getCamera());

        // the player — starts off-bike at the door for the intro cutscene
        player     = new Player(INTRO_BIKE_X, INTRO_BIKE_Y);
        catOffBike = new CatOffBike(INTRO_DOOR_X, INTRO_DOOR_Y);
        parkedBikeTexture = new Texture(Gdx.files.internal("small assets/catBike.png"));
        isOnBike = false;
        bikeParked = true;
        parkedBikeX = INTRO_BIKE_X;
        parkedBikeY = INTRO_BIKE_Y;
        introState = IntroState.FADE_WAIT;

        police = new ArrayList<>();
        police.add(new PoliceEnemy(3000f, 300f));

        puddles = new ArrayList<>();
        puddles.add(new PuddleEnemy(2200f, 100f));
        puddles.add(new PuddleEnemy(3950f, 650f));


        gate1ObstacleTexture = new Texture(Gdx.files.internal("small assets/obstacle 30 fish.png"));
        // Render at fixed 500px wide, height scaled proportionally; centred at X=3832, top flush with map 1 top
        gate1DrawWidth  = 500f;
        gate1DrawHeight = gate1DrawWidth / gate1ObstacleTexture.getWidth() * gate1ObstacleTexture.getHeight();
        gate1ObstacleX = 3766f - gate1DrawWidth / 2f;
        gate1ObstacleY = MAP_HEIGHT_PER_PART - gate1DrawHeight;

        gate2ObstacleTexture = new Texture(Gdx.files.internal("small assets/obstacle 70 fish.png"));
        // 500px wide, proportional height; centred at X=550, top flush with map 2 top
        gate2DrawWidth  = 500f;
        gate2DrawHeight = gate2DrawWidth / gate2ObstacleTexture.getWidth() * gate2ObstacleTexture.getHeight();
        gate2ObstacleX = 500f - gate2DrawWidth / 2f;
        gate2ObstacleY = mapYOffsets[2] - gate2DrawHeight; // top of map 2 = mapYOffsets[2]

        fishHudTexture = new Texture(Gdx.files.internal("small assets/fish.png"));
        hudBatch  = new SpriteBatch();
        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, VIEW_WIDTH, VIEW_HEIGHT);
        hudFont   = new BitmapFont();
        hudFont.getData().setScale(2f);

        pauseTexture  = new Texture(Gdx.files.internal("Pause/Pause_screen2.png"));
        pauseBatch    = new SpriteBatch();
        pauseViewport = new ExtendViewport(VIEW_WIDTH, VIEW_HEIGHT);

        timer = new Timer();
        timer.start();

        ticketYellow   = new Texture(Gdx.files.internal("orderTickets/ticketSmallHouseYellow.png"));
        ticketBlue     = new Texture(Gdx.files.internal("orderTickets/ticketSmallHouseBlue.png"));
        ticketPink     = new Texture(Gdx.files.internal("orderTickets/ticketSmallHousePink.png"));
        ticketBigBlue  = new Texture(Gdx.files.internal("orderTickets/ticketBigHouseBlue.png"));
        ticketBigBlack = new Texture(Gdx.files.internal("orderTickets/ticketBigHouseBlack.png"));
        ticketBigRed   = new Texture(Gdx.files.internal("orderTickets/ticketBigHouseRed.png"));
        ticketBigBrown = new Texture(Gdx.files.internal("orderTickets/ticketBigHouseBrown.png"));


        // Map 1 houses (world Y 0–902)
        List<House> map1Houses = new ArrayList<>();
        map1Houses.add(new House(1700f, 300f, ticketYellow));
        map1Houses.add(new House(2630f, 300f, ticketBlue));
        map1Houses.add(new House(4755f, 300f, ticketPink));
        housesByMap.add(map1Houses);


        // Map 2 houses (world Y 1804–2706) — positions are placeholders, tune against map art
        List<House> map2Houses = new ArrayList<>();
        map2Houses.add(new House(1699f, MAP_HEIGHT_PER_PART * 2 + 310f, ticketBigBrown));
        whitehouse = new House(2845f, MAP_HEIGHT_PER_PART * 2 + 310f, ticketBigRed); // The whitehouse
        map2Houses.add(whitehouse);
        map2Houses.add(new House(3808f, MAP_HEIGHT_PER_PART * 2 + 310f, ticketBigBlack));
        map2Houses.add(new House(4870f, MAP_HEIGHT_PER_PART * 2 + 310f, ticketBigBlue));
        housesByMap.add(map2Houses);

        // Map 3 house Array is empty -> the president level (no orders)
        List<House> map3Houses = new ArrayList<>();
        housesByMap.add(map3Houses);

        spawnFish();

        if (SettingsScreen.soundOn) {
            backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/backgroundMusic.mp3"));
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(0.5f);
            backgroundMusic.play();
        } else {
            backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("audio/backgroundMusic.mp3"));
            backgroundMusic.setLooping(true);
            backgroundMusic.setVolume(0.5f);
        }

        deliveredSound = Gdx.audio.newSound(Gdx.files.internal("audio/deliveredOrder.mp3"));
        deliveryAlertSound = Gdx.audio.newSound(Gdx.files.internal("audio/deliveryDing.mp3"));
        activeNotifications = new ArrayList<>();

        fishSound = Gdx.audio.newSound(Gdx.files.internal("audio/fishPickUp.mp3"));

        // Open with iris expanding from black (FADE_IN only — no preceding FADE_OUT)
        transitionState = TransitionState.FADE_IN;
        transitionTimer = 0f;

        // Zoom in on the door for the intro cutscene
        gameCamera.getCamera().zoom = INTRO_ZOOM;
        gameCamera.update(INTRO_DOOR_X, INTRO_DOOR_Y);
    }

    /**
     * Spawns {@value #TARGET_FISH_COUNT} fish per map section on drivable road tiles.
     * Reads each map's barrier pixmap to reject positions that fall on non-road pixels.
     */
    private void spawnFish() {
        fishList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            spawnFishForMap(i);
        }
    }

    /**
     * Randomly places up to {@value #TARGET_FISH_COUNT} fish on road tiles for one map section.
     * Uses the barrier pixmap: pixels with alpha &lt; 128 are considered road.
     * Retries up to 10,000 times before giving up if road tiles are scarce.
     *
     * @param mapIndex the map section index (0 = map1, 1 = map2, 2 = map3)
     */
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
            // Map 3 has tree borders at edges — use tighter margins to avoid unreachable spawns
            float minLocalY = (mapIndex == 2) ? 150f : 50f;
            float maxLocalY = (mapIndex == 2) ? imgH - 200f : imgH - 50f;
            float localY = minLocalY + (float) Math.random() * (maxLocalY - minLocalY);

            int pixelX = (int)(worldX + BARRIER_X_OFFSET);
            int pixelY = imgH - 1 - (int)localY;

            if (pixelX < 0 || pixelX >= pm.getWidth() || pixelY < 0 || pixelY >= imgH) continue;

            int pixel = pm.getPixel(pixelX, pixelY);
            boolean isRoad = (pixel & 0xFF) < 128;
            if (isRoad && hasRoadMargin(pm, pixelX, pixelY, imgH, 50)) {
                fishList.add(new Fish(worldX, localY+ yOffset + 10f));
                spawned++;
            }
        }
    }

    /**
     * Returns true if all 8 sampled points at {@code radius} pixels distance
     * (cardinal + diagonal) around (cx, cy) are road tiles (alpha &lt; 128).
     * Points outside pixmap bounds are treated as barriers.
     */
    private boolean hasRoadMargin(Pixmap pm, int cx, int cy, int imgH, int radius) {
        int[] dx = {radius, -radius, 0,      0,       radius,  -radius,  radius, -radius};
        int[] dy = {0,       0,      radius, -radius,  radius,  -radius, -radius,  radius};
        for (int i = 0; i < dx.length; i++) {
            int nx = cx + dx[i];
            int ny = cy + dy[i];
            if (nx < 0 || nx >= pm.getWidth() || ny < 0 || ny >= imgH) return false;
            if ((pm.getPixel(nx, ny) & 0xFF) >= 128) return false;
        }
        return true;
    }

    /**
     * Called by LibGDX when the window is resized. Updates both the game world viewport
     * and the pause overlay viewport to match the new screen dimensions.
     *
     * @param width  the new screen width in pixels
     * @param height the new screen height in pixels
     */
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        pauseViewport.update(width, height, true);
    }

    /**
     * Called by LibGDX every frame to update game state and draw everything.
     *
     * <p>Update phase (when not paused):
     * <ul>
     *   <li>Handles ESC to toggle pause</li>
     *   <li>Ticks the timer; transitions to {@link EndScreen} on timeout</li>
     *   <li>Processes bike mount/dismount input and WASD movement</li>
     *   <li>Runs gate checks, map transitions, gate-fade animations, the intro cutscene,
     *       cinematic pan, and delivery order updates</li>
     *   <li>Checks for the Obama Whitehouse trigger and fish collection / police respawn</li>
     * </ul>
     *
     * <p>Render phase:
     * <ul>
     *   <li>Draws the current map, gate obstacles, houses, puddles, fish, player, and police</li>
     *   <li>Renders the HUD (fish count, timer, prompts), delivery notifications, gate-fade
     *       overlays, iris-wipe effect, and pause screen as needed</li>
     * </ul>
     *
     * @param delta time in seconds since the last frame
     */
    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE) && cinematicState == CinematicState.NONE && introState == IntroState.DONE) {
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
            if (introState == IntroState.DONE) timer.update(delta);
            if (timer.isFinished()) {
                if (backgroundMusic != null) {
                    backgroundMusic.stop();
                    backgroundMusic.dispose();
                    backgroundMusic = null;
                }
                dispose();
                game.setScreen(new EndScreen(game, fishCollected, timer.getElapsedSeconds() + totalTimePenalties, false));
                return;
            }
            if (transitionState == TransitionState.NONE
                    && cinematicState == CinematicState.NONE
                    && gate1FadeState == Gate1FadeState.NONE
                    && introState == IntroState.DONE) {

                // mount and dismount bike
                if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
                    if (isOnBike) {
                        parkedBikeX = player.getCenterX();
                        parkedBikeY = player.getCenterY();
                        catOffBike.setPosition(player.getCenterX(), player.getCenterY());
                    } else {
                        player.setPosition(catOffBike.getCenterX(), catOffBike.getCenterY());
                    }
                    isOnBike = !isOnBike;
                }

                if (isOnBike) {
                    player.update(delta, barrierLookup);
                } else {
                    catOffBike.update(delta, barrierLookup);
                }
                checkGates();
            }
            // Capture state and timer before update so renderIris uses pre-advance values.
            preRenderTransitionState = transitionState;
            preRenderTransitionTimer = transitionTimer;
            updateTransition(delta);
            updateGate1Fade(delta);
            updateGate2Fade(delta);
            updateCinematic(delta);
            cinematicBars.update(delta);
            // Intro cutscene update
            if (introState != IntroState.DONE) {
                updateIntro(delta);
            }
            // During cinematic the camera is driven by updateCinematic; during intro by updateIntro; hand back to normal tracking otherwise.
            if (cinematicState == CinematicState.NONE && introState == IntroState.DONE) {
                gameCamera.update(getActiveX(), getActiveY());
            }
            // Check Obama trigger first — before any potential EndScreen triggers
            showObamaPrompt = false;
            if (!obamaTriggered && isPlayerNearWhitehouse()) {
                showObamaPrompt = true;
                if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                    obamaTriggered = true;
                    triggerObamaScene();
                    return;
                }
            }
            if (cinematicState == CinematicState.NONE
                    && gate1FadeState == Gate1FadeState.NONE
                    && checkFishCollection()) return;
            gateMessageTimer -= delta;

            if (introState == IntroState.DONE) updateDeliveryNotifications(delta);
        }

        // Delivery prompt + interaction — only tick current-map houses after intro is done and while not paused
        showDeliverPrompt = false;
        if (!isPaused && introState == IntroState.DONE) {
            for (House house : housesByMap.get(currentMapIndex)) {
                house.update(delta);
                if (cinematicState == CinematicState.NONE
                        && house.hasOrder()
                        && house.isPlayerInRange(getActiveX(), getActiveY())) {
                    showDeliverPrompt = true;
                    if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                        if (!isOnBike) {
                            if (SettingsScreen.soundOn) {
                                deliveredSound.play(1.0f);
                            }
                            if (house.tryDeliver(getActiveX(), getActiveY())) {
                                fishCollected += 10;
                            }
                        }
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

        if (currentMapIndex == 0 && gate1ObstacleActive) {
            batch.draw(gate1ObstacleTexture, gate1ObstacleX, gate1ObstacleY, gate1DrawWidth, gate1DrawHeight);
        }
        if (currentMapIndex == 1 && gate2ObstacleActive) {
            batch.draw(gate2ObstacleTexture, gate2ObstacleX, gate2ObstacleY, gate2DrawWidth, gate2DrawHeight);
        }

        for (House house : housesByMap.get(currentMapIndex)) {
            house.render(batch);
        }

        player.resetSpeed();
        catOffBike.resetSpeed();
        boolean isOnPuddle = false;
        for (PuddleEnemy puddle : puddles) {
            if (puddle.onPuddle(getActiveX(), getActiveY())) {
                isOnPuddle = true;
                if (isOnBike) player.setSpeed(70f);
                else catOffBike.setSpeed(70f);

                if (!wasOnPuddle) {
                    fishCollected -= 1;
                }
            }
            puddle.render(batch);
        }
        wasOnPuddle = isOnPuddle;

        for (Fish fish : fishList) {
            fish.render(batch);
        }
        // replaced player.render(batch)
        if (!isOnBike) {
            float bikeSize = 110f;
            batch.draw(parkedBikeTexture,
                    parkedBikeX - bikeSize / 2f,
                    parkedBikeY - bikeSize / 2f,
                    bikeSize, bikeSize);
        }
        if (isOnBike) player.render(batch);
        else catOffBike.render(batch);

        for (PoliceEnemy enemy : police) {
            enemy.resetSpeed();
            for (PuddleEnemy puddle : puddles) {
                if (puddle.onPuddle(enemy.getCenterX(), enemy.getCenterY())) {
                    enemy.setChaseSpeed(120f);
                    enemy.setWanderSpeed(50f);
                    break;
                }
            }
            if (!isPaused && transitionState == TransitionState.NONE
                    && (cinematicState == CinematicState.NONE || currentMapIndex == 2)) {
                enemy.update(delta, getActiveX(), getActiveY(), barrierLookup);
            }
            enemy.render(batch);
        }

        batch.end();

        renderHud();
        renderDeliveryNotifications();
        renderGate1Fade();
        renderGate2Fade();

        if (preRenderTransitionState != TransitionState.NONE) {
            float halfDiag = (float) Math.sqrt(
                (VIEW_WIDTH / 2f) * (VIEW_WIDTH / 2f) + (VIEW_HEIGHT / 2f) * (VIEW_HEIGHT / 2f)
            ) + 50f;
            float t = Math.min(preRenderTransitionTimer, TRANSITION_DURATION) / TRANSITION_DURATION;
            float irisRadius = (preRenderTransitionState == TransitionState.FADE_OUT)
                ? halfDiag * (1f - t)
                : halfDiag * t;
            renderIris(Math.max(0f, irisRadius));
        }

        if (isPaused) {
            renderPause();
        }

    }


    /**
     * Synchronises on-screen delivery notification panels with the active house orders.
     * Creates a new {@link DeliveryNotification} when a house gains an order that has no
     * panel yet, plays the alert sound, ticks existing panels, and removes expired ones
     * while compacting the remaining slot indices.
     *
     * @param delta time in seconds since the last frame
     */
    private void updateDeliveryNotifications(float delta) {
        List<House> currentMapHouses = housesByMap.get(activeDeliveryMap);

        for (House house : currentMapHouses) {
            if (house.hasOrder()) {
                boolean hasNotification = false;
                for (DeliveryNotification notif : activeNotifications) {
                    if (notif.getHouse() == house) {
                        hasNotification = true;
                        break;
                    }
                }

                if (!hasNotification) {
                    DeliveryNotification newNotif = new DeliveryNotification(
                            house, house.getOrderIndicator(), activeNotifications.size());
                    activeNotifications.add(newNotif);

                    // Play alert sound
                    if (SettingsScreen.soundOn) {
                        deliveryAlertSound.play(0.7f);
                    }
                }
            }
        }

        for (DeliveryNotification notif : activeNotifications) {
            notif.update(delta);
        }

        for (int i = activeNotifications.size() - 1; i >= 0; i--) {
            if (activeNotifications.get(i).isExpired()) {
                activeNotifications.remove(i);

                for (int j = 0; j < activeNotifications.size(); j++) {
                    activeNotifications.get(j).setSlotIndex(j);
                }
            }
        }
    }

    /**
     * Draws all active delivery notification panels to the HUD using the HUD batch
     * and camera so they appear in screen space regardless of world camera position.
     */
    private void renderDeliveryNotifications() {
        hudBatch.setProjectionMatrix(hudCamera.combined);
        hudBatch.begin();
        for (DeliveryNotification notif : activeNotifications) {
            notif.render(hudBatch, hudFont, VIEW_WIDTH, VIEW_HEIGHT, whiteTexture);
        }
        hudBatch.end();
    }

    /**
     * Checks whether the player is near a gate obstacle and handles interaction.
     * If the player has collected enough fish and presses E, the gate obstacle is cleared
     * and the iris-wipe transition to the next map is queued.
     * If the player lacks fish, a message is shown and their position is clamped behind the gate.
     * Called only while no map transition or fade is in progress.
     */
    private void checkGates() {
        float px = getActiveX();
        float py = getActiveY();

        //Gate 1
        if (currentMapIndex == 0 && gate1ObstacleActive) {
            float obBottom = gate1ObstacleY;
            float obRight  = gate1ObstacleX + gate1DrawWidth;

            if (py >= obBottom - 50f && py <= obBottom + 50f &&
                    px >= gate1ObstacleX && px <= obRight) {

                int fishNeeded = FISH_QUOTA_GATE1 - fishCollected;

                if (fishNeeded > 0) {
                    setGateMessage("Need " + fishNeeded + " more fish!");
                } else {
                    setGateMessage("Press E to clear!");

                    if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                        gate1ObstacleActive = false;
                        gate1Cleared = true;
                    }
                }
                if (py >= obBottom) {
                    if (isOnBike) player.setPosition(px, obBottom - 20f);
                    else catOffBike.setPosition(px, obBottom - 20f);
                }
            }
        }

        // Gate 1b: once obstacle is cleared, touching the top edge triggers map 2
        if (currentMapIndex == 0 && gate1Cleared && py >= MAP_HEIGHT_PER_PART - 10f) {
            pendingMapIndex = 1;
            transitionState = TransitionState.FADE_OUT;
            transitionTimer = 0f;
        }

        // Hard boundary: keep player inside map 1 until cleared (and until the transition fires)
        // with:
        if (py > MAP_HEIGHT_PER_PART && currentMapIndex < 1 && !gate1Cleared) {
            if (isOnBike) player.setPosition(px, MAP_HEIGHT_PER_PART - 80f);
            else catOffBike.setPosition(px, MAP_HEIGHT_PER_PART - 80f);
        }

        //Gate 2
        if (currentMapIndex == 1 && gate2ObstacleActive) {
            float obBottom = gate2ObstacleY;
            float obRight  = gate2ObstacleX + gate2DrawWidth;

            if (py >= obBottom - 50f && py <= obBottom + 50f &&
                    px >= gate2ObstacleX && px <= obRight) {

                int fishNeeded = FISH_QUOTA_GATE2 - fishCollected;

                if (fishNeeded > 0) {
                    setGateMessage("Need " + fishNeeded + " more fish!");
                } else {
                    setGateMessage("Press E to clear!");

                    if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
                        gate2ObstacleActive = false;
                        gate2Cleared = true;
                    }
                }

                if (currentMapIndex == 1 && py > mapYOffsets[2] && !gate2Cleared) {
                    if (isOnBike) player.setPosition(px, mapYOffsets[2] - 80f);
                    else catOffBike.setPosition(px, mapYOffsets[2] - 80f);
                }
            }
        }

        // Gate 2b: once obstacle is cleared, touching the top edge triggers map 3
        if (currentMapIndex == 1 && gate2Cleared && py >= mapYOffsets[2] - 10f) {
            pendingMapIndex = 2;
            transitionState = TransitionState.FADE_OUT;
            transitionTimer = 0f;
        }

        // Hard boundary: keep player inside map 2 until cleared
        if (currentMapIndex == 1 && py > mapYOffsets[2] && !gate2Cleared) {
            if (isOnBike) player.setPosition(px, mapYOffsets[2] - 80f);
            else catOffBike.setPosition(px, mapYOffsets[2] - 80f);
        }
    }

    /**
     * Advances the iris-wipe transition timer each frame.
     * When FADE_OUT completes, sets a one-frame deferred flag so the screen renders
     * fully black before {@link #activateMap(int)} is called and FADE_IN begins.
     * When FADE_IN completes, starts the map-3 cinematic if transitioning to map 3.
     *
     * @param delta time in seconds since the last frame
     */
    private void updateTransition(float delta) {
        if (transitionState == TransitionState.NONE) return;

        // Execute the deferred FADE_OUT→FADE_IN switch on the frame after FADE_OUT reaches full black.
        // This guarantees one rendered frame at radius=0 (full black) before activateMap fires.
        if (pendingFadeInSwitch) {
            pendingFadeInSwitch = false;
            activateMap(pendingMapIndex);
            transitionTimer = 0f;
            transitionState = TransitionState.FADE_IN;
            return;
        }

        transitionTimer += delta;
        if (transitionState == TransitionState.FADE_OUT && transitionTimer >= TRANSITION_DURATION) {
            // Clamp so preRenderTransitionTimer == TRANSITION_DURATION next frame → renders full black.
            transitionTimer = TRANSITION_DURATION;
            pendingFadeInSwitch = true;
        } else if (transitionState == TransitionState.FADE_IN && transitionTimer >= TRANSITION_DURATION) {
            transitionTimer = 0f;
            transitionState = TransitionState.NONE;
            if (pendingMapIndex == 2) {
                startMap3Cinematic();
            }
        }
    }

    /**
     * Advances the gate-1 black-fade animation used to visually remove the gate-1 obstacle.
     * Transitions through FADE_OUT (screen goes black) → removes obstacle → FADE_IN
     * (screen returns to normal), then sets {@code gate1Cleared} so the top-edge trigger fires.
     *
     * @param delta time in seconds since the last frame
     */
    private void updateGate1Fade(float delta) {
        if (gate1FadeState == Gate1FadeState.NONE) return;
        gate1FadeTimer += delta;
        if (gate1FadeState == Gate1FadeState.FADE_OUT && gate1FadeTimer >= GATE1_FADE_DURATION) {
            // Screen is fully black — remove the obstacle, start fading back in
            gate1ObstacleActive = false;
            gate1FadeTimer      = 0f;
            gate1FadeState      = Gate1FadeState.FADE_IN;
        } else if (gate1FadeState == Gate1FadeState.FADE_IN && gate1FadeTimer >= GATE1_FADE_DURATION) {
            gate1FadeTimer = 0f;
            gate1FadeState = Gate1FadeState.NONE;
            gate1Cleared   = true;
        }
    }

    /** Full-screen black overlay that drives the gate 1 obstacle-clear animation. */
    private void renderGate1Fade() {
        if (gate1FadeState == Gate1FadeState.NONE) return;
        float alpha = (gate1FadeState == Gate1FadeState.FADE_OUT)
            ? gate1FadeTimer / GATE1_FADE_DURATION          // 0 → 1
            : 1f - gate1FadeTimer / GATE1_FADE_DURATION;   // 1 → 0
        hudBatch.setProjectionMatrix(hudCamera.combined);
        hudBatch.begin();
        hudBatch.setColor(0f, 0f, 0f, alpha);
        hudBatch.draw(blackTexture, 0, 0, VIEW_WIDTH, VIEW_HEIGHT);
        hudBatch.setColor(Color.WHITE);
        hudBatch.end();
    }

    /**
     * Advances the gate-2 black-fade animation used to visually remove the gate-2 obstacle.
     * Mirrors the gate-1 logic: FADE_OUT → obstacle removed → FADE_IN → {@code gate2Cleared} set.
     *
     * @param delta time in seconds since the last frame
     */
    private void updateGate2Fade(float delta) {
        if (gate2FadeState == Gate2FadeState.NONE) return;
        gate2FadeTimer += delta;
        if (gate2FadeState == Gate2FadeState.FADE_OUT && gate2FadeTimer >= GATE2_FADE_DURATION) {
            gate2ObstacleActive = false;
            gate2FadeTimer      = 0f;
            gate2FadeState      = Gate2FadeState.FADE_IN;
        } else if (gate2FadeState == Gate2FadeState.FADE_IN && gate2FadeTimer >= GATE2_FADE_DURATION) {
            gate2FadeTimer = 0f;
            gate2FadeState = Gate2FadeState.NONE;
            gate2Cleared   = true;
        }
    }

    /** Full-screen black overlay that drives the gate 2 obstacle-clear animation. */
    private void renderGate2Fade() {
        if (gate2FadeState == Gate2FadeState.NONE) return;
        float alpha = (gate2FadeState == Gate2FadeState.FADE_OUT)
            ? gate2FadeTimer / GATE2_FADE_DURATION
            : 1f - gate2FadeTimer / GATE2_FADE_DURATION;
        hudBatch.setProjectionMatrix(hudCamera.combined);
        hudBatch.begin();
        hudBatch.setColor(0f, 0f, 0f, alpha);
        hudBatch.draw(blackTexture, 0, 0, VIEW_WIDTH, VIEW_HEIGHT);
        hudBatch.setColor(Color.WHITE);
        hudBatch.end();
    }

    /**
     * Begins the map-3 intro cinematic pan.
     * Records the camera's current position and zoom as the pan start point,
     * resets the cinematic timer, sets the state to PAN_TO_HOUSES, and shows
     * the letterbox bars.
     */
    private void startMap3Cinematic() {
        OrthographicCamera cam = gameCamera.getCamera();
        panStartX    = cam.position.x;
        panStartY    = cam.position.y;
        panStartZoom = cam.zoom;
        cinematicTimer = 0f;
        cinematicState = CinematicState.PAN_TO_HOUSES;
        cinematicBars.show();
    }

    /**
     * Drives the opening intro cutscene state machine each frame.
     *
     * <p>States in order:
     * <ol>
     *   <li>FADE_WAIT — holds the camera zoomed in on the door while the iris wipe plays</li>
     *   <li>DOOR_DELAY — brief pause at the door before the cat starts walking</li>
     *   <li>WALK_TO_BIKE — the off-bike cat walks toward the bike; camera follows zoomed in</li>
     *   <li>WAIT_MOUNT — cat stands at the bike, prompting the player to press Q</li>
     *   <li>MOUNT — player presses Q; camera smoothly zooms out to normal play zoom</li>
     *   <li>DONE — cutscene complete; normal gameplay resumes</li>
     * </ol>
     *
     * @param delta time in seconds since the last frame
     */
    private void updateIntro(float delta) {
        OrthographicCamera cam = gameCamera.getCamera();
        switch (introState) {
            case FADE_WAIT:
                // Keep camera zoomed in on the door while the iris wipe plays
                cam.zoom = INTRO_ZOOM;
                cam.position.x = catOffBike.getCenterX();
                cam.position.y = catOffBike.getCenterY();
                cam.update();
                if (transitionState == TransitionState.NONE) {
                    introState = IntroState.DOOR_DELAY;
                    introTimer = 0f;
                }
                break;

            case DOOR_DELAY:
                // 1-second pause at the door before the cat starts walking
                introTimer += delta;
                cam.zoom = INTRO_ZOOM;
                cam.position.x = catOffBike.getCenterX();
                cam.position.y = catOffBike.getCenterY();
                cam.update();
                if (introTimer >= INTRO_DOOR_DELAY) {
                    introState = IntroState.WALK_TO_BIKE;
                }
                break;

            case WALK_TO_BIKE:
                catOffBike.walkToward(INTRO_STOP_X, INTRO_STOP_Y, delta, INTRO_WALK_SPEED);
                // Camera follows catOffBike while zoomed in
                cam.zoom = INTRO_ZOOM;
                cam.position.x = catOffBike.getCenterX();
                cam.position.y = catOffBike.getCenterY();
                cam.update();
                // Check if arrived near the bike
                float dx = INTRO_STOP_X - catOffBike.getCenterX();
                float dy = INTRO_STOP_Y - catOffBike.getCenterY();
                if (dx * dx + dy * dy < 5f * 5f) {
                    introState = IntroState.WAIT_MOUNT;
                    showIntroMountPrompt = true;
                }
                break;

            case WAIT_MOUNT:
                // Cat stands near bike, waiting for player to press Q
                cam.zoom = INTRO_ZOOM;
                cam.position.x = catOffBike.getCenterX();
                cam.position.y = catOffBike.getCenterY();
                cam.update();
                if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
                    isOnBike = true;
                    player.setPosition(INTRO_BIKE_X, INTRO_BIKE_Y);
                    showIntroMountPrompt = false;
                    introState = IntroState.MOUNT;
                    introTimer = 0f;
                }
                break;

            case MOUNT:
                introTimer += delta;
                // Smoothly zoom out from INTRO_ZOOM to 1.0
                cam.zoom = Math.min(1.0f, cam.zoom + INTRO_ZOOM_OUT_SPEED * delta);
                gameCamera.update(player.getCenterX(), player.getCenterY());
                if (introTimer >= INTRO_MOUNT_DURATION && cam.zoom >= 1.0f) {
                    cam.zoom = 1.0f;
                    introState = IntroState.DONE;
                }
                break;

            case DONE:
                break;
        }
    }

    /**
     * Drives the map-3 intro cinematic pan each frame.
     *
     * <p>States in order:
     * <ol>
     *   <li>PAN_TO_HOUSES — smoothly pans and zooms to show the full map-3 width</li>
     *   <li>HOLD — holds on the overview; hides letterbox bars when hold time elapses</li>
     *   <li>PAN_BACK — smoothly pans and zooms back to the player's position at zoom 1</li>
     * </ol>
     * All panning uses smoothstep easing. After PAN_BACK completes, the state returns to
     * NONE and normal camera tracking resumes.
     *
     * @param delta time in seconds since the last frame
     */
    private void updateCinematic(float delta) {
        if (cinematicState == CinematicState.NONE) return;
        cinematicTimer += delta;

        OrthographicCamera cam = gameCamera.getCamera();
        float mapMaxX = mapWidths[2] - BARRIER_X_OFFSET;
        float mapMinY = mapYOffsets[2];
        float mapMaxY = mapYOffsets[2] + mapHeights[2];

        // Pan target: horizontal center of map 3, vertical center
        float targetX = mapMaxX / 2f;
        float targetY = (mapMinY + mapMaxY) / 2f;
        // Zoom exactly enough to fit the full map width inside the viewport
        float targetZoom = mapMaxX / VIEW_WIDTH;

        if (cinematicState == CinematicState.PAN_TO_HOUSES) {
            float t = smoothStep(MathUtils.clamp(cinematicTimer / CINEMATIC_PAN_DURATION, 0f, 1f));
            cam.zoom = MathUtils.lerp(panStartZoom, targetZoom, t);
            float rawX = MathUtils.lerp(panStartX, targetX, t);
            float rawY = MathUtils.lerp(panStartY, targetY, t);
            clampAndApply(cam, rawX, rawY, mapMinY, mapMaxY, mapMaxX);

            if (cinematicTimer >= CINEMATIC_PAN_DURATION) {
                cinematicState = CinematicState.HOLD;
                cinematicTimer = 0f;
            }

        } else if (cinematicState == CinematicState.HOLD) {
            cam.zoom = targetZoom;
            clampAndApply(cam, targetX, targetY, mapMinY, mapMaxY, mapMaxX);

            if (cinematicTimer >= CINEMATIC_HOLD_DURATION) {
                // Capture exact hold position as new pan start
                panStartX    = cam.position.x;
                panStartY    = cam.position.y;
                panStartZoom = cam.zoom;
                cinematicState = CinematicState.PAN_BACK;
                cinematicTimer = 0f;
                cinematicBars.hide();
            }

        } else if (cinematicState == CinematicState.PAN_BACK) {
            float t = smoothStep(MathUtils.clamp(cinematicTimer / CINEMATIC_PAN_DURATION, 0f, 1f));
            cam.zoom = MathUtils.lerp(panStartZoom, 1f, t);

            // Final resting position: where normal camera tracking would place us at zoom=1
            float finalHalfW = VIEW_WIDTH  / 2f;
            float finalHalfH = VIEW_HEIGHT / 2f;
            float finalTargetX = MathUtils.clamp(getActiveX(), finalHalfW, mapMaxX - finalHalfW);
            float finalTargetY = MathUtils.clamp(getActiveY(), mapMinY + finalHalfH, mapMaxY - finalHalfH);

            float rawX = MathUtils.lerp(panStartX, finalTargetX, t);
            float rawY = MathUtils.lerp(panStartY, finalTargetY, t);
            clampAndApply(cam, rawX, rawY, mapMinY, mapMaxY, mapMaxX);

            if (cinematicTimer >= CINEMATIC_PAN_DURATION) {
                cam.zoom = 1f;
                cinematicState = CinematicState.NONE;
                // gameCamera.update() takes over next frame
            }
        }
    }

    /** Clamps camera to map bounds at current zoom, then calls cam.update().
     *  If the zoomed view exceeds a map dimension, the camera is centered on that axis. */
    private void clampAndApply(OrthographicCamera cam, float rawX, float rawY,
                                float mapMinY, float mapMaxY, float mapMaxX) {
        float halfW = VIEW_WIDTH  * cam.zoom / 2f;
        float halfH = VIEW_HEIGHT * cam.zoom / 2f;
        // When view wider/taller than the map, center; otherwise clamp to keep within bounds.
        cam.position.x = (halfW * 2f >= mapMaxX)
            ? mapMaxX / 2f
            : MathUtils.clamp(rawX, halfW, mapMaxX - halfW);
        cam.position.y = (halfH * 2f >= mapMaxY - mapMinY)
            ? (mapMinY + mapMaxY) / 2f
            : MathUtils.clamp(rawY, mapMinY + halfH, mapMaxY - halfH);
        cam.update();
    }

    /** Smoothstep easing: starts and ends slow, fast in the middle. */
    private float smoothStep(float t) {
        return GameLogicHelper.smoothStep(t);
    }

    /**
     * Switches the active map to the given index mid-transition (called at full-black frame).
     * Repositions the player and off-bike cat at the new map's spawn point, updates camera
     * bounds, clears delivery notifications, and spawns enemies for maps 2 and 3.
     *
     * @param index the map index to activate (0 = map1, 1 = map2, 2 = map3)
     */
    private void activateMap(int index) {
        // Calculate spawn positions dynamically based on actual map offsets
        float[] spawnX = {300f, 5600f, 1250f};
        float[] spawnY = {300f, mapYOffsets[1] + 100f, mapYOffsets[2] + 100f};
        // Police spawns offset from player spawn
        float[] policeX = {800f, 4600f, 1850f};
        float[] policeY = {300f, mapYOffsets[1] + 100f, mapYOffsets[2] + 100f};

        //player.setPosition(spawnX[index], spawnY[index]);
        //currentMapIndex = Math.max(currentMapIndex, index);
        // changed:
        player.setPosition(spawnX[index], spawnY[index]);
        catOffBike.setPosition(spawnX[index], spawnY[index]);
        isOnBike = true;
        currentMapIndex = Math.max(currentMapIndex, index);

        activeDeliveryMap = index;
        activeNotifications.clear();

        gameCamera.setMaxX(mapWidths[index] - BARRIER_X_OFFSET);
        gameCamera.setYBounds(mapYOffsets[index], mapYOffsets[index] + mapHeights[index]);
        for (PoliceEnemy enemy : police) {
            enemy.setPosition(policeX[index], policeY[index]);
        }

        if (index >= 1) {
            police.clear();
            puddles.clear();
        }

        if (index >= 1) {
            spawnEnemiesForMap(index);
        }
    }

    /**
     * Populates the police and puddle enemy lists from the hard-coded spawn-coordinate arrays
     * for the given map. Spawns are offset by the map's world-Y origin so coordinates in the
     * arrays are relative to the local map bottom.
     *
     * @param index the map index (1 = map2, 2 = map3); map1 enemies are created in {@link #show()}
     */
    private void spawnEnemiesForMap(int index) {
        float[][] policeSpawns = (index == 1) ? MAP2_POLICE_SPAWNS : MAP3_POLICE_SPAWNS;
        float[][] puddleSpawns = (index == 1) ? MAP2_PUDDLE_SPAWNS : MAP3_PUDDLE_SPAWNS;
        float yOffset = mapYOffsets[index];

        for (float[] spawn : policeSpawns) {
            police.add(new PoliceEnemy(spawn[0], yOffset + spawn[1]));
        }
        for (float[] spawn : puddleSpawns) {
            puddles.add(new PuddleEnemy(spawn[0], yOffset + spawn[1]));
        }
    }

    /**
     * Renders the iris-wipe effect using the OpenGL stencil buffer.
     * Draws a filled circle of the given radius into the stencil buffer (no colour output),
     * then covers every pixel outside the circle with solid black, creating the classic
     * iris-wipe look used for map transitions and game open/close.
     *
     * @param irisRadius radius of the transparent "open" circle in HUD/screen pixels;
     *                   0 = fully black, &ge; half-diagonal = fully visible
     */
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

    /**
     * Returns the number of house deliveries still pending on the specified map.
     *
     * @param mapIndex the map index to query
     * @return number of houses on that map that still have an active order
     */
    private int deliveriesNeeded(int mapIndex) {
        return GameLogicHelper.deliveriesNeeded(housesByMap.get(mapIndex));
    }

    /**
     * Returns {@code true} when the player is within the Obama Whitehouse trigger zone
     * on map 3, as determined by {@link GameLogicHelper#isNearWhitehouse}.
     *
     * @return {@code true} if the Obama cutscene prompt should be shown
     */
    private boolean isPlayerNearWhitehouse() {
        // X: image pixel coords minus BARRIER_X_OFFSET (75) because map is drawn at x=-75
        // Y: mapYOffset + mapHeight - imagePixelY (image Y=0 is top, world Y=0 is bottom)
        return GameLogicHelper.isNearWhitehouse(
            getActiveX(), getActiveY(), currentMapIndex,
            1080f - BARRIER_X_OFFSET, mapYOffsets[2] + mapHeights[2] - 940f,
            1775f - BARRIER_X_OFFSET, mapYOffsets[2] + mapHeights[2] - 940f + 150f);
    }

    /**
     * Stops the timer and music, disposes this screen's resources, and transitions
     * to the {@link ObamaScreen} cutscene, passing elapsed time, fish count, and the
     * player's current world position so the cutscene can start with the correct layout.
     */
    private void triggerObamaScene() {
        timer.stop();
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.dispose();
            backgroundMusic = null;
        }
        int elapsed = timer.getElapsedSeconds();
        float yOffset = mapYOffsets[2];
        float height  = mapHeights[2];
        float px = getActiveX();
        float py = getActiveY();
        dispose();
        game.setScreen(new ObamaScreen(game, elapsed, fishCollected, yOffset, height, px, py, SettingsScreen.soundOn));
    }

    /** @return the X centre of whichever entity is currently active (on-bike player or off-bike cat) */
    private float getActiveX() { return isOnBike ? player.getCenterX() : catOffBike.getCenterX(); }

    /** @return the Y centre of whichever entity is currently active (on-bike player or off-bike cat) */
    private float getActiveY() { return isOnBike ? player.getCenterY() : catOffBike.getCenterY(); }

    /**
     * Sets a temporary HUD message shown near a gate obstacle for {@code 0.1} seconds.
     * Subsequent calls within that window reset the timer, keeping the message visible.
     *
     * @param msg the text to display (e.g. "Need 5 more fish!" or "Press E to clear!")
     */
    private void setGateMessage(String msg) {
        gateMessage = msg;
        gateMessageTimer = 0.1f;
    }

    /**
     * Collects nearby fish and checks loss/respawn conditions each frame.
     *
     * <ul>
     *   <li>Collects any uncollected fish within {@code collectRadius} pixels of the active entity</li>
     *   <li>If total fish drop below 0, transitions immediately to {@link EndScreen} (loss)</li>
     *   <li>If a police enemy is catching the player:
     *     <ul>
     *       <li>If deducting the fish penalty would drop below 0 → EndScreen (loss)</li>
     *       <li>Otherwise, calls {@link Respawn#respawn} to reposition, deduct fish, and
     *           add a time penalty, then returns {@code true} to skip the rest of this frame</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * @return {@code true} if a screen transition or respawn occurred and the caller should return early
     */
    private boolean checkFishCollection() {
        float collectRadius = 60f;
        float rSquared = collectRadius * collectRadius;
        for (Fish fish : fishList) {
            if (fish.isCollected()) continue;
            float dx = fish.getCenterX() - getActiveX();
            float dy = fish.getCenterY() - getActiveY();
            if (dx * dx + dy * dy < rSquared) {
                if (SettingsScreen.soundOn) {
                    fishSound.play(5.0f);
                }
                fish.collect();
                fishCollected++;
            }
        }

        if (fishCollected < 0) {
            timer.stop();
            if (backgroundMusic != null) {
                backgroundMusic.stop();
                backgroundMusic.dispose();
                backgroundMusic = null;
            }
            dispose();
            game.setScreen(new EndScreen(game, fishCollected, timer.getElapsedSeconds() + totalTimePenalties, false));
            return true;
        }

        for (PoliceEnemy enemy : police) {
            if (enemy.isCatching(getActiveX(), getActiveY())) {
                if (fishCollected - RESPAWN_FISH_PENALTY < 0) {
                    timer.stop();
                    int elapsed = timer.getElapsedSeconds() + totalTimePenalties;
                    dispose();
                    game.setScreen(new EndScreen(game, fishCollected - RESPAWN_FISH_PENALTY, elapsed, false));
                    return true;
                }
                fishCollected = Respawn.respawn(player, police, timer, currentMapIndex,
                        fishCollected, mapYOffsets, RESPAWN_FISH_PENALTY, RESPAWN_TIME_PENALTY);
                totalTimePenalties += (int) RESPAWN_TIME_PENALTY;
                catOffBike.setPosition(player.getCenterX(), player.getCenterY());
                isOnBike = true;
                return true;
            }
        }
        return false;
    }


    /**
     * Draws the heads-up display in screen space using the HUD batch and orthographic camera.
     * Renders the fish-count icon and number, the countdown timer, contextual prompts
     * (deliver, mount bike, Obama, gate messages), and the cinematic letterbox bars.
     */
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

        if (showDeliverPrompt || showObamaPrompt) {
            String prompt = isOnBike
                    ?  "Get off your bike (Q) to deliver!"
                    : "Press E to deliver!";

            hudFont.draw(hudBatch, prompt, VIEW_WIDTH / 2 - 100f, 80f);
        }

        if (showIntroMountPrompt) {
            hudFont.draw(hudBatch, "Press Q to mount bike!", VIEW_WIDTH / 2 - 100f, 80f);
        }

        if (gateMessageTimer > 0) {
            hudFont.draw(hudBatch, gateMessage, VIEW_WIDTH / 2 - 200f, 150f);
        }

        cinematicBars.render(hudBatch);

        if (cinematicState == CinematicState.HOLD) {
            String msg = "Your final task: deliver the pizza to the president!";
            hudFont.draw(hudBatch, msg, VIEW_WIDTH / 2f - 380f, VIEW_HEIGHT * 0.13f);
        }

        hudBatch.end();
    }

    /**
     * Renders the pause overlay and handles button clicks (Resume, Restart, Quit).
     * Uses a separate viewport so the overlay always fills the screen regardless of zoom.
     * Unprojects touch coordinates to check which button region was tapped.
     */
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
                backgroundMusic.play();
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

    /**
     * Releases all OpenGL and audio resources owned by this screen.
     * Safe to call multiple times; null-checks guard every disposable before disposing.
     * Sets music and sound references to null after disposal to prevent double-dispose.
     */
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
        if (ticketYellow   != null) ticketYellow.dispose();
        if (ticketBlue     != null) ticketBlue.dispose();
        if (ticketPink     != null) ticketPink.dispose();
        if (ticketBigBlue  != null) ticketBigBlue.dispose();
        if (ticketBigBlack != null) ticketBigBlack.dispose();
        if (ticketBigRed   != null) ticketBigRed.dispose();
        if (ticketBigBrown != null) ticketBigBrown.dispose();
        if (gate1ObstacleTexture != null) gate1ObstacleTexture.dispose();
        if (gate2ObstacleTexture != null) gate2ObstacleTexture.dispose();
        shapeRenderer.dispose();
        blackTexture.dispose();
        whiteTexture.dispose();
        cinematicBars.dispose();
        if (backgroundMusic != null) {
            backgroundMusic.stop();
            backgroundMusic.dispose();
        }
        if (deliveredSound != null) deliveredSound.dispose();
        fishSound.dispose();
        catOffBike.dispose();
        if (parkedBikeTexture != null) parkedBikeTexture.dispose();
        if (deliveryAlertSound != null) deliveryAlertSound.dispose();
    }
}
