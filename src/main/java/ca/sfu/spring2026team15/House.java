package ca.sfu.spring2026team15;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * A delivery target building on the map.
 *
 * <p>Houses cycle through states automatically:
 * <ol>
 *   <li>Waiting — a random timer counts down before a new order appears</li>
 *   <li>Has order — an order indicator sprite is shown above the house and an order timer
 *       counts down; if not delivered in time the order expires</li>
 * </ol>
 * Delivering requires the player to be off-bike and within {@value #ORDER_RADIUS} units,
 * calling {@link #tryDeliver}. A successful delivery awards 10 fish in {@link GameScreen}.
 */
public class House {
    private static final float SIZE = 100f;
    private static final float ORDER_RADIUS = 150f;
    private static final float ORDER_DURATION = 10f; // seconds order stays active

    private final float x, y;
    private boolean hasOrder = false;
    private boolean everDelivered = false;
    private float orderTimer = 0f;
    private float spawnTimer = 0f;
    private float nextSpawnTime;

    private final Texture orderIndicator; // the in front of the house

    /**
     * Creates a house at the given world position.
     * The first order will appear after a random delay of 3–10 seconds.
     *
     * @param x              world X of the house bottom-left corner
     * @param y              world Y of the house bottom-left corner
     * @param orderIndicator ticket texture drawn above the house when an order is active
     */
    public House(float x, float y, Texture orderIndicator) {
        this.x = x;
        this.y = y;
        this.orderIndicator = orderIndicator;
        nextSpawnTime = 3f + (float) Math.random() * 7f; // first order in 3-10 seconds
    }

    /**
     * Advances the house's order state each frame.
     * Counts down the spawn timer to create new orders and the order timer to expire them.
     *
     * @param delta time in seconds since the last frame
     */
    public void update(float delta) {
        if (!hasOrder) {
            spawnTimer += delta;
            if (spawnTimer >= nextSpawnTime) {
                hasOrder = true;
                orderTimer = 0f;
                spawnTimer = 0f;
                nextSpawnTime = 5f + (float) Math.random() * 10f;
            }
        } else {
            orderTimer += delta;
            if (orderTimer >= ORDER_DURATION) {
                hasOrder = false;
                spawnTimer = 0f;
            }
        }


    }

    /**
     * Attempts to complete the active order if the player is within delivery range.
     * On success, clears the order and marks the house as ever-delivered.
     *
     * @param playerX player's world X
     * @param playerY player's world Y
     * @return {@code true} if the delivery was successful; {@code false} if there is no order
     *         or the player is out of range
     */
    public boolean tryDeliver(float playerX, float playerY) {
        if (!hasOrder) return false;
        float dx = playerX - x;
        float dy = playerY - y;
        boolean inRange = dx * dx + dy * dy < ORDER_RADIUS * ORDER_RADIUS;
        if (inRange) {
            hasOrder = false;
            spawnTimer = 0f;
            everDelivered = true;
            return true; // delivery successful
        }
        return false;
    }

    /**
     * Returns the number of seconds remaining before the current order expires.
     * @return remaining seconds, or a full duration value if no order is active
     */
    public float getOrderTimeRemaining() {
        return ORDER_DURATION - orderTimer;
    }

    /**
     * Returns the total duration an order stays active before expiring.
     * @return order duration in seconds ({@value #ORDER_DURATION})
     */
    public float getOrderDuration() {
        return ORDER_DURATION;
    }

    /**
     * Returns {@code true} if the player is within delivery range of this house.
     * Used to show the deliver prompt in {@link GameScreen} without committing a delivery.
     *
     * @param playerX player's world X
     * @param playerY player's world Y
     * @return {@code true} if within {@value #ORDER_RADIUS} units
     */
    public boolean isPlayerInRange(float playerX, float playerY) {
        float dx = playerX - x;
        float dy = playerY - y;
        return dx * dx + dy * dy < ORDER_RADIUS * ORDER_RADIUS;
    }

    /**
     * Draws the order indicator sprite above the house when an order is active.
     * Nothing is drawn when there is no active order.
     *
     * @param batch the active {@link SpriteBatch} (must be begun)
     */
    public void render(SpriteBatch batch) {
        if (hasOrder) {
            batch.draw(orderIndicator, x - 10f, y + SIZE - 30, 70f, 70f);
        }
    }

    /** @return {@code true} if this house currently has an active order */
    public boolean hasOrder() { return hasOrder; }

    /** @return {@code true} if at least one delivery has ever been completed to this house */
    public boolean wasDelivered() { return everDelivered; }

    /** @return world X of this house's bottom-left corner */
    public float getX() { return x; }

    /** @return world Y of this house's bottom-left corner */
    public float getY() { return y; }

    /** @return the order indicator texture used by {@link DeliveryNotification} */
    public Texture getOrderIndicator() { return orderIndicator; }

}