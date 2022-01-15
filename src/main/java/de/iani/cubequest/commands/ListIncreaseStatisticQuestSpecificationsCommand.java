package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.generation.IncreaseStatisticQuestSpecification.IncreaseStatisticQuestPossibilitiesSpecification;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.Collections;
import java.util.List;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;


public class ListIncreaseStatisticQuestSpecificationsCommand extends SubCommand {
    
    public static final String COMMAND_PATH = "listIncreaseStatisticQuestSpecifications";
    public static final String FULL_COMMAND = "quest " + COMMAND_PATH;
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {
        
        List<BaseComponent[]> list =
                IncreaseStatisticQuestPossibilitiesSpecification.getInstance().getSpecificationInfo();
        if (list.isEmpty()) {
            ChatAndTextUtil.sendNormalMessage(sender, "Es gibt keine Statistik-Quest-Möglichkeiten.");
            return true;
        }
        
        ChatAndTextUtil.sendMessagesPaged(sender, ChatAndTextUtil.bcToSendableList(list), args.getNext(1) - 1,
                "Statistik-Quest-Möglichkeiten", FULL_COMMAND);
        
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUEST_SPECIFICATIONS_PERMISSION;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        return Collections.emptyList();
    }
    
}
