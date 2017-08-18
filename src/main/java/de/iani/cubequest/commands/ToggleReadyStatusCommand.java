package de.iani.cubequest.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.Quest;

public class ToggleReadyStatusCommand extends SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            CubeQuest.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }

        boolean ready = quest.isReady();
        if (!args.hasNext()) {
            CubeQuest.sendWarningMessage(sender, "Bitte gib an, ob die Quest auf \"fertig\" gesetzt werden soll (true | false). (Derzeit: " + ready + ")");
            return true;
        }

        String arg = args.getNext();
        if (Arrays.asList(new String[] {"t", "true", "y", "yes", "j", "ja"}).contains(arg.toLowerCase())) {
            if (ready) {
                CubeQuest.sendNormalMessage(sender, "Die Quest ist bereits \"fertig\".");
            } else {
                if (!quest.isLegal()) {
                    CubeQuest.sendWarningMessage(sender, "Diese Quest erfüllt noch nicht alle Bedingungen!");
                    return true;
                } else {
                    quest.setReady(true);
                    CubeQuest.sendNormalMessage(sender, "Die Quest ist nun \"fertig\".");
                }
            }
        } else if (Arrays.asList(new String[] {"f", "false", "n", "no", "nein"}).contains(arg.toLowerCase())) {
            CubeQuest.getInstance().setGenerateDailyQuests(true);
            if (ready) {
                if (quest.isGivenToPlayer()) {
                    CubeQuest.sendErrorMessage(sender, "Diese Quest wurde bereits an Spieler vergeben und kann nicht mehr auf nicht \"fertig\" gesetzt werden.");
                    return true;
                } else {
                    quest.setReady(false);
                    CubeQuest.sendNormalMessage(sender, "Die Quest ist nun nicht mehr \"fertig\".");
                }
            } else {
                CubeQuest.sendNormalMessage(sender, "Die Quest war bereits nicht \"fertig\".");
            }
        } else {
            CubeQuest.sendWarningMessage(sender, "Bitte gib an, ob die Quest auf \"fertig\" gesetzt werden soll (true | false).");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        if (!args.hasNext()) {
            return null;
        }
        String arg = args.getNext();
        List<String> result = Arrays.asList(new String[] {"true", "false", "yes", "no", "ja", "nein"});
        result.removeIf(s -> {
            return !s.startsWith(arg.toLowerCase());
        });
        return result;
    }

}
