package ca.sfu.spring2026team15;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;

/**
 * Wraps LibGDX's {@link OrthographicCamera} with map-boundary clamping.
 *
 * <p>The camera tracks the active entity and clamps its position so the viewport never
 * shows outside the current map's edges. Bounds are updated via {@link #setMaxX} and
 * {@link #setYBounds} when the player transitions to a new map section.
 */
public class GameCamera {
    private final OrthographicCamera camera;
    private float maxX;
    private float minY, maxY;
    private final float halfViewW;
    private final float halfViewH;

    /**
     * Creates a camera configured for the given viewport dimensions and initial map bounds.
     *
     * @param viewportWidth  logical viewport width (world units)
     * @param viewportHeight logical viewport height (world units)
     * @param maxX           right boundary of the current map in world units
     * @param minY           bottom boundary of the current map in world units
     * @param maxY           top boundary of the current map in world units
     */
    public GameCamera(float viewportWidth, float viewportHeight, float maxX, float minY, float maxY) {
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.halfViewW = viewportWidth / 2f;
        this.halfViewH = viewportHeight / 2f;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, viewportWidth, viewportHeight);
    }

    /**
     * Expands the right-edge X clamp — call when entering a wider map.
     * @param newMaxX the new right boundary of the map in world units
     */
    public void setMaxX(float newMaxX) {
        this.maxX = newMaxX;
    }

    /**
     * Updates the Y clamp with raw map edges (halfViewH offset applied internally).
     * @param minY bottom boundary of the map in world units
     * @param maxY top boundary of the map in world units
     */
    public void setYBounds(float minY, float maxY) {
        this.minY = minY;
        this.maxY = maxY;
    }

    /**
     * Centres the camera on the active entity position, then clamps to the current map bounds
     * so the viewport never shows outside the map edges.
     *
     * @param playerX world X of the entity to follow
     * @param playerY world Y of the entity to follow
     */
    public void update(float playerX, float playerY) {
        camera.position.x = MathUtils.clamp(playerX, halfViewW, maxX - halfViewW);
        camera.position.y = MathUtils.clamp(playerY, minY + halfViewH, maxY - halfViewH);
        camera.update();
    }

    /**
     * Returns the underlying {@link OrthographicCamera} for direct manipulation
     * (e.g. zoom changes during cinematics or the intro cutscene).
     *
     * @return the wrapped {@link OrthographicCamera}
     */
    public OrthographicCamera getCamera() {
        return camera;
    }
}
