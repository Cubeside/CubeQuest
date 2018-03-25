package de.iani.cubequest.commands;

import com.google.common.base.Verify;
import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.generation.ClickInteractorQuestSpecification;
import de.iani.cubequest.generation.DeliveryQuestSpecification.DeliveryQuestPossibilitiesSpecification;
import de.iani.cubequest.generation.DeliveryQuestSpecification.DeliveryReceiverSpecification;
import de.iani.cubequest.generation.QuestGenerator;
import de.iani.cubequest.interaction.PlayerInteractInteractorEvent;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.HashMap;
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

public class AddOrRemoveInteractorForSpecificationCommand extends SubCommand implements Listener {
    
    private InteractorRequiredFor requiredFor;
    
    private Map<UUID, Object> currentlySelectingInteractor;
    
    public enum InteractorRequiredFor {
        CLICK_Interactor_SPECIFICATION("addClickInteractorQuestSpecification", true, false),
        ADD_DELIVERY_TARGET_SPECIFICATION("addDeliveryReceiverSpecification", true, true),
        REMOVE_DELIVERY_TARGET_SPECIFICATION("removeDeliveryReceiverSpecification", false, true);
        
        public final String command;
        public final boolean adds;
        public final boolean mapsToName;
        
        private InteractorRequiredFor(String command, boolean adds, boolean mapsToName) {
            this.command = command;
            this.adds = adds;
            this.mapsToName = mapsToName;
        }
    }
    
    public AddOrRemoveInteractorForSpecificationCommand(InteractorRequiredFor requiredFor) {
        Verify.verifyNotNull(requiredFor);
        this.requiredFor = requiredFor;
        
        if (!CubeQuest.getInstance().hasCitizensPlugin()) {
            return;
        }
        
        this.currentlySelectingInteractor = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, CubeQuest.getInstance());
    }
    
    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteractInteractorEvent(PlayerInteractInteractorEvent event) {
        Object removed = this.currentlySelectingInteractor.remove(event.getPlayer().getUniqueId());
        if (removed == null) {
            return;
        }
        
        event.setCancelled(true);
        
        if (this.requiredFor == InteractorRequiredFor.CLICK_Interactor_SPECIFICATION) {
            ClickInteractorQuestSpecification specification =
                    (ClickInteractorQuestSpecification) removed;
            specification.setInteractor(event.getInteractor());
            QuestGenerator.getInstance().addPossibleQuest(specification);
            ChatAndTextUtil.sendNormalMessage(event.getPlayer(),
                    "Neue Klick-Interactor-Quest-Spezifikation erfolgreich erstellt!");
            return;
        } else if (this.requiredFor.mapsToName) {
            String name = (String) removed;
            DeliveryReceiverSpecification specification = new DeliveryReceiverSpecification();
            specification.setName(ChatAndTextUtil.convertColors(name));
            specification.setInteractor(event.getInteractor());
            
            DeliveryQuestPossibilitiesSpecification instance =
                    DeliveryQuestPossibilitiesSpecification.getInstance();
            boolean result = this.requiredFor.adds ? instance.addTarget(specification)
                    : instance.removeTarget(specification);
            if (result) {
                ChatAndTextUtil.sendNormalMessage(event.getPlayer(), "Lieferungsziel erfolgreich "
                        + (this.requiredFor.adds ? "hinzugefügt" : "entfernt") + ".");
            } else {
                ChatAndTextUtil.sendWarningMessage(event.getPlayer(), "Dieses Lieferungsziel war "
                        + (this.requiredFor.adds ? "bereits" : "nicht") + " eingetragen.");
            }
        } else {
            assert (false);
        }
        
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
                    "Auf diesem Server ist das Citizens-Plugin nicht installiert!");
            return true;
        }
        
        return onCommandInternal(sender, args);
    }
    
    private boolean onCommandInternal(CommandSender sender, ArgsParser args) {
        
        Object mapTo;
        
        if (this.requiredFor == InteractorRequiredFor.CLICK_Interactor_SPECIFICATION) {
            if (args.remaining() < 1) {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Bitte gib die Schwierigkeit des Interactors an.");
                return true;
            }
            
            double difficulty = args.getNext(Double.MIN_VALUE);
            if (difficulty <= 0.0 || difficulty > 1.0) {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Bitte gib die Schwierigkeit als Kommazahl echt größer 0 und kleiner gleich 1 an.");
                return true;
            }
            
            String[] messages = args.getAll("").split("\\|");
            if (messages.length != 2) {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Bitte gib den Namen des Interactors und die Vergabenachricht an, getrennt von einem |.");
                return true;
            }
            
            ClickInteractorQuestSpecification specification =
                    new ClickInteractorQuestSpecification();
            specification.setDifficulty(difficulty);
            specification.setInteractorName(ChatAndTextUtil.convertColors(messages[0]));
            specification.setGiveMessage(ChatAndTextUtil.convertColors(messages[1]));
            
            mapTo = specification;
        } else if (this.requiredFor.mapsToName) {
            if (!args.hasNext()) {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Bitte gib den Namen des Lieferungsempfängers an.");
                return true;
            }
            
            String name = args.getAll("");
            mapTo = name;
        } else {
            assert (false);
            mapTo = null;
        }
        
        this.currentlySelectingInteractor.put(((Player) sender).getUniqueId(), mapTo);
        ChatAndTextUtil.sendNormalMessage(sender,
                "Bitte rechtsklicke den Interactor für diese Spezifikation. Rechtsklicke irgendetwas anderes, um die Auswahl abzubrechen.");
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUEST_SPECIFICATIONS_PERMISSION;
    }
    
    @Override
    public boolean requiresPlayer() {
        return true;
    }
    
}
