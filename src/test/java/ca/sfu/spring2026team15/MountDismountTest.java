package ca.sfu.spring2026team15;


import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for the mount / dismount position transfer between Player and CatOffBike.
 * Covers: mount/dismount, Q to mount/dismount.
 *
 * The logic under test mirrors GameScreen:
 *   Dismount: catOffBike.setPosition(player.getCenterX(), player.getCenterY())
 *   Remount : player.setPosition(catOffBike.getCenterX(), catOffBike.getCenterY())
 */
public class MountDismountTest extends GdxTestSetup {

    private Player player;
    private CatOffBike catOffBike;

    @Before
    public void setUp() {
        player = new Player(400f, 300f, true);
        catOffBike = new CatOffBike(0f, 0f, true);
    }

    // --- single feature / interaction: Q to mount/dismount ---

    @Test
    public void dismountPlacesCatAtPlayerPosition() {
        // Simulate Q dismount: cat inherits player's position
        catOffBike.setPosition(player.getCenterX(), player.getCenterY());
        assertEquals(400f, catOffBike.getCenterX(), 0.01f);
        assertEquals(300f, catOffBike.getCenterY(), 0.01f);
    }

    @Test
    public void remountPlacesPlayerAtCatPosition() {
        // Move the off-bike cat to some different position
        catOffBike.setPosition(700f, 600f);
        // Simulate Q remount: player inherits cat's position
        player.setPosition(catOffBike.getCenterX(), catOffBike.getCenterY());
        assertEquals(700f, player.getCenterX(), 0.01f);
        assertEquals(600f, player.getCenterY(), 0.01f);
    }

    @Test
    public void dismountThenRemountPreservesPosition() {
        float startX = 350f;
        float startY = 250f;
        player.setPosition(startX, startY);

        // Dismount
        catOffBike.setPosition(player.getCenterX(), player.getCenterY());
        // Remount immediately
        player.setPosition(catOffBike.getCenterX(), catOffBike.getCenterY());

        assertEquals(startX, player.getCenterX(), 0.01f);
        assertEquals(startY, player.getCenterY(), 0.01f);
    }

    @Test
    public void multipleDismountsUpdateCatPosition() {
        player.setPosition(100f, 100f);
        catOffBike.setPosition(player.getCenterX(), player.getCenterY());

        player.setPosition(900f, 800f);
        catOffBike.setPosition(player.getCenterX(), player.getCenterY());

        assertEquals(900f, catOffBike.getCenterX(), 0.01f);
        assertEquals(800f, catOffBike.getCenterY(), 0.01f);
    }
}
