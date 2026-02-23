package ca.sfu.spring2026team15;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Game;

public class Main extends Game {
    @Override
    public void create() {
        setScreen(new StartScreen(this));
    }
}
