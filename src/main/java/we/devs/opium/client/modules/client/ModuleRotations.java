package we.devs.opium.client.modules.client;

import we.devs.opium.api.manager.module.Module;
import we.devs.opium.api.manager.module.RegisterModule;
import we.devs.opium.client.values.impl.ValueCategory;
import we.devs.opium.client.values.impl.ValueEnum;
import we.devs.opium.client.values.impl.ValueNumber;

@RegisterModule(name="Rotations", description="Rotations of the client", category=Module.Category.CLIENT)
public class ModuleRotations extends Module {
    public static ModuleRotations INSTANCE;
    // BLOCK SETTINGS
    ValueCategory blockCategory = new ValueCategory("Block Rotations","Settings for block rotations");
    public ValueNumber blockSmoothness = new ValueNumber("Smoothness", "Smoothness", "",this.blockCategory, 60, 1, 100);
    public ValueEnum blockRotationType = new ValueEnum("Type","Type","What type of rotations to use for block rotations.",this.blockCategory,blockRotations.Packet);

    //ENTITY SETTINGS
    ValueCategory entityCategory = new ValueCategory("Entity Rotations","Settings for entity rotations");
    public ValueNumber entitySmoothness = new ValueNumber("Smoothness", "Smoothness", "",this.entityCategory, 60, 1, 100);
    public ValueEnum entityRotationType = new ValueEnum("Type","Type","What type of rotations to use for entity rotations.",this.entityCategory,entityRotations.NCP);


    public blockRotations getBlockRotations() {
        if (blockRotationType.equals(blockRotations.Packet)) {
            return  blockRotations.Packet;
        }
        if (blockRotationType.equals(blockRotations.NCP)) {
            return  blockRotations.NCP;
        }
        if (blockRotationType.equals(blockRotations.Grim)) {
            return  blockRotations.Grim;
        }
        return null;
    }

    public int getBlockSmooth() {
        return (int) blockSmoothness.getValue();
    }

    public blockRotations getEntityRotations() {
        if (blockRotationType.equals(blockRotations.Packet)) {
            return  blockRotations.Packet;
        }
        if (blockRotationType.equals(blockRotations.NCP)) {
            return  blockRotations.NCP;
        }
        if (blockRotationType.equals(blockRotations.Grim)) {
            return  blockRotations.Grim;
        }
        return null;
    }

    public int getEntitySmooth() {
        return (int) blockSmoothness.getValue();
    }

    public ModuleRotations() {
        INSTANCE = this;
    }

    public static ModuleRotations getInstance() {
        return INSTANCE;
    }

    public enum blockRotations {
        Packet,
        NCP,
        Grim
    }
    public enum entityRotations {
        Packet,
        NCP,
        Grim
    }

}
