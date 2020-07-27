package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.ChatAndTextUtil.StringMsg;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;


public class ListServerFlagsCommand extends SubCommand {
    
    public static final String COMMAND_PATH = "listServerFlags";
    public static final String FULL_COMMAND = "quest " + COMMAND_PATH;
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString, ArgsParser args) {
        
        int page;
        if (args.hasNext()) {
            page = args.getNext(0) - 1;
            if (page < 0) {
                ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Seitenzahl als positive Ganzzahl an.");
                return true;
            }
        } else {
            page = 0;
        }
        
        List<StringMsg> flagList = CubeQuest.getInstance().getServerFlags().stream().map(f -> new StringMsg(f))
                .collect(Collectors.toCollection(() -> new ArrayList<>()));
        Collections.sort(flagList, (s1, s2) -> s1.msg.compareTo(s2.msg));
        
        if (flagList.isEmpty()) {
            ChatAndTextUtil.sendNormalMessage(sender, "Dieser Server hat keine Flags.");
            return true;
        }
        
        ChatAndTextUtil.sendMessagesPaged(sender, flagList, args.getNext(0), "Server-Flags", FULL_COMMAND);
        
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
    
}
