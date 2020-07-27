package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.Collections;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class QuestInfoCommand extends SubCommand {
    
    public static final String COMMAND_PATH = "questInfo";
    public static final String FULL_COMMAND = "quest " + COMMAND_PATH;
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString, ArgsParser args) {
        
        if (!args.hasNext()) {
            Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
            if (quest != null) {
                Bukkit.dispatchCommand(sender, "cubequest questInfo " + quest.getId());
                return true;
            }
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib eine Quest an.");
            return true;
        }
        
        Quest quest = ChatAndTextUtil.getQuest(sender, args, FULL_COMMAND + " ", "", "Info zu Quest ", " anzeigen");
        if (quest == null) {
            return true;
        }
        
        List<BaseComponent[]> info = quest.getQuestInfo();
        ComponentBuilder builder = new ComponentBuilder("[EDITIEREN]");
        builder.bold(true).color(quest.isReady() ? ChatColor.RED : ChatColor.GREEN)
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Quest " + quest.getId() + " editieren").create()))
                .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cubequest edit " + quest.getId()));
        info.add(builder.create());
        
        ChatAndTextUtil.sendBaseComponent(sender, info);
        sender.sendMessage("");
        
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        return Collections.emptyList();
    }
    
    @Override
    public String getUsage() {
        return "<Quest (Editing oder Id oder Name)>";
    }
    
}
