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
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import net.citizensnpcs.api.event.NPCClickEvent;

public class ModifyQuestGiverCommand extends SubCommand implements Listener {

    private QuestGiverModification type;
    private Map<UUID, Integer> currentlySelectingNPC = null;

    public enum QuestGiverModification {
        REMOVE("removeQuestGiver", false),
        ADD_DAILY_QUEST_GIVER("addDailyQuestGiver", false),
        REMOVE_DAILY_QUEST_GIVER("removeDailyQuestGiver", false),
        ADD_QUEST("addQuestToGiver", true),
        REMOVE_QUEST("removeQuestFromGiver", true);

        public final String command;
        public final boolean requiresQuestId;

        private QuestGiverModification(String command, boolean requiresQuestId) {
            this.command = command;
            this.requiresQuestId = requiresQuestId;
        }
    }

    public ModifyQuestGiverCommand(QuestGiverModification type) {
        if (!CubeQuest.getInstance().hasCitizensPlugin() || !CubeQuest.getInstance().hasInteractiveBooksAPI()) {
            return;
        }

        this.type = type;
        initInternal();
    }

    private void initInternal() {
        Bukkit.getPluginManager().registerEvents(this, CubeQuest.getInstance());
        currentlySelectingNPC = new HashMap<UUID, Integer>();
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onNPCClickEvent(NPCClickEvent event) {
        Integer questId = currentlySelectingNPC.remove(event.getClicker().getUniqueId());
        if (questId == null) {
            return;
        }

        QuestGiver giver = CubeQuest.getInstance().getQuestGiver(event.getNPC());
        if (giver == null) {
            ChatAndTextUtil.sendWarningMessage(event.getClicker(), "Dieser NPC ist kein QuestGiver. Auswahl abgebrochen.");
            return;
        }

        Bukkit.dispatchCommand(event.getClicker(), "cubequest " + type.command + (type.requiresQuestId? " " + questId : "") + " " + giver.getName());
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

    @SuppressWarnings("null")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

        if (currentlySelectingNPC == null) {
            ChatAndTextUtil.sendErrorMessage(sender, "Auf dem Server müssen das Citizens-Plugin und die InteractiveBooksAPI installiert sein, eins von beidem ist nicht der Fall!");
            return true;
        }

        Quest quest = null;

        if (type.requiresQuestId) {
            if (!args.hasNext()) {
                ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib eine Quest an.");
                return true;
            }

            quest = ChatAndTextUtil.getQuest(sender, args, "/cubequest " + type.command + " ", "",
                    "Quest ", " " + (type == QuestGiverModification.ADD_QUEST? "zu QuestGiver hinzufügen" : "von QuestGiver entfernen"));

            if (quest == null) {
                return true;
            }
        }

        if (!args.hasNext()) {
            if (!(sender instanceof Player)) {
                ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib den Namen des QuestGivers an.");
                return true;
            }
            if (currentlySelectingNPC.containsKey(((Player) sender).getUniqueId())) {
                ChatAndTextUtil.sendWarningMessage(sender, "Du wählst bereits einen NPC aus.");
                return true;
            }

            currentlySelectingNPC.put(((Player) sender).getUniqueId(), quest == null? 0 : quest.getId());
            ChatAndTextUtil.sendNormalMessage(sender, "Bitte klicke den NPC des QuestGivers an. Klicke irgendetwas anderes an, um die Auswahl abzubrechen.");
            return true;
        }

        String name = args.getNext();
        QuestGiver giver = CubeQuest.getInstance().getQuestGiver(name);

        if (giver == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Einen QuestGiver mit diesem Namen gibt es nicht.");
            return true;
        }

        boolean result;
        switch (type) {
            case REMOVE:
                CubeQuest.getInstance().removeQuestGiver(name);
                ChatAndTextUtil.sendNormalMessage(sender, "QuestGiver entfernt.");
                return true;
            case ADD_DAILY_QUEST_GIVER:
                result = CubeQuest.getInstance().addDailyQuestGiver(name);
                if (result) {
                    ChatAndTextUtil.sendNormalMessage(sender, "QuestGiver \"" + name + "\" wird nun DailyQuests verteilen.");
                } else {
                    ChatAndTextUtil.sendWarningMessage(sender, "QuestGiver \"" + name + "\" verteilt bereits DailyQuests.");
                }
                return true;
            case REMOVE_DAILY_QUEST_GIVER:
                result = CubeQuest.getInstance().removeDailyQuestGiver(name);
                if (result) {
                    ChatAndTextUtil.sendNormalMessage(sender, "QuestGiver \"" + name + "\" wird nun keine DailyQuests mehr verteilen.");
                } else {
                    ChatAndTextUtil.sendWarningMessage(sender, "QuestGiver \"" + name + "\" verteiltte bereits keine DailyQuests.");
                }
                return true;
            case ADD_QUEST:
                result = CubeQuest.getInstance().getQuestGiver(name).addQuest(quest);
                if (result) {
                    ChatAndTextUtil.sendNormalMessage(sender, "QuestGiver \"" + name + "\" wird nun die " + quest.getTypeName() + " " + quest.getId() + " verteilen.");
                } else {
                    ChatAndTextUtil.sendWarningMessage(sender, "QuestGiver \"" + name + "\" hat die " + quest.getTypeName() + " " + quest.getId() + " bereits verteilt.");
                }
                return true;
            case REMOVE_QUEST:
                result = CubeQuest.getInstance().getQuestGiver(name).removeQuest(quest);
                if (result) {
                    ChatAndTextUtil.sendNormalMessage(sender, "QuestGiver \"" + name + "\" wird nun die " + quest.getTypeName() + " " + quest.getId() + " nicht mehr verteilen.");
                } else {
                    ChatAndTextUtil.sendWarningMessage(sender, "QuestGiver \"" + name + "\" hat die " + quest.getTypeName() + " " + quest.getId() + " bereits nicht verteilt.");
                }
                return true;
            default: throw new NullPointerException("type");
        }
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUEST_GIVERS_PERMISSION;
    }

}
