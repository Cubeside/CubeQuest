package de.iani.cubequest.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.Reward;
import de.iani.cubequest.quests.Quest;

public class SetRewardInventoryCommand extends SubCommand {

    private boolean success;

    public SetRewardInventoryCommand(boolean success) {
        this.success = success;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {
        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            CubeQuest.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
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

        CubeQuest.sendNormalMessage(sender, "Items in " + (success? "Erfolgsbelohnung" : "Trostpreis") + " für " + quest.getTypeName() + " [" + quest.getId() + "] gesetzt.");

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
