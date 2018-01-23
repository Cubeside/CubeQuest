package de.iani.cubequest.commands;

import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.google.common.collect.Iterables;
import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.quests.QuestType;
import de.iani.cubequest.util.ChatAndTextUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;


public class DeleteQuestCommand extends SubCommand {
    
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
        
        String questIdString = args.seeNext("");
        String questString = args.getAll("");
        Quest quest;
        try {
            int id = Integer.parseInt(questIdString);
            quest = QuestManager.getInstance().getQuest(id);
            if (quest == null) {
                // ChatAndTextUtil.sendWarningMessage(sender,
                // "Es gibt keine Quest mit der ID " + id + ".");
                // return true;
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            Set<Quest> quests = QuestManager.getInstance().getQuests(questString);
            if (quests.isEmpty()) {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Es gibt keine Quest mit dem Namen " + questString + ".");
                return true;
            } else if (quests.size() > 1) {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Es gibt mehrere Quests mit diesem Namen, bitte wähle eine aus:");
                for (Quest q: quests) {
                    if (sender instanceof Player) {
                        HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new ComponentBuilder("Info zu Quest " + q.getId()).create());
                        ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                "/cubequest questInfo " + q.getId());
                        String msg = CubeQuest.PLUGIN_TAG + " " + ChatColor.GOLD + q.getTypeName()
                                + " " + q.getId();
                        ComponentBuilder cb =
                                new ComponentBuilder("").append(msg).event(ce).event(he);
                        ((Player) sender).spigot().sendMessage(cb.create());
                    } else {
                        ChatAndTextUtil.sendWarningMessage(sender,
                                QuestType.getQuestType(q.getClass()) + " " + q.getId());
                    }
                }
                return true;
            }
            quest = Iterables.getFirst(quests, null);
        }
        
        if (!questString.equals(quest.getId() + " DELETE")) {
            ChatAndTextUtil.sendWarningMessage(sender, "Soll die Quest " + quest.getId()
                    + " wirklich unwiderruflich gelöscht werden? Dann nutze den Befehl /cubequest delete "
                    + quest.getId() + " DELETE");
            Bukkit.dispatchCommand(sender, "cubequest info " + quest.getId());
            return true;
        }
        
        if (!QuestManager.getInstance().deleteQuest(quest)) {
            ChatAndTextUtil.sendErrorMessage(sender,
                    "Quest konnte nicht gelöscht werden. Folgende Meldungen wurden gemacht:");
            for (String s: CubeQuest.getInstance().popStoredMessages()) {
                ChatAndTextUtil.sendWarningMessage(sender, s);
            }
            return true;
        }
        
        ChatAndTextUtil.sendNormalMessage(sender, "Quest " + quest + " gelöscht.");
        String[] stored = CubeQuest.getInstance().popStoredMessages();
        // skip first which is msg that the selected quest has been deleted
        for (int i = 1; i < stored.length; i++) {
            ChatAndTextUtil.sendNormalMessage(sender, stored[i]);
        }
        
        return true;
        
    }
    
}
