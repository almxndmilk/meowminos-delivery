package ca.sfu.spring2026team15;

import com.badlogic.gdx.math.Vector2;

/**
 * 
 * Replaces the pattern of four separate float constants
 * (X1, X2, Y1, Y2) that always travel together as a data clump
 *
 * 
 */
public final class HitZone {

    private final float x1, x2, y1, y2;

    /**
     * Creates a rectangular hit zone from two opposite corner coordinates.
     *
     * @param x1 left edge (world coords)
     * @param x2 right edge (world coords)
     * @param y1 bottom edge (world coords, Y-up)
     * @param y2 top edge (world coords, Y-up)
     */
    public HitZone(float x1, float x2, float y1, float y2) {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
    }

    /**
     * Returns true if the given point lies inside this zone (inclusive on all edges).
     *
     * @param point unprojected world-space touch/click position
     * @return {@code true} if the point is within the zone bounds
     */
    public boolean contains(Vector2 point) {
        return point.x >= x1 && point.x <= x2
            && point.y >= y1 && point.y <= y2;
    }
}