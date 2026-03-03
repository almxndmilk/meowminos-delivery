package ca.sfu.spring2026team15;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class House {
    private static final float SIZE = 100f;
    private static final float ORDER_RADIUS = 150f;
    private static final float ORDER_DURATION = 10f; // seconds order stays active

    private final float x, y;
    private boolean hasOrder = false;
    private float orderTimer = 0f;
    private float spawnTimer = 0f;
    private float nextSpawnTime;

    private final Texture orderIndicator; // the in front of the house

    public House(float x, float y, Texture orderIndicator) {
        this.x = x;
        this.y = y;
        this.orderIndicator = orderIndicator;
        nextSpawnTime = 3f + (float) Math.random() * 7f; // first order in 3-10 seconds
    }

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

    public boolean tryDeliver(float playerX, float playerY) {
        if (!hasOrder) return false;
        float dx = playerX - x;
        float dy = playerY - y;
        boolean inRange = dx * dx + dy * dy < ORDER_RADIUS * ORDER_RADIUS;
        if (inRange) {
            hasOrder = false;
            spawnTimer = 0f;
            return true; // delivery successful
        }
        return false;
    }

    public boolean isPlayerInRange(float playerX, float playerY) {
        float dx = playerX - x;
        float dy = playerY - y;
        return dx * dx + dy * dy < ORDER_RADIUS * ORDER_RADIUS;
    }

    public void render(SpriteBatch batch) {
        if (hasOrder) {
            batch.draw(orderIndicator, x - 10f, y + SIZE, 70f, 70f);
        }
    }

    public boolean hasOrder() { return hasOrder; }
    public float getX() { return x; }
    public float getY() { return y; }
}