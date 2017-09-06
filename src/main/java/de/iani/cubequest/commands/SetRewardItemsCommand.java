package de.iani.cubequest.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.Reward;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;

public class SetRewardItemsCommand extends SubCommand {

    private boolean success;

    public SetRewardItemsCommand(boolean success) {
        this.success = success;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {
        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }

        Player player = (Player) sender;    // sicher wegen requiresPlayer returns true
        Reward formerReward = success? quest.getSuccessReward() : quest.getFailReward();
        ItemStack[] newContent = player.getInventory().getContents();

        Reward resultReward = new Reward(formerReward == null? 0 : formerReward.getCubes(), newContent);
        if (success) {
            quest.setSuccessReward(resultReward);
        } else {
            quest.setFailReward(resultReward);
        }

        if (resultReward.isEmpty()) {
            ChatAndTextUtil.sendNormalMessage(sender, (success? "Erfolgsbelohnung" : "Trostpreis") + " für " + quest.getTypeName() + " [" + quest.getId() + "] entfernt.");
        } else {
            ChatAndTextUtil.sendNormalMessage(sender, "Items in " + (success? "Erfolgsbelohnung" : "Trostpreis") + " für " + quest.getTypeName() + " [" + quest.getId() + "] gesetzt.");
        }

        return true;
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }

}
