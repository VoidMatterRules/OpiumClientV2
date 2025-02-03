package we.devs.opium.client.modules.miscellaneous;

import net.minecraft.entity.player.PlayerEntity;
import we.devs.opium.Opium;
import we.devs.opium.client.events.DeathEvent;
import we.devs.opium.api.manager.module.Module;
import we.devs.opium.api.manager.module.RegisterModule;

import java.util.Objects;
import java.util.Random;

@RegisterModule(name = "AutoEZ", description = "Gives out Messages to the Chat on a Player Kill.", category = Module.Category.MISCELLANEOUS)
public class ModuleAutoEZ extends Module {
    private final String[] EZ = new String[] {
            "<player> dumped on by 0piumh4ck.cc lel pooron!",
            "<player> DESTROYED by 0piumh4ck.cc LOL!!!",
            "EZZZ <player> pooron owned by 0piumh4ck.cc!",
            "0piumh4ck.cc owns you <player>!",
            "EZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZZ <player> -0piumh4ck.cc",
            "LOL <player> DUMB DOG DIES TO 0piumh4ck.cc LOL!! EZ!!!",
            "<player> sent to the death screen by 0piumh4ck.cc lel"
    };

    @Override
    public void onDeath(DeathEvent event) {
        if (mc.player == null) {
            return;
        }
        if (Objects.equals(event.getEntity().getName().getString(), mc.player.getName().getString()) || Opium.FRIEND_MANAGER.isFriend(event.getEntity().getName().getString())) {
            return;
        }
        if (event.getEntity() instanceof PlayerEntity) {
            Random random = new Random();
            String msg = EZ[random.nextInt(EZ.length - 1)];
            mc.player.networkHandler.sendChatMessage(msg.replace("<player>", event.getEntity().getName().getString()));
        }
    }
}
