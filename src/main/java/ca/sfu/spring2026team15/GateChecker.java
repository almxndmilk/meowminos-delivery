package ca.sfu.spring2026team15;

public class GateChecker {

    public enum GateResult { NOT_AT_GATE, INSUFFICIENT_FISH, CAN_CLEAR }

    /**
     * Checks whether the player is at a gate zone and can pass through.
     *
     * @param playerX   player world X (centre)
     * @param playerY   player world Y (centre)
     * @param fishCount current fish held
     * @param gateX     left edge of gate corridor in world X
     * @param gateY     Y coordinate of the gate boundary
     * @param gateWidth width of the gate corridor
     * @param fishQuota minimum fish required to pass
     * @return CAN_CLEAR, INSUFFICIENT_FISH, or NOT_AT_GATE
     */
    public static GateResult check(float playerX, float playerY, int fishCount,
                                   float gateX, float gateY, float gateWidth, int fishQuota) {
        float gateRight = gateX + gateWidth;
        boolean atGate = playerY >= gateY - 50f && playerY <= gateY + 50f
                      && playerX >= gateX && playerX <= gateRight;
        if (!atGate) return GateResult.NOT_AT_GATE;
        if (fishCount < fishQuota) return GateResult.INSUFFICIENT_FISH;
        return GateResult.CAN_CLEAR;
    }
}
