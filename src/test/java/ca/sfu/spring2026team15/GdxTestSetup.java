package ca.sfu.spring2026team15;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Vector2;
import org.junit.BeforeClass;
import org.objenesis.ObjenesisStd;

import java.lang.reflect.Field;
import java.util.Random;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Base class for all tests that need LibGDX entity classes.
 * Uses Objenesis to create instances without calling constructors
 * (bypassing Texture / OpenGL loading), then sets required fields via reflection.
 */
public abstract class GdxTestSetup {

    private static final ObjenesisStd OBJENESIS = new ObjenesisStd();

    @BeforeClass
    public static void initGdxMocks() {
        Gdx.app = mock(Application.class);

        Files mockFiles = mock(Files.class);
        when(mockFiles.internal(anyString())).thenReturn(null);
        Gdx.files = mockFiles;

        GL20 gl = mock(GL20.class);
        Gdx.gl = gl;
        Gdx.gl20 = gl;

        Audio mockAudio = mock(Audio.class);
        when(mockAudio.newSound(any())).thenReturn(mock(Sound.class));
        when(mockAudio.newMusic(any())).thenReturn(mock(Music.class));
        Gdx.audio = mockAudio;

        Gdx.input = mock(Input.class);

        SettingsScreen.soundOn = false;
    }

    // ---- factory methods ----

    static Player makePlayer(float x, float y) {
        Player p = OBJENESIS.newInstance(Player.class);
        setField(p, "position", new Vector2(x, y));
        setField(p, "currentSpeed", 400f);
        setEnumField(p, "lastDirection", "DOWN");
        setEnumField(p, "prevDirection", "DOWN");
        return p;
    }

    static CatOffBike makeCat(float x, float y) {
        CatOffBike c = OBJENESIS.newInstance(CatOffBike.class);
        setField(c, "position", new Vector2(x, y));
        setField(c, "currentSpeed", 250f);
        setEnumField(c, "lastDirection", "DOWN");
        setEnumField(c, "prevDirection", "DOWN");
        return c;
    }

    static PoliceEnemy makePolice(float x, float y) {
        PoliceEnemy p = OBJENESIS.newInstance(PoliceEnemy.class);
        setField(p, "position", new Vector2(x, y));
        setField(p, "spawnX", x);
        setField(p, "spawnY", y);
        setField(p, "VAR_CHASE_SPEED", 250f);
        setField(p, "VAR_WANDER_SPEED", 80f);
        setField(p, "random", new Random(0));
        setEnumField(p, "alertState", "NONE");
        setEnumField(p, "lastDirection", "DOWN");
        return p;
    }

    static Fish makeFish(float x, float y) {
        Fish f = OBJENESIS.newInstance(Fish.class);
        setField(f, "position", new Vector2(x, y));
        setField(f, "collected", false);
        return f;
    }

    static PuddleEnemy makePuddle(float x, float y) {
        PuddleEnemy p = OBJENESIS.newInstance(PuddleEnemy.class);
        setField(p, "position", new Vector2(x, y));
        return p;
    }

    // ---- reflection helpers ----

    protected static void setField(Object target, String name, Object value) {
        try {
            Field f = findField(target.getClass(), name);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Could not set field '" + name + "'", e);
        }
    }

    protected static void setEnumField(Object target, String fieldName, String constantName) {
        try {
            Field f = findField(target.getClass(), fieldName);
            f.setAccessible(true);
            for (Object constant : f.getType().getEnumConstants()) {
                if (constant.toString().equals(constantName)) {
                    f.set(target, constant);
                    return;
                }
            }
            throw new IllegalArgumentException("Constant '" + constantName + "' not found in " + f.getType());
        } catch (Exception e) {
            throw new RuntimeException("Could not set enum field '" + fieldName + "'", e);
        }
    }

    private static Field findField(Class<?> clazz, String name) throws NoSuchFieldException {
        while (clazz != null) {
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                clazz = clazz.getSuperclass();
            }
        }
        throw new NoSuchFieldException(name);
    }
}
