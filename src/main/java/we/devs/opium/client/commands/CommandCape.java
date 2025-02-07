package we.devs.opium.client.commands;

import net.minecraft.util.Identifier;
import we.devs.opium.api.manager.CapeManager;
import we.devs.opium.api.manager.command.Command;
import we.devs.opium.api.manager.command.RegisterCommand;
import we.devs.opium.api.utilities.ChatUtils;

@RegisterCommand(name="Cape", description="Let's you change your cape", syntax="cape <name>", aliases={"cape"})
public class CommandCape extends Command {
    @Override
    public void onCommand(String[] args) {
        if (args.length == 1) {
            setCape(args[0]);
            ChatUtils.sendMessage("Cape set to " + args[0],"Cape");
        } else {
            this.sendSyntax();
        }
    }

    private static void setCape(String capeName) {
        Identifier newCape = Identifier.of("opium", "textures/capes/" + capeName + ".png");
        CapeManager.setCurrentCape(newCape);
    }
}
