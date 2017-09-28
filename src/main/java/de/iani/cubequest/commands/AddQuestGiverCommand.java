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
import de.iani.cubequest.questGiving.QuestGiver;
import de.iani.cubequest.util.ChatAndTextUtil;
import net.citizensnpcs.api.event.NPCClickEvent;

public class AddQuestGiverCommand extends SubCommand implements Listener {

    private Map<UUID, String> currentlySelectingNPC = null;

    public AddQuestGiverCommand() {
        if (!CubeQuest.getInstance().hasCitizensPlugin() || !CubeQuest.getInstance().hasInteractiveBooksAPI()) {
            return;
        }

        initInternal();
    }

    private void initInternal() {
        Bukkit.getPluginManager().registerEvents(this, CubeQuest.getInstance());
        currentlySelectingNPC = new HashMap<UUID, String>();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onNPCClickEvent(NPCClickEvent event) {
        String name = currentlySelectingNPC.remove(event.getClicker().getUniqueId());
        if (name == null) {
            return;
        }

        if (CubeQuest.getInstance().getQuestGiver(name) != null) {
            ChatAndTextUtil.sendWarningMessage(event.getClicker(), "In der Zwischenzeit wurde bereits ein QuestGiver mit diesem Namen angelegt. Auswahl abgebrochen.");
            return;
        }
        QuestGiver other = CubeQuest.getInstance().getQuestGiver(event.getNPC());
        if (other != null) {
            ChatAndTextUtil.sendWarningMessage(event.getClicker(), "Dieser NPC ist bereits als QuestGiver mit dem Namen " + other.getName() + " eingetragen.");
            return;
        }

        CubeQuest.getInstance().addQuestGiver(new QuestGiver(event.getNPC(), name));
        ChatAndTextUtil.sendNormalMessage(event.getClicker(), "QuestGiver " + name + " gesetzt!");
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
            ChatAndTextUtil.sendErrorMessage(sender, "Auf dem Server müssen das Citizens-Plugin und die InteractiveBooksAPI installiert sein, eins von beidem ist nicht der Fall!");
            return true;
        }

        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib einen Namen für den neuen QuestGiver an.");
            return true;
        }

        String name = args.getNext();

        if (args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "QuestGiver-Namen dürfen keine Leerzeichen enthalten.");
            return true;
        }

        if (CubeQuest.getInstance().getQuestGiver(name) != null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Einen QuestGiver mit diesem Namen gibt es bereits.");
            return true;
        }

        currentlySelectingNPC.put(((Player) sender).getUniqueId(), name);
        ChatAndTextUtil.sendNormalMessage(sender, "Bitte klicke den NPC für diesen QuestGiver an. Klicke irgendetwas anderes an, um die Auswahl abzubrechen.");
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

}
