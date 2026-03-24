package ca.sfu.spring2026team15;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests for House delivery mechanics.
 * House(x, y, Texture) — texture is only used in render(); can pass null.
 * Covers: Delivery system, E to deliver pizza.
 */
public class HouseTest {

    private static final float X = 500f;
    private static final float Y = 500f;
    private static final float ORDER_RADIUS = 150f;

    // --- single feature: Delivery system ---

    @Test
    public void houseStartsWithNoOrder() {
        House house = new House(X, Y, null);
        assertFalse(house.hasOrder());
    }

    @Test
    public void houseGetsOrderAfterSpawnTime() {
        House house = new House(X, Y, null);
        // Advance well past the max first spawn delay (10s)
        house.update(15f);
        assertTrue(house.hasOrder());
    }

    @Test
    public void deliverySucceedsWhenPlayerInRange() {
        House house = new House(X, Y, null);
        house.update(15f);
        assertTrue(house.hasOrder());

        // player at exactly house position — definitely in range
        boolean delivered = house.tryDeliver(X, Y);
        assertTrue(delivered);
    }

    @Test
    public void deliveryFailsWhenPlayerOutOfRange() {
        House house = new House(X, Y, null);
        house.update(15f);

        boolean delivered = house.tryDeliver(X + ORDER_RADIUS + 100f, Y);
        assertFalse(delivered);
    }

    @Test
    public void deliveryClearsOrder() {
        House house = new House(X, Y, null);
        house.update(15f);
        house.tryDeliver(X, Y);
        assertFalse(house.hasOrder());
    }

    @Test
    public void deliveryFailsWhenNoActiveOrder() {
        House house = new House(X, Y, null);
        assertFalse(house.tryDeliver(X, Y));
    }

    @Test
    public void orderExpiresAfterDuration() {
        House house = new House(X, Y, null);
        house.update(15f); // force order to spawn
        assertTrue(house.hasOrder());
        house.update(11f); // ORDER_DURATION = 10s — expire it
        assertFalse(house.hasOrder());
    }

    @Test
    public void wasDeliveredFalseBeforeAnyDelivery() {
        House house = new House(X, Y, null);
        assertFalse(house.wasDelivered());
    }

    @Test
    public void wasDeliveredTrueAfterSuccessfulDelivery() {
        House house = new House(X, Y, null);
        house.update(15f);
        house.tryDeliver(X, Y);
        assertTrue(house.wasDelivered());
    }

    @Test
    public void isPlayerInRangeTrueWhenClose() {
        House house = new House(X, Y, null);
        assertTrue(house.isPlayerInRange(X + 10f, Y + 10f));
    }

    @Test
    public void isPlayerInRangeFalseWhenFar() {
        House house = new House(X, Y, null);
        assertFalse(house.isPlayerInRange(X + ORDER_RADIUS + 50f, Y));
    }

    // --- getOrderTimeRemaining() ---

    @Test
    public void getOrderTimeRemainingStartsAt10() {
        House house = new House(X, Y, null);
        house.update(15f); // spawn order, orderTimer resets to 0
        // orderTimeRemaining = ORDER_DURATION - orderTimer = 10 - 0 = 10
        assertEquals(10f, house.getOrderTimeRemaining(), 0.01f);
    }

    @Test
    public void getOrderTimeRemainingDecreasesAfterUpdate() {
        House house = new House(X, Y, null);
        house.update(15f); // spawn order
        house.update(3f);  // orderTimer = 3
        // orderTimeRemaining = 10 - 3 = 7
        assertEquals(7f, house.getOrderTimeRemaining(), 0.01f);
    }

    @Test
    public void getOrderTimeRemainingApproachesZero() {
        House house = new House(X, Y, null);
        house.update(15f); // spawn order
        house.update(9.5f); // orderTimer = 9.5
        assertEquals(0.5f, house.getOrderTimeRemaining(), 0.01f);
    }

    // --- getOrderDuration() ---

    @Test
    public void getOrderDurationReturns10() {
        House house = new House(X, Y, null);
        assertEquals(10f, house.getOrderDuration(), 0.01f);
    }

    // --- getX() and getY() accessors ---

    @Test
    public void getXReturnsConstructorX() {
        House house = new House(X, Y, null);
        assertEquals(X, house.getX(), 0.01f);
    }

    @Test
    public void getYReturnsConstructorY() {
        House house = new House(X, Y, null);
        assertEquals(Y, house.getY(), 0.01f);
    }

    @Test
    public void getXAndGetYWithDifferentValues() {
        House house = new House(123f, 456f, null);
        assertEquals(123f, house.getX(), 0.01f);
        assertEquals(456f, house.getY(), 0.01f);
    }
}
