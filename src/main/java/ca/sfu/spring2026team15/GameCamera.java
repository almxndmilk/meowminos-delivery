package ca.sfu.spring2026team15;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;

public class GameCamera {
    private final OrthographicCamera camera;
    private final float mapWidth;
    private final float mapHeight;
    private final float halfViewW;
    private final float halfViewH;

    public GameCamera(float viewportWidth, float viewportHeight, float mapWidth, float mapHeight) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.halfViewW = viewportWidth / 2f;
        this.halfViewH = viewportHeight / 2f;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, viewportWidth, viewportHeight);
    }

    // Centers the camera on (playerX, playerY), then clamps to map bounds.
    public void update(float playerX, float playerY) {
        camera.position.x = MathUtils.clamp(playerX, halfViewW, mapWidth - halfViewW);
        camera.position.y = MathUtils.clamp(playerY, halfViewH, mapHeight - halfViewH);
        camera.update();
    }

    public OrthographicCamera getCamera() {
        return camera;
    }
}
