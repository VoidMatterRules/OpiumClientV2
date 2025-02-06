package we.devs.opium.asm.mixins;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import we.devs.opium.Opium;
import we.devs.opium.api.manager.miscellaneous.UUIDManager;
import we.devs.opium.client.modules.client.ModuleCapes;

import static we.devs.opium.api.utilities.IMinecraft.mc;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin {

    @Unique
    private static final Identifier CAPE = ModuleCapes.getCape() ;

    @Shadow @Nullable protected abstract PlayerListEntry getPlayerListEntry();

    @Unique
    private static @NotNull SkinTextures getModifiedSkinTexture(SkinTextures original) {
        return new SkinTextures(original.texture(), original.textureUrl(), AbstractClientPlayerEntityMixin.CAPE, original.elytraTexture(), original.model(), original.secure());
    }

    @ModifyReturnValue(method = "getSkinTextures", at = @At(value = "RETURN"))
    SkinTextures getSkinTextures(SkinTextures original) {
        if(this.getPlayerListEntry() != null && (UUIDManager.isAdded(this.getPlayerListEntry().getProfile().getId()))) {
            return getModifiedSkinTexture(original);
        }
        return original;
    }

}
