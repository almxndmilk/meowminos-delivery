package ca.sfu.spring2026team15;

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
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

/**
 * Cutscene screen that plays after the player triggers the Obama Whitehouse delivery.
 *
 * <p>The sequence runs through three phases:
 * <ol>
 *   <li><b>OBAMA_WALK</b> — Obama walks out of the Whitehouse door toward the player;
 *       the camera slowly pans toward the door</li>
 *   <li><b>PLAYER_WALK</b> — the cat walks toward Obama and stops beside him</li>
 *   <li><b>TALK</b> — Obama's voice clip plays while subtitles are progressively revealed;
 *       a fallback timer is used when sound is off</li>
 * </ol>
 * Any key press or touch skips the cutscene immediately.
 * After the talk phase ends (audio + {@value #END_DELAY}s delay) the screen transitions to
 * {@link EndScreen} with the win flag set.
 *
 * <p>Coordinate conversion used throughout (image pixel → world):
 * <pre>worldX = imgX − 75,  worldY = map3YOffset + (map3Height − imgY)</pre>
 */
public class ObamaScreen implements Screen {

    private static final float MAP_DRAW_X        = -75f;
    private static final float FRAME_DURATION    = 0.3f;

    // Obama — same 150×150 size as police sprites
    private static final float OBAMA_W           = 150f;
    private static final float OBAMA_H           = 150f;
    private static final float OBAMA_WALK_SPEED  = 25f;  // px/s downward

    // Player sprite rendered during cutscene
    private static final float PLAYER_SIZE       = 150f;
    private static final float PLAYER_WALK_SPEED = 80f;  // px/s horizontal

    // Fallback talk duration when sound is off
    private static final float TALK_FALLBACK_DURATION = 10f;
    // Delay after audio finishes before transitioning to EndScreen
    private static final float END_DELAY = 2f;
    // LibGDX Music volume range is [0, 1].
    private static final float OBAMA_VOICE_VOLUME = 1.0f;

    // Dialogue spoken by Obama during the talk phase
    private static final String DIALOGUE =
        "Thank you for the pizza kind, citizen. You are doing a great job with "
        + "delivering this pizza. I love Meowmino's pizza. It's the best pizza in the "
        + "whole town. Anyways, here is your tip. Thank you for getting here in time. "
        + "I am going to return to my presidential duties.";

    // Cutscene phases
    private enum Phase { OBAMA_WALK, PLAYER_WALK, TALK }
    private Phase phase = Phase.OBAMA_WALK;

    private final Main game;
    private final int  elapsedSeconds;
    private final int fishCollected;
    private final float map3YOffset, map3Height;

    // Obama world positions (bottom-left of sprite)
    private float obamaX;
    private float obamaY;
    private final float obamaTargetX;
    private final float obamaTargetY;

    // Camera pan: slowly sweep toward the White House while Obama walks out
    private final float camStartY;
    private final float camEndY;
    private float camCurrentY;  // tracks live camera Y so resize() can restore it

    // Player sprite state
    private float playerX;
    private float playerY;
    private final float playerTargetX;
    private final float playerTargetY;
    private final boolean playerFacingRight;

    private boolean transitioning = false;

    // Timers / animation
    private float animTimer  = 0f;
    private boolean showFrame1 = true;
    private float talkTimer  = 0f;   // fallback timer when sound is off
    private float dialogueTimer = 0f; // tracks time for progressive text reveal
    private boolean audioFinished = false;
    private float endDelayTimer = 0f;

    // Audio
    private Music obamaVoice;
    private boolean audioStarted = false;
    private final boolean soundOn;

    // World-space rendering
    private SpriteBatch    batch;
    private ExtendViewport viewport;
    private OrthographicCamera camera;

    // HUD-space rendering (screen-space, matches GameScreen HUD style)
    private SpriteBatch        hudBatch;
    private OrthographicCamera hudCamera;
    private BitmapFont         font;
    private Texture            blackPixel;

    // Textures
    private Texture mapTexture;
    private Texture obamaWalk1;
    private Texture obamaWalk2;
    private Texture obamaTalk1;
    private Texture obamaTalk2;
    private Texture playerFrame1;
    private Texture playerFrame2;

    /**
     * Constructs the Obama cutscene screen and pre-computes all world-space positions.
     *
     * <p>Obama's starting position is derived from the Whitehouse door image coordinates
     * {@code (1465–1500, 810)} and his target from {@code (1475, 885)}.
     * The player is placed to the left or right of Obama depending on which side
     * {@code playerCenterX} falls, and walks to stand beside him during PLAYER_WALK.
     *
     * @param game           the application controller used to switch screens
     * @param elapsedSeconds total game time in seconds (passed through to {@link EndScreen})
     * @param fishCollected  fish count at time of trigger (passed through to {@link EndScreen})
     * @param map3YOffset    world Y of map 3's bottom edge ({@code mapYOffsets[2]})
     * @param map3Height     pixel height of map part3.png ({@code mapHeights[2]})
     * @param playerCenterX  player's world-center X when the cutscene was triggered
     * @param playerCenterY  player's world-center Y when the cutscene was triggered
     * @param soundOn        whether game audio is enabled (mirrors {@link SettingsScreen#soundOn})
     */
    public ObamaScreen(Main game, int elapsedSeconds, int fishCollected,
                       float map3YOffset, float map3Height,
                       float playerCenterX, float playerCenterY, boolean soundOn) {
        this.game           = game;
        this.soundOn        = soundOn;
        this.elapsedSeconds = elapsedSeconds;
        this.fishCollected  = fishCollected;
        this.map3YOffset    = map3YOffset;
        this.map3Height     = map3Height;

        // Obama: start at door, walk to (1475, 885) image coords
        float doorWorldX  = 1407f;
        float doorBottomY = map3YOffset + map3Height - 810f;
        this.obamaX = doorWorldX - OBAMA_W / 2f;    // centre Obama on door X
        this.obamaY = doorBottomY;

        this.obamaTargetX = 1400f - OBAMA_W / 2f;   // = 1325
        this.obamaTargetY = map3YOffset + map3Height - 885f;

        // Camera pan: start near player/trigger zone, slowly pan toward the White House door
        this.camStartY   = map3YOffset + map3Height - 910f;
        this.camEndY     = map3YOffset + map3Height - 790f;
        this.camCurrentY = camStartY;

        // Player sprite: start position (bottom-left from center)
        this.playerX = playerCenterX - PLAYER_SIZE / 2f;
        this.playerY = playerCenterY - PLAYER_SIZE / 2f;

        // Decide which side to stand on and which direction to walk
        float obamaCenterX = obamaTargetX + OBAMA_W / 2f;  // ≈ 1400
        if (playerCenterX < obamaCenterX) {
            // Player is left of Obama → walk right, stop to Obama's left
            this.playerTargetX   = obamaTargetX - PLAYER_SIZE - 10f;
            this.playerFacingRight = true;
        } else {
            // Player is right of Obama → walk left, stop to Obama's right
            this.playerTargetX   = obamaTargetX + OBAMA_W + 10f;
            this.playerFacingRight = false;
        }
        this.playerTargetY = obamaTargetY;

        // World-space camera (zoomed in 2× for close-up cutscene)
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1280f, 720f);
        camera.zoom = 0.5f;
        camera.position.set(1428f, camCurrentY, 0);
        camera.update();

        viewport = new ExtendViewport(1280f, 720f, camera);
        batch    = new SpriteBatch();
        
        // Explicitly initialize timing variables to ensure clean state after respawn
        this.animTimer = 0f;
        this.showFrame1 = true;
        this.talkTimer = 0f;
        this.dialogueTimer = 0f;
        this.audioStarted = false;
        this.audioFinished = false;
        this.endDelayTimer = 0f;
    }

    /**
     * Called by LibGDX when this screen becomes active.
     * Loads all textures (map, Obama walk/talk frames, player frames),
     * sets up the Obama voice audio with a completion listener, and initialises
     * the HUD batch, camera, font, and black-pixel texture for subtitle rendering.
     */
    @Override
    public void show() {
        mapTexture   = new Texture(Gdx.files.internal("map/map part3.png"));
        obamaWalk1   = new Texture(Gdx.files.internal("Obama/obama_walk1.png"));
        obamaWalk2   = new Texture(Gdx.files.internal("Obama/obama_walk2.png"));
        obamaTalk1   = new Texture(Gdx.files.internal("Obama/obama_talk1.png"));
        obamaTalk2   = new Texture(Gdx.files.internal("Obama/obama_talk2.png"));
        playerFrame1 = new Texture(Gdx.files.internal("catOffBike/catOffBike1.png"));
        playerFrame2 = new Texture(Gdx.files.internal("catOffBike/catOffBike2.png"));

        obamaVoice = Gdx.audio.newMusic(Gdx.files.internal("Obama/obamaCutScene.mp3"));
        obamaVoice.setVolume(OBAMA_VOICE_VOLUME);
        obamaVoice.setOnCompletionListener(music -> {
            audioFinished = true;
            endDelayTimer = 0f;
        });

        // HUD layer — matches GameScreen style exactly
        hudBatch  = new SpriteBatch();
        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(false, 1280f, 720f);

        font = new BitmapFont();
        font.getData().setScale(2f);

        Pixmap pm = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pm.setColor(Color.BLACK);
        pm.fill();
        blackPixel = new Texture(pm);
        pm.dispose();
    }

    /**
     * Called by LibGDX every frame to advance the cutscene and draw everything.
     * Any touch or key press immediately skips to {@link EndScreen}.
     * Advances the shared animation frame timer, delegates to the current phase
     * update method, then draws the world scene and (during TALK) the subtitle banner.
     *
     * @param delta time in seconds since the last frame
     */
    @Override
    public void render(float delta) {
        // Skip cutscene on Space
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            transitionToEnd();
            return;
        }

        // Advance animation frame timer (shared for both Obama and player)
        animTimer += delta;
        if (animTimer >= FRAME_DURATION) {
            animTimer  -= FRAME_DURATION;
            showFrame1  = !showFrame1;
        }

        switch (phase) {
            case OBAMA_WALK:
                updateObamaWalk(delta);
                break;
            case PLAYER_WALK:
                updatePlayerWalk(delta);
                break;
            case TALK:
                updateTalkPhase(delta);
                break;
        }

        if (transitioning) return;

        drawScene();

        if (phase == Phase.TALK) {
            drawDialogue();
        }
    }

    /**
     * Advances the OBAMA_WALK phase: moves Obama downward (and slightly horizontally) toward
     * his target position while slowly panning the camera upward toward the Whitehouse door.
     * Transitions to PLAYER_WALK when Obama reaches his target.
     *
     * @param delta time in seconds since the last frame
     */
    private void updateObamaWalk(float delta) {
        // Walk Obama downward toward target
        obamaY -= OBAMA_WALK_SPEED * delta;

        // Gently slide X toward target as well (minor correction if door/target differ)
        float xDiff = obamaTargetX - obamaX;
        if (Math.abs(xDiff) > 1f) {
            obamaX += Math.signum(xDiff) * OBAMA_WALK_SPEED * delta;
        }

        // Camera slowly pans upward (toward White House) during Obama's walk
        float panSpeed = (camEndY - camStartY) * (OBAMA_WALK_SPEED / Math.abs(obamaTargetY - (map3YOffset + map3Height - 810f) + 1f));
        camCurrentY = Math.min(camCurrentY + Math.abs(panSpeed) * delta, camEndY);
        camera.position.set(1428f, camCurrentY, 0);
        camera.update();

        // Check if Obama has reached (or passed) the target
        if (obamaY <= obamaTargetY) {
            obamaY = obamaTargetY;
            obamaX = obamaTargetX;
            phase = Phase.PLAYER_WALK;
            animTimer = 0f;
            showFrame1 = true;
        }
    }

    /**
     * Advances the PLAYER_WALK phase: slides the player sprite toward Obama's side.
     * Transitions to TALK once the player is within 5 pixels of their target position.
     *
     * @param delta time in seconds since the last frame
     */
    private void updatePlayerWalk(float delta) {
        float dx = playerTargetX - playerX;
        if (Math.abs(dx) <= 5f) {
            // Player has arrived — lock position and switch to talk
            playerX = playerTargetX;
            playerY = playerTargetY;
            phase = Phase.TALK;
            return;
        }
        playerX += Math.signum(dx) * PLAYER_WALK_SPEED * delta;

        // Also slide player Y toward target Y
        float dy = playerTargetY - playerY;
        if (Math.abs(dy) > 1f) {
            playerY += Math.signum(dy) * PLAYER_WALK_SPEED * delta;
        }
    }

    /**
     * Advances the TALK phase: starts the Obama voice clip on the first frame, ticks the
     * dialogue reveal timer, and (when sound is off) uses {@link #TALK_FALLBACK_DURATION}
     * as a stand-in for the audio length. Calls {@link #transitionToEnd()} after the audio
     * finishes plus a {@link #END_DELAY} second buffer.
     *
     * @param delta time in seconds since the last frame
     */
    private void updateTalkPhase(float delta) {
        // Start audio once on the first frame of TALK; only check completion
        // on subsequent frames so isPlaying() has time to return true.
        if (!audioStarted) {
            audioStarted = true;
            dialogueTimer = 0f;
            talkTimer = 0f;
            audioFinished = false;
            endDelayTimer = 0f;
            if (soundOn) {
                obamaVoice.play();
            }
            return; // skip completion check this frame
        }

        // Update dialogue timer for progressive text reveal
        dialogueTimer += delta;

        // Sound on: completion listener sets audioFinished exactly at clip end.
        // Sound off: use fallback duration so cutscene can still progress.
        if (!audioFinished && !soundOn) {
            talkTimer += delta;
            if (talkTimer >= TALK_FALLBACK_DURATION) {
                audioFinished = true;
                endDelayTimer = 0f;
            }
        }

        // After audio finishes, wait for END_DELAY before transitioning
        if (audioFinished) {
            endDelayTimer += delta;
            if (endDelayTimer >= END_DELAY) {
                transitionToEnd();
            }
        }
    }

    /**
     * Renders the world-space cutscene: the map-3 tile, Obama's current animation frame,
     * and the player sprite (mirrored horizontally if the player is facing right).
     */
    private void drawScene() {
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Map
        batch.draw(mapTexture, MAP_DRAW_X, map3YOffset, mapTexture.getWidth(), mapTexture.getHeight());

        // Obama sprite
        Texture obamaFrame;
        if (phase == Phase.TALK) {
            obamaFrame = showFrame1 ? obamaTalk1 : obamaTalk2;
        } else {
            obamaFrame = showFrame1 ? obamaWalk1 : obamaWalk2;
        }
        batch.draw(obamaFrame, obamaX, obamaY, OBAMA_W, OBAMA_H);

        // Player sprite (always visible — static during OBAMA_WALK, walks during PLAYER_WALK)
        {
            Texture playerTex = (phase == Phase.PLAYER_WALK && !showFrame1) ? playerFrame2 : playerFrame1;
            if (playerFacingRight) {
                // Mirror: draw from right edge leftward
                batch.draw(playerTex, playerX + PLAYER_SIZE, playerY, -PLAYER_SIZE, PLAYER_SIZE);
            } else {
                batch.draw(playerTex, playerX, playerY, PLAYER_SIZE, PLAYER_SIZE);
            }
        }

        batch.end();
    }

    /**
     * Renders the dialogue subtitle banner in screen-space over the world scene.
     * Draws a semi-transparent black strip at the bottom of the viewport and overlays
     * the progressively revealed {@link #DIALOGUE} text using {@link #getProgressiveDialogue()}.
     */
    private void drawDialogue() {
        float bannerH = 220f;
        float margin  = 90f;
        float textW   = 1280f - margin * 2f;

        hudBatch.setProjectionMatrix(hudCamera.combined);
        hudBatch.begin();

        // Semi-transparent black banner at bottom (matches GameScreen cinematic banner)
        hudBatch.setColor(0f, 0f, 0f, 0.70f);
        hudBatch.draw(blackPixel, 0, 0, 1280f, bannerH);
        hudBatch.setColor(Color.WHITE);

        // Calculate how much of the dialogue to show based on elapsed time
        String textToShow = getProgressiveDialogue();
        font.draw(hudBatch, textToShow, margin, bannerH - 18f, textW, Align.left, true);

        hudBatch.end();
    }

    /**
     * Returns the portion of {@link #DIALOGUE} that should be visible based on elapsed time.
     * Words are revealed at a rate of {@code wordCount / TALK_FALLBACK_DURATION} words per second.
     *
     * @return the substring of dialogue visible at the current {@code dialogueTimer} value
     */
    private String getProgressiveDialogue() {
        // Split dialogue into words
        String[] words = DIALOGUE.split(" ");
        
        float totalDuration = TALK_FALLBACK_DURATION;
        // LibGDX does not easily provide audio clip duration at runtime,
        // so TALK_FALLBACK_DURATION is always used as the total duration.
        
        // Calculate how many words should be visible
        float wordsPerSecond = words.length / totalDuration;
        int wordsToShow = Math.min(words.length, (int)(dialogueTimer * wordsPerSecond));
        
        // Build the partial dialogue string
        if (wordsToShow <= 0) {
            return "";
        }
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < wordsToShow; i++) {
            if (i > 0) result.append(" ");
            result.append(words[i]);
        }
        
        return result.toString();
    }

    /**
     * Disposes this screen and transitions to {@link EndScreen} with the win flag set.
     * Guard-flag prevents double-transition if called more than once in a frame.
     */
    private void transitionToEnd() {
        if (transitioning) return;
        transitioning = true;
        dispose();
        game.setScreen(new EndScreen(game, fishCollected, elapsedSeconds, true)); // true = came from ObamaScreen
    }

    /**
     * Called by LibGDX when the window is resized.
     * Updates the viewport and restores the camera's zoom and position since
     * {@code ExtendViewport.update} resets the camera to the viewport centre.
     *
     * @param width  new screen width in pixels
     * @param height new screen height in pixels
     */
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        // Restore live camera position and zoom after viewport reset
        camera.zoom = 0.5f;
        camera.position.set(1428f, camCurrentY, 0);
        camera.update();
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    /**
     * Releases all OpenGL and audio resources owned by this screen.
     * Each disposable is null-checked before disposing and set to {@code null} afterwards
     * to prevent double-dispose if called more than once.
     */
    @Override
    public void dispose() {
        if (obamaVoice  != null) { obamaVoice.stop(); obamaVoice.dispose();  obamaVoice  = null; }
        if (mapTexture  != null) { mapTexture.dispose();                      mapTexture  = null; }
        if (obamaWalk1  != null) { obamaWalk1.dispose();                      obamaWalk1  = null; }
        if (obamaWalk2  != null) { obamaWalk2.dispose();                      obamaWalk2  = null; }
        if (obamaTalk1  != null) { obamaTalk1.dispose();                      obamaTalk1  = null; }
        if (obamaTalk2  != null) { obamaTalk2.dispose();                      obamaTalk2  = null; }
        if (playerFrame1 != null) { playerFrame1.dispose();                   playerFrame1 = null; }
        if (playerFrame2 != null) { playerFrame2.dispose();                   playerFrame2 = null; }
        if (font        != null) { font.dispose();                            font        = null; }
        if (blackPixel  != null) { blackPixel.dispose();                      blackPixel  = null; }
        if (hudBatch    != null) { hudBatch.dispose();                        hudBatch    = null; }
        if (batch       != null) { batch.dispose();                           batch       = null; }
    }
}
