package we.devs.opium.client.commands;

import we.devs.opium.api.manager.command.Command;
import we.devs.opium.api.manager.command.RegisterCommand;
import we.devs.opium.api.manager.miscellaneous.UUIDManager;
import we.devs.opium.api.utilities.ChatUtils;

import java.util.UUID;

@RegisterCommand(name = "Opium", description = "Adds a uuid to the uuidmanager [TEST MODULE NEED TO REMOVE BEFORE RELEASE]", syntax = "opium <name>", aliases = {"opium", "uuidm"})
public class CommandOpium extends Command {

    @Override
    public void onCommand(String[] args) {
        if (args.length >= 1) {
            UUIDManager.addPlayerUUID(UUID.fromString(args[0]));
            ChatUtils.sendMessage("Added " + args[0] + " to the UUID Manager", "UUID Manager");
            ChatUtils.sendMessage("Your UUID Is: " + mc.player.getUuid(), "UUID Manager");
        }
    }
}
