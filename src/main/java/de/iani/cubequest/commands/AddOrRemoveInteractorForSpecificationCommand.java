package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.generation.ClickInteractorQuestSpecification;
import de.iani.cubequest.generation.DeliveryQuestSpecification.DeliveryQuestPossibilitiesSpecification;
import de.iani.cubequest.generation.DeliveryQuestSpecification.DeliveryReceiverSpecification;
import de.iani.cubequest.generation.QuestGenerator;
import de.iani.cubequest.interaction.PlayerInteractInteractorEvent;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.ComponentUtilAdventure;
import de.iani.cubesideutils.Pair;
import de.iani.cubesideutils.StringUtil;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

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
        this.requiredFor = requiredFor;

        this.currentlySelectingInteractor = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, CubeQuest.getInstance());
        CubeQuest.getInstance().getEventListener()
                .addOnPlayerQuit(player -> this.currentlySelectingInteractor.remove(player.getUniqueId()));
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteractInteractorEvent(PlayerInteractInteractorEvent<?> event) {
        Object removed = this.currentlySelectingInteractor.remove(event.getPlayer().getUniqueId());
        if (removed == null) {
            return;
        }

        event.setCancelled(true);

        if (this.requiredFor == InteractorRequiredFor.CLICK_Interactor_SPECIFICATION) {
            ClickInteractorQuestSpecification specification = (ClickInteractorQuestSpecification) removed;
            specification.setInteractor(event.getInteractor());
            QuestGenerator.getInstance().addPossibleQuest(specification);
            ChatAndTextUtil.sendNormalMessage(event.getPlayer(),
                    "Neue Klick-Interactor-Quest-Spezifikation erfolgreich erstellt!");
            return;
        } else if (this.requiredFor.mapsToName) {
            Component name = (Component) removed;
            DeliveryReceiverSpecification specification = new DeliveryReceiverSpecification();
            specification.setName(name);
            specification.setInteractor(event.getInteractor());

            DeliveryQuestPossibilitiesSpecification instance = DeliveryQuestPossibilitiesSpecification.getInstance();
            boolean result =
                    this.requiredFor.adds ? instance.addTarget(specification) : instance.removeTarget(specification);
            if (result) {
                ChatAndTextUtil.sendNormalMessage(event.getPlayer(),
                        "Lieferungsziel erfolgreich " + (this.requiredFor.adds ? "hinzugefügt" : "entfernt") + ".");
            } else {
                ChatAndTextUtil.sendWarningMessage(event.getPlayer(),
                        "Dieses Lieferungsziel war " + (this.requiredFor.adds ? "bereits" : "nicht") + " eingetragen.");
            }
        } else {
            assert (false);
        }

    }

    @EventHandler(ignoreCancelled = false)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) {
            return;
        }
        if (this.currentlySelectingInteractor.remove(event.getPlayer().getUniqueId()) != null) {
            ChatAndTextUtil.sendWarningMessage(event.getPlayer(), "Auswahl abgebrochen.");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

        Object mapTo;

        if (this.requiredFor == InteractorRequiredFor.CLICK_Interactor_SPECIFICATION) {
            if (args.remaining() < 1) {
                ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Schwierigkeit des Interactors an.");
                return true;
            }

            double difficulty = args.getNext(Double.MIN_VALUE);
            if (difficulty <= 0.0 || difficulty > 1.0) {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Bitte gib die Schwierigkeit als Kommazahl echt größer 0 und kleiner gleich 1 an.");
                return true;
            }

            Pair<String, String> messages = StringUtil.splitAtPipe(args.getAll(null));
            if (messages == null) {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Bitte gib den Namen des Interactors und die Vergabenachricht an, getrennt von einem |.");
                return true;
            }

            ClickInteractorQuestSpecification specification = new ClickInteractorQuestSpecification();
            specification.setDifficulty(difficulty);
            try {
                specification.setInteractorName(ComponentUtilAdventure.convertEscaped(messages.first));
                specification.setGiveMessage(ComponentUtilAdventure.convertEscaped(messages.second));
            } catch (ParseException e) {
                ChatAndTextUtil.sendWarningMessage(sender, "Ungültiger Text: ", e.getMessage());
                return true;
            }

            mapTo = specification;
        } else if (this.requiredFor.mapsToName) {
            if (!args.hasNext()) {
                ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib den Namen des Lieferungsempfängers an.");
                return true;
            }

            Component name;
            try {
                name = ComponentUtilAdventure.convertEscaped(args.getAll(null));
            } catch (ParseException e) {
                ChatAndTextUtil.sendWarningMessage(sender, "Ungültiger Text: ", e.getMessage());
                return true;
            }
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

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        return Collections.emptyList();
    }

    @Override
    public String getUsage() {
        if (this.requiredFor == InteractorRequiredFor.CLICK_Interactor_SPECIFICATION) {
            return "<Schwierigkeit> <InteractorName>|<Vergabenachricht>";
        }
        return "<InteractorName>";
    }

}
