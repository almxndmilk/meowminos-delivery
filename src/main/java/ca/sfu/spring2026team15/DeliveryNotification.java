package ca.sfu.spring2026team15;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Color;

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

    public DeliveryNotification(House house, Texture ticketTexture, int slotIndex) {
        this.house = house;
        this.ticketTexture = ticketTexture;
        this.slotIndex = slotIndex;
        this.slidePosition = 0f; // Start off-screen
    }

    public void setSlotIndex(int index) {
        this.slotIndex = index;
    }

    public int getSlotIndex() {
        return slotIndex;
    }

    public House getHouse() {
        return house;
    }

    public boolean isExpired() {
        return !house.hasOrder();
    }

    public void update(float delta) {
        //Slide in animation
        if (slidePosition < 1f) {
            slidePosition += SLIDE_SPEED * delta;
            if (slidePosition > 1f) slidePosition = 1f;
        }
    }

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