package ca.sfu.spring2026team15;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for ObamaDialogueHelper
 * Tests dialogue reveal timing, whitehouse proximity detection,
 * and police catch/detection range logic extracted from ObamaScreen,
 * GameScreen, and PoliceEnemy
 */
public class ObamaDialogueHelperTest {

    // ─── Dialogue reveal tests ───────────────────────────────────────────────

    /**
     * At time 0, no words should be shown yet
     */
    @Test
    public void testNoWordsAtTimeZero() {
        String result = ObamaDialogueHelper.getProgressiveDialogue(0f);
        assertEquals("", result);
    }

    /**
     * After the full duration, all words should be visible
     */
    @Test
    public void testAllWordsShownAtFullDuration() {
        int total = ObamaDialogueHelper.getWordCount();
        int shown = ObamaDialogueHelper.getWordsToShow(
                ObamaDialogueHelper.TALK_FALLBACK_DURATION);
        assertEquals(total, shown);
    }

    /**
     * After more time than the duration, word count should not exceed total words
     */
    @Test
    public void testWordsDoNotExceedTotalAfterLongTime() {
        int total = ObamaDialogueHelper.getWordCount();
        int shown = ObamaDialogueHelper.getWordsToShow(9999f);
        assertEquals(total, shown);
    }

    /**
     * At half the duration, roughly half the words should be shown
     */
    @Test
    public void testHalfwayShowsRoughlyHalfWords() {
        int total = ObamaDialogueHelper.getWordCount();
        int shown = ObamaDialogueHelper.getWordsToShow(
                ObamaDialogueHelper.TALK_FALLBACK_DURATION / 2f);
        assertTrue(shown > 0);
        assertTrue(shown < total);
    }

    /**
     * The dialogue string should not be empty after some time has passed
     */
    @Test
    public void testDialogueNotEmptyAfterSomeTime() {
        String result = ObamaDialogueHelper.getProgressiveDialogue(3f);
        assertFalse(result.isEmpty());
    }

    /**
     * The dialogue should start with "Thank" (the first word)
     */
    @Test
    public void testDialogueStartsWithFirstWord() {
        String result = ObamaDialogueHelper.getProgressiveDialogue(3f);
        assertTrue(result.startsWith("Thank"));
    }

    /**
     * More time elapsed should show more words than less time
     */
    @Test
    public void testMoreTimeShowsMoreWords() {
        int wordsAt3 = ObamaDialogueHelper.getWordsToShow(3f);
        int wordsAt6 = ObamaDialogueHelper.getWordsToShow(6f);
        assertTrue(wordsAt6 > wordsAt3);
    }

    /**
     * The dialogue word count should be greater than zero
     */
    @Test
    public void testWordCountIsPositive() {
        assertTrue(ObamaDialogueHelper.getWordCount() > 0);
    }

    // ─── Whitehouse proximity tests ──────────────────────────────────────────

    /**
     * Player inside the whitehouse trigger zone should return true
     */
    @Test
    public void testPlayerInsideWhitehouseZone() {
        float map3YOffset = 1804f;
        float map3Height  = 902f;
        float px = 1400f;
        float py = map3YOffset + map3Height - 900f;
        assertTrue(ObamaDialogueHelper.isPlayerNearWhitehouse(
                px, py, 2, map3YOffset, map3Height));
    }

    /**
     * Player outside the whitehouse trigger zone on the X axis should return false
     */
    @Test
    public void testPlayerOutsideWhitehouseZoneX() {
        float map3YOffset = 1804f;
        float map3Height  = 902f;
        float px = 100f; // too far left
        float py = map3YOffset + map3Height - 900f;
        assertFalse(ObamaDialogueHelper.isPlayerNearWhitehouse(
                px, py, 2, map3YOffset, map3Height));
    }

    /**
     * Player far below the whitehouse trigger zone should return false
     */
    @Test
    public void testPlayerOutsideWhitehouseZoneY() {
        float map3YOffset = 1804f;
        float map3Height  = 902f;
        float px = 1400f;
        float py = 1700f; // below 1766, clearly outside the zone
        assertFalse(ObamaDialogueHelper.isPlayerNearWhitehouse(
                px, py, 2, map3YOffset, map3Height));
    }

    /**
     * Player on the wrong map should return false even if coordinates match
     */
    @Test
    public void testPlayerOnWrongMapNotNearWhitehouse() {
        float map3YOffset = 1804f;
        float map3Height  = 902f;
        float px = 1400f;
        float py = map3YOffset + map3Height - 900f;
        assertFalse(ObamaDialogueHelper.isPlayerNearWhitehouse(
                px, py, 0, map3YOffset, map3Height)); // map 0, not 2
    }

    /**
     * Player at the left edge of the whitehouse zone should return true.
     */
    @Test
    public void testPlayerAtLeftEdgeOfWhitehouseZone() {
        float map3YOffset = 1804f;
        float map3Height  = 902f;
        float px = 1080f; // exactly left boundary
        float py = map3YOffset + map3Height - 900f;
        assertTrue(ObamaDialogueHelper.isPlayerNearWhitehouse(
                px, py, 2, map3YOffset, map3Height));
    }

    // ─── Police catch distance tests ─────────────────────────────────────────

    /**
     * Police exactly on the player should be catching them.
     */
    @Test
    public void testPoliceAtSamePositionIsCatching() {
        assertTrue(ObamaDialogueHelper.isWithinCatchDistance(
                100f, 100f, 100f, 100f));
    }

    /**
     * Police 74 pixels away should be catching the player.
     */
    @Test
    public void testPolice74pxAwayIsCatching() {
        assertTrue(ObamaDialogueHelper.isWithinCatchDistance(
                100f, 100f, 174f, 100f));
    }

    /**
     * Police exactly 75 pixels away should not be catching — strict less-than.
     */
    @Test
    public void testPoliceExactly75pxNotCatching() {
        assertFalse(ObamaDialogueHelper.isWithinCatchDistance(
                100f, 100f, 175f, 100f));
    }

    /**
     * Police 76 pixels away should not be catching the player.
     */
    @Test
    public void testPolice76pxAwayNotCatching() {
        assertFalse(ObamaDialogueHelper.isWithinCatchDistance(
                100f, 100f, 176f, 100f));
    }

    // ─── Police detection range tests ────────────────────────────────────────

    /**
     * Police within 400 pixels should detect the player.
     */
    @Test
    public void testPoliceWithin400pxDetectsPlayer() {
        assertTrue(ObamaDialogueHelper.isWithinDetectionRange(
                100f, 100f, 400f, 100f)); // 300px away
    }

    /**
     * Police exactly 400 pixels away should still detect the player.
     */
    @Test
    public void testPoliceExactly400pxDetectsPlayer() {
        assertTrue(ObamaDialogueHelper.isWithinDetectionRange(
                100f, 100f, 500f, 100f)); // exactly 400px away
    }

    /**
     * Police more than 400 pixels away should not detect the player.
     */
    @Test
    public void testPoliceBeyond400pxNoDetection() {
        assertFalse(ObamaDialogueHelper.isWithinDetectionRange(
                100f, 100f, 600f, 100f)); // 500px away
    }

    /**
     * Police at the same position as the player should detect them.
     */
    @Test
    public void testPoliceAtSamePositionDetectsPlayer() {
        assertTrue(ObamaDialogueHelper.isWithinDetectionRange(
                100f, 100f, 100f, 100f));
    }
}
