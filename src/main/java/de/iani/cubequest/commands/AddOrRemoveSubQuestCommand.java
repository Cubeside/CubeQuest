package de.iani.cubequest.commands;

import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Iterables;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.QuestType;
import de.iani.cubequest.quests.ComplexQuest;
import de.iani.cubequest.quests.Quest;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

public class AddOrRemoveSubQuestCommand extends SubCommand {

    private boolean add;

    public AddOrRemoveSubQuestCommand(boolean add) {
        this.add = add;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            CubeQuest.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }

        if (!(quest instanceof ComplexQuest)) {
            CubeQuest.sendWarningMessage(sender, "Diese Quest unterstützt keine Unterquests.");
            return true;
        }

        if (!args.hasNext()) {
            CubeQuest.sendWarningMessage(sender, "Bitte gib die " + (add? "hinzuzufügende" : "zu entfernende") + " Unterquest an.");
            return true;
        }

        String otherQuestString = args.getNext();
        Quest otherQuest;
        try {
            int id = Integer.parseInt(otherQuestString);
            otherQuest = QuestManager.getInstance().getQuest(id);
            if (otherQuest == null) {
                CubeQuest.sendWarningMessage(sender, "Es gibt keine Quest mit der ID " + id + ".");
                return true;
            }
        } catch (NumberFormatException e) {
            Set<Quest> quests = QuestManager.getInstance().getQuests(otherQuestString);
            if (quests.isEmpty()) {
                CubeQuest.sendWarningMessage(sender, "Es gibt keine Quest mit dem Namen " + otherQuestString + ".");
                return true;
            } else if (quests.size() > 1) {
                CubeQuest.sendWarningMessage(sender, "Es gibt mehrere Quests mit diesem Namen, bitte wähle eine aus:");
                for (Quest q: quests) {
                    if (sender instanceof Player) {
                        HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Quest " + q.getId() + " " + (add? "hinzufügen" : "entfernen") + ".").create());
                        ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cubequest " + (add? "add" : "remove") + "SubQuest " + q.getId());
                        String msg = CubeQuest.PLUGIN_TAG + ChatColor.GOLD + q.getTypeName() + " " + q.getId();
                        ComponentBuilder cb = new ComponentBuilder("").append(msg).event(ce).event(he);
                        ((Player) sender).spigot().sendMessage(cb.create());
                    } else {
                        CubeQuest.sendWarningMessage(sender, QuestType.getQuestType(q.getClass()) + " " + q.getId());
                    }
                }
                return true;
            }
            otherQuest = Iterables.getFirst(quests, null);
        }

        if (add) {
            if (((ComplexQuest) quest).addPartQuest(otherQuest)) {
                CubeQuest.sendNormalMessage(sender, "SubQuest hinzugefügt.");
            } else {
                CubeQuest.sendWarningMessage(sender, "SubQuest war bereits enthalten.");
            }
        } else {
            if (((ComplexQuest) quest).removePartQuest(otherQuest)) {
                CubeQuest.sendNormalMessage(sender, "SubQuest entfernt.");
            } else {
                CubeQuest.sendWarningMessage(sender, "SubQuest war nicht enthalten.");
            }
        }
        return true;
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }

}
