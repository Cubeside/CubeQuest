package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.exceptions.QuestDeletionFailedException;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;


public class DeleteQuestCommand extends SubCommand {
    
    public static final String COMMAND_PATH = "delete";
    public static final String FULL_COMMAND = "quest " + COMMAND_PATH;
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args) {
        
        if (!args.hasNext()) {
            Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
            if (quest != null) {
                Bukkit.dispatchCommand(sender, "cubequest delete " + quest.getId());
                return true;
            }
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib eine Quest an.");
            return true;
        }
        
        // String questIdString = args.seeNext("");
        // String questString = args.getAll("");
        // Quest quest;
        // try {
        // int id = Integer.parseInt(questIdString);
        // quest = QuestManager.getInstance().getQuest(id);
        // if (quest == null) {
        // // ChatAndTextUtil.sendWarningMessage(sender,
        // // "Es gibt keine Quest mit der ID " + id + ".");
        // // return true;
        // throw new NumberFormatException();
        // }
        // } catch (NumberFormatException e) {
        // Set<Quest> quests = QuestManager.getInstance().getQuests(questString);
        // if (quests.isEmpty()) {
        // ChatAndTextUtil.sendWarningMessage(sender,
        // "Es gibt keine Quest mit dem Namen " + questString + ".");
        // return true;
        // } else if (quests.size() > 1) {
        // ChatAndTextUtil.sendWarningMessage(sender,
        // "Es gibt mehrere Quests mit diesem Namen, bitte wähle eine aus:");
        // for (Quest q: quests) {
        // if (sender instanceof Player) {
        // HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
        // new ComponentBuilder("Info zu Quest " + q.getId()).create());
        // ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND,
        // "/cubequest questInfo " + q.getId());
        // String msg = CubeQuest.PLUGIN_TAG + " " + ChatColor.GOLD + q.getTypeName()
        // + " " + q.getId();
        // ComponentBuilder cb =
        // new ComponentBuilder("").append(msg).event(ce).event(he);
        // ((Player) sender).spigot().sendMessage(cb.create());
        // } else {
        // ChatAndTextUtil.sendWarningMessage(sender,
        // QuestType.getQuestType(q.getClass()) + " " + q.getId());
        // }
        // }
        // return true;
        // }
        // quest = Iterables.getFirst(quests, null);
        // }
        
        String questString = args.seeAll("");
        Quest quest = ChatAndTextUtil.getQuest(sender, args, "/cubequest questInfo ", "",
                "Info zu Quest ", "");
        
        if (quest == null) {
            return true;
        }
        
        if (!questString.equals(quest.getId() + " DELETE")) {
            Bukkit.dispatchCommand(sender, "cubequest info " + quest.getId());
            ChatAndTextUtil.sendWarningMessage(sender, "Soll die Quest " + quest.getId()
                    + " wirklich unwiderruflich gelöscht werden? Dann nutze den Befehl /cubequest delete "
                    + quest.getId() + " DELETE");
            return true;
        }
        
        try {
            QuestManager.getInstance().deleteQuest(quest);
        } catch (QuestDeletionFailedException e) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Quest konnte nicht gelöscht werden. Fehlermeldung:");
            ChatAndTextUtil.sendWarningMessage(sender, e.getLocalizedMessage());
            
            Throwable reason = e;
            while (reason instanceof QuestDeletionFailedException) {
                if (reason == reason.getCause()) {
                    break;
                }
                reason = reason.getCause();
            }
            
            if (reason != null) {
                CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                        "An unexpected exception occured while trying to delete quest with id "
                                + quest.getId(),
                        e);
            }
            
            return true;
        }
        
        ChatAndTextUtil.sendNormalMessage(sender, "Quest " + quest + " gelöscht.");
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias,
            ArgsParser args) {
        return Collections.emptyList();
    }
    
    @Override
    public String getUsage() {
        return "<Quest> [DELETE]";
    }
    
}
