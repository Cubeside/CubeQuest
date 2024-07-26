package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.Collection;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;


public class ReloadQuestCommand extends SubCommand {

    public static final String COMMAND_PATH = "reloadQuest";
    public static final String FULL_COMMAND = "quest " + COMMAND_PATH;
    public static final String ALL_STRING = "ALL";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Quest an, die neugeladen werden soll (oder ",
                    ALL_STRING, " um alle Quests neuzuladen).");
            return true;
        }

        if (args.seeAll("").equalsIgnoreCase(ALL_STRING)) {
            ChatAndTextUtil.sendNormalMessage(sender, "Lade alle Quests neu...");
            CubeQuest.getInstance().getQuestCreator().loadQuests();
            ChatAndTextUtil.sendNormalMessage(sender, "Alle Quests neugeladen (nur auf diesem Server).");
            return true;
        }

        Quest quest;
        String idString = args.seeAll("");
        try {
            int questId = Integer.parseInt(idString);
            quest = CubeQuest.getInstance().getQuestCreator().loadOrRefresh(questId);
        } catch (NumberFormatException e) {
            quest = ChatAndTextUtil.getQuest(sender, args, FULL_COMMAND, "", "Quest ", " neuladen");
            CubeQuest.getInstance().getQuestCreator().refreshQuest(quest);
        }

        quest.updateIfReal();
        ChatAndTextUtil.sendNormalMessage(sender, quest, " neugeladen (auf allen Servern).");
        return true;
    }

    @Override
    public Collection<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        args.getNext("");
        if (!args.hasNext()) {
            return List.of(ALL_STRING);
        }
        return List.of();
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }

}
