package ca.sfu.spring2026team15;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.math.Vector2;

public class Main extends ApplicationAdapter {
    private Texture catBike;
    private SpriteBatch spriteBatch;
    private FitViewport viewport;
    private Texture background;
    Vector2 position;
    float moveSpeed;

    @Override
    public void create() {

        System.out.println("=== GAME CREATE ===");
        try {
            catBike = new Texture(Gdx.files.internal("cataseprite1.png"));
            System.out.println("✓ Texture loaded: " + catBike.getWidth() + "x" + catBike.getHeight());
        } catch (Exception e) {
            System.err.println("✗ FAILED TO LOAD TEXTURE: " + e.getMessage());
            e.printStackTrace();
        }

        background = new Texture(Gdx.files.internal("IMG_5555.png"));
        spriteBatch = new SpriteBatch();
        position = new Vector2(0,0);
        moveSpeed = 10;
        viewport = new FitViewport(800, 500);
        System.out.println("✓ SpriteBatch and Viewport created");
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {

        if (Gdx.input.isKeyPressed(Input.Keys.W))
            position.y += moveSpeed * Gdx.graphics.getDeltaTime();
        else if(Gdx.input.isKeyPressed(Input.Keys.S))
            position.y -= moveSpeed * Gdx.graphics.getDeltaTime();
        if (Gdx.input.isKeyPressed(Input.Keys.D))
            position.x += moveSpeed * Gdx.graphics.getDeltaTime();
        else if (Gdx.input.isKeyPressed(Input.Keys.A))
            position.x -= moveSpeed * Gdx.graphics.getDeltaTime();

        ScreenUtils.clear(Color.BLACK);
        spriteBatch.begin();

        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();

        spriteBatch.draw(background, 0,0, worldWidth, worldHeight);
        spriteBatch.draw(catBike, position.x, position.y, 100, 100);
//        if (catBike != null) {
//
//        }
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