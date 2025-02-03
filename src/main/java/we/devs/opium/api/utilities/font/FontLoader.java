package we.devs.opium.api.utilities.font;

import we.devs.opium.Opium;
import net.minecraft.client.MinecraftClient;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;

public class FontLoader {
    private static final String FONTS_FOLDER = "opium/fonts";
    private static final String ICON_FONTS_FOLDER = "opium/fonts/icons";
    private static final String[] DEFAULT_FONT = {"font.ttf"};

    public static Font[] loadFonts() {
        return loadFonts(FONTS_FOLDER, DEFAULT_FONT);
    }

    private static Font[] loadFonts(String folder, String[] defaultFonts) {
        File gameDir = MinecraftClient.getInstance().runDirectory;
        File fontsDir = new File(gameDir, folder);
        if (!fontsDir.exists()) {
            fontsDir.mkdirs();
        }

        for (String s : defaultFonts) {
            File defaultFontFile = new File(fontsDir, s);
            try (InputStream inputStream = FontLoader.class.getResourceAsStream("/assets/" + FONTS_FOLDER + "/" + s)) {
                assert inputStream != null;
                Files.copy(inputStream, defaultFontFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ignored) {
            }
        }

        LinkedList<Font> fonts = new LinkedList<>();
        for (File file : Objects.requireNonNull(fontsDir.listFiles())) {
            if (file.isFile() && (file.getName().toLowerCase().endsWith(".ttf") || file.getName().toLowerCase().endsWith(".otf"))) {
                try {
                    Font[] fontArray = Font.createFonts(file);
                    Collections.addAll(fonts, fontArray);
                } catch (FontFormatException | IOException e) {
                    Opium.LOGGER.error("An error has occurred while converting file to font format", e);
                }
            }
        }

        return fonts.toArray(new Font[0]);
    }
}
