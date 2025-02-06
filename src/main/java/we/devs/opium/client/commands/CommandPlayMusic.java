package we.devs.opium.client.commands;

import we.devs.opium.Opium;
import we.devs.opium.api.manager.command.Command;
import we.devs.opium.api.manager.command.RegisterCommand;
import we.devs.opium.api.utilities.ChatUtils;
import we.devs.opium.client.modules.client.ModuleCommands;

import static we.devs.opium.api.manager.music.CustomMusicPlayer.playCustomMusic;

@RegisterCommand(name="play", description="Bitch ass Test Command Should delete if i forgett", syntax="nigga <name>", aliases={"pl"})
public class CommandPlayMusic extends Command {
    @Override
    public void onCommand(String[] args) {
        if (args.length == 1) {
            playCustomMusic(args[0]);
            ChatUtils.sendMessage(args[0] + " is playing!");
        } else {
            this.sendSyntax();
        }
    }
}
