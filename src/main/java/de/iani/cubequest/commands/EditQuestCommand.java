package de.iani.cubequest.commands;

import java.util.Set;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.google.common.collect.Iterables;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.QuestType;
import de.iani.cubequest.quests.Quest;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

public class EditQuestCommand extends SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

        if (!args.hasNext()) {
            CubeQuest.sendWarningMessage(sender, "Bitte gib einen Quest-Typ an.");
            return true;
        }

        String questString = args.getNext();
        Quest quest;
        try {
            int id = Integer.parseInt(questString);
            quest = QuestManager.getInstance().getQuest(id);
            if (quest == null) {
                CubeQuest.sendWarningMessage(sender, "Es gibt keine Quest mit der ID " + id + ".");
                return true;
            }
        } catch (NumberFormatException e) {
            Set<Quest> quests = QuestManager.getInstance().getQuests(questString);
            if (quests.isEmpty()) {
                CubeQuest.sendWarningMessage(sender, "Es gibt keine Quest mit dem Namen " + questString + ".");
                return true;
            } else if (quests.size() > 1) {
                CubeQuest.sendWarningMessage(sender, "Es gibt mehrere Quests mit diesem Namen, bitte wähle eine aus:");
                for (Quest q: quests) {
                    if (sender instanceof Player) {
                        HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Quest " + q.getId() + " editieren").create());
                        ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cubequest edit " + q.getId());
                        String msg = CubeQuest.PLUGIN_TAG + ChatColor.GOLD + q.getTypeName() + " " + q.getId();
                        ComponentBuilder cb = new ComponentBuilder("").append(msg).event(ce).event(he);
                        ((Player) sender).spigot().sendMessage(cb.create());
                    } else {
                        CubeQuest.sendWarningMessage(sender, QuestType.getQuestType(q.getClass()) + " " + q.getId());
                    }
                }
                return true;
            }
            quest = Iterables.getFirst(quests, null);
        }

        if (quest.isReady()) {
            CubeQuest.sendWarningMessage(sender, "Diese Quest ist bereits auf \"fertig\" gesetzt. Sie zu bearbeiten kann unbekannte Nebenwirkungen haben, es wird davon abgeraten.");
        }

        CubeQuest.getInstance().getQuestEditor().startEdit(sender, quest);

        return true;
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }

}
