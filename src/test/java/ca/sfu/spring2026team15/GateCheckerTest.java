package ca.sfu.spring2026team15;

import org.junit.Test;

import static org.junit.Assert.*;
import static ca.sfu.spring2026team15.GateChecker.GateResult.*;

/**
 * Tests for GateChecker — pure Java, no LibGDX required.
 * Covers: map transition, Gate + fish count, Characters + gate.
 */
public class GateCheckerTest {

    // Gate 1: corridor X 3500–4200 (width 700), Y = 660, quota 30 fish
    private static final float G1_X     = 3500f;
    private static final float G1_Y     = 660f;
    private static final float G1_WIDTH = 700f;
    private static final int   G1_QUOTA = 30;

    // Gate 2: corridor X 0–700 (width 700), Y = 902, quota 20 fish
    private static final float G2_X     = 0f;
    private static final float G2_Y     = 902f;
    private static final float G2_WIDTH = 700f;
    private static final int   G2_QUOTA = 20;

    // --- single feature: map transition ---

    @Test
    public void notAtGateWhenPlayerFarFromGateY() {
        GateChecker.GateResult result =
            GateChecker.check(3600f, 100f, 50, G1_X, G1_Y, G1_WIDTH, G1_QUOTA);
        assertEquals(NOT_AT_GATE, result);
    }

    @Test
    public void notAtGateWhenPlayerOutsideXCorridor() {
        GateChecker.GateResult result =
            GateChecker.check(3000f, G1_Y, 50, G1_X, G1_Y, G1_WIDTH, G1_QUOTA);
        assertEquals(NOT_AT_GATE, result);
    }

    // --- interaction: Gate + fish count ---

    @Test
    public void insufficientFishWhenAtGateButTooFew() {
        GateChecker.GateResult result =
            GateChecker.check(3600f, G1_Y, 10, G1_X, G1_Y, G1_WIDTH, G1_QUOTA);
        assertEquals(INSUFFICIENT_FISH, result);
    }

    @Test
    public void canClearWhenAtGateWithExactQuota() {
        GateChecker.GateResult result =
            GateChecker.check(3600f, G1_Y, 30, G1_X, G1_Y, G1_WIDTH, G1_QUOTA);
        assertEquals(CAN_CLEAR, result);
    }

    @Test
    public void canClearWhenAtGateWithMoreThanQuota() {
        GateChecker.GateResult result =
            GateChecker.check(3600f, G1_Y, 99, G1_X, G1_Y, G1_WIDTH, G1_QUOTA);
        assertEquals(CAN_CLEAR, result);
    }

    @Test
    public void gate2WorksWithOwnQuota() {
        // Player in gate-2 corridor, exactly at gate-2 Y, with quota met
        GateChecker.GateResult result =
            GateChecker.check(350f, G2_Y, G2_QUOTA, G2_X, G2_Y, G2_WIDTH, G2_QUOTA);
        assertEquals(CAN_CLEAR, result);
    }

    @Test
    public void gate2InsufficientFish() {
        GateChecker.GateResult result =
            GateChecker.check(350f, G2_Y, 5, G2_X, G2_Y, G2_WIDTH, G2_QUOTA);
        assertEquals(INSUFFICIENT_FISH, result);
    }

    @Test
    public void playerAtEdgeOfGateYTolerance() {
        // Y tolerance is ±50
        GateChecker.GateResult inside =
            GateChecker.check(3600f, G1_Y + 49f, 50, G1_X, G1_Y, G1_WIDTH, G1_QUOTA);
        assertEquals(CAN_CLEAR, inside);

        GateChecker.GateResult outside =
            GateChecker.check(3600f, G1_Y + 51f, 50, G1_X, G1_Y, G1_WIDTH, G1_QUOTA);
        assertEquals(NOT_AT_GATE, outside);
    }
}
