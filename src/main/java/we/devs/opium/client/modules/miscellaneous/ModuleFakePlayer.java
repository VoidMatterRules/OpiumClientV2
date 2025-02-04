package we.devs.opium.client.modules.miscellaneous;

import com.google.common.eventbus.Subscribe;
import we.devs.opium.api.events.DisconnectEvent;
import we.devs.opium.api.events.PushEntityEvent;
import we.devs.opium.api.manager.module.Module;
import we.devs.opium.api.manager.module.RegisterModule;
import we.devs.opium.api.utilities.FakePlayerEntity;

@RegisterModule(
        name = "FakePlayer",
        description = "Used for configing.",
        category = Module.Category.MISCELLANEOUS
)
public class ModuleFakePlayer extends Module {
    private FakePlayerEntity fakePlayer;

    public void onEnable() {
        if (mc.player != null && mc.world != null) {
            FakePlayerEntity fakePlayer = new FakePlayerEntity(mc.player, "Opium-Fake-Player", 20.0f, true);
            fakePlayer.spawn();
        }
    }

    public void onDisable() {
        if (this.fakePlayer != null) {
            this.fakePlayer.despawn();
            this.fakePlayer = null;
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        this.fakePlayer = null;
        this.disable(true);
    }

    @Subscribe
    public void onPushEntity(PushEntityEvent event) {
        if (event.getPushed().equals(mc.player) && event.getPusher().equals(this.fakePlayer)) {
            event.cancel();
        }
    }

}
