package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.actions.QuestAction;
import de.iani.cubequest.actions.RewardAction;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class GetQuestRewardItemsCommand extends SubCommand {

    public static final String COMMAND_PATH = "getRewardItems";

    public static final String FULL_COMMAND = "quest " + COMMAND_PATH;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {
        Quest quest = ChatAndTextUtil.getQuest(sender, args, FULL_COMMAND + " ", "", "Belohnungs-Items von Quest ",
                " erhalten");
        if (quest == null) {
            return true;
        }

        int total = 0;
        int successful = 0;
        for (QuestAction action : quest.getSuccessActions()) {
            if (!(action instanceof RewardAction ra)) {
                continue;
            }
            total++;
            if (ra.getReward().giveItemsDirectly((Player) sender)) {
                successful++;
            }
        }
        if (total == 0) {
            ChatAndTextUtil.sendWarningMessage(sender, "Diese Quest hat keine Item-Belohnungen.");
        } else if (total == successful) {
            ChatAndTextUtil.sendNormalMessage(sender, "Items ausgegeben.");
        } else if (successful > 0) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Einige Items ausgegeben, andere passen nicht in dein Inventar.");
        } else {
            ChatAndTextUtil.sendWarningMessage(sender, "Die Items passen nicht in dein Inventar.");
        }

        return true;
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUEST_STATES_PERMISSION;
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }

}
