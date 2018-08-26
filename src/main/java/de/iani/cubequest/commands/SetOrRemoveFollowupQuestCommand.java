package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.ComplexQuest;
import de.iani.cubequest.quests.ComplexQuest.CircleInQuestGraphException;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SetOrRemoveFollowupQuestCommand extends SubCommand {
    
    public static final String SET_COMMAND_PATH = "setFollowupQuest";
    public static final String FULL_SET_COMMAND = "quest " + SET_COMMAND_PATH;
    public static final String REMOVE_COMMAND_PATH = "removeFollowupQuest";
    public static final String FULL_REMOVE_COMMAND = "quest " + REMOVE_COMMAND_PATH;
    
    private boolean set;
    
    public SetOrRemoveFollowupQuestCommand(boolean set) {
        this.set = set;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args) {
        
        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }
        
        if (!(quest instanceof ComplexQuest)) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Diese Quest unterstützt keine Nachfolgequest.");
            return true;
        }
        
        if (!this.set) {
            ((ComplexQuest) quest).setFollowupQuest(null);
            ChatAndTextUtil.sendNormalMessage(sender, "Nachfolgequest entfernt.");
            return true;
        }
        
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die neue Nachfolgequest an.");
            return true;
        }
        
        // String otherQuestString = args.getNext();
        Quest otherQuest = ChatAndTextUtil.getQuest(sender, args, "/cubequest setFollowupQuest ",
                "", "Quest ", " als Nachfolger festlegen");
        if (otherQuest == null) {
            return true;
        }
        
        try {
            ((ComplexQuest) quest).setFollowupQuest(otherQuest);
        } catch (CircleInQuestGraphException e) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Diese Unterquest hinzuzufügen würde einen Zirkelschluss im Quest-Graph erzeugen (sprich: die hinzuzufügende Quest ist die selbe Quest oder das gilt für eine ihre Unterquests).");
            return true;
        }
        ChatAndTextUtil.sendNormalMessage(sender, "Nachfolgequest gesetzt.");
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias,
            ArgsParser args) {
        return Collections.emptyList();
    }
    
    @Override
    public String getUsage() {
        return "<FollowupQuest (Id oder Name)>";
    }
    
}
