package de.iani.cubequest.commands;

import java.util.HashSet;
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
import org.bukkit.event.player.PlayerQuitEvent;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.NPCQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.npc.NPC;

public class SetQuestNPCCommand extends SubCommand implements Listener {

    private Set<UUID> currentlySelectingNPC = null;

    public SetQuestNPCCommand() {
        if (!CubeQuest.getInstance().hasCitizensPlugin()) {
            return;
        }

        initInternal();
    }

    private void initInternal() {
        Bukkit.getPluginManager().registerEvents(this, CubeQuest.getInstance());
        currentlySelectingNPC = new HashSet<>();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onNPCClickEvent(NPCRightClickEvent event) {
        if (currentlySelectingNPC.remove(event.getClicker().getUniqueId())) {
            Bukkit.dispatchCommand(event.getClicker(), "quest setNPC " + event.getNPC().getId());
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) {
            return;
        }
        if (currentlySelectingNPC.remove(event.getPlayer().getUniqueId())) {
            ChatAndTextUtil.sendWarningMessage(event.getPlayer(), "Auswahl abgebrochen.");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        if (currentlySelectingNPC.remove(event.getPlayer().getUniqueId())) {
            ChatAndTextUtil.sendWarningMessage(event.getPlayer(), "Auswahl abgebrochen.");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

        if (!CubeQuest.getInstance().hasCitizensPlugin()) {
            ChatAndTextUtil.sendErrorMessage(sender, "Auf diesem Server ist das Citizens-Plugin nicht installiert!");
            return true;
        }

        return internalOnCommand(sender, args);
    }

    private boolean internalOnCommand(CommandSender sender, ArgsParser args) {
        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }

        if (!(quest instanceof NPCQuest)) {
            ChatAndTextUtil.sendWarningMessage(sender, "Diese Quest erfordert keinen NPC.");
            return true;
        }

        if (!args.hasNext()) {
            if (!(sender instanceof Player)) {
                ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib eine NPC-ID an.");
                return true;
            }
            if (currentlySelectingNPC.add(((Player) sender).getUniqueId())) {
                ChatAndTextUtil.sendNormalMessage(sender, "Bitte wähle durch Rechtsklick einen NPC aus.");
            } else {
                ChatAndTextUtil.sendWarningMessage(sender, "Du wählst bereits einen NPC aus.");
            }
            return true;
        }

        int id = args.getNext(-1);
        if (id < 0) {
            ChatAndTextUtil.sendWarningMessage(sender, "Die NPC-ID muss eine nicht-negative Ganzzahl sein.");
            return true;
        }

        NPC npc = CubeQuest.getInstance().getNPCReg().getById(id);
        if (npc == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "NPC mit der ID " + id + " nicht gefunden.");
            return true;
        }

        ((NPCQuest) quest).setNPC(npc.getId());
        ChatAndTextUtil.sendNormalMessage(sender, "NPC gesetzt.");

        return true;
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }

}
