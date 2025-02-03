package we.devs.opium.asm.mixins;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import we.devs.opium.Opium;
import we.devs.opium.api.utilities.HWIDValidator;

@Mixin(MinecraftClient.class)
public class ClientTickMixin {

    @Unique
    int ticks = 0;

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        //if(!HWIDValidator.valid && MinecraftClient.getInstance().world != null) HWIDValidator.isHWIDValid(Opium.devEnv, true);
    }
}
