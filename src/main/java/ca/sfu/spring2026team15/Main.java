package ca.sfu.spring2026team15;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main extends ApplicationAdapter {
    private Texture catBike;
    private SpriteBatch spriteBatch;
    private FitViewport viewport;

    @Override
    public void create() {
        System.out.println("=== GAME CREATE ===");
        try {
            catBike = new Texture(Gdx.files.internal("catBike.png"));
            System.out.println("✓ Texture loaded: " + catBike.getWidth() + "x" + catBike.getHeight());
        } catch (Exception e) {
            System.err.println("✗ FAILED TO LOAD TEXTURE: " + e.getMessage());
            e.printStackTrace();
        }

        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(8, 5);
        System.out.println("✓ SpriteBatch and Viewport created");
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        ScreenUtils.clear(Color.BLACK);
        spriteBatch.begin();

        if (catBike != null) {
            spriteBatch.draw(catBike, 100, 100, 256, 256);
        }

        spriteBatch.end();
    }

    @Override
    public void dispose() {
        spriteBatch.dispose();
        if (catBike != null) {
            catBike.dispose();
        }
    }
}