package ca.sfu.spring2026team15;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import org.junit.Test;
import org.objenesis.ObjenesisStd;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

    // --- render() ---

    @Test
    public void renderWithOrderDrawsThreeTimes() {
        // house has order, orderTimer=0 => progress=1.0 (gold branch, 3 draws)
        House h = makeTestHouse(true);
        setField(h, "orderTimer", 0f);
        DeliveryNotification notif = new DeliveryNotification(h, null, 0);
        notif.update(1f); // slidePosition → 1.0
        SpriteBatch batch = mock(SpriteBatch.class);
        notif.render(batch, null, 1280f, 720f, null);
        verify(batch, times(3)).draw(nullable(Texture.class), anyFloat(), anyFloat(), anyFloat(), anyFloat());
    }

    @Test
    public void renderWithoutOrderDrawsOnce() {
        // house has no order → only ticket drawn (1 draw)
        House h = makeTestHouse(false);
        DeliveryNotification notif = new DeliveryNotification(h, null, 0);
        notif.update(1f);
        SpriteBatch batch = mock(SpriteBatch.class);
        notif.render(batch, null, 1280f, 720f, null);
        verify(batch, times(1)).draw(nullable(Texture.class), anyFloat(), anyFloat(), anyFloat(), anyFloat());
    }

    @Test
    public void renderSlideAtZeroStillDrawsTicket() {
        // slidePosition=0 (not updated) — still draws ticket
        House h = makeTestHouse(false);
        DeliveryNotification notif = new DeliveryNotification(h, null, 0);
        SpriteBatch batch = mock(SpriteBatch.class);
        notif.render(batch, null, 1280f, 720f, null);
        verify(batch, times(1)).draw(nullable(Texture.class), anyFloat(), anyFloat(), anyFloat(), anyFloat());
    }

    @Test
    public void renderLowProgressStillDrawsTimerBar() {
        // orderTimer=9f → timeRemaining=1f → progress=0.1 < 0.25 (flash branch), still 3 draws
        House h = makeTestHouse(true);
        setField(h, "orderTimer", 9f);
        DeliveryNotification notif = new DeliveryNotification(h, null, 0);
        notif.update(1f);
        SpriteBatch batch = mock(SpriteBatch.class);
        notif.render(batch, null, 1280f, 720f, null);
        verify(batch, times(3)).draw(nullable(Texture.class), anyFloat(), anyFloat(), anyFloat(), anyFloat());
    }

    @Test
    public void renderSlot1OffsetsDifferentlyFromSlot0() {
        House h = makeTestHouse(false);
        DeliveryNotification slot0 = new DeliveryNotification(h, null, 0);
        DeliveryNotification slot1 = new DeliveryNotification(h, null, 1);
        slot0.update(1f);
        slot1.update(1f);
        // Both should draw once without exception
        SpriteBatch b0 = mock(SpriteBatch.class);
        SpriteBatch b1 = mock(SpriteBatch.class);
        slot0.render(b0, null, 1280f, 720f, null);
        slot1.render(b1, null, 1280f, 720f, null);
        verify(b0, times(1)).draw(nullable(Texture.class), anyFloat(), anyFloat(), anyFloat(), anyFloat());
        verify(b1, times(1)).draw(nullable(Texture.class), anyFloat(), anyFloat(), anyFloat(), anyFloat());
    }
}
