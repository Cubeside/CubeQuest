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
import de.iani.cubequest.questGiving.QuestGiver;
import de.iani.cubequest.util.ChatAndTextUtil;
import net.citizensnpcs.api.event.NPCClickEvent;

public class ChangeQuestGiverCommand extends SubCommand implements Listener {

    private QuestGiverModification type;
    private Set<UUID> currentlySelectingNPC = null;

    public enum QuestGiverModification {
        REMOVE("removeQuestGiver"),
        ADD_DAILY_QUEST_GIVER("addDailyQuestGiver"),
        REMOVE_DAILY_QUEST_GIVER("removeDailyQuestGiver");

        public final String command;

        private QuestGiverModification(String command) {
            this.command = command;
        }
    }

    public ChangeQuestGiverCommand(QuestGiverModification type) {
        if (!CubeQuest.getInstance().hasCitizensPlugin() || !CubeQuest.getInstance().hasInteractiveBooksAPI()) {
            return;
        }

        this.type = type;
        initInternal();
    }

    private void initInternal() {
        Bukkit.getPluginManager().registerEvents(this, CubeQuest.getInstance());
        currentlySelectingNPC = new HashSet<UUID>();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onNPCClickEvent(NPCClickEvent event) {
        if (!currentlySelectingNPC.remove(event.getClicker().getUniqueId())) {
            return;
        }

        QuestGiver giver = CubeQuest.getInstance().getQuestGiver(event.getNPC());
        if (giver == null) {
            ChatAndTextUtil.sendWarningMessage(event.getClicker(), "Dieser NPC ist kein QuestGiver. Auswahl abgebrochen.");
            return;
        }

        Bukkit.dispatchCommand(event.getClicker(), "cubequest " + type.command + " " + giver.getName());
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) {
            return;
        }
        if (currentlySelectingNPC.remove(event.getPlayer().getUniqueId())) {
            ChatAndTextUtil.sendWarningMessage(event.getPlayer(), "Auswahl abgebrochen.");
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

        if (currentlySelectingNPC == null) {
            ChatAndTextUtil.sendErrorMessage(sender, "Auf dem Server müssen das Citizens-Plugin und die InteractiveBooksAPI installiert sein, eins von beidem ist nicht der Fall!");
            return true;
        }

        if (!args.hasNext()) {
            if (!(sender instanceof Player)) {
                ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib den Namen des QuestGivers an.");
                return true;
            }
            if (currentlySelectingNPC.contains(((Player) sender).getUniqueId())) {
                ChatAndTextUtil.sendWarningMessage(sender, "Du wählst bereits einen NPC aus.");
                return true;
            }

            currentlySelectingNPC.add(((Player) sender).getUniqueId());
            ChatAndTextUtil.sendNormalMessage(sender, "Bitte klicke den NPC des QuestGivers an. Klicke irgendetwas anderes an, um die Auswahl abzubrechen.");
            return true;
        }

        String name = args.getNext();
        QuestGiver giver = CubeQuest.getInstance().getQuestGiver(name);

        if (giver == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Einen QuestGiver mit diesem Namen gibt es nicht.");
            return true;
        }

        if (type == QuestGiverModification.REMOVE) {
            CubeQuest.getInstance().removeQuestGiver(name);
            ChatAndTextUtil.sendNormalMessage(sender, "QuestGiver entfernt.");
            return true;
        }

        boolean result = type == QuestGiverModification.ADD_DAILY_QUEST_GIVER? CubeQuest.getInstance().addDailyQuestGiver(name) : CubeQuest.getInstance().removeDailyQuestGiver(name);
        if (result) {
            ChatAndTextUtil.sendNormalMessage(sender, "QuestGiver \"" + name + "\" wird nun "
                    + (type == QuestGiverModification.ADD_DAILY_QUEST_GIVER? "" : "keine ") + "DailyQuests "
                    + (type == QuestGiverModification.ADD_DAILY_QUEST_GIVER? "" : "mehr ") + "verteilen.");
        } else {
            ChatAndTextUtil.sendWarningMessage(sender, "QuestGiver \"" + name + "\" hat bereits "
                    + (type == QuestGiverModification.ADD_DAILY_QUEST_GIVER? "" : "keine ") + " DailyQuests verteilt.");
        }
        return true;
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUEST_GIVERS_PERMISSION;
    }

}
