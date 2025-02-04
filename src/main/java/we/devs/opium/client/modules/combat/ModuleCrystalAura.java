package we.devs.opium.client.modules.combat;

import we.devs.opium.client.values.impl.ValueBoolean;
import we.devs.opium.client.values.impl.ValueCategory;
import we.devs.opium.client.values.impl.ValueNumber;

public class ModuleCrystalAura {

    ValueBoolean place = new ValueBoolean("Place", "Place", "Places Crystals", true);
    ValueBoolean explode = new ValueBoolean("Explode", "Explode", "Explodes Crystals", true);
    ValueBoolean onlyOwn = new ValueBoolean("OnlyOwn", "OnlyOwn", "Only Explodes own Crystals", true);
    ValueBoolean antiWeakness = new ValueBoolean("AntiWeakness", "AntiWeakness", "Uses a Sword or other Tool to counteract weakness.", true);

    ValueCategory rangeCategory = new ValueCategory("Ranges", "All range related settings");
    ValueNumber targetRange = new ValueNumber("TargetRange", "TargetRange", "Range at Which at Player will be included for the Damage calculation", this.rangeCategory, 12.5, 1.0, 20.0);

}

