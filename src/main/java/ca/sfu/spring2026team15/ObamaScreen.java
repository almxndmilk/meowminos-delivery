package ca.sfu.spring2026team15;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

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

    // How long the talk pose is held before transitioning to EndScreen
    private static final float TALK_DURATION     = 2.0f;

    // Cutscene phases
    private enum Phase { OBAMA_WALK, PLAYER_WALK, TALK }
    private Phase phase = Phase.OBAMA_WALK;

    private final Main game;
    private final int  elapsedSeconds;
    private final float map3YOffset, map3Height;

    // Obama world positions (bottom-left of sprite)
    private float obamaX;
    private float obamaY;
    private final float obamaTargetX;
    private final float obamaTargetY;

    // Camera pan: slowly sweep toward the White House while Obama walks out
    // camStartY: frames the player / trigger zone; camEndY: frames the door
    private final float camStartY;
    private final float camEndY;
    private float camCurrentY;  // tracks live camera Y so resize() can restore it

    // Player sprite state
    private float playerX;
    private float playerY;
    private final float playerTargetX;
    private final float playerTargetY;
    private final boolean playerFacingRight;

    // Timers / animation
    private float animTimer  = 0f;
    private boolean showFrame1 = true;
    private float talkTimer  = 0f;

    // LibGDX rendering
    private SpriteBatch    batch;
    private ExtendViewport viewport;
    private OrthographicCamera camera;

    // Textures
    private Texture mapTexture;
    private Texture obamaWalk1;
    private Texture obamaWalk2;
    private Texture obamaTalk;
    private Texture playerFrame1;
    private Texture playerFrame2;

    /**
     * @param map3YOffset    mapYOffsets[2] — world Y of map 3's bottom edge
     * @param map3Height     mapHeights[2]  — actual pixel height of map part3.png
     * @param playerCenterX  player's center X when the scene was triggered
     * @param playerCenterY  player's center Y when the scene was triggered
     *
     * Coordinate conversion (image pixel → world):
     *   worldX = imgX − 75
     *   worldY = map3YOffset + (map3Height − imgY)
     *
     * Door image coords:   (1465,745)→(1500,810)  → Obama starts at worldX≈1407, worldY=map3YOffset+map3Height−810
     * Target image coords: (1475,885)              → worldX=1400, worldY=map3YOffset+map3Height−885
     */
    public ObamaScreen(Main game, int elapsedSeconds,
                       float map3YOffset, float map3Height,
                       float playerCenterX, float playerCenterY) {
        this.game           = game;
        this.elapsedSeconds = elapsedSeconds;
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

        // Build camera + viewport (starts at camStartY)
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1280f, 720f);
        camera.position.set(1428f, camCurrentY, 0);
        camera.update();

        viewport = new ExtendViewport(1280f, 720f, camera);
        batch    = new SpriteBatch();
    }

    @Override
    public void show() {
        mapTexture   = new Texture(Gdx.files.internal("map/map part3.png"));
        obamaWalk1   = new Texture(Gdx.files.internal("Obama/obama_walk1.png"));
        obamaWalk2   = new Texture(Gdx.files.internal("Obama/obama_walk2.png"));
        obamaTalk    = new Texture(Gdx.files.internal("Obama/obama_talk.png"));
        playerFrame1 = new Texture(Gdx.files.internal("catOffBike/catOffBike1.png"));
        playerFrame2 = new Texture(Gdx.files.internal("catOffBike/catOffBike2.png"));
    }

    @Override
    public void render(float delta) {
        // Skip cutscene on any input
        if (Gdx.input.justTouched() || anyKeyJustPressed()) {
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
                talkTimer += delta;
                if (talkTimer >= TALK_DURATION) {
                    transitionToEnd();
                    return;
                }
                break;
        }

        drawScene();
    }

    private void updateObamaWalk(float delta) {
        // Walk Obama downward toward target
        obamaY -= OBAMA_WALK_SPEED * delta;

        // Gently slide X toward target as well (minor correction if door/target differ)
        float xDiff = obamaTargetX - obamaX;
        if (Math.abs(xDiff) > 1f) {
            obamaX += Math.signum(xDiff) * OBAMA_WALK_SPEED * delta;
        }

        // Camera slowly pans upward (toward White House) during Obama's walk
        // Use fixed pan speed so it feels smooth regardless of Obama arriving early
        float panSpeed = (camEndY - camStartY) * (OBAMA_WALK_SPEED / Math.abs(obamaTargetY - (map3YOffset + map3Height - 810f) + 1f));
        camCurrentY = Math.min(camCurrentY + Math.abs(panSpeed) * delta, camEndY);
        camera.position.set(1428f, camCurrentY, 0);
        camera.update();

        // Check if Obama has reached (or passed) the target
        if (obamaY <= obamaTargetY) {
            obamaY = obamaTargetY;
            obamaX = obamaTargetX;
            phase = Phase.PLAYER_WALK;
            animTimer = 0f;  // reset anim for player walk
            showFrame1 = true;
        }
    }

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
            obamaFrame = obamaTalk;
        } else {
            obamaFrame = showFrame1 ? obamaWalk1 : obamaWalk2;
        }
        batch.draw(obamaFrame, obamaX, obamaY, OBAMA_W, OBAMA_H);

        // Player sprite (shown during PLAYER_WALK and TALK)
        if (phase == Phase.PLAYER_WALK || phase == Phase.TALK) {
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

    private boolean anyKeyJustPressed() {
        for (int key = 0; key < 256; key++) {
            if (Gdx.input.isKeyJustPressed(key)) return true;
        }
        return false;
    }

    private void transitionToEnd() {
        dispose();
        game.setScreen(new EndScreen(game, elapsedSeconds));
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
        // Restore live camera position after viewport reset
        camera.position.set(1428f, camCurrentY, 0);
        camera.update();
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        if (mapTexture   != null) { mapTexture.dispose();   mapTexture   = null; }
        if (obamaWalk1   != null) { obamaWalk1.dispose();   obamaWalk1   = null; }
        if (obamaWalk2   != null) { obamaWalk2.dispose();   obamaWalk2   = null; }
        if (obamaTalk    != null) { obamaTalk.dispose();    obamaTalk    = null; }
        if (playerFrame1 != null) { playerFrame1.dispose(); playerFrame1 = null; }
        if (playerFrame2 != null) { playerFrame2.dispose(); playerFrame2 = null; }
        if (batch        != null) { batch.dispose();        batch        = null; }
    }
}
