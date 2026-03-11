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
    private static final float CUTSCENE_DURATION   = 8f;
    private static final float FRAME_DURATION      = 0.3f;
    private static final float MAP_HEIGHT_PER_PART = 902f;
    private static final float MAP3_Y_OFFSET       = MAP_HEIGHT_PER_PART * 2; // 1804
    private static final float MAP_DRAW_X          = -75f;
    private static final float CAM_X               = 2700f;
    private static final float CAM_Y               = MAP3_Y_OFFSET + MAP_HEIGHT_PER_PART / 2f; // 2255
    private static final float OBAMA_W             = 200f;
    private static final float OBAMA_H             = 300f;
    private static final float OBAMA_START_X       = 2150f;
    private static final float OBAMA_START_Y       = MAP3_Y_OFFSET + 150f; // 1954
    private static final float OBAMA_WALK_SPEED    = 60f;

    private final Main game;
    private final int elapsedSeconds;

    private SpriteBatch batch;
    private ExtendViewport viewport;
    private OrthographicCamera camera;

    private Texture mapTexture;
    private Texture obamaWalk1;
    private Texture obamaWalk2;

    private float cutsceneTimer = 0f;
    private float animTimer     = 0f;
    private boolean showFrame1  = true;
    private float obamaX = OBAMA_START_X;
    private float obamaY = OBAMA_START_Y;

    public ObamaScreen(Main game, int elapsedSeconds) {
        this.game           = game;
        this.elapsedSeconds = elapsedSeconds;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1280f, 720f);
        camera.position.set(CAM_X, CAM_Y, 0);
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
        if (Gdx.input.justTouched() || anyKeyJustPressed()) {
            transitionToEnd();
            return;
        }

        cutsceneTimer += delta;
        if (cutsceneTimer >= CUTSCENE_DURATION) {
            transitionToEnd();
            return;
        }

        animTimer += delta;
        if (animTimer >= FRAME_DURATION) {
            animTimer -= FRAME_DURATION;
            showFrame1 = !showFrame1;
        }

        obamaX += OBAMA_WALK_SPEED * delta;

        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(mapTexture, MAP_DRAW_X, MAP3_Y_OFFSET,
                   mapTexture.getWidth(), mapTexture.getHeight());
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
        camera.position.set(CAM_X, CAM_Y, 0);
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
