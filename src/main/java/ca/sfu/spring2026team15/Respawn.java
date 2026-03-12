package ca.sfu.spring2026team15;

import java.util.List;

public class Respawn {

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