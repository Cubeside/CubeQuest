package de.iani.cubequest.commands;

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

import com.google.common.base.Verify;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.generation.ClickNPCQuestSpecification;
import de.iani.cubequest.generation.DeliveryQuestSpecification.DeliveryQuestPossibilitiesSpecification;
import de.iani.cubequest.generation.DeliveryQuestSpecification.DeliveryReceiverSpecification;
import de.iani.cubequest.generation.QuestGenerator;
import de.iani.cubequest.util.ChatAndTextUtil;
import net.citizensnpcs.api.event.NPCRightClickEvent;

public class AddOrRemoveNPCForSpecificationCommand extends SubCommand implements Listener {

    private NPCRequiredFor requiredFor;

    private Map<UUID, Object> currentlySelectingNPC;

    public enum NPCRequiredFor {
        CLICK_NPC_SPECIFICATION("addClickNPCQuestSpecification", true, false),
        ADD_DELIVERY_TARGET_SPECIFICATION("addDeliveryReceiverSpecification", true, true),
        REMOVE_DELIVERY_TARGET_SPECIFICATION("removeDeliveryReceiverSpecification", false, true);

        public final String command;
        public final boolean adds;
        public final boolean mapsToName;

        private NPCRequiredFor(String command, boolean adds, boolean mapsToName) {
            this.command = command;
            this.adds = adds;
            this.mapsToName = mapsToName;
        }
    }

    public AddOrRemoveNPCForSpecificationCommand(NPCRequiredFor requiredFor) {
        Verify.verifyNotNull(requiredFor);
        this.requiredFor = requiredFor;

        if (!CubeQuest.getInstance().hasCitizensPlugin()) {
            return;
        }

        initInternal();
    }

    private void initInternal() {
//        switch (requiredFor) {
//            case CLICK_NPC_SPECIFICATION: currentlySelectingNPC = new HashMap<UUID, ClickNPCQuestSpecification>(); break;
//            case ADD_DELIVERY_TARGET_SPECIFICATION:
//            case REMOVE_DELIVERY_TARGET_SPECIFICATION: currentlySelectingNPC = new HashMap<UUID, String>(); break;
//            default: throw new NullPointerException();
//        }
        currentlySelectingNPC = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, CubeQuest.getInstance());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onNPCClickEvent(NPCRightClickEvent event) {
        Object removed = currentlySelectingNPC.remove(event.getClicker().getUniqueId());
        if (removed == null) {
            return;
        }

        event.setCancelled(true);

        if (requiredFor == NPCRequiredFor.CLICK_NPC_SPECIFICATION) {
            ClickNPCQuestSpecification specification = (ClickNPCQuestSpecification) removed;
            specification.setNPC(event.getNPC().getId());
            QuestGenerator.getInstance().addPossibleQuest(specification);
            ChatAndTextUtil.sendNormalMessage(event.getClicker(), "Neue Klick-NPC-Quest-Spezifikation erfolgreich erstellt!");
            return;
        } else if (requiredFor.mapsToName) {
            String name = (String) removed;
            DeliveryReceiverSpecification specification = new DeliveryReceiverSpecification();
            specification.setName(name);
            specification.setNPC(event.getNPC());

            DeliveryQuestPossibilitiesSpecification instance = DeliveryQuestPossibilitiesSpecification.getInstance();
            boolean result = requiredFor.adds? instance.addTarget(specification) : instance.removeTarget(specification);
            if (result) {
                ChatAndTextUtil.sendNormalMessage(event.getClicker(), "Lieferungsziel erfolgreich " + (requiredFor.adds? "hinzugefügt" : "entfernt") + ".");
            } else {
                ChatAndTextUtil.sendWarningMessage(event.getClicker(), "Dieses Lieferungsziel war " + (requiredFor.adds? "bereits" : "nicht") + " eingetragen.");
            }
        } else {
            assert(false);
        }

    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) {
            return;
        }
        if (currentlySelectingNPC.remove(event.getPlayer().getUniqueId()) != null) {
            ChatAndTextUtil.sendWarningMessage(event.getPlayer(), "Auswahl abgebrochen.");
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        if (currentlySelectingNPC.remove(event.getPlayer().getUniqueId()) != null) {
            ChatAndTextUtil.sendWarningMessage(event.getPlayer(), "Auswahl abgebrochen.");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

        if (currentlySelectingNPC == null) {
            ChatAndTextUtil.sendErrorMessage(sender, "Auf diesem Server ist das Citizens-Plugin nicht installiert!");
            return true;
        }

        return onCommandInternal(sender, args);
    }

    private boolean onCommandInternal(CommandSender sender, ArgsParser args) {

        Object mapTo;

        if (requiredFor == NPCRequiredFor.CLICK_NPC_SPECIFICATION) {
            if (args.remaining() < 1) {
                ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Schwierigkeit des NPCs an.");
                return true;
            }

            double difficulty = args.getNext(Double.MIN_VALUE);
            if (difficulty <= 0.0 || difficulty > 1.0) {
                ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Schwierigkeit als Kommazahl echt größer 0 und kleiner gleich 1 an.");
                return true;
            }

            String[] messages = args.getAll("").split("\\|");
            if (messages.length != 2) {

                ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Vergabe- und Erfolgsnachricht an, getrennt von einem |.");
                return true;
            }

            ClickNPCQuestSpecification specification = new ClickNPCQuestSpecification();
            specification.setDifficulty(difficulty);
            specification.setGiveMessage(messages[0]);
            specification.setSuccessMessage(messages[1]);

            mapTo = specification;
        } else if (requiredFor.mapsToName) {
            if (!args.hasNext()) {
                ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib den Namen des Lieferungsempfängers an.");
                return true;
            }

            String name = args.getAll("");
            mapTo = name;
        } else {
            assert(false);
            mapTo = null;
        }

        currentlySelectingNPC.put(((Player) sender).getUniqueId(), mapTo);
        ChatAndTextUtil.sendNormalMessage(sender, "Bitte rechtsklicke den NPC für diese Spezifikation. Rechtsklicke irgendetwas anderes, um die Auswahl abzubrechen.");
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
