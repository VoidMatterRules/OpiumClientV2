package we.devs.opium.client.commands;

import we.devs.opium.api.manager.command.Command;
import we.devs.opium.api.manager.command.RegisterCommand;
import we.devs.opium.api.utilities.FakePlayerEntity;

@RegisterCommand(name="fakeplayer", description="Spawns A Fake Player", syntax="fakeplayer <name>", aliases={"fp"})
public class CommandFakePlayer extends Command {
    @Override
    public void onCommand(String[] args) {
        if (args.length == 1) {
            FakePlayerEntity fakePlayer = new FakePlayerEntity(mc.player, args[0], 20.0f, true);
            fakePlayer.spawn();
        } else {
            this.sendSyntax();
        }
    }
}
