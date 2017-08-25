package de.iani.cubequest.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.util.ChatUtil;

public class TogglePayRewardsCommand extends SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

        boolean doesPay = CubeQuest.getInstance().isPayRewards();
        if (!args.hasNext()) {
            ChatUtil.sendWarningMessage(sender, "Bitte gib an, ob der Server Belohnungen verteilen soll (true | false). (Derzeit: " + doesPay + ")");
            return true;
        }

        String arg = args.getNext();
        if (Arrays.asList(new String[] {"t", "true", "y", "yes", "j", "ja"}).contains(arg.toLowerCase())) {
            CubeQuest.getInstance().setPayRewards(true);
            if (doesPay) {
                ChatUtil.sendNormalMessage(sender, "Der Server verteilt bereits Belohnungen.");
            } else {
                ChatUtil.sendNormalMessage(sender, "Der Server verteilt nun Belohnungen.");
            }
        } else if (Arrays.asList(new String[] {"f", "false", "n", "no", "nein"}).contains(arg.toLowerCase())) {
            CubeQuest.getInstance().setPayRewards(false);
            if (doesPay) {
                ChatUtil.sendNormalMessage(sender, "Der Server verteilt nun keine Belohnungen mehr.");
            } else {
                ChatUtil.sendNormalMessage(sender, "Der Server verteilt bereits keine Belohnungen.");
            }
        } else {
            ChatUtil.sendWarningMessage(sender, "Bitte gib an, ob der Server Belohnungen verteilen soll (true oder false).");
        }

        return true;
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.TOGGLE_SERVER_PROPERTIES_PERMISSION;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        String arg = args.getNext("");
        List<String> result = Arrays.asList(new String[] {"true", "false", "yes", "no", "ja", "nein"});
        result.removeIf(s -> {
            return !s.startsWith(arg.toLowerCase());
        });
        return result;
    }

}
