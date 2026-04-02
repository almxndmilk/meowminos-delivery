package ca.sfu.spring2026team15;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import com.badlogic.gdx.math.Vector2;

/**
 * Tests for GameLogicHelper — pure logic extracted from GameScreen and EndScreen.
 */
public class GameLogicHelperTest {

    private static final float DELTA = 0.001f;

    // --- smoothStep ---
    @Test public void smoothStepAt0Returns0() { assertEquals(0f, GameLogicHelper.smoothStep(0f), DELTA); }
    @Test public void smoothStepAt1Returns1() { assertEquals(1f, GameLogicHelper.smoothStep(1f), DELTA); }
    @Test public void smoothStepAtHalfReturnsHalf() { assertEquals(0.5f, GameLogicHelper.smoothStep(0.5f), DELTA); }
    @Test public void smoothStepAt025IsLessThan025() { assertTrue(GameLogicHelper.smoothStep(0.25f) < 0.25f); }
    @Test public void smoothStepAt075IsGreaterThan075() { assertTrue(GameLogicHelper.smoothStep(0.75f) > 0.75f); }
    @Test public void smoothStepIsMonotonic() {
        for (float t = 0f; t < 0.95f; t += 0.1f) {
            assertTrue(GameLogicHelper.smoothStep(t + 0.05f) >= GameLogicHelper.smoothStep(t));
        }
    }
    @Test public void smoothStepSymmetricAroundHalf() {
        // smoothStep(0.25) + smoothStep(0.75) should equal 1
        assertEquals(1f, GameLogicHelper.smoothStep(0.25f) + GameLogicHelper.smoothStep(0.75f), DELTA);
    }

    // --- calculateIrisRadius ---
    @Test public void irisRadiusFadeOutAtStart() {
        assertEquals(100f, GameLogicHelper.calculateIrisRadius(100f, 0f, 1.2f, true), DELTA);
    }
    @Test public void irisRadiusFadeOutAtEnd() {
        assertEquals(0f, GameLogicHelper.calculateIrisRadius(100f, 1.2f, 1.2f, true), DELTA);
    }
    @Test public void irisRadiusFadeInAtStart() {
        assertEquals(0f, GameLogicHelper.calculateIrisRadius(100f, 0f, 1.2f, false), DELTA);
    }
    @Test public void irisRadiusFadeInAtEnd() {
        assertEquals(100f, GameLogicHelper.calculateIrisRadius(100f, 1.2f, 1.2f, false), DELTA);
    }
    @Test public void irisRadiusFadeOutMidway() {
        assertEquals(50f, GameLogicHelper.calculateIrisRadius(100f, 0.6f, 1.2f, true), DELTA);
    }
    @Test public void irisRadiusFadeInMidway() {
        assertEquals(50f, GameLogicHelper.calculateIrisRadius(100f, 0.6f, 1.2f, false), DELTA);
    }
    @Test public void irisRadiusClampsTimerToMax() {
        assertEquals(0f, GameLogicHelper.calculateIrisRadius(100f, 5f, 1.2f, true), DELTA);
    }
    @Test public void irisRadiusFadeInClampsTimer() {
        assertEquals(100f, GameLogicHelper.calculateIrisRadius(100f, 5f, 1.2f, false), DELTA);
    }

    // --- calculateHalfDiagonal ---
    @Test public void halfDiagonalFor1280x720() {
        float expected = (float) Math.sqrt(640f * 640f + 360f * 360f) + 50f;
        assertEquals(expected, GameLogicHelper.calculateHalfDiagonal(1280f, 720f), DELTA);
    }
    @Test public void halfDiagonalForSquareViewport() {
        float expected = (float) Math.sqrt(500f * 500f + 500f * 500f) + 50f;
        assertEquals(expected, GameLogicHelper.calculateHalfDiagonal(1000f, 1000f), DELTA);
    }
    @Test public void halfDiagonalAlwaysPositive() {
        assertTrue(GameLogicHelper.calculateHalfDiagonal(100f, 100f) > 0f);
    }

    // --- calculateGateFadeAlpha ---
    @Test public void gateFadeOutStart() { assertEquals(0f, GameLogicHelper.calculateGateFadeAlpha(0f, 1f, true), DELTA); }
    @Test public void gateFadeOutEnd() { assertEquals(1f, GameLogicHelper.calculateGateFadeAlpha(1f, 1f, true), DELTA); }
    @Test public void gateFadeInStart() { assertEquals(1f, GameLogicHelper.calculateGateFadeAlpha(0f, 1f, false), DELTA); }
    @Test public void gateFadeInEnd() { assertEquals(0f, GameLogicHelper.calculateGateFadeAlpha(1f, 1f, false), DELTA); }
    @Test public void gateFadeMidway() { assertEquals(0.5f, GameLogicHelper.calculateGateFadeAlpha(0.5f, 1f, true), DELTA); }
    @Test public void gateFadeInMidway() { assertEquals(0.5f, GameLogicHelper.calculateGateFadeAlpha(0.5f, 1f, false), DELTA); }

    // --- isNearGateObstacle ---
    @Test public void nearGateObstacleCenter() {
        assertTrue(GameLogicHelper.isNearGateObstacle(3800f, 850f, 3500f, 860f, 500f));
    }
    @Test public void nearGateObstacleTooFarBelow() {
        assertFalse(GameLogicHelper.isNearGateObstacle(3800f, 700f, 3500f, 860f, 500f));
    }
    @Test public void nearGateObstacleLeftOfObstacle() {
        assertFalse(GameLogicHelper.isNearGateObstacle(3000f, 860f, 3500f, 860f, 500f));
    }
    @Test public void nearGateObstacleRightOfObstacle() {
        assertFalse(GameLogicHelper.isNearGateObstacle(4200f, 860f, 3500f, 860f, 500f));
    }
    @Test public void nearGateObstacleJustBelow50px() {
        assertTrue(GameLogicHelper.isNearGateObstacle(3700f, 815f, 3500f, 860f, 500f));
    }
    @Test public void nearGateObstacleJustAbove50px() {
        assertTrue(GameLogicHelper.isNearGateObstacle(3700f, 905f, 3500f, 860f, 500f));
    }
    @Test public void nearGateObstacleAtLeftEdge() {
        assertTrue(GameLogicHelper.isNearGateObstacle(3500f, 860f, 3500f, 860f, 500f));
    }
    @Test public void nearGateObstacleAtRightEdge() {
        assertTrue(GameLogicHelper.isNearGateObstacle(4000f, 860f, 3500f, 860f, 500f));
    }

    // --- fishNeededForGate ---
    @Test public void fishNeededWhenNotEnough() { assertEquals(20, GameLogicHelper.fishNeededForGate(10, 30)); }
    @Test public void fishNeededWhenExactly() { assertEquals(0, GameLogicHelper.fishNeededForGate(30, 30)); }
    @Test public void fishNeededWhenExcess() { assertEquals(0, GameLogicHelper.fishNeededForGate(50, 30)); }
    @Test public void fishNeededWhenZeroCollected() { assertEquals(30, GameLogicHelper.fishNeededForGate(0, 30)); }
    @Test public void fishNeededGate2() { assertEquals(15, GameLogicHelper.fishNeededForGate(5, 20)); }

    // --- isFishInRange ---
    @Test public void fishInRangeAtSamePosition() {
        assertTrue(GameLogicHelper.isFishInRange(100f, 100f, 100f, 100f, 60f));
    }
    @Test public void fishInRangeJustInside() {
        assertTrue(GameLogicHelper.isFishInRange(150f, 100f, 100f, 100f, 60f));
    }
    @Test public void fishOutOfRange() {
        assertFalse(GameLogicHelper.isFishInRange(200f, 200f, 100f, 100f, 60f));
    }
    @Test public void fishExactlyAtRadiusBoundary() {
        assertFalse(GameLogicHelper.isFishInRange(160f, 100f, 100f, 100f, 60f));
    }
    @Test public void fishInRangeDiagonal() {
        // 40,40 distance = sqrt(3200) ≈ 56.6 < 60
        assertTrue(GameLogicHelper.isFishInRange(140f, 140f, 100f, 100f, 60f));
    }
    @Test public void fishOutOfRangeDiagonal() {
        // 45,45 distance = sqrt(4050) ≈ 63.6 > 60
        assertFalse(GameLogicHelper.isFishInRange(145f, 145f, 100f, 100f, 60f));
    }

    // --- isValidPixmapCoord ---
    @Test public void validCoordCenter() { assertTrue(GameLogicHelper.isValidPixmapCoord(50, 50, 100, 100)); }
    @Test public void validCoordOrigin() { assertTrue(GameLogicHelper.isValidPixmapCoord(0, 0, 100, 100)); }
    @Test public void validCoordMaxValid() { assertTrue(GameLogicHelper.isValidPixmapCoord(99, 99, 100, 100)); }
    @Test public void invalidCoordNegativeX() { assertFalse(GameLogicHelper.isValidPixmapCoord(-1, 50, 100, 100)); }
    @Test public void invalidCoordNegativeY() { assertFalse(GameLogicHelper.isValidPixmapCoord(50, -1, 100, 100)); }
    @Test public void invalidCoordXAtWidth() { assertFalse(GameLogicHelper.isValidPixmapCoord(100, 50, 100, 100)); }
    @Test public void invalidCoordYAtHeight() { assertFalse(GameLogicHelper.isValidPixmapCoord(50, 100, 100, 100)); }

    // --- isRoadPixel ---
    @Test public void roadPixelAlpha0() { assertTrue(GameLogicHelper.isRoadPixel(0x00)); }
    @Test public void roadPixelAlpha127() { assertTrue(GameLogicHelper.isRoadPixel(0x7F)); }
    @Test public void notRoadPixelAlpha128() { assertFalse(GameLogicHelper.isRoadPixel(0x80)); }
    @Test public void notRoadPixelAlpha255() { assertFalse(GameLogicHelper.isRoadPixel(0xFF)); }
    @Test public void roadPixelWithHigherBytes() { assertTrue(GameLogicHelper.isRoadPixel(0xFFFFFF00)); }
    @Test public void notRoadPixelFullWhite() { assertFalse(GameLogicHelper.isRoadPixel(0xFFFFFFFF)); }

    // --- clampCameraToMapBounds ---
    @Test public void clampCameraCenter() {
        float[] r = GameLogicHelper.clampCameraToMapBounds(2723f, 1353f, 1280f, 720f, 1f, 902f, 1804f, 5371f);
        assertEquals(2723f, r[0], DELTA);
        assertEquals(1353f, r[1], DELTA);
    }
    @Test public void clampCameraLeftEdge() {
        float[] r = GameLogicHelper.clampCameraToMapBounds(0f, 1353f, 1280f, 720f, 1f, 902f, 1804f, 5371f);
        assertEquals(640f, r[0], DELTA);
    }
    @Test public void clampCameraRightEdge() {
        float[] r = GameLogicHelper.clampCameraToMapBounds(99999f, 1353f, 1280f, 720f, 1f, 902f, 1804f, 5371f);
        assertEquals(5371f - 640f, r[0], DELTA);
    }
    @Test public void clampCameraTopEdge() {
        float[] r = GameLogicHelper.clampCameraToMapBounds(2723f, 99999f, 1280f, 720f, 1f, 902f, 1804f, 5371f);
        assertEquals(1444f, r[1], DELTA);
    }
    @Test public void clampCameraBottomEdge() {
        float[] r = GameLogicHelper.clampCameraToMapBounds(2723f, 0f, 1280f, 720f, 1f, 902f, 1804f, 5371f);
        assertEquals(1262f, r[1], DELTA);
    }
    @Test public void clampCameraViewWiderThanMap() {
        float[] r = GameLogicHelper.clampCameraToMapBounds(100f, 1353f, 1280f, 720f, 10f, 902f, 1804f, 5371f);
        assertEquals(5371f / 2f, r[0], DELTA);
    }
    @Test public void clampCameraViewTallerThanMap() {
        float[] r = GameLogicHelper.clampCameraToMapBounds(2723f, 1353f, 1280f, 720f, 10f, 902f, 1804f, 5371f);
        assertEquals((902f + 1804f) / 2f, r[1], DELTA);
    }

    // --- getScoreDigits ---
    @Test public void scoreDigits0() { assertArrayEquals(new int[]{0,0,0,0,0}, GameLogicHelper.getScoreDigits(0)); }
    @Test public void scoreDigits12345() { assertArrayEquals(new int[]{1,2,3,4,5}, GameLogicHelper.getScoreDigits(12345)); }
    @Test public void scoreDigitsMaxCap() { assertArrayEquals(new int[]{9,9,9,9,9}, GameLogicHelper.getScoreDigits(100000)); }
    @Test public void scoreDigitsNegative() { assertArrayEquals(new int[]{0,0,0,0,0}, GameLogicHelper.getScoreDigits(-5)); }
    @Test public void scoreDigits42() { assertArrayEquals(new int[]{0,0,0,4,2}, GameLogicHelper.getScoreDigits(42)); }
    @Test public void scoreDigits99999() { assertArrayEquals(new int[]{9,9,9,9,9}, GameLogicHelper.getScoreDigits(99999)); }
    @Test public void scoreDigits10000() { assertArrayEquals(new int[]{1,0,0,0,0}, GameLogicHelper.getScoreDigits(10000)); }

    // --- getTimeDigits ---
    @Test public void timeDigits0Seconds() { assertArrayEquals(new int[]{0,0,0,0}, GameLogicHelper.getTimeDigits(0)); }
    @Test public void timeDigits5Minutes() { assertArrayEquals(new int[]{0,5,0,0}, GameLogicHelper.getTimeDigits(300)); }
    @Test public void timeDigits2Min10Sec() { assertArrayEquals(new int[]{0,2,1,0}, GameLogicHelper.getTimeDigits(130)); }
    @Test public void timeDigits1Min1Sec() { assertArrayEquals(new int[]{0,1,0,1}, GameLogicHelper.getTimeDigits(61)); }
    @Test public void timeDigitsCapsAt99Min() { assertArrayEquals(new int[]{9,9,5,9}, GameLogicHelper.getTimeDigits(99*60+59)); }
    @Test public void timeDigitsOverflow() {
        assertArrayEquals(new int[]{9,9,0,0}, GameLogicHelper.getTimeDigits(6000));
    }

    // --- isNearWhitehouse ---
    @Test public void nearWhitehouseCorrectPosition() {
        assertTrue(GameLogicHelper.isNearWhitehouse(1400f, 2000f, 2, 1200f, 1900f, 1600f, 2100f));
    }
    @Test public void nearWhitehouseWrongMap() {
        assertFalse(GameLogicHelper.isNearWhitehouse(1400f, 2000f, 1, 1200f, 1900f, 1600f, 2100f));
    }
    @Test public void nearWhitehouseMap0() {
        assertFalse(GameLogicHelper.isNearWhitehouse(1400f, 2000f, 0, 1200f, 1900f, 1600f, 2100f));
    }
    @Test public void nearWhitehouseTooFarLeft() {
        assertFalse(GameLogicHelper.isNearWhitehouse(1000f, 2000f, 2, 1200f, 1900f, 1600f, 2100f));
    }
    @Test public void nearWhitehouseTooFarRight() {
        assertFalse(GameLogicHelper.isNearWhitehouse(1700f, 2000f, 2, 1200f, 1900f, 1600f, 2100f));
    }
    @Test public void nearWhitehouseTooHigh() {
        assertFalse(GameLogicHelper.isNearWhitehouse(1400f, 2200f, 2, 1200f, 1900f, 1600f, 2100f));
    }
    @Test public void nearWhitehouseTooLow() {
        assertFalse(GameLogicHelper.isNearWhitehouse(1400f, 1800f, 2, 1200f, 1900f, 1600f, 2100f));
    }

    // --- isAtIntroTarget ---
    @Test public void atIntroTargetExact() { assertTrue(GameLogicHelper.isAtIntroTarget(100f, 100f, 100f, 100f)); }
    @Test public void atIntroTargetClose() { assertTrue(GameLogicHelper.isAtIntroTarget(97f, 97f, 100f, 100f)); }
    @Test public void notAtIntroTargetFar() { assertFalse(GameLogicHelper.isAtIntroTarget(90f, 90f, 100f, 100f)); }
    @Test public void atIntroTargetJustInside5px() {
        // 3,4 distance = 5 → dx*dx+dy*dy = 25, NOT < 25
        assertFalse(GameLogicHelper.isAtIntroTarget(97f, 96f, 100f, 100f));
    }
    @Test public void atIntroTargetJustUnder5px() {
        // 3,3 distance = sqrt(18) ≈ 4.24 < 5
        assertTrue(GameLogicHelper.isAtIntroTarget(97f, 97f, 100f, 100f));
    }

    // --- calculateMountZoom ---
    @Test public void mountZoomIncrements() {
        assertEquals(0.65f, GameLogicHelper.calculateMountZoom(0.5f, 1.5f, 0.1f), DELTA);
    }
    @Test public void mountZoomCapsAt1() {
        assertEquals(1.0f, GameLogicHelper.calculateMountZoom(0.9f, 1.5f, 1f), DELTA);
    }
    @Test public void mountZoomAlreadyAt1() {
        assertEquals(1.0f, GameLogicHelper.calculateMountZoom(1.0f, 1.5f, 0.1f), DELTA);
    }
    @Test public void mountZoomSmallDelta() {
        assertEquals(0.515f, GameLogicHelper.calculateMountZoom(0.5f, 1.5f, 0.01f), DELTA);
    }

    // --- isInButtonBounds ---
    @Test public void buttonBoundsCenter() {
        assertTrue(GameLogicHelper.isInButtonBounds(635f, 395f, 520f, 345f, 750f, 445f));
    }
    @Test public void buttonBoundsOutsideLeft() {
        assertFalse(GameLogicHelper.isInButtonBounds(500f, 395f, 520f, 345f, 750f, 445f));
    }
    @Test public void buttonBoundsOutsideRight() {
        assertFalse(GameLogicHelper.isInButtonBounds(760f, 395f, 520f, 345f, 750f, 445f));
    }
    @Test public void buttonBoundsOutsideAbove() {
        assertFalse(GameLogicHelper.isInButtonBounds(635f, 450f, 520f, 345f, 750f, 445f));
    }
    @Test public void buttonBoundsOutsideBelow() {
        assertFalse(GameLogicHelper.isInButtonBounds(635f, 340f, 520f, 345f, 750f, 445f));
    }
    @Test public void buttonBoundsAtLeftEdge() {
        assertTrue(GameLogicHelper.isInButtonBounds(520f, 395f, 520f, 345f, 750f, 445f));
    }
    @Test public void buttonBoundsAtRightEdge() {
        assertTrue(GameLogicHelper.isInButtonBounds(750f, 395f, 520f, 345f, 750f, 445f));
    }
    @Test public void buttonBoundsAtTopEdge() {
        assertTrue(GameLogicHelper.isInButtonBounds(635f, 445f, 520f, 345f, 750f, 445f));
    }
    @Test public void buttonBoundsAtBottomEdge() {
        assertTrue(GameLogicHelper.isInButtonBounds(635f, 345f, 520f, 345f, 750f, 445f));
    }
    @Test public void buttonBoundsQuitButton() {
        assertTrue(GameLogicHelper.isInButtonBounds(635f, 185f, 540f, 135f, 730f, 235f));
    }
    @Test public void buttonBoundsQuitButtonOutside() {
        assertFalse(GameLogicHelper.isInButtonBounds(535f, 185f, 540f, 135f, 730f, 235f));
    }

    // --- getPlayerSpawnPosition ---
    @Test public void spawnPositionMap0() {
        float[] offsets = {0f, 902f, 1804f};
        Vector2 pos = GameLogicHelper.getPlayerSpawnPosition(0, offsets);
        assertEquals(300f, pos.x, DELTA);
        assertEquals(300f, pos.y, DELTA);
    }
    @Test public void spawnPositionMap1() {
        float[] offsets = {0f, 902f, 1804f};
        Vector2 pos = GameLogicHelper.getPlayerSpawnPosition(1, offsets);
        assertEquals(5600f, pos.x, DELTA);
        assertEquals(1002f, pos.y, DELTA);
    }
    @Test public void spawnPositionMap2() {
        float[] offsets = {0f, 902f, 1804f};
        Vector2 pos = GameLogicHelper.getPlayerSpawnPosition(2, offsets);
        assertEquals(1250f, pos.x, DELTA);
        assertEquals(1904f, pos.y, DELTA);
    }
    @Test public void spawnPositionDefault() {
        float[] offsets = {0f, 902f, 1804f};
        Vector2 pos = GameLogicHelper.getPlayerSpawnPosition(99, offsets);
        assertEquals(300f, pos.x, DELTA);
        assertEquals(300f, pos.y, DELTA);
    }

    // --- isRespawnGameOver ---
    @Test public void respawnGameOverWhenFishBelowPenalty() {
        assertTrue(GameLogicHelper.isRespawnGameOver(5, 10));
    }
    @Test public void respawnNotGameOverWhenFishEqualPenalty() {
        assertFalse(GameLogicHelper.isRespawnGameOver(10, 10));
    }
    @Test public void respawnNotGameOverWhenFishAbovePenalty() {
        assertFalse(GameLogicHelper.isRespawnGameOver(20, 10));
    }
    @Test public void respawnGameOverWhenZeroFishAndPenalty() {
        assertTrue(GameLogicHelper.isRespawnGameOver(0, 10));
    }

    // --- isOnPuddle ---
    @Test public void onPuddleSamePosition() {
        assertTrue(GameLogicHelper.isOnPuddle(100f, 100f, 100f, 100f));
    }
    @Test public void onPuddleJustInside() {
        assertTrue(GameLogicHelper.isOnPuddle(100f, 100f, 166f, 100f));
    }
    @Test public void onPuddleJustOutside() {
        assertFalse(GameLogicHelper.isOnPuddle(100f, 100f, 167f, 100f));
    }
    @Test public void onPuddleDiagonal() {
        // 47,47 distance ≈ 66.5 < 67
        assertTrue(GameLogicHelper.isOnPuddle(100f, 100f, 147f, 147f));
    }
    @Test public void onPuddleOutOfRange() {
        assertFalse(GameLogicHelper.isOnPuddle(100f, 100f, 200f, 200f));
    }

    // --- clampToMapBoundary ---
    @Test public void clampWithinMap() {
        assertEquals(500f, GameLogicHelper.clampToMapBoundary(500f, 902f), DELTA);
    }
    @Test public void clampAboveMap() {
        assertEquals(822f, GameLogicHelper.clampToMapBoundary(950f, 902f), DELTA);
    }
    @Test public void clampExactlyAtBoundary() {
        assertEquals(902f, GameLogicHelper.clampToMapBoundary(902f, 902f), DELTA);
    }

    // --- lerp ---
    @Test public void lerpAt0() { assertEquals(10f, GameLogicHelper.lerp(10f, 20f, 0f), DELTA); }
    @Test public void lerpAt1() { assertEquals(20f, GameLogicHelper.lerp(10f, 20f, 1f), DELTA); }
    @Test public void lerpAtHalf() { assertEquals(15f, GameLogicHelper.lerp(10f, 20f, 0.5f), DELTA); }
    @Test public void lerpNegativeRange() { assertEquals(-5f, GameLogicHelper.lerp(0f, -10f, 0.5f), DELTA); }

    // --- cinematicLerp ---
    @Test public void cinematicLerpAtStart() {
        assertEquals(100f, GameLogicHelper.cinematicLerp(100f, 500f, 0f, 2.5f), DELTA);
    }
    @Test public void cinematicLerpAtEnd() {
        assertEquals(500f, GameLogicHelper.cinematicLerp(100f, 500f, 2.5f, 2.5f), DELTA);
    }
    @Test public void cinematicLerpAtMidpoint() {
        assertEquals(300f, GameLogicHelper.cinematicLerp(100f, 500f, 1.25f, 2.5f), DELTA);
    }
    @Test public void cinematicLerpClampsOverflow() {
        assertEquals(500f, GameLogicHelper.cinematicLerp(100f, 500f, 5f, 2.5f), DELTA);
    }
    @Test public void cinematicLerpClampsNegativeTimer() {
        assertEquals(100f, GameLogicHelper.cinematicLerp(100f, 500f, -1f, 2.5f), DELTA);
    }
}
