package ca.sfu.spring2026team15;

/**
 * Functional interface for pixel-perfect barrier collision detection against a map's barrier pixmap.
 *
 * <p>The production implementation in {@link GameScreen} samples the appropriate barrier pixmap
 * for the given world position and returns {@code true} if the pixel alpha is &ge; 128 (barrier)
 * or if the position is outside the pixmap bounds.
 * Test implementations can provide simplified barriers (e.g. always open or always blocked).
 */
@FunctionalInterface
public interface BarrierLookup {
    /**
     * Returns {@code true} if the given world coordinate falls on a solid barrier tile.
     *
     * @param worldX world X coordinate to test
     * @param worldY world Y coordinate to test
     * @return {@code true} if movement into this position should be blocked
     */
    boolean isBarrier(float worldX, float worldY);
}
