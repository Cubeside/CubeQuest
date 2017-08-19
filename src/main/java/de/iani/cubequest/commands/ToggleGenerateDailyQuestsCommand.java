package de.iani.cubequest.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import de.iani.cubequest.CubeQuest;

public class ToggleGenerateDailyQuestsCommand extends SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

        boolean doesGenerate = CubeQuest.getInstance().isGeneratingDailyQuests();
        if (!args.hasNext()) {
            CubeQuest.sendWarningMessage(sender, "Bitte gib an, ob der Server DailyQuests generieren soll (true | false). (Derzeit: " + doesGenerate + ")");
            return true;
        }

        String arg = args.getNext();
        if (Arrays.asList(new String[] {"t", "true", "y", "yes", "j", "ja"}).contains(arg.toLowerCase())) {
            CubeQuest.getInstance().setGenerateDailyQuests(true);
            if (doesGenerate) {
                CubeQuest.sendNormalMessage(sender, "Der Server generiert bereits DailyQuests.");
            } else {
                CubeQuest.sendNormalMessage(sender, "Der Server generiert nun DailyQuests.");
            }
        } else if (Arrays.asList(new String[] {"f", "false", "n", "no", "nein"}).contains(arg.toLowerCase())) {
            CubeQuest.getInstance().setGenerateDailyQuests(true);
            if (doesGenerate) {
                CubeQuest.sendNormalMessage(sender, "Der Server generiert nun keine DailyQuests mehr.");
            } else {
                CubeQuest.sendNormalMessage(sender, "Der Server generierte bereits keine DailyQuests.");
            }
        } else {
            CubeQuest.sendWarningMessage(sender, "Bitte gib an, ob der Server DailyQuests generieren soll (true oder false).");
        }

        return true;
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.TOGGLE_CREATE_DAILY_QUESTS_PERMISSION;
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
