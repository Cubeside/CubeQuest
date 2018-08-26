package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.Reward;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.ItemStackUtil;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class SetRewardItemsCommand extends SubCommand implements Listener {
    
    public static final String SUCCESS_COMMAND_PATH = "setSuccessRewardItems";
    public static final String FULL_SUCCESS_COMMAND = "quest " + SUCCESS_COMMAND_PATH;
    public static final String FAIL_COMMAND_PATH = "setFailRewardItems";
    public static final String FULL_FAIL_COMMAND = "quest " + FAIL_COMMAND_PATH;
    
    private boolean success;
    
    private Set<UUID> currentlyEditing;
    
    public SetRewardItemsCommand(boolean success) {
        this.success = success;
        this.currentlyEditing = new HashSet<>();
        Bukkit.getPluginManager().registerEvents(this, CubeQuest.getInstance());
        CubeQuest.getInstance().getEventListener()
                .addOnPlayerQuit(player -> this.currentlyEditing.remove(player.getUniqueId()));
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
        if (!this.currentlyEditing.add(player.getUniqueId())) {
            ChatAndTextUtil.sendWarningMessage(sender, "Du bearbeitest bereits eine Belohnung.");
            return true;
        }
        
        Reward formerReward = this.success ? quest.getSuccessReward() : quest.getFailReward();
        Inventory inventory = Bukkit.createInventory(player, 27,
                (this.success ? "Erfolgsbelohnung" : "Trostpreis") + " [Quest " + quest.getId()
                        + "]");
        if (formerReward != null) {
            inventory.addItem(formerReward.getItems());
        }
        player.openInventory(inventory);
        
        return true;
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClosedEvent(InventoryCloseEvent event) {
        if (!this.currentlyEditing.remove(event.getPlayer().getUniqueId())) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        
        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(player);
        if (quest == null) {
            ChatAndTextUtil.sendWarningMessage(player,
                    "Du bearbeitest keine Quest mehr, kein Belohnung gesetzt!");
            return;
        }
        
        Reward formerReward = this.success ? quest.getSuccessReward() : quest.getFailReward();
        int cubes = formerReward == null ? 0 : formerReward.getCubes();
        int questPoints = formerReward == null ? 0 : formerReward.getQuestPoints();
        int xp = formerReward == null ? 0 : formerReward.getXp();
        
        ItemStack[] items = ItemStackUtil.shrinkItemStack(event.getInventory().getContents());
        event.getInventory().clear();
        event.getInventory().addItem(items);
        items = event.getInventory().getContents();
        
        Reward resultReward = new Reward(cubes, questPoints, xp, items);
        
        if (this.success) {
            quest.setSuccessReward(resultReward);
        } else {
            quest.setFailReward(resultReward);
        }
        
        if (resultReward.isEmpty()) {
            ChatAndTextUtil.sendNormalMessage(player,
                    (this.success ? "Erfolgsbelohnung" : "Trostpreis") + " für "
                            + quest.getTypeName() + " [" + quest.getId() + "] entfernt.");
        } else {
            ChatAndTextUtil.sendNormalMessage(player,
                    "Items in " + (this.success ? "Erfolgsbelohnung" : "Trostpreis") + " für "
                            + quest.getTypeName() + " [" + quest.getId() + "] gesetzt.");
        }
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
