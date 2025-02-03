package we.devs.opium.api.manager.music;

import net.minecraft.client.sound.SoundInstance;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Random;

public class MusicStateManager {
    private static boolean isPlayingCustomMusic = false;
    private static final List<Identifier> CUSTOM_MUSIC_TRACKS = List.of(
            Identifier.of("opium", "kencarson_overseas"),
            Identifier.of("opium", "kencarson_ss"),
            Identifier.of("opium", "playboicarti_24songs"),
            Identifier.of("opium", "playboicarti_eviljordan"),
            Identifier.of("opium", "destroylonely_nostylist"),
            Identifier.of("opium", "kencarson_hardcore"),
            Identifier.of("opium", "kencarson_moneyandsex"),
            Identifier.of("opium", "kencarson_boss"),
            Identifier.of("opium", "kencarson_freeyoungthug"),
            Identifier.of("opium", "playboicarti_lean4real"),
            Identifier.of("opium", "playboicarti_longtime"),
            Identifier.of("opium", "playboicarti_vampanthem"),
            Identifier.of("opium", "homixidegang_guitars"),
            Identifier.of("opium", "kencarson_mdma"),
            Identifier.of("opium", "homixidegang_rckstarbitch"),
            Identifier.of("opium", "playboicarti_onthattime"),
            Identifier.of("opium", "destroylonely_intheair"),
            Identifier.of("opium", "kencarson_swagoverload"),
            Identifier.of("opium", "destroylonely_vvsvalentine")
    );
    private static final Random RANDOM = new Random();
    private static SoundInstance currentSongInstance;
    private static Identifier lastPlayedTrack = null; // Safes last played track

    public static boolean isPlayingCustomMusic() {
        return isPlayingCustomMusic;
    }

    public static void setPlayingCustomMusic(boolean playing) {
        isPlayingCustomMusic = playing;
    }

    public static Identifier getRandomMusicTrack() {
        Identifier nextTrack;

        // Makes sure that the new song is different from the last played song
        do {
            nextTrack = CUSTOM_MUSIC_TRACKS.get(RANDOM.nextInt(CUSTOM_MUSIC_TRACKS.size()));
        } while (nextTrack.equals(lastPlayedTrack) && CUSTOM_MUSIC_TRACKS.size() > 1);

        lastPlayedTrack = nextTrack; // Update the last played track to the new one

        return nextTrack;
    }

    public static void setCurrentSong(SoundInstance songInstance) {
        currentSongInstance = songInstance;
    }

    public static SoundInstance getCurrentSongInstance() {
        return currentSongInstance;
    }
}