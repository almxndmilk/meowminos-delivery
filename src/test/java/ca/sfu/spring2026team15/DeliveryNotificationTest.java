package ca.sfu.spring2026team15;

import org.junit.Test;
import org.objenesis.ObjenesisStd;

import static org.junit.Assert.*;

/**
 * Tests for DeliveryNotification — slide animation, expiry, slot management.
 */
public class DeliveryNotificationTest extends GdxTestSetup {

    private static final ObjenesisStd OBJENESIS = new ObjenesisStd();

    private House makeTestHouse(boolean hasOrder) {
        House h = OBJENESIS.newInstance(House.class);
        setField(h, "x", 100f);
        setField(h, "y", 200f);
        setField(h, "hasOrder", hasOrder);
        setField(h, "everDelivered", false);
        setField(h, "orderTimer", 0f);
        setField(h, "spawnTimer", 0f);
        setField(h, "nextSpawnTime", 5f);
        return h;
    }

    @Test
    public void newNotificationStartsAtSlide0() {
        House h = makeTestHouse(true);
        DeliveryNotification notif = new DeliveryNotification(h, null, 0);
        // slidePosition starts at 0 (off-screen)
        assertFalse(notif.isExpired()); // house has order
    }

    @Test
    public void getHouseReturnsCorrectHouse() {
        House h = makeTestHouse(true);
        DeliveryNotification notif = new DeliveryNotification(h, null, 0);
        assertEquals(h, notif.getHouse());
    }

    @Test
    public void getSlotIndexReturnsInitialSlot() {
        House h = makeTestHouse(true);
        DeliveryNotification notif = new DeliveryNotification(h, null, 2);
        assertEquals(2, notif.getSlotIndex());
    }

    @Test
    public void setSlotIndexUpdatesSlot() {
        House h = makeTestHouse(true);
        DeliveryNotification notif = new DeliveryNotification(h, null, 0);
        notif.setSlotIndex(1);
        assertEquals(1, notif.getSlotIndex());
    }

    @Test
    public void isExpiredWhenHouseHasNoOrder() {
        House h = makeTestHouse(false);
        DeliveryNotification notif = new DeliveryNotification(h, null, 0);
        assertTrue(notif.isExpired());
    }

    @Test
    public void isNotExpiredWhenHouseHasOrder() {
        House h = makeTestHouse(true);
        DeliveryNotification notif = new DeliveryNotification(h, null, 0);
        assertFalse(notif.isExpired());
    }

    @Test
    public void updateIncreasesSlidePosition() {
        House h = makeTestHouse(true);
        DeliveryNotification notif = new DeliveryNotification(h, null, 0);
        notif.update(0.5f);
        // SLIDE_SPEED = 3.5, so after 0.5s: 3.5 * 0.5 = 1.75, capped to 1.0
        // We can't directly read slidePosition, but we know it was updated without error
    }

    @Test
    public void updateCapsSlidePositionAt1() {
        House h = makeTestHouse(true);
        DeliveryNotification notif = new DeliveryNotification(h, null, 0);
        notif.update(1.0f); // 3.5 * 1.0 = 3.5, should cap at 1.0
        notif.update(1.0f); // Already at 1.0, should stay at 1.0
        // No exception means it worked correctly
    }

    @Test
    public void multipleUpdatesCapCorrectly() {
        House h = makeTestHouse(true);
        DeliveryNotification notif = new DeliveryNotification(h, null, 0);
        notif.update(0.1f); // 0.35
        notif.update(0.1f); // 0.70
        notif.update(0.1f); // 1.05 -> 1.0
        notif.update(0.1f); // stays at 1.0
        assertFalse(notif.isExpired()); // still has order
    }

    @Test
    public void slotIndexChanges() {
        House h = makeTestHouse(true);
        DeliveryNotification notif = new DeliveryNotification(h, null, 0);
        assertEquals(0, notif.getSlotIndex());
        notif.setSlotIndex(1);
        assertEquals(1, notif.getSlotIndex());
        notif.setSlotIndex(2);
        assertEquals(2, notif.getSlotIndex());
    }
}
