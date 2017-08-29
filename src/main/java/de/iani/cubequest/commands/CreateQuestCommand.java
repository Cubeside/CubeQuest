package de.iani.cubequest.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestType;
import de.iani.cubequest.quests.NPCQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

public class CreateQuestCommand extends SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib einen Quest-Typ an.");
            return true;
        }

        String typeString = args.getNext();
        QuestType type;
        try {
            type = QuestType.valueOf(typeString);
        } catch (IllegalArgumentException e) {
            ChatAndTextUtil.sendWarningMessage(sender, "Unbekannter Quest-Typ " + typeString + ".");
            return true;
        }

        Class<? extends Quest> questClass = type.questClass;
        if (NPCQuest.class.isAssignableFrom(questClass) && !CubeQuest.getInstance().hasCitizensPlugin()) {
            ChatAndTextUtil.sendErrorMessage(sender, "NPC-Quests können nur auf Servern ertellt werden, auf denen das Citizens-Plugin installiert ist.");
            return true;
        }

        Quest quest = CubeQuest.getInstance().getQuestCreator().createQuest(questClass);

        int id = quest.getId();
        if (sender instanceof Player) {
            HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Quest " + id + " editieren").create());
            ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cubequest edit " + id);
            String msg = CubeQuest.PLUGIN_TAG + " " + ChatColor.GREEN + type + " mit Quest-ID " + id + " erfolgreich erstellt! ";
            ComponentBuilder cb = new ComponentBuilder(msg).append("[EDITIEREN]").event(ce).event(he);
            ((Player) sender).spigot().sendMessage(cb.create());
        } else {
            ChatAndTextUtil.sendNormalMessage(sender, type + " mit Quest-ID " + id + " erfolgreich erstellt!");
        }

        return true;
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        String arg = args.getNext("");
        List<String> result = new ArrayList<String>();
        for (QuestType type: QuestType.values()) {
            String typeString = type.toString();
            if (typeString.toLowerCase().startsWith(arg.toLowerCase())) {
                result.add(typeString);
            }
        }
        return result;
    }

}
