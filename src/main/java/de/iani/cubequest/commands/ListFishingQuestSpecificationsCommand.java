package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.generation.QuestGenerator;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.Collections;
import java.util.List;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;


public class ListFishingQuestSpecificationsCommand extends SubCommand {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args) {
        
        List<BaseComponent[]> list = QuestGenerator.getInstance().getFishingSpecificationInfo();
        if (list.isEmpty()) {
            ChatAndTextUtil.sendNormalMessage(sender,
                    "Es gibt keine Angel-Quest-Materialkombinationen.");
            return true;
        }
        
        ChatAndTextUtil.sendMessagesPaged(sender, ChatAndTextUtil.bcToSendableList(list),
                args.getNext(1) - 1, "Angel-Quest-Materialkombinationen",
                "/quest listFishingQuestSpecifications");
        
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUEST_SPECIFICATIONS_PERMISSION;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias,
            ArgsParser args) {
        return Collections.emptyList();
    }
    
}
