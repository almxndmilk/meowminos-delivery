package ca.sfu.spring2026team15;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

public class ObamaScreen implements Screen {

    private static final float CUTSCENE_DURATION = 8f;
    private static final float FRAME_DURATION    = 0.3f;
    private static final float MAP_DRAW_X        = -75f;

    // Obama sprite size — door is 35×65 image px; Obama slightly wider/taller for visibility
    private static final float OBAMA_W           = 50f;
    private static final float OBAMA_H           = 100f;
    private static final float OBAMA_WALK_SPEED  = 30f; // walks downward (−Y) toward the player

    private final Main game;
    private final int  elapsedSeconds;

    // Camera and Obama positions computed from live map dimensions passed in from GameScreen
    private final float camX, camY;
    private final float map3YOffset, map3Height;

    private SpriteBatch     batch;
    private ExtendViewport  viewport;
    private OrthographicCamera camera;

    private Texture mapTexture;
    private Texture obamaWalk1;
    private Texture obamaWalk2;

    private float   cutsceneTimer = 0f;
    private float   animTimer     = 0f;
    private boolean showFrame1    = true;

    // Obama world position — updated each frame
    private float obamaX;
    private float obamaY;

    /**
     * @param map3YOffset  mapYOffsets[2] from GameScreen — world Y of map 3's bottom edge
     * @param map3Height   mapHeights[2]  from GameScreen — actual pixel height of map part3.png
     *
     * Coordinate conversion used throughout (image pixel → world):
     *   worldX = imgX - 75
     *   worldY = map3YOffset + (map3Height - imgY)
     *
     * Door image coords:    (1465,745)→(1500,810)  → worldX center 1408, worldY bottom = map3YOffset+map3Height-810
     * Trigger image coords: (1155,940)→(1850,860)  → worldX 1080–1775
     * Camera imgY centre:   (745+940)/2 = 842.5    → worldY = map3YOffset+map3Height-843
     * Camera imgX centre:   (1155+1850)/2−75 = 1428
     */
    public ObamaScreen(Main game, int elapsedSeconds, float map3YOffset, float map3Height) {
        this.game           = game;
        this.elapsedSeconds = elapsedSeconds;
        this.map3YOffset    = map3YOffset;
        this.map3Height     = map3Height;

        // Camera centred to frame both the door and the trigger zone below it
        this.camX = 1428f;
        this.camY = map3YOffset + map3Height - 843f;

        // Obama starts at the door, centred horizontally, at the door's bottom edge
        this.obamaX = 1408f - OBAMA_W / 2f;              // door worldX centre − half sprite
        this.obamaY = map3YOffset + map3Height - 810f;    // door bottom in world Y

        camera   = new OrthographicCamera();
        camera.setToOrtho(false, 1280f, 720f);
        camera.position.set(camX, camY, 0);
        camera.update();

        viewport = new ExtendViewport(1280f, 720f, camera);
        batch    = new SpriteBatch();
    }

    @Override
    public void show() {
        mapTexture = new Texture(Gdx.files.internal("map/map part3.png"));
        obamaWalk1 = new Texture(Gdx.files.internal("Obama/obama_walk1.png"));
        obamaWalk2 = new Texture(Gdx.files.internal("Obama/obama_walk2.png"));
    }

    @Override
    public void render(float delta) {
        // Skip on any input
        if (Gdx.input.justTouched() || anyKeyJustPressed()) {
            transitionToEnd();
            return;
        }

        cutsceneTimer += delta;
        if (cutsceneTimer >= CUTSCENE_DURATION) {
            transitionToEnd();
            return;
        }

        // Toggle animation frames
        animTimer += delta;
        if (animTimer >= FRAME_DURATION) {
            animTimer  -= FRAME_DURATION;
            showFrame1  = !showFrame1;
        }

        // Obama walks downward (−Y = toward the player who triggered the scene)
        obamaY -= OBAMA_WALK_SPEED * delta;

        // Render
        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(mapTexture, MAP_DRAW_X, map3YOffset, mapTexture.getWidth(), mapTexture.getHeight());
        Texture frame = showFrame1 ? obamaWalk1 : obamaWalk2;
        batch.draw(frame, obamaX, obamaY, OBAMA_W, OBAMA_H);
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
        // Re-lock the camera after the viewport resets it
        camera.position.set(camX, camY, 0);
        camera.update();
    }

    @Override public void pause()  {}
    @Override public void resume() {}
    @Override public void hide()   {}

    @Override
    public void dispose() {
        if (mapTexture != null) { mapTexture.dispose(); mapTexture = null; }
        if (obamaWalk1 != null) { obamaWalk1.dispose(); obamaWalk1 = null; }
        if (obamaWalk2 != null) { obamaWalk2.dispose(); obamaWalk2 = null; }
        if (batch      != null) { batch.dispose();      batch      = null; }
    }
}
