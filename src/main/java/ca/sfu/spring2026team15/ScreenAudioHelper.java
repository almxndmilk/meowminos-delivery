package ca.sfu.spring2026team15;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

/**
 * Shared audio utilities for Screen classes.
 *
 * Centralises the repeated audio-setup pattern that previously existed in both SettingsScreen 
 *  and InstructionsScreen, so that volume values and file paths only need to be maintained in one place
 */
public final class ScreenAudioHelper {

    /** Volume used for all UI transition sounds. */
    public static final float UI_SOUND_VOLUME = 0.8f;

    private ScreenAudioHelper() {}

    /**
     * Loads a sound from the given internal path and plays it once at
     * {@link #UI_SOUND_VOLUME} if sound is currently enabled.
     *
     * @param path internal asset path
     * @return the loaded Sound (caller is responsible for disposing it)
     */
    public static Sound loadAndPlayEntrySound(String path) {
        Sound sound = Gdx.audio.newSound(Gdx.files.internal(path));
        if (SettingsScreen.soundOn) {
            sound.play(UI_SOUND_VOLUME);
        }
        return sound;
    }

    /**
     * Loads a sound from the given internal path without playing it.
     *
     * @param path internal asset path
     * @return the loaded Sound (caller is responsible for disposing it)
     */
    public static Sound load(String path) {
        return Gdx.audio.newSound(Gdx.files.internal(path));
    }

    /**
     * Plays the given sound at {@link #UI_SOUND_VOLUME} if sound is enabled.
     *
     * @param sound the Sound to play
     */
    public static void playIfEnabled(Sound sound) {
        if (SettingsScreen.soundOn) {
            sound.play(UI_SOUND_VOLUME);
        }
    }
}