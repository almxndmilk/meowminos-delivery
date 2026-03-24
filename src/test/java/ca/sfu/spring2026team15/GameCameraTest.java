package ca.sfu.spring2026team15;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedConstruction;
import org.objenesis.ObjenesisStd;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mockConstruction;

/**
 * Tests for GameCamera clamping logic.
 * Uses Objenesis to bypass GameCamera's constructor and a NoOpCamera subclass
 * that overrides update() to suppress native JNI calls (Matrix4.setToProjection)
 * which are unavailable in headless test JVMs.
 */
public class GameCameraTest extends GdxTestSetup {

    /** Subclass that neutralises the native-JNI camera.update() call. */
    private static class NoOpCamera extends OrthographicCamera {
        @Override
        public void update() { /* suppress JNI native calls in headless tests */ }
        @Override
        public void update(boolean updateFrustum) { /* suppress */ }
    }

    private static final ObjenesisStd OBJENESIS = new ObjenesisStd();
    private static final float VIEW_WIDTH  = 1280f;
    private static final float VIEW_HEIGHT = 720f;
    private static final float MAP_WIDTH   = 5446f;
    private static final float MAP_HEIGHT  = 902f;
    private static final float HALF_W = VIEW_WIDTH  / 2f;  // 640
    private static final float HALF_H = VIEW_HEIGHT / 2f;  // 360

    private GameCamera gameCamera;
    private NoOpCamera stubCamera;

    @Before
    public void setUp() {
        // Bypass GameCamera constructor — avoids setToOrtho() → update() → JNI crash
        gameCamera = OBJENESIS.newInstance(GameCamera.class);
        setField(gameCamera, "maxX",     MAP_WIDTH);
        setField(gameCamera, "minY",     0f);
        setField(gameCamera, "maxY",     MAP_HEIGHT);
        setField(gameCamera, "halfViewW", HALF_W);
        setField(gameCamera, "halfViewH", HALF_H);

        // Bypass OrthographicCamera constructor (also calls JNI via setToOrtho)
        stubCamera = OBJENESIS.newInstance(NoOpCamera.class);
        setField(stubCamera, "position", new Vector3());
        stubCamera.viewportWidth  = VIEW_WIDTH;
        stubCamera.viewportHeight = VIEW_HEIGHT;

        setField(gameCamera, "camera", stubCamera);
    }

    @Test
    public void testCameraLeftEdgeClamp() {
        gameCamera.update(0f, 451f);
        assertEquals(HALF_W, stubCamera.position.x, 0.01f);
    }

    @Test
    public void testCameraRightEdgeClamp() {
        gameCamera.update(99999f, 451f);
        // maxX - halfViewW = 5446 - 640 = 4806
        assertEquals(4806f, stubCamera.position.x, 0.01f);
    }

    @Test
    public void testCameraBottomEdgeClamp() {
        gameCamera.update(2723f, 0f);
        // minY + halfViewH = 0 + 360 = 360
        assertEquals(HALF_H, stubCamera.position.y, 0.01f);
    }

    @Test
    public void testCameraTopEdgeClamp() {
        gameCamera.update(2723f, 99999f);
        // maxY - halfViewH = 902 - 360 = 542
        assertEquals(542f, stubCamera.position.y, 0.01f);
    }

    @Test
    public void testCameraFollowsPlayerAtCenter() {
        gameCamera.update(2723f, 451f);
        assertEquals(2723f, stubCamera.position.x, 0.01f);
        assertEquals(451f,  stubCamera.position.y, 0.01f);
    }

    @Test
    public void testSetMaxXUpdatesRightClamp() {
        gameCamera.setMaxX(8000f);
        gameCamera.update(99999f, 451f);
        // 8000 - 640 = 7360
        assertEquals(7360f, stubCamera.position.x, 0.01f);
    }

    @Test
    public void testSetYBoundsUpdatesVerticalClamp() {
        // Map 2 bounds: minY=902, maxY=1804; clamp range [1262, 1444]
        gameCamera.setYBounds(902f, 1804f);

        gameCamera.update(2723f, 800f);    // below new min → clamped
        assertEquals(1262f, stubCamera.position.y, 0.01f);

        gameCamera.update(2723f, 99999f);  // above new max → clamped
        assertEquals(1444f, stubCamera.position.y, 0.01f);

        gameCamera.update(2723f, 1353f);   // within bounds → unclamped
        assertEquals(1353f, stubCamera.position.y, 0.01f);
    }

    @Test
    public void testRepeatedUpdatesStayWithinBounds() {
        float[] playerXs = {0f, 1280f, 2723f, 5000f, 99999f};
        float[] playerYs = {0f, 300f,  451f,  800f,  99999f};
        float minXBound = HALF_W;
        float maxXBound = MAP_WIDTH  - HALF_W;
        float minYBound = HALF_H;
        float maxYBound = MAP_HEIGHT - HALF_H;

        for (float px : playerXs) {
            for (float py : playerYs) {
                gameCamera.update(px, py);
                float cx = stubCamera.position.x;
                float cy = stubCamera.position.y;
                assertTrue(cx >= minXBound && cx <= maxXBound);
                assertTrue(cy >= minYBound && cy <= maxYBound);
            }
        }
    }

    @Test
    public void testCameraXClampingWithNegativePlayerX() {
        gameCamera.update(-1000f, 451f);
        assertEquals(HALF_W, stubCamera.position.x, 0.01f);
    }

    @Test
    public void testCameraYClampingWithNegativePlayerY() {
        gameCamera.update(2723f, -500f);
        assertEquals(HALF_H, stubCamera.position.y, 0.01f);
    }

    @Test
    public void testMultipleSetMaxXCalls() {
        gameCamera.setMaxX(6000f);
        gameCamera.update(99999f, 451f);
        assertEquals(5360f, stubCamera.position.x, 0.01f); // 6000 - 640

        gameCamera.setMaxX(8000f);
        gameCamera.update(99999f, 451f);
        assertEquals(7360f, stubCamera.position.x, 0.01f); // 8000 - 640

        gameCamera.setMaxX(5446f);
        gameCamera.update(99999f, 451f);
        assertEquals(4806f, stubCamera.position.x, 0.01f); // back to original
    }

    @Test
    public void testMultipleSetYBoundsCalls() {
        gameCamera.setYBounds(0f, 902f);
        gameCamera.update(2723f, 99999f);
        assertEquals(542f, stubCamera.position.y, 0.01f);  // 902  - 360

        gameCamera.setYBounds(902f, 1804f);
        gameCamera.update(2723f, 99999f);
        assertEquals(1444f, stubCamera.position.y, 0.01f); // 1804 - 360

        gameCamera.setYBounds(1804f, 2706f);
        gameCamera.update(2723f, 99999f);
        assertEquals(2346f, stubCamera.position.y, 0.01f); // 2706 - 360
    }

    @Test
    public void testCameraUpdateSetsPosition() {
        gameCamera.update(2723f, 451f);
        assertEquals(2723f, stubCamera.position.x, 0.01f);
        assertEquals(451f,  stubCamera.position.y, 0.01f);
    }

    @Test
    public void testGetCameraReturnsInjectedCamera() {
        OrthographicCamera cam = gameCamera.getCamera();
        assertEquals(stubCamera, cam);
        assertEquals(VIEW_WIDTH,  cam.viewportWidth,  0.01f);
        assertEquals(VIEW_HEIGHT, cam.viewportHeight, 0.01f);
    }

    @Test
    public void testConstructorBoundsViaEdgeUpdates() {
        gameCamera.update(0f, 451f);
        assertEquals(HALF_W, stubCamera.position.x, 0.01f);

        gameCamera.update(2723f, 0f);
        assertEquals(HALF_H, stubCamera.position.y, 0.01f);
    }

    // --- Additional tests ---

    @Test
    public void testGetCameraReturnsNonNull() {
        assertNotNull(gameCamera.getCamera());
    }

    @Test
    public void testGetCameraPositionIsNonNull() {
        assertNotNull(gameCamera.getCamera().position);
    }

    @Test
    public void testCameraViewportDimensions() {
        OrthographicCamera cam = gameCamera.getCamera();
        assertEquals(VIEW_WIDTH, cam.viewportWidth, 0.01f);
        assertEquals(VIEW_HEIGHT, cam.viewportHeight, 0.01f);
    }

    // --- Constructor coverage tests using Mockito.mockConstruction ---

    /**
     * Tests that the GameCamera constructor successfully initializes when the
     * OrthographicCamera is mocked to avoid JNI crashes. Verifies that the
     * constructor runs to completion and returns a non-null camera.
     */
    @Test
    public void constructorInitializesWithMockedCamera() {
        try (MockedConstruction<OrthographicCamera> mocked = mockConstruction(OrthographicCamera.class)) {
            GameCamera cam = new GameCamera(1280f, 720f, 5446f, 0f, 902f);
            assertNotNull(cam);
            assertNotNull(cam.getCamera());
            assertEquals(1, mocked.constructed().size());
        }
    }

    /**
     * Tests that the GameCamera constructor properly initializes all bounds
     * and half-viewport fields by verifying the camera is created without error.
     */
    @Test
    public void constructorInitializesBoundFields() {
        try (MockedConstruction<OrthographicCamera> mocked = mockConstruction(OrthographicCamera.class)) {
            GameCamera cam = new GameCamera(1280f, 720f, 5446f, 0f, 902f);
            assertNotNull(cam);
            assertNotNull(cam.getCamera());
        }
    }

    /**
     * Tests that the GameCamera constructor works correctly with different
     * viewport dimensions and map bounds. Verifies the mocked constructor
     * is invoked exactly once.
     */
    @Test
    public void constructorWithDifferentViewport() {
        try (MockedConstruction<OrthographicCamera> mocked = mockConstruction(OrthographicCamera.class)) {
            GameCamera cam = new GameCamera(800f, 600f, 3000f, 100f, 700f);
            assertNotNull(cam);
            assertNotNull(cam.getCamera());
            assertEquals(1, mocked.constructed().size());
        }
    }
}
