package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.generation.QuestGenerator;
import de.iani.cubequest.quests.EntityTypesAndAmountQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;


public class PerformUpdateCommand extends SubCommand {

    public static final String COMMAND_PATH = "performUpdate";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {
        if (!args.hasNext() || !args.next().equals("UPDATE")) {
            ChatAndTextUtil.sendErrorMessage(sender,
                    "Confirm with parameter UPDATE that you know what what you're doing.");
            return true;
        }
        ChatAndTextUtil.sendNormalMessage(sender, "Updating...");

        for (Quest quest : QuestManager.getInstance().getQuests()) {
            if (quest.performDataUpdate() || !quest.getGiveActions().isEmpty() || !quest.getSuccessActions().isEmpty()
                    || !quest.getFailActions().isEmpty() || quest instanceof EntityTypesAndAmountQuest) {
                quest.updateIfReal();
            }
        }

        QuestGenerator.getInstance().performDataUpdate();

        ChatAndTextUtil.sendNormalMessage(sender, "Update completed.");
        return true;
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.TOGGLE_SERVER_PROPERTIES_PERMISSION;
    }

}
