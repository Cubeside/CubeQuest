package de.iani.cubequest.commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestType;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

public class CreateQuestCommand extends SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

        if (!args.hasNext()) {
            ChatUtil.sendWarningMessage(sender, "Bitte gib einen Quest-Typ an.");
            return true;
        }

        String typeString = args.getNext();
        QuestType type;
        try {
            type = QuestType.valueOf(typeString);
        } catch (IllegalArgumentException e) {
            ChatUtil.sendWarningMessage(sender, "Unbekannter Quest-Typ " + typeString + ".");
            return true;
        }

        Class<? extends Quest> questClass = type.getQuestClass();
        Quest quest = CubeQuest.getInstance().getQuestCreator().createQuest(questClass);

        int id = quest.getId();
        if (sender instanceof Player) {
            HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Quest " + id + " editieren").create());
            ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cubequest edit " + id);
            String msg = CubeQuest.PLUGIN_TAG + ChatColor.GREEN + type + " mit Quest-ID " + id + " erfolgreich erstellt! ";
            ComponentBuilder cb = new ComponentBuilder(msg).append("[EDITIEREN]").event(ce).event(he);
            ((Player) sender).spigot().sendMessage(cb.create());
        } else {
            ChatUtil.sendNormalMessage(sender, type + " mit Quest-ID " + id + " erfolgreich erstellt!");
        }

        return true;
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        if (!args.hasNext()) {
            return null;
        }
        String arg = args.getNext();
        List<QuestType> typeList = Arrays.asList(QuestType.values());
        List<String> result = new ArrayList<String>();
        for (QuestType type: typeList) {
            String typeString = type.toString();
            if (arg.toLowerCase().startsWith(typeString.toLowerCase())) {
                result.add(typeString);
            }
        }
        return result;
    }

}
