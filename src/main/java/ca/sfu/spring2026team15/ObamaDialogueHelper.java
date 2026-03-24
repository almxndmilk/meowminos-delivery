package ca.sfu.spring2026team15;

/**
 * Helper class containing pure logic extracted from ObamaScreen.
 * Handles progressive dialogue text reveal calculation.
 * Separated from ObamaScreen so it can be unit tested without LibGDX.
 */
public class ObamaDialogueHelper {

    /** The full dialogue spoken by Obama during the cutscene. */
    public static final String DIALOGUE =
            "Thank you for the pizza kind, citizen. You are doing a great job with "
                    + "delivering this pizza. I love Meowmino's pizza. It's the best pizza in the "
                    + "whole town. Anyways, here is your tip. Thank you for getting here in time. "
                    + "I am going to return to my presidential duties.";

    /** Total fallback duration in seconds for the dialogue. */
    public static final float TALK_FALLBACK_DURATION = 10f;

    /**
     * Returns how much of the dialogue should be visible based on elapsed time.
     *
     * @param dialogueTimer how many seconds have passed since dialogue started
     * @return the partial dialogue string to display
     */
    public static String getProgressiveDialogue(float dialogueTimer) {
        String[] words = DIALOGUE.split(" ");
        float wordsPerSecond = words.length / TALK_FALLBACK_DURATION;
        int wordsToShow = Math.min(words.length, (int)(dialogueTimer * wordsPerSecond));

        if (wordsToShow <= 0) return "";

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < wordsToShow; i++) {
            if (i > 0) result.append(" ");
            result.append(words[i]);
        }
        return result.toString();
    }

    /**
     * Returns the total number of words in the dialogue.
     *
     * @return word count
     */
    public static int getWordCount() {
        return DIALOGUE.split(" ").length;
    }

    /**
     * Returns how many words should be visible at the given time.
     *
     * @param dialogueTimer seconds elapsed
     * @return number of words to show
     */
    public static int getWordsToShow(float dialogueTimer) {
        String[] words = DIALOGUE.split(" ");
        float wordsPerSecond = words.length / TALK_FALLBACK_DURATION;
        return Math.min(words.length, (int)(dialogueTimer * wordsPerSecond));
    }

    /**
     * Returns true if the player position is within the whitehouse trigger zone.
     * Extracted from GameScreen.isPlayerNearWhitehouse().
     *
     * @param px player X
     * @param py player Y
     * @param currentMapIndex which map the player is on
     * @param map3YOffset world Y of map 3 bottom edge
     * @param map3Height height of map 3
     * @return true if near the whitehouse
     */
    public static boolean isPlayerNearWhitehouse(float px, float py,
                                                 int currentMapIndex,
                                                 float map3YOffset,
                                                 float map3Height) {
        return currentMapIndex == 2
                && px >= 1080f && px <= 1775f
                && py >= map3YOffset + map3Height - 940f
                && py <= map3YOffset + map3Height - 860f;
    }

    /**
     * Returns true if the police enemy is close enough to catch the player.
     * Extracted from PoliceEnemy.isCatching().
     *
     * @param enemyX police X
     * @param enemyY police Y
     * @param playerX player X
     * @param playerY player Y
     * @return true if within catch distance
     */
    public static boolean isWithinCatchDistance(float enemyX, float enemyY,
                                                float playerX, float playerY) {
        float dx = enemyX - playerX;
        float dy = enemyY - playerY;
        return dx * dx + dy * dy < 75f * 75f;
    }

    /**
     * Returns true if the police enemy is within detection range of the player.
     * Extracted from PoliceEnemy.update().
     *
     * @param enemyX police X
     * @param enemyY police Y
     * @param playerX player X
     * @param playerY player Y
     * @return true if within detection range
     */
    public static boolean isWithinDetectionRange(float enemyX, float enemyY,
                                                 float playerX, float playerY) {
        float dx = enemyX - playerX;
        float dy = enemyY - playerY;
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        return dist <= 400f;
    }
}