package ca.sfu.spring2026team15;

/**
 * Utility class that evaluates whether the player can pass through a gate.
 *
 * <p>Returns a {@link GateResult} enum indicating whether the player is not near the gate,
 * near but lacks the required fish, or near and eligible to clear it.
 * All logic is in the static {@link #check} method; the class is not instantiated.
 */
public class GateChecker {

    /**
     * Result of a gate check.
     * <ul>
     *   <li>{@code NOT_AT_GATE} — player is not near the gate</li>
     *   <li>{@code INSUFFICIENT_FISH} — player is at the gate but lacks the required fish</li>
     *   <li>{@code CAN_CLEAR} — player is at the gate and has enough fish to clear it</li>
     * </ul>
     */
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
