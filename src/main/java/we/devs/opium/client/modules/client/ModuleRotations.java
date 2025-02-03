package we.devs.opium.client.modules.client;

import we.devs.opium.api.manager.module.Module;
import we.devs.opium.api.manager.module.RegisterModule;
import we.devs.opium.client.values.impl.ValueNumber;

@RegisterModule(name="Rotations", description="Rotations of the client", category=Module.Category.CLIENT)
public class ModuleRotations extends Module {
    public static ModuleRotations INSTANCE;
    public ValueNumber smoothness = new ValueNumber("RotationSmoothness", "Smoothness", "", 60, 1, 100);

    public ModuleRotations() {
        INSTANCE = this;
    }
}
