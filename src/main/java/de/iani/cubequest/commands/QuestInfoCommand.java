package de.iani.cubequest.commands;

import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Iterables;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.QuestType;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

public class QuestInfoCommand extends SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

        if (!args.hasNext()) {
            Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
            if (quest != null) {
                Bukkit.dispatchCommand(sender, "cubequest questInfo " + quest.getId());
                return true;
            }
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib eine Quest an.");
            return true;
        }

        String questString = args.getAll("");
        Quest quest;
        try {
            int id = Integer.parseInt(questString);
            quest = QuestManager.getInstance().getQuest(id);
            if (quest == null) {
                ChatAndTextUtil.sendWarningMessage(sender, "Es gibt keine Quest mit der ID " + id + ".");
                return true;
            }
        } catch (NumberFormatException e) {
            Set<Quest> quests = QuestManager.getInstance().getQuests(questString);
            if (quests.isEmpty()) {
                ChatAndTextUtil.sendWarningMessage(sender, "Es gibt keine Quest mit dem Namen " + questString + ".");
                return true;
            } else if (quests.size() > 1) {
                ChatAndTextUtil.sendWarningMessage(sender, "Es gibt mehrere Quests mit diesem Namen, bitte wähle eine aus:");
                for (Quest q: quests) {
                    if (sender instanceof Player) {
                        HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Info zu Quest " + q.getId()).create());
                        ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cubequest questInfo " + q.getId());
                        String msg = CubeQuest.PLUGIN_TAG + " " + ChatColor.GOLD + q.getTypeName() + " " + q.getId();
                        ComponentBuilder cb = new ComponentBuilder("").append(msg).event(ce).event(he);
                        ((Player) sender).spigot().sendMessage(cb.create());
                    } else {
                        ChatAndTextUtil.sendWarningMessage(sender, QuestType.getQuestType(q.getClass()) + " " + q.getId());
                    }
                }
                return true;
            }
            quest = Iterables.getFirst(quests, null);
        }

        List<BaseComponent[]> info = quest.getQuestInfo();
        boolean notEditing = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender) == null;
        if (notEditing) {
            ComponentBuilder builder = new ComponentBuilder("[EDITIEREN]");
            builder.bold(true).color(quest.isReady()? ChatColor.RED : ChatColor.GREEN)
                    .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Quest " + quest.getId() + " editieren").create()))
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cubequest edit " + quest.getId()));
            info.add(builder.create());
        }

        if (sender instanceof Player) {
            for (BaseComponent[] bc: info) {
                ((Player) sender).spigot().sendMessage(bc);
            }
        } else {
            for (BaseComponent[] bca: info) {
                String msg = "";
                for (BaseComponent bc: bca) {
                    msg += bc.toPlainText() + " ";
                }
                sender.sendMessage(msg);
            }
        }

        if (notEditing) {
            sender.sendMessage("");
        }

        return true;
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }

}
