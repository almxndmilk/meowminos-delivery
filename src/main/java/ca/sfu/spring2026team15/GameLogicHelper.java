package ca.sfu.spring2026team15;

import com.badlogic.gdx.math.Vector2;
import java.util.List;

/**
 * Pure-logic helper methods extracted from GameScreen for testability.
 * All methods are static and have no LibGDX rendering dependencies.
 */
public class GameLogicHelper {

    private GameLogicHelper() {} // utility class

    // --- Transition math ---

    /**
     * Smoothstep easing function: starts and ends slow, fast in the middle.
     * @param t normalised input in [0, 1]
     * @return eased output in [0, 1]
     */
    public static float smoothStep(float t) {
        return t * t * (3f - 2f * t);
    }

    /**
     * Calculates the iris-wipe circle radius for the current frame.
     * @param halfDiag  half the screen diagonal (maximum radius when fully open)
     * @param timer     elapsed time within the current transition phase
     * @param duration  total duration of the transition phase
     * @param isFadeOut {@code true} if fading out (radius shrinks); {@code false} if fading in
     * @return the iris radius to pass to the stencil renderer
     */
    public static float calculateIrisRadius(float halfDiag, float timer, float duration, boolean isFadeOut) {
        float t = Math.min(timer, duration) / duration;
        return isFadeOut ? halfDiag * (1f - t) : halfDiag * t;
    }

    /**
     * Calculates the half-diagonal of the viewport, used as the maximum iris radius.
     * Adds 50 pixels of padding to ensure the black overlay fully covers screen corners.
     * @param viewWidth  viewport width in pixels
     * @param viewHeight viewport height in pixels
     * @return half-diagonal + 50 pixels
     */
    public static float calculateHalfDiagonal(float viewWidth, float viewHeight) {
        return (float) Math.sqrt(
            (viewWidth / 2f) * (viewWidth / 2f) + (viewHeight / 2f) * (viewHeight / 2f)
        ) + 50f;
    }

    // --- Gate fade math ---

    /**
     * Calculates the black-overlay alpha for a gate obstacle fade animation.
     * @param timer     elapsed time in the current fade phase
     * @param duration  total fade duration
     * @param isFadeOut {@code true} if fading to black; {@code false} if fading back in
     * @return alpha in [0, 1]
     */
    public static float calculateGateFadeAlpha(float timer, float duration, boolean isFadeOut) {
        return isFadeOut ? timer / duration : 1f - timer / duration;
    }

    // --- Gate obstacle collision ---

    /**
     * Returns {@code true} if the player is within the gate obstacle's interaction zone.
     * The zone extends 50 pixels above and below the obstacle's bottom edge.
     * @param playerX       player's world-centre X
     * @param playerY       player's world-centre Y
     * @param obstacleX     left edge of the obstacle sprite
     * @param obstacleY     bottom edge of the obstacle sprite
     * @param obstacleWidth rendered width of the obstacle sprite
     * @return {@code true} if within 50 px vertically and within horizontal bounds
     */
    public static boolean isNearGateObstacle(float playerX, float playerY,
                                              float obstacleX, float obstacleY,
                                              float obstacleWidth) {
        float obBottom = obstacleY;
        float obRight = obstacleX + obstacleWidth;
        return playerY >= obBottom - 50f && playerY <= obBottom + 50f
            && playerX >= obstacleX && playerX <= obRight;
    }

    /**
     * Calculates how many more fish the player needs to clear a gate.
     * @param fishCollected current fish count
     * @param fishQuota     fish required to pass
     * @return deficit (positive = needs more); 0 if already enough
     */
    public static int fishNeededForGate(int fishCollected, int fishQuota) {
        return Math.max(0, fishQuota - fishCollected);
    }

    // --- Fish collection ---

    /**
     * Returns {@code true} if a fish is within collection range of the player.
     * @param fishCenterX   fish's world-centre X
     * @param fishCenterY   fish's world-centre Y
     * @param playerX       player's world-centre X
     * @param playerY       player's world-centre Y
     * @param collectRadius collection radius in world units
     * @return {@code true} if the fish is within the radius
     */
    public static boolean isFishInRange(float fishCenterX, float fishCenterY,
                                         float playerX, float playerY,
                                         float collectRadius) {
        float dx = fishCenterX - playerX;
        float dy = fishCenterY - playerY;
        return dx * dx + dy * dy < collectRadius * collectRadius;
    }

    // --- Fish spawning validation ---

    /**
     * Returns {@code true} if the given pixel coordinates lie within pixmap bounds.
     * @param pixelX   pixel X to test
     * @param pixelY   pixel Y to test
     * @param pmWidth  pixmap width
     * @param pmHeight pixmap height
     * @return {@code true} if within bounds
     */
    public static boolean isValidPixmapCoord(int pixelX, int pixelY, int pmWidth, int pmHeight) {
        return pixelX >= 0 && pixelX < pmWidth && pixelY >= 0 && pixelY < pmHeight;
    }

    /**
     * Returns {@code true} if the given RGBA8888 pixel value represents a road tile.
     * Road tiles have an alpha value below 128 in the barrier pixmap.
     * @param pixel raw RGBA8888 pixel value from a {@link com.badlogic.gdx.graphics.Pixmap}
     * @return {@code true} if the pixel is a road (alpha &lt; 128)
     */
    public static boolean isRoadPixel(int pixel) {
        return (pixel & 0xFF) < 128;
    }

    // --- Camera clamping (cinematic) ---

    /**
     * Clamps camera position to map bounds at a given zoom level.
     * When the zoomed view exceeds a dimension, the camera is centered on that axis.
     *
     * @param rawX      unclamped camera centre X
     * @param rawY      unclamped camera centre Y
     * @param viewWidth  viewport width in world units
     * @param viewHeight viewport height in world units
     * @param zoom       current camera zoom factor
     * @param mapMinY    bottom boundary of the map in world units
     * @param mapMaxY    top boundary of the map in world units
     * @param mapMaxX    right boundary of the map in world units
     * @return {@code float[2]} containing the clamped camera centre {@code {x, y}}
     */
    public static float[] clampCameraToMapBounds(float rawX, float rawY,
                                                   float viewWidth, float viewHeight,
                                                   float zoom,
                                                   float mapMinY, float mapMaxY,
                                                   float mapMaxX) {
        float halfW = viewWidth * zoom / 2f;
        float halfH = viewHeight * zoom / 2f;
        float cx = (halfW * 2f >= mapMaxX)
            ? mapMaxX / 2f
            : clamp(rawX, halfW, mapMaxX - halfW);
        float cy = (halfH * 2f >= mapMaxY - mapMinY)
            ? (mapMinY + mapMaxY) / 2f
            : clamp(rawY, mapMinY + halfH, mapMaxY - halfH);
        return new float[]{cx, cy};
    }

    // --- Score / time formatting (from EndScreen) ---

    /**
     * Decomposes a score into five zero-padded decimal digits for digit-sprite rendering.
     * @param score raw score (clamped to [0, 99999])
     * @return array of five digits, most-significant first
     */
    public static int[] getScoreDigits(int score) {
        int s = Math.max(0, Math.min(score, 99999));
        return new int[]{
            s / 10000,
            (s / 1000) % 10,
            (s / 100) % 10,
            (s / 10) % 10,
            s % 10
        };
    }

    /**
     * Decomposes a duration into four digits in MM:SS order for digit-sprite rendering.
     * Minutes are capped at 99.
     * @param totalSeconds total elapsed time in seconds
     * @return {@code [MM tens, MM units, SS tens, SS units]}
     */
    public static int[] getTimeDigits(int totalSeconds) {
        int mm = Math.min(totalSeconds / 60, 99);
        int ss = totalSeconds % 60;
        return new int[]{mm / 10, mm % 10, ss / 10, ss % 10};
    }

    // --- Delivery management ---

    /**
     * Counts houses that have never had a successful delivery.
     * @param houses list of houses on a map section
     * @return number of houses that still need at least one delivery
     */
    public static int deliveriesNeeded(List<House> houses) {
        int count = 0;
        for (House h : houses) {
            if (!h.wasDelivered()) count++;
        }
        return count;
    }

    // --- Whitehouse proximity check ---

    /**
     * Returns {@code true} when the player is within the Obama Whitehouse delivery trigger
     * zone on map 3. The zone is defined by any two opposite corner coordinates.
     * @param playerX        player's world-centre X
     * @param playerY        player's world-centre Y
     * @param currentMapIndex must be 2 (map 3) for the zone to activate
     * @param corner1X       X of one corner of the trigger rectangle
     * @param corner1Y       Y of one corner of the trigger rectangle
     * @param corner2X       X of the opposite corner of the trigger rectangle
     * @param corner2Y       Y of the opposite corner of the trigger rectangle
     * @return {@code true} if on map 3 and within the Whitehouse trigger rectangle
     */
    public static boolean isNearWhitehouse(float playerX, float playerY,
                                            int currentMapIndex,
                                            float corner1X, float corner1Y,
                                            float corner2X, float corner2Y) {
        float minX = Math.min(corner1X, corner2X);
        float maxX = Math.max(corner1X, corner2X);
        float minY = Math.min(corner1Y, corner2Y);
        float maxY = Math.max(corner1Y, corner2Y);
        return currentMapIndex == 2
            && playerX >= minX && playerX <= maxX
            && playerY >= minY && playerY <= maxY;
    }

    // --- Intro cutscene helpers ---

    /**
     * Returns {@code true} if the off-bike cat has arrived near the target (within 5 px).
     *
     * @param catX    current X of the cat's centre
     * @param catY    current Y of the cat's centre
     * @param targetX target X to walk toward
     * @param targetY target Y to walk toward
     * @return {@code true} if the cat is within 5 pixels of the target
     */
    public static boolean isAtIntroTarget(float catX, float catY, float targetX, float targetY) {
        float dx = targetX - catX;
        float dy = targetY - catY;
        return dx * dx + dy * dy < 5f * 5f;
    }

    /**
     * Calculates the camera zoom level during the intro mount phase, capped at 1.0.
     *
     * @param currentZoom  the current zoom factor
     * @param zoomOutSpeed rate at which zoom increases per second
     * @param delta        time in seconds since the last frame
     * @return the new zoom factor, clamped to a maximum of {@code 1.0}
     */
    public static float calculateMountZoom(float currentZoom, float zoomOutSpeed, float delta) {
        return Math.min(1.0f, currentZoom + zoomOutSpeed * delta);
    }

    // --- Pause menu button bounds ---

    /**
     * Returns {@code true} if a touch point lies within a rectangular button region.
     *
     * @param touchX unprojected touch X in HUD/screen space
     * @param touchY unprojected touch Y in HUD/screen space
     * @param x1     left edge of the button
     * @param y1     bottom edge of the button
     * @param x2     right edge of the button
     * @param y2     top edge of the button
     * @return {@code true} if the touch point is inside the rectangle
     */
    public static boolean isInButtonBounds(float touchX, float touchY,
                                            float x1, float y1, float x2, float y2) {
        return touchX >= x1 && touchX <= x2 && touchY >= y1 && touchY <= y2;
    }

    // --- Spawn positions ---

    /**
     * Returns the player spawn position for a given map index.
     *
     * @param mapIndex   the map section index (0, 1, or 2)
     * @param mapYOffsets world-Y origins of each map section
     * @return the spawn position as a {@link Vector2}
     */
    public static Vector2 getPlayerSpawnPosition(int mapIndex, float[] mapYOffsets) {
        switch (mapIndex) {
            case 1: return new Vector2(5600f, mapYOffsets[1] + 100f);
            case 2: return new Vector2(1250f, mapYOffsets[2] + 100f);
            default: return new Vector2(300f, 300f);
        }
    }

    // --- Respawn eligibility ---

    /**
     * Returns {@code true} if applying the fish penalty would leave the player with fewer than
     * zero fish, triggering a game over.
     *
     * @param fishCollected current fish count before the penalty
     * @param fishPenalty   number of fish to deduct on respawn
     * @return {@code true} if {@code fishCollected - fishPenalty < 0}
     */
    public static boolean isRespawnGameOver(int fishCollected, int fishPenalty) {
        return fishCollected - fishPenalty < 0;
    }

    // --- Puddle overlap ---

    /**
     * Returns {@code true} if the entity is on a puddle (within 67 units of the puddle centre).
     *
     * @param puddleX world-centre X of the puddle
     * @param puddleY world-centre Y of the puddle
     * @param entityX world-centre X of the entity to test
     * @param entityY world-centre Y of the entity to test
     * @return {@code true} if the entity is within 67 units of the puddle centre
     */
    public static boolean isOnPuddle(float puddleX, float puddleY,
                                      float entityX, float entityY) {
        float dx = puddleX - entityX;
        float dy = puddleY - entityY;
        return Math.sqrt(dx * dx + dy * dy) < 67;
    }

    // --- Map boundary enforcement ---

    /**
     * Clamps the player's Y coordinate so they cannot move above the current map boundary.
     *
     * @param playerY current player Y position
     * @param mapTopY the maximum Y allowed (top boundary of the current map section)
     * @return the clamped Y position (80 units below {@code mapTopY} if exceeded)
     */
    public static float clampToMapBoundary(float playerY, float mapTopY) {
        return playerY > mapTopY ? mapTopY - 80f : playerY;
    }

    // --- Cinematic interpolation ---

    /**
     * Linear interpolation between two values.
     * @param start start value (returned when {@code t = 0})
     * @param end   end value (returned when {@code t = 1})
     * @param t     blend factor in [0, 1]
     * @return interpolated value
     */
    public static float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }

    /**
     * Interpolates between two values using smoothstep easing, useful for cinematic pans.
     * @param start    starting value
     * @param end      ending value
     * @param timer    elapsed time in the pan
     * @param duration total duration of the pan
     * @return eased interpolated value
     */
    public static float cinematicLerp(float start, float end, float timer, float duration) {
        float t = Math.max(0f, Math.min(timer / duration, 1f));
        float eased = smoothStep(t);
        return lerp(start, end, eased);
    }

    // --- Utility ---

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
