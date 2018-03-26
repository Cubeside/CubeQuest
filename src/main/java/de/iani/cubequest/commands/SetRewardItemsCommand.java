package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.Reward;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class SetRewardItemsCommand extends SubCommand {
    
    private boolean success;
    
    public SetRewardItemsCommand(boolean success) {
        this.success = success;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args) {
        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }
        
        Player player = (Player) sender; // sicher wegen requiresPlayer returns true
        Reward formerReward = this.success ? quest.getSuccessReward() : quest.getFailReward();
        ItemStack[] newContent = player.getInventory().getContents();
        
        Reward resultReward =
                new Reward(formerReward == null ? 0 : formerReward.getCubes(), newContent);
        if (this.success) {
            quest.setSuccessReward(resultReward);
        } else {
            quest.setFailReward(resultReward);
        }
        
        if (resultReward.isEmpty()) {
            ChatAndTextUtil.sendNormalMessage(sender,
                    (this.success ? "Erfolgsbelohnung" : "Trostpreis") + " für "
                            + quest.getTypeName() + " [" + quest.getId() + "] entfernt.");
        } else {
            ChatAndTextUtil.sendNormalMessage(sender,
                    "Items in " + (this.success ? "Erfolgsbelohnung" : "Trostpreis") + " für "
                            + quest.getTypeName() + " [" + quest.getId() + "] gesetzt.");
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
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias,
            ArgsParser args) {
        return Collections.emptyList();
    }
    
}
