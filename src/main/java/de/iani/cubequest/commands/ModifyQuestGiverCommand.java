package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestGiver;
import de.iani.cubequest.interaction.PlayerInteractInteractorEvent;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.StringUtil;
import de.iani.cubesideutils.commands.ArgsParser;
import de.iani.cubesideutils.commands.SubCommand;
import java.util.ArrayList;
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

public class ModifyQuestGiverCommand extends SubCommand implements Listener {
    
    private QuestGiverModification type;
    private Map<UUID, Object> currentlySelectingInteractor;
    
    public enum QuestGiverModification {
        
        REMOVE("removeQuestGiver", false),
        ADD_DAILY_QUEST_GIVER("addDailyQuestGiver", false),
        REMOVE_DAILY_QUEST_GIVER("removeDailyQuestGiver", false),
        ADD_QUEST("addQuestToGiver", true),
        REMOVE_QUEST("removeQuestFromGiver", true),
        SET_REACT_IF_NO_QUEST("setGiverReactIfNoQuest", false);
        
        public final String command;
        public final boolean requiresQuestId;
        
        private QuestGiverModification(String command, boolean requiresQuestId) {
            this.command = command;
            this.requiresQuestId = requiresQuestId;
        }
    }
    
    public ModifyQuestGiverCommand(QuestGiverModification type) {
        this.type = type;
        this.currentlySelectingInteractor = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, CubeQuest.getInstance());
        CubeQuest.getInstance().getEventListener().addOnPlayerQuit(player -> this.currentlySelectingInteractor.remove(player.getUniqueId()));
    }
    
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteractInteractorEvent(PlayerInteractInteractorEvent<?> event) {
        Object data = this.currentlySelectingInteractor.remove(event.getPlayer().getUniqueId());
        
        if (data == null) {
            return;
        }
        
        Integer questId = this.type.requiresQuestId ? (Integer) data : null;
        Boolean reactValue = this.type == QuestGiverModification.SET_REACT_IF_NO_QUEST ? (Boolean) data : null;
        
        event.setCancelled(true);
        
        QuestGiver giver = CubeQuest.getInstance().getQuestGiver(event.getInteractor());
        if (giver == null) {
            ChatAndTextUtil.sendWarningMessage(event.getPlayer(), "Dieser Interactor ist kein QuestGiver. Auswahl abgebrochen.");
            return;
        }
        
        Bukkit.dispatchCommand(event.getPlayer(), "cubequest " + this.type.command + (this.type.requiresQuestId ? " " + questId : "")
                + (this.type == QuestGiverModification.SET_REACT_IF_NO_QUEST ? " " + reactValue : "") + " " + giver.getName());
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
    
    @SuppressWarnings("null")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString, ArgsParser args) {
        
        if (this.currentlySelectingInteractor == null) {
            ChatAndTextUtil.sendErrorMessage(sender, "Auf dem Server muss die InteractiveBooksAPI installiert sein, das ist nicht der Fall!");
            return true;
        }
        
        Quest quest = null;
        
        if (this.type.requiresQuestId) {
            quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
            
            if (quest == null && !args.hasNext()) {
                ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib eine Quest an.");
                return true;
            }
            
            if (quest == null || args.hasNext()) {
                quest = ChatAndTextUtil.getQuest(sender, args, "/cubequest " + this.type.command + " ", "", "Quest ",
                        " " + (this.type == QuestGiverModification.ADD_QUEST ? "zu QuestGiver hinzufügen" : "von QuestGiver entfernen"));
            }
            
            if (quest == null) {
                return true;
            }
        }
        
        boolean reactValue = false;
        if (this.type == QuestGiverModification.SET_REACT_IF_NO_QUEST) {
            if (!args.hasNext()) {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Bitte gib an, ob der QuestGiver reagieren soll, wenn er für einen Spieler keine neuen Quests oder Teaser hat.");
                return true;
            }
            
            String reactValueString = args.next();
            if (StringUtil.TRUE_STRINGS.contains(reactValueString)) {
                reactValue = true;
            } else if (StringUtil.FALSE_STRINGS.contains(reactValueString)) {
                reactValue = false;
            } else {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Bitte gib mit true oder false an, ob der Quest-Giver reagieren soll, wenn er für einen Spieler keine neuen Quests oder Teaser hat.");
                return true;
            }
        }
        
        if (!args.hasNext()) {
            if (!(sender instanceof Player)) {
                ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib den Namen des QuestGivers an.");
                return true;
            }
            if (this.currentlySelectingInteractor.containsKey(((Player) sender).getUniqueId())) {
                ChatAndTextUtil.sendWarningMessage(sender, "Du wählst bereits einen Interactor aus.");
                return true;
            }
            
            this.currentlySelectingInteractor.put(((Player) sender).getUniqueId(), this.type.requiresQuestId ? (quest == null ? 0 : quest.getId())
                    : this.type == QuestGiverModification.SET_REACT_IF_NO_QUEST ? reactValue : new Object());
            ChatAndTextUtil.sendNormalMessage(sender,
                    "Bitte rechtsklicke den Interactor des QuestGivers. Rechtsklicke irgendetwas anderes, um die Auswahl abzubrechen.");
            return true;
        }
        
        String name = args.getNext();
        QuestGiver giver = CubeQuest.getInstance().getQuestGiver(name);
        
        if (giver == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Einen QuestGiver mit dem Namen \"" + name + "\" gibt es nicht.");
            return true;
        }
        
        boolean result;
        switch (this.type) {
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
                    ChatAndTextUtil.sendNormalMessage(sender,
                            "QuestGiver \"" + name + "\" wird nun die " + quest.getTypeName() + " " + quest.getId() + " verteilen.");
                } else {
                    ChatAndTextUtil.sendWarningMessage(sender,
                            "QuestGiver \"" + name + "\" hat die " + quest.getTypeName() + " " + quest.getId() + " bereits verteilt.");
                }
                return true;
            case REMOVE_QUEST:
                result = CubeQuest.getInstance().getQuestGiver(name).removeQuest(quest);
                if (result) {
                    ChatAndTextUtil.sendNormalMessage(sender,
                            "QuestGiver \"" + name + "\" wird nun die " + quest.getTypeName() + " " + quest.getId() + " nicht mehr verteilen.");
                } else {
                    ChatAndTextUtil.sendWarningMessage(sender,
                            "QuestGiver \"" + name + "\" hat die " + quest.getTypeName() + " " + quest.getId() + " bereits nicht verteilt.");
                }
                return true;
            case SET_REACT_IF_NO_QUEST:
                result = reactValue != giver.isReactIfNoQuest();
                giver.setReactIfNoQuest(reactValue);
                
                if (result) {
                    if (reactValue) {
                        ChatAndTextUtil.sendNormalMessage(sender, "QuestGiver \"" + giver.getName()
                                + "\" wird nun auch dann reagieren, wenn er für einen Spieler keine neuen Quests oder Teaser hat.");
                    } else {
                        ChatAndTextUtil.sendNormalMessage(sender, "QuestGiver \"" + giver.getName()
                                + "\" wird nun nicht mehr reagieren, wenn er für einen Spieler keine neuen Quests oder Teaser hat.");
                    }
                } else {
                    if (reactValue) {
                        ChatAndTextUtil.sendWarningMessage(sender, "QuestGiver \"" + giver.getName()
                                + "\" reagiert bereits, wenn er für einen Spieler keine neuen Quests oder Teaser hat.");
                    } else {
                        ChatAndTextUtil.sendWarningMessage(sender, "QuestGiver \"" + giver.getName()
                                + "\" reagiert bereits nicht, wenn er für einen Spieler keine neuen Quests oder Teaser hat.");
                    }
                }
                return true;
            default:
                throw new NullPointerException("type");
        }
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUEST_GIVERS_PERMISSION;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        if (this.type.requiresQuestId) {
            if (CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender) == null) {
                args.next();
            }
        }
        
        List<String> result = new ArrayList<>();
        
        switch (this.type) {
            case ADD_DAILY_QUEST_GIVER:
                for (QuestGiver giver : CubeQuest.getInstance().getQuestGivers()) {
                    if (!CubeQuest.getInstance().getDailyQuestGivers().contains(giver)) {
                        result.add(giver.getName());
                    }
                }
                break;
            
            case ADD_QUEST:
            case REMOVE:
            case REMOVE_QUEST:
            case SET_REACT_IF_NO_QUEST:
                for (QuestGiver giver : CubeQuest.getInstance().getQuestGivers()) {
                    result.add(giver.getName());
                }
                break;
            
            case REMOVE_DAILY_QUEST_GIVER:
                for (QuestGiver giver : CubeQuest.getInstance().getDailyQuestGivers()) {
                    result.add(giver.getName());
                }
                break;
            
            default:
                assert (false);
                break;
        }
        
        return ChatAndTextUtil.polishTabCompleteList(result, args.getNext(""));
    }
    
    @Override
    public String getUsage() {
        switch (this.type) {
            case REMOVE:
            case ADD_DAILY_QUEST_GIVER:
            case REMOVE_DAILY_QUEST_GIVER:
                return "[GiverName]";
            case ADD_QUEST:
            case REMOVE_QUEST:
                return "<Quest (Editing oder Id oder Name)> [GiverName]";
            case SET_REACT_IF_NO_QUEST:
                return "<true | false> [GiverName]";
        }
        throw new NullPointerException();
    }
    
}
