package ca.sfu.spring2026team15;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;

public class GameCamera {
    private final OrthographicCamera camera;
    private float maxX;
    private float minY, maxY;
    private final float halfViewW;
    private final float halfViewH;

    public GameCamera(float viewportWidth, float viewportHeight, float maxX, float minY, float maxY) {
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.halfViewW = viewportWidth / 2f;
        this.halfViewH = viewportHeight / 2f;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, viewportWidth, viewportHeight);
    }

    /** Expand the right-edge X clamp — call when entering a wider map. */
    public void setMaxX(float newMaxX) {
        this.maxX = newMaxX;
    }

    /** Update the Y clamp with raw map edges (halfViewH offset applied internally). */
    public void setYBounds(float minY, float maxY) {
        this.minY = minY;
        this.maxY = maxY;
    }

    // Centers the camera on (playerX, playerY), then clamps to current map bounds.
    public void update(float playerX, float playerY) {
        camera.position.x = MathUtils.clamp(playerX, halfViewW, maxX - halfViewW);
        camera.position.y = MathUtils.clamp(playerY, minY + halfViewH, maxY - halfViewH);
        camera.update();
    }

    public OrthographicCamera getCamera() {
        return camera;
    }
}
