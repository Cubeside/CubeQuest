package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestGiver;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.quests.ComplexQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;


public class ListReferencesCommand extends SubCommand {
    
    public static final String COMMAND_PATH = "references";
    public static final String FULL_COMMAND = "quest " + COMMAND_PATH;
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {
        Quest quest =
                ChatAndTextUtil.getQuest(sender, args, FULL_COMMAND + " ", "", "Vorkommen von Quest ", " anzeigen");
        if (quest == null) {
            return true;
        }
        
        List<BaseComponent[]> results = new ArrayList<>();
        for (QuestGiver giver : CubeQuest.getInstance().getQuestGivers()) {
            if (giver.hasQuest(quest)) {
                results.add(new ComponentBuilder("Quest-Giver").color(ChatColor.BLUE).append(" ")
                        .append(giver.getName()).color(ChatColor.GREEN).create());
            }
        }
        
        for (ComplexQuest other : QuestManager.getInstance().getQuests(ComplexQuest.class)) {
            if (other.getSubQuests().contains(quest)) {
                ComponentBuilder builder = new ComponentBuilder("Complex-Quest").color(ChatColor.LIGHT_PURPLE)
                        .append(" " + other.getId()).color(ChatColor.GREEN);
                HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Info anzeigen"));
                ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/" + QuestInfoCommand.FULL_COMMAND + " " + other.getId());
                builder.event(he).event(ce);
                if (!other.getInternalName().isEmpty()) {
                    builder.append(" (").append(other.getInternalName()).append(")");
                }
                results.add(builder.create());
            }
        }
        
        ChatAndTextUtil.sendNormalMessage(sender, "Vorkommen von Quest " + quest.getId()
                + (quest.getInternalName().isEmpty() ? "" : (" (" + quest.getInternalName() + ")")) + ":");
        if (results.isEmpty()) {
            sender.sendMessage(ChatColor.GOLD + "-- KEINE --");
        }
        for (BaseComponent[] bc : results) {
            sender.sendMessage(bc);
        }
        
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
    
}
