package we.devs.opium.api.manager;

import net.minecraft.util.Identifier;

public class CapeManager {
    private static Identifier currentCape = Identifier.of("opium", "textures/capes/cape.png");

    public static Identifier getCape() {
        return currentCape;
    }

    public static void setCurrentCape(Identifier cape) {
        currentCape = cape;
    }
}