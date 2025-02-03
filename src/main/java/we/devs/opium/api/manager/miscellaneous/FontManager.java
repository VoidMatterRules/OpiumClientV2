package we.devs.opium.api.manager.miscellaneous;

import me.x150.renderer.font.FontRenderer;
import we.devs.opium.api.utilities.font.FontLoader;
import we.devs.opium.api.utilities.font.FontRenderers;
import we.devs.opium.api.utilities.font.fxFontRenderer;

import java.awt.*;

public class FontManager {
    public static Font[] fonts;
    public static int hudFontSize = 8, clientFontSize = 8;
    public static FontManager INSTANCE = new FontManager();

    public FontManager() {
        refresh();
    }

    public void refresh() {
        fonts = FontLoader.loadFonts();
    }

    public void registerFonts() {
        FontRenderers.fontRenderer = new FontRenderer(fonts, hudFontSize); //Default font
        FontRenderers.fxfontRenderer = new fxFontRenderer(fonts, clientFontSize);

        FontRenderers.Super_Small_fxfontRenderer = new fxFontRenderer(fonts, 4f);

        FontRenderers.Small_fxfontRenderer = new fxFontRenderer(fonts, 6f);

        FontRenderers.Mid_fxfontRenderer = new fxFontRenderer(fonts, 8f);

        FontRenderers.Large_fxfontRenderer = new fxFontRenderer(fonts, 13f);
    }
}