package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.interaction.PlayerInteractInteractorEvent;
import de.iani.cubequest.questGiving.QuestGiver;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class AddQuestGiverCommand extends SubCommand implements Listener {
    
    private Map<UUID, String> currentlySelectingInteractor;
    
    public AddQuestGiverCommand() {
        if (!CubeQuest.getInstance().hasCitizensPlugin()) {
            return;
        }
        
        initInternal();
    }
    
    private void initInternal() {
        Bukkit.getPluginManager().registerEvents(this, CubeQuest.getInstance());
        this.currentlySelectingInteractor = new HashMap<>();
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteractInteractorEvent(PlayerInteractInteractorEvent event) {
        String name = this.currentlySelectingInteractor.remove(event.getPlayer().getUniqueId());
        if (name == null) {
            return;
        }
        
        event.setCancelled(true);
        
        if (CubeQuest.getInstance().getQuestGiver(name) != null) {
            ChatAndTextUtil.sendWarningMessage(event.getPlayer(),
                    "In der Zwischenzeit wurde bereits ein QuestGiver mit diesem Namen angelegt. Auswahl abgebrochen.");
            return;
        }
        QuestGiver other = CubeQuest.getInstance().getQuestGiver(event.getInteractor());
        if (other != null) {
            ChatAndTextUtil.sendWarningMessage(event.getPlayer(),
                    "Dieser Interactor ist bereits als QuestGiver mit dem Namen " + other.getName()
                            + " eingetragen.");
            return;
        }
        
        CubeQuest.getInstance().addQuestGiver(new QuestGiver(event.getInteractor(), name));
        ChatAndTextUtil.sendNormalMessage(event.getPlayer(), "QuestGiver " + name + " gesetzt!");
    }
    
    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) {
            return;
        }
        if (this.currentlySelectingInteractor.remove(event.getPlayer().getUniqueId()) != null) {
            ChatAndTextUtil.sendWarningMessage(event.getPlayer(), "Auswahl abgebrochen.");
        }
    }
    
    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        if (this.currentlySelectingInteractor.remove(event.getPlayer().getUniqueId()) != null) {
            ChatAndTextUtil.sendWarningMessage(event.getPlayer(), "Auswahl abgebrochen.");
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args) {
        
        if (this.currentlySelectingInteractor == null) {
            ChatAndTextUtil.sendErrorMessage(sender,
                    "Auf dem Server müssen das Citizens-Plugin und die InteractiveBooksAPI installiert sein, eins von beidem ist nicht der Fall!");
            return true;
        }
        
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Bitte gib einen Namen für den neuen QuestGiver an.");
            return true;
        }
        
        String name = args.getNext();
        
        if (args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "QuestGiver-Namen dürfen keine Leerzeichen enthalten.");
            return true;
        }
        
        if (CubeQuest.getInstance().getQuestGiver(name) != null) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Einen QuestGiver mit diesem Namen gibt es bereits.");
            return true;
        }
        
        this.currentlySelectingInteractor.put(((Player) sender).getUniqueId(), name);
        ChatAndTextUtil.sendNormalMessage(sender,
                "Bitte rechtsklicke den Interactor für diesen QuestGiver. Rechtsklicke irgendetwas anderes, um die Auswahl abzubrechen.");
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUEST_GIVERS_PERMISSION;
    }
    
    @Override
    public boolean requiresPlayer() {
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias,
            ArgsParser args) {
        return Collections.emptyList();
    }
    
    @Override
    public String getUsage() {
        return "<QuestGiverName>";
    }
    
}
