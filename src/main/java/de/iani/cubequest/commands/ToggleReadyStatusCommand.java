package de.iani.cubequest.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;

public class ToggleReadyStatusCommand extends SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }

        boolean ready = quest.isReady();
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib an, ob die Quest auf \"fertig\" gesetzt werden soll (true | false). (Derzeit: " + ready + ")");
            return true;
        }

        String arg = args.getNext();
        if (Arrays.asList(new String[] {"t", "true", "y", "yes", "j", "ja"}).contains(arg.toLowerCase())) {
            if (ready) {
                ChatAndTextUtil.sendNormalMessage(sender, "Die Quest ist bereits \"fertig\".");
            } else {
                if (!quest.isLegal()) {
                    ChatAndTextUtil.sendWarningMessage(sender, "Diese Quest erfüllt noch nicht alle Bedingungen!");
                    return true;
                } else {
                    quest.setReady(true);
                    ChatAndTextUtil.sendNormalMessage(sender, "Die Quest ist nun \"fertig\".");
                }
            }
        } else if (Arrays.asList(new String[] {"f", "false", "n", "no", "nein"}).contains(arg.toLowerCase())) {
            CubeQuest.getInstance().setGenerateDailyQuests(true);
            if (ready) {
                if (quest.isGivenToPlayer()) {
                    ChatAndTextUtil.sendErrorMessage(sender, "Diese Quest wurde bereits an Spieler vergeben und kann nicht mehr auf nicht \"fertig\" gesetzt werden.");
                    return true;
                } else {
                    quest.setReady(false);
                    ChatAndTextUtil.sendNormalMessage(sender, "Die Quest ist nun nicht mehr \"fertig\".");
                }
            } else {
                ChatAndTextUtil.sendNormalMessage(sender, "Die Quest war bereits nicht \"fertig\".");
            }
        } else {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib an, ob die Quest auf \"fertig\" gesetzt werden soll (true | false).");
        }

        return true;
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
