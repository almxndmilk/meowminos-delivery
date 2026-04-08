package ca.sfu.spring2026team15;

import java.util.List;

/**
 * Utility class providing the respawn logic triggered when a police enemy catches the player.
 * All logic is in a single static method; the class is not instantiated.
 */
public class Respawn {

    /** Utility class; do not instantiate. */
    private Respawn() {}

    /**
     * Respawns the player at the current map's starting position, deducts fish and time
     * penalties, and resets all police enemies to their spawn points.
     *
     * @param player          the player entity to reposition
     * @param police          all active police enemies to reset
     * @param timer           the game timer from which the time penalty is subtracted
     * @param currentMapIndex the map the player is currently on (0, 1, or 2)
     * @param currentFish     the player's current fish count before the penalty
     * @param mapYOffsets     world-Y origins of each map section (used to compute spawn Y)
     * @param fishPenalty     number of fish to deduct (clamped so fish never go below 0)
     * @param timePenalty     seconds to subtract from the countdown timer
     * @return the updated fish count after applying the penalty
     */
    public static int respawn(Player player, List<PoliceEnemy> police, Timer timer,
                              int currentMapIndex, int currentFish, float[] mapYOffsets,
                              int fishPenalty, float timePenalty) {

        int newFishCount = Math.max(0, currentFish - fishPenalty);
        timer.subtractTime(timePenalty);

        if (currentMapIndex == 0) {
            player.setPosition(300f, 300f);
        }
        else if (currentMapIndex == 1) {
            player.setPosition(5600f, mapYOffsets[1] + 100f);
        }
        else if (currentMapIndex == 2) {
            player.setPosition(1250f, mapYOffsets[2] + 100f);
        }

        for (PoliceEnemy enemy : police) {
            enemy.resetToSpawn();
        }

        return newFishCount;
    }
}