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

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.generation.ClickNPCQuestSpecification;
import de.iani.cubequest.generation.QuestGenerator;
import de.iani.cubequest.util.ChatAndTextUtil;
import net.citizensnpcs.api.event.NPCRightClickEvent;

public class AddClickNPCQuestSpecificationCommand extends SubCommand implements Listener {

    private Map<UUID, ClickNPCQuestSpecification> currentlySelectingNPC;

    public AddClickNPCQuestSpecificationCommand() {
        if (!CubeQuest.getInstance().hasCitizensPlugin()) {
            return;
        }

        initInternal();
    }

    private void initInternal() {
        Bukkit.getPluginManager().registerEvents(this, CubeQuest.getInstance());
        currentlySelectingNPC = new HashMap<>();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onNPCClickEvent(NPCRightClickEvent event) {
        ClickNPCQuestSpecification specification = currentlySelectingNPC.remove(event.getClicker().getUniqueId());
        if (specification == null) {
            return;
        }

        specification.setNPC(event.getNPC().getId());
        QuestGenerator.getInstance().addPossibleQuest(specification);
        ChatAndTextUtil.sendNormalMessage(event.getClicker(), "Neue Klick-NPC-Quest-Spezifikation erfolgreich erstellt!");
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

        currentlySelectingNPC.put(((Player) sender).getUniqueId(), specification);
        ChatAndTextUtil.sendNormalMessage(sender, "Bitte rechtsklicke den NPC für diese Spezifikation an. Rechtsklicke irgendetwas anderes an, um die Auswahl abzubrechen.");
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
