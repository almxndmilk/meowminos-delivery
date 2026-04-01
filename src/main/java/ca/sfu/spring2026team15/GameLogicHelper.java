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

    /** Smoothstep easing: starts and ends slow, fast in the middle. */
    public static float smoothStep(float t) {
        return t * t * (3f - 2f * t);
    }

    /** Calculate iris wipe radius from transition state. */
    public static float calculateIrisRadius(float halfDiag, float timer, float duration, boolean isFadeOut) {
        float t = Math.min(timer, duration) / duration;
        return isFadeOut ? halfDiag * (1f - t) : halfDiag * t;
    }

    /** Calculate the half-diagonal used for iris transition sizing. */
    public static float calculateHalfDiagonal(float viewWidth, float viewHeight) {
        return (float) Math.sqrt(
            (viewWidth / 2f) * (viewWidth / 2f) + (viewHeight / 2f) * (viewHeight / 2f)
        ) + 50f;
    }

    // --- Gate fade math ---

    /** Calculate the alpha for a gate obstacle fade overlay. */
    public static float calculateGateFadeAlpha(float timer, float duration, boolean isFadeOut) {
        return isFadeOut ? timer / duration : 1f - timer / duration;
    }

    // --- Gate obstacle collision ---

    /** Check if player is near a gate obstacle (within 50px vertically, within obstacle X bounds). */
    public static boolean isNearGateObstacle(float playerX, float playerY,
                                              float obstacleX, float obstacleY,
                                              float obstacleWidth) {
        float obBottom = obstacleY;
        float obRight = obstacleX + obstacleWidth;
        return playerY >= obBottom - 50f && playerY <= obBottom + 50f
            && playerX >= obstacleX && playerX <= obRight;
    }

    /** Calculate fish needed to clear a gate. Returns 0 if player has enough. */
    public static int fishNeededForGate(int fishCollected, int fishQuota) {
        return Math.max(0, fishQuota - fishCollected);
    }

    // --- Fish collection ---

    /** Check if a fish is within collection radius of the player. */
    public static boolean isFishInRange(float fishCenterX, float fishCenterY,
                                         float playerX, float playerY,
                                         float collectRadius) {
        float dx = fishCenterX - playerX;
        float dy = fishCenterY - playerY;
        return dx * dx + dy * dy < collectRadius * collectRadius;
    }

    // --- Fish spawning validation ---

    /** Validate that pixel coordinates are within pixmap bounds. */
    public static boolean isValidPixmapCoord(int pixelX, int pixelY, int pmWidth, int pmHeight) {
        return pixelX >= 0 && pixelX < pmWidth && pixelY >= 0 && pixelY < pmHeight;
    }

    /** Check if a pixel represents a road (alpha < 128). */
    public static boolean isRoadPixel(int pixel) {
        return (pixel & 0xFF) < 128;
    }

    // --- Camera clamping (cinematic) ---

    /**
     * Clamp camera position to map bounds at a given zoom level.
     * Returns float[2] = {clampedX, clampedY}.
     * When the zoomed view exceeds a dimension, the camera is centered on that axis.
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

    /** Get zero-padded 5-digit score array (capped 0-99999). */
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

    /** Get MM:SS digit array from total seconds. */
    public static int[] getTimeDigits(int totalSeconds) {
        int mm = Math.min(totalSeconds / 60, 99);
        int ss = totalSeconds % 60;
        return new int[]{mm / 10, mm % 10, ss / 10, ss % 10};
    }

    // --- Delivery management ---

    /** Count houses that still need delivery on a given map. */
    public static int deliveriesNeeded(List<House> houses) {
        int count = 0;
        for (House h : houses) {
            if (!h.wasDelivered()) count++;
        }
        return count;
    }

    // --- Whitehouse proximity check ---

    /** Check if player is in the whitehouse trigger zone on map 3. */
    public static boolean isNearWhitehouse(float playerX, float playerY,
                                            int currentMapIndex,
                                            float map3YOffset, float map3Height) {
        return currentMapIndex == 2
            && playerX >= 1080f && playerX <= 1775f
            && playerY >= map3YOffset + map3Height - 940f
            && playerY <= map3YOffset + map3Height - 860f;
    }

    // --- Intro cutscene helpers ---

    /** Check if catOffBike has arrived near the target (within 5px). */
    public static boolean isAtIntroTarget(float catX, float catY, float targetX, float targetY) {
        float dx = targetX - catX;
        float dy = targetY - catY;
        return dx * dx + dy * dy < 5f * 5f;
    }

    /** Calculate camera zoom during intro mount phase. */
    public static float calculateMountZoom(float currentZoom, float zoomOutSpeed, float delta) {
        return Math.min(1.0f, currentZoom + zoomOutSpeed * delta);
    }

    // --- Pause menu button bounds ---

    /** Check if a touch point is within a rectangular button region. */
    public static boolean isInButtonBounds(float touchX, float touchY,
                                            float x1, float y1, float x2, float y2) {
        return touchX >= x1 && touchX <= x2 && touchY >= y1 && touchY <= y2;
    }

    // --- Spawn positions ---

    /** Get player spawn position for a given map index as a single Vector2. */
    public static Vector2 getPlayerSpawnPosition(int mapIndex, float[] mapYOffsets) {
        switch (mapIndex) {
            case 1: return new Vector2(5600f, mapYOffsets[1] + 100f);
            case 2: return new Vector2(1250f, mapYOffsets[2] + 100f);
            default: return new Vector2(300f, 300f);
        }
    }

    // --- Respawn eligibility ---

    /** Returns true if respawn would cause game over (fish after penalty < 0). */
    public static boolean isRespawnGameOver(int fishCollected, int fishPenalty) {
        return fishCollected - fishPenalty < 0;
    }

    // --- Puddle overlap ---

    /** Check if player is on a puddle (distance < 67 from puddle center). */
    public static boolean isOnPuddle(float puddleX, float puddleY,
                                      float entityX, float entityY) {
        float dx = puddleX - entityX;
        float dy = puddleY - entityY;
        return Math.sqrt(dx * dx + dy * dy) < 67;
    }

    // --- Map boundary enforcement ---

    /** Clamp player Y to stay within the current map if gate is not cleared. */
    public static float clampToMapBoundary(float playerY, float mapTopY) {
        return playerY > mapTopY ? mapTopY - 80f : playerY;
    }

    // --- Cinematic interpolation ---

    /** Linear interpolation between two values. */
    public static float lerp(float start, float end, float t) {
        return start + (end - start) * t;
    }

    /** Calculate cinematic pan position using smoothstep easing. */
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
