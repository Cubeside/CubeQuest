package de.iani.cubequest.commands.edit;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.commands.ArgsParser;
import de.iani.cubequest.commands.SubCommand;
import de.iani.cubequest.quests.DeliveryQuest;
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

public class SetDeliveryInventoryCommand extends SubCommand implements Listener {
    
    public static final String COMMAND_PATH = "setDelivery";
    public static final String FULL_COMMAND = "quest " + COMMAND_PATH;
    
    private Set<UUID> currentlyEditing;
    
    public SetDeliveryInventoryCommand() {
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
        
        if (!(quest instanceof DeliveryQuest)) {
            ChatAndTextUtil.sendWarningMessage(sender, "Diese Quest erfordert keine Lieferung.");
            return true;
        }
        
        Player player = (Player) sender; // sicher wegen requiresPlayer returns true
        if (!this.currentlyEditing.add(player.getUniqueId())) {
            ChatAndTextUtil.sendWarningMessage(sender, "Du bearbeitest bereits eine Lieferung.");
            return true;
        }
        
        Inventory inventory = Bukkit.createInventory(player, 27,
                ("Lierefungsumfang [Quest " + quest.getId() + "]"));
        inventory.addItem(((DeliveryQuest) quest).getDelivery());
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
                    "Du bearbeitest keine Quest mehr, keine Lieferung gesetzt!");
            return;
        }
        
        if (!(quest instanceof DeliveryQuest)) {
            ChatAndTextUtil.sendWarningMessage(player,
                    "Du bearbeitest keine Lieferungsquest mehr, keine Lieferung gesetzt!");
            return;
        }
        
        ItemStack[] items = ItemStackUtil.shrinkItemStack(event.getInventory().getContents());
        event.getInventory().clear();
        event.getInventory().addItem(items);
        items = event.getInventory().getContents();
        
        ((DeliveryQuest) quest).setDelivery(items);
        
        ChatAndTextUtil.sendNormalMessage(player, "Lieferugsnumfang für " + quest.getTypeName()
                + " [" + quest.getId() + "] gesetzt.");
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
