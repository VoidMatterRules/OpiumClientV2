package we.devs.opium.api.manager.music;

import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.util.WorldSavePath;

import java.io.File;
import java.nio.file.Path;

public class CustomMusicPlayer {

    public static void playCustomMusic(String fileName) {
        Path musicFolder = Path.of("mod_music");
        File musicFile = musicFolder.resolve(fileName).toFile();

        if (musicFile.exists()) {
            try {
                // Load the sound file as a resource
                Identifier soundId = Identifier.of("opium", fileName);
                SoundManager soundManager = MinecraftClient.getInstance().getSoundManager();

                // Create a SoundEvent dynamically
                SoundEvent soundEvent = SoundEvent.of(soundId);

                // Play the sound
                PositionedSoundInstance soundInstance = PositionedSoundInstance.master(soundEvent, 1.0F, 1.0F);
                soundManager.play(soundInstance);
            } catch (Exception e) {
                System.err.println("Failed to play sound: " + e.getMessage());
            }
        } else {
            System.err.println("Music file not found: " + musicFile.getAbsolutePath());
        }
    }
}