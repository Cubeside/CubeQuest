package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.interaction.Interactor;
import de.iani.cubequest.interaction.InteractorType;
import de.iani.cubequest.interaction.PlayerInteractInteractorEvent;
import de.iani.cubequest.quests.InteractorQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
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
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class SetOrRemoveQuestInteractorCommand extends SubCommand implements Listener {
    
    private boolean add;
    private Set<UUID> currentlySelectingInteractor = null;
    
    public SetOrRemoveQuestInteractorCommand(boolean add) {
        this.add = add;
        if (add) {
            this.currentlySelectingInteractor = new HashSet<>();
            Bukkit.getPluginManager().registerEvents(this, CubeQuest.getInstance());
            CubeQuest.getInstance().getEventListener()
                    .addOnPlayerQuit(player -> this.currentlySelectingInteractor.remove(player.getUniqueId()));
        }
    }
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteractInteractorEvent(PlayerInteractInteractorEvent<?> event) {
        if (!this.add) {
            return;
        }
        if (this.currentlySelectingInteractor.remove(event.getPlayer().getUniqueId())) {
            Bukkit.dispatchCommand(event.getPlayer(),
                    "quest setInteractor " + InteractorType.fromClass(event.getInteractor().getClass()) + " "
                            + event.getInteractor().getIdentifier().toString());
            event.setCancelled(true);
        }
    }
    
    @EventHandler(ignoreCancelled = false)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) {
            return;
        }
        if (!this.add) {
            return;
        }
        if (this.currentlySelectingInteractor.remove(event.getPlayer().getUniqueId())) {
            ChatAndTextUtil.sendWarningMessage(event.getPlayer(), "Auswahl abgebrochen.");
            event.setCancelled(true);
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {
        
        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }
        
        if (!(quest instanceof InteractorQuest)) {
            ChatAndTextUtil.sendWarningMessage(sender, "Diese Quest erfordert keinen Interactor.");
            return true;
        }
        
        Interactor interactor;
        if (!this.add) {
            interactor = null;
        } else {
            
            if (!args.hasNext()) {
                if (!(sender instanceof Player)) {
                    ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib einen Interactor-Typ an.");
                    return true;
                }
                if (this.currentlySelectingInteractor.add(((Player) sender).getUniqueId())) {
                    ChatAndTextUtil.sendNormalMessage(sender, "Bitte wähle durch Rechtsklick einen Interactor aus.");
                } else {
                    ChatAndTextUtil.sendWarningMessage(sender, "Du wählst bereits einen Interactor aus.");
                }
                return true;
            }
            
            String typeName = args.next();
            InteractorType type = InteractorType.fromString(typeName);
            if (type == null) {
                ChatAndTextUtil.sendWarningMessage(sender, typeName + " ist kein gültiger Interactor-Typ.");
                return true;
            }
            
            if (!args.hasNext()) {
                ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib einen Interactor-Identifier an.");
                return true;
            }
            
            String identifierString = args.next();
            Object identifier;
            try {
                identifier = CubeQuest.getInstance().getInteractorCreator().parseIdentifier(type, identifierString);
                if (identifier == null) {
                    throw new IllegalArgumentException("Unknown cause.");
                }
            } catch (Exception e) {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "\"" + identifierString + "\" ist kein gültiger Identifier für einen Interactor vom Typ " + type
                                + ": " + e.getMessage());
                return true;
            }
            
            interactor = CubeQuest.getInstance().getInteractorCreator().createInteractor(type, identifier);
            if (interactor == null) {
                ChatAndTextUtil.sendWarningMessage(sender, "Interactor nicht gefunden.");
                return true;
            }
        }
        
        ((InteractorQuest) quest).setInteractor(interactor);
        ChatAndTextUtil.sendNormalMessage(sender, this.add ? "Interactor gesetzt." : "Interactor entfernt.");
        
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        return Collections.emptyList();
    }
    
    @Override
    public String getUsage() {
        return "";
    }
    
}
