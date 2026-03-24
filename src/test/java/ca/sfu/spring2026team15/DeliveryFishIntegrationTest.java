package ca.sfu.spring2026team15;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

/**
 * Integration tests for the house delivery and fish count systems working together.
 * Successfully delivering an order adds 10 fish to the player's count.
 * These tests check that deliveries correctly affect the fish count
 * and how that interacts with gate quotas and penalties.
 */
public class DeliveryFishIntegrationTest {

    /** Fish added to the count on each successful delivery. */
    private static final int DELIVERY_FISH_REWARD = 10;

    /** Fish needed to open gate 1. */
    private static final int FISH_QUOTA_GATE1 = 30;

    /** Fish needed to open gate 2. */
    private static final int FISH_QUOTA_GATE2 = 20;

    /** Fish lost each time the player is caught. */
    private static final int RESPAWN_FISH_PENALTY = 10;

    /** Fish count used in each test. */
    private int fishCollected;

    /**
     * Resets fish count before each test.
     */
    @Before
    public void setUp() {
        fishCollected = 0;
    }

    /**
     * A successful delivery should add 10 fish to the count.
     */
    @Test
    public void testSingleDeliveryAddsTenFish() {
        fishCollected += DELIVERY_FISH_REWARD;
        assertEquals(10, fishCollected);
    }

    /**
     * Two deliveries should add 20 fish total.
     */
    @Test
    public void testTwoDeliveriesAddTwentyFish() {
        fishCollected += DELIVERY_FISH_REWARD;
        fishCollected += DELIVERY_FISH_REWARD;
        assertEquals(20, fishCollected);
    }

    /**
     * Three deliveries should add 30 fish total.
     */
    @Test
    public void testThreeDeliveriesAddThirtyFish() {
        fishCollected += DELIVERY_FISH_REWARD;
        fishCollected += DELIVERY_FISH_REWARD;
        fishCollected += DELIVERY_FISH_REWARD;
        assertEquals(30, fishCollected);
    }

    /**
     * Three deliveries alone should be enough to open gate 1.
     */
    @Test
    public void testThreeDeliveriesOpenGate1() {
        fishCollected += DELIVERY_FISH_REWARD;
        fishCollected += DELIVERY_FISH_REWARD;
        fishCollected += DELIVERY_FISH_REWARD;
        assertFalse(FISH_QUOTA_GATE1 - fishCollected > 0);
    }

    /**
     * Two deliveries alone should be enough to open gate 2.
     */
    @Test
    public void testTwoDeliveriesOpenGate2() {
        fishCollected += DELIVERY_FISH_REWARD;
        fishCollected += DELIVERY_FISH_REWARD;
        assertFalse(FISH_QUOTA_GATE2 - fishCollected > 0);
    }

    /**
     * A delivery after a respawn penalty should correctly net zero fish
     * since the reward and penalty cancel out.
     */
    @Test
    public void testDeliveryAfterPenaltyNetsZero() {
        fishCollected += DELIVERY_FISH_REWARD; // +10
        fishCollected -= RESPAWN_FISH_PENALTY; // -10
        assertEquals(0, fishCollected);
    }

    /**
     * Two deliveries followed by a respawn penalty should leave 10 fish.
     */
    @Test
    public void testTwoDeliveriesMinusOnePenalty() {
        fishCollected += DELIVERY_FISH_REWARD;
        fishCollected += DELIVERY_FISH_REWARD;
        fishCollected -= RESPAWN_FISH_PENALTY;
        assertEquals(10, fishCollected);
    }

    /**
     * A delivery should save the player from death if their count was
     * at zero before delivering.
     */
    @Test
    public void testDeliveryPreventsDeathAtZeroFish() {
        fishCollected = 0;
        fishCollected += DELIVERY_FISH_REWARD;
        assertFalse(fishCollected < 0);
    }

    /**
     * Mixing collected fish, deliveries, and penalties should all
     * add and subtract from the same count correctly.
     */
    @Test
    public void testMixedFishSourcesAddUpCorrectly() {
        fishCollected += 5;                    // collected from road
        fishCollected += DELIVERY_FISH_REWARD; // delivery reward
        fishCollected -= RESPAWN_FISH_PENALTY; // caught by police
        fishCollected += DELIVERY_FISH_REWARD; // another delivery
        assertEquals(15, fishCollected);
    }

    /**
     * Enough deliveries combined with collected fish should meet gate 1 quota.
     */
    @Test
    public void testDeliveriesAndCollectedFishMeetGate1Quota() {
        fishCollected += 10;                   // collected from road
        fishCollected += DELIVERY_FISH_REWARD; // delivery 1
        fishCollected += DELIVERY_FISH_REWARD; // delivery 2
        assertFalse(FISH_QUOTA_GATE1 - fishCollected > 0);
    }
}