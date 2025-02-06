package we.devs.opium.client.modules.client;

import net.minecraft.util.Identifier;
import we.devs.opium.api.manager.module.Module;
import we.devs.opium.api.manager.module.RegisterModule;
import we.devs.opium.api.utilities.ChatUtils;
import we.devs.opium.client.values.impl.*;

import java.awt.*;

@RegisterModule(name="Capes", description="Manages the client's capes", category=Module.Category.CLIENT, persistent=true)
public class ModuleCapes extends Module {
    public static ModuleCapes INSTANCE;
    public static final ValueEnum Cape = new ValueEnum("Mode", "Mode", "Lets You Change Capes", Mode.ORIGINAL);

    public ModuleCapes() {
        INSTANCE = this;
    }

    private enum Mode {
        ORIGINAL,
        Dreizz1,
        Dreizz2
    }

    public static Identifier getCape() {
        if (Mode.ORIGINAL == Cape.getValue()) {
            return Identifier.of("opium", "textures/capes/original.png");
        } else if (Mode.Dreizz2 == Cape.getValue()) {return Identifier.of("opium", "textures/capes/dreizz2.png");}
        else {return Identifier.of("opium", "textures/capes/dreizz1.png");}
    }
}