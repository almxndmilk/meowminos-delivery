package ca.sfu.spring2026team15;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;

/**
 * A HUD panel that slides in from the right side of the screen to announce an active delivery order.
 *
 * <p>Each notification corresponds to one {@link House} with an active order.
 * Multiple notifications stack left-to-right via a slot index (0 = rightmost).
 * A timer progress bar is drawn above the ticket and flashes red when under 25 % remaining.
 * The notification is considered expired once its associated house no longer has an order.
 */
public class DeliveryNotification {
    private House house;
    private Texture ticketTexture;
    private float slidePosition;
    private int slotIndex; //0=right,1=mid,2=left

    private static final float SLIDE_SPEED = 3.5f;
    private static final float TICKET_WIDTH = 120f;
    private static final float TICKET_HEIGHT = 100f;
    private static final float TICKET_SPACING = 10f;
    private static final float SCREEN_RIGHT_MARGIN = 20f;
    private static final float SCREEN_BOTTOM_MARGIN = 20f;
    private static final float TIMER_BAR_HEIGHT = 18f;

    /**
     * Creates a new delivery notification panel that starts off-screen and slides in.
     *
     * @param house         the house whose order this notification represents
     * @param ticketTexture the ticket image to display on the panel
     * @param slotIndex     horizontal slot position (0 = rightmost, 1 = next left, etc.)
     */
    public DeliveryNotification(House house, Texture ticketTexture, int slotIndex) {
        this.house = house;
        this.ticketTexture = ticketTexture;
        this.slotIndex = slotIndex;
        this.slidePosition = 0f; // Start off-screen
    }

    /**
     * Updates the horizontal slot position, used when other notifications are removed and
     * the remaining panels need to be shifted right.
     *
     * @param index new slot index (0 = rightmost)
     */
    public void setSlotIndex(int index) {
        this.slotIndex = index;
    }

    /**
     * Returns the current horizontal slot index.
     * @return slot index (0 = rightmost)
     */
    public int getSlotIndex() {
        return slotIndex;
    }

    /**
     * Returns the {@link House} this notification is tracking.
     * @return the house whose order this notification represents
     */
    public House getHouse() {
        return house;
    }

    /**
     * Returns {@code true} when the associated house no longer has an active order,
     * signalling that this notification should be removed from the active list.
     *
     * @return {@code true} if the order is gone (delivered or timed out)
     */
    public boolean isExpired() {
        return !house.hasOrder();
    }

    /**
     * Advances the slide-in animation each frame until the panel is fully on-screen.
     *
     * @param delta time in seconds since the last frame
     */
    public void update(float delta) {
        //Slide in animation
        if (slidePosition < 1f) {
            slidePosition += SLIDE_SPEED * delta;
            if (slidePosition > 1f) slidePosition = 1f;
        }
    }

    /**
     * Draws the ticket panel and its timer progress bar in HUD/screen space.
     * The bar shrinks left-to-right as the order time runs out, and flashes red at under 25 %.
     *
     * @param batch        the active HUD {@link SpriteBatch} (must be begun)
     * @param font         bitmap font for any text labels (currently unused but kept for API consistency)
     * @param viewWidth    HUD viewport width used to compute right-side positioning
     * @param viewHeight   HUD viewport height (unused; bottom margin is hardcoded)
     * @param whiteTexture a 1×1 white pixel texture used to draw the progress bar
     */
    public void render(SpriteBatch batch, BitmapFont font, float viewWidth, float viewHeight, Texture whiteTexture) {
        //calc position based on slot index and slide position
        float baseX = viewWidth - SCREEN_RIGHT_MARGIN - TICKET_WIDTH;
        float offsetX = slotIndex * (TICKET_WIDTH + TICKET_SPACING);
        float currentX = baseX - offsetX;

        //slide animation
        float slideOffset = (1f - slidePosition) * (viewWidth - currentX + 50f);
        currentX += slideOffset;

        float y = SCREEN_BOTTOM_MARGIN;

        //Draw ticket
        batch.draw(ticketTexture, currentX, y, TICKET_WIDTH, TICKET_HEIGHT);

        if (house.hasOrder()) {
            float timeRemaining = house.getOrderTimeRemaining();
            float totalTime = house.getOrderDuration();
            float progress = Math.max(0, timeRemaining / totalTime);

            float barY = y + TICKET_HEIGHT + 5f;
            float borderThickness = 1.5f;

            batch.setColor(Color.WHITE);
            batch.draw(whiteTexture, currentX, barY, TICKET_WIDTH, TIMER_BAR_HEIGHT);

            if (progress < 0.25f && (System.currentTimeMillis() / 250) % 2 == 0) {
                batch.setColor(Color.RED);
            } else {
                batch.setColor(1f, 0.804f, 0f, 1f);
            }

            float innerWidth = (TICKET_WIDTH - (borderThickness * 2)) * progress;
            float innerHeight = TIMER_BAR_HEIGHT - (borderThickness * 2);

            batch.draw(whiteTexture,
                    currentX + borderThickness,
                    barY + borderThickness,
                    innerWidth,
                    innerHeight
            );

            batch.setColor(Color.WHITE);
        }
    }
}