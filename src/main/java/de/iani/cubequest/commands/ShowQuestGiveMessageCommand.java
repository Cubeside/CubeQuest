package de.iani.cubequest.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;

public class ShowQuestGiveMessageCommand extends SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die ID der Quest an, deren Vergabe-Nachricht du noch einmal erhalten möchtest.");
            return true;
        }

        int id = args.getNext(-1);
        Quest quest = id > 0? QuestManager.getInstance().getQuest(id) : null;
        if (quest == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Eine Quest mit dieser ID gibt es nicht.");
            return true;
        }

        boolean hasQuest = (sender instanceof Player) && CubeQuest.getInstance().getPlayerData((Player) sender).isGivenTo(id);

        if (!hasQuest && !sender.hasPermission(CubeQuest.EDIT_QUESTS_PERMISSION)) {
            ChatAndTextUtil.sendErrorMessage(sender, "Du kannst dir nur die Vergabe-Nachricht von Quests anzeigen lassen, die bei dir gerade offen sind.");
            return true;
        }

        if (quest.getGiveMessage() == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Diese Quest hat keine Vergabe-Nachricht.");
            return true;
        }

        ChatAndTextUtil.sendNormalMessage(sender, "Vergabe-Nachricht zu Quest " + (quest.getName() == null? id : quest.getName()) + ":");
        sender.sendMessage(quest.getGiveMessage());
        return true;
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.ACCEPT_QUESTS_PERMISSION;
    }

}
