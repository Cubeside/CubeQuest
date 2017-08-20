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

public class SetOrRemoveFailiureQuestCommand extends SubCommand {

    private boolean set;

    public SetOrRemoveFailiureQuestCommand(boolean set) {
        this.set = set;
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
            CubeQuest.sendWarningMessage(sender, "Diese Quest unterstützt keine Fail-Bedingung.");
            return true;
        }

        if (!set) {
            ((ComplexQuest) quest).setFailCondition(null);
            CubeQuest.sendNormalMessage(sender, "Fail-Bedingung entfernt.");
            return true;
        }

        if (!args.hasNext()) {
            CubeQuest.sendWarningMessage(sender, "Bitte gib die neue Fail-Bedingung an.");
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
                        HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Quest " + q.getId() + " als Fail-Bedingung festlegen.").create());
                        ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cubequest setFailiureQuest " + q.getId());
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

        ((ComplexQuest) quest).setFailCondition(otherQuest);
        CubeQuest.sendNormalMessage(sender, "Fail-Bedingung gesetzt.");
        return true;
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }

}
