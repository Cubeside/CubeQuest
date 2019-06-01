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

public class AddOrRemoveSubQuestCommand extends SubCommand {
    
    public static final String ADD_COMMAND_PATH = "addSubQuest";
    public static final String FULL_ADD_COMMAND = "quest " + ADD_COMMAND_PATH;
    public static final String REMOVE_COMMAND_PATH = "removeSubQuest";
    public static final String FULL_REMOVE_COMMAND = "quest " + REMOVE_COMMAND_PATH;
    
    private boolean add;
    
    public AddOrRemoveSubQuestCommand(boolean add) {
        this.add = add;
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
                    "Diese Quest unterstützt keine Unterquests.");
            return true;
        }
        
        if (((ComplexQuest) quest).isAchievementQuest()
                && ((ComplexQuest) quest).getSubQuests().size() == 1) {
            if (this.add) {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Zu einer Achievement-Quest können keine weiteren Unterquests hinzugefügt werden.");
            } else {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Die Unterquest einer Achievement-Quest kann nicht entfernt werden.");
            }
            return true;
        }
        
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die "
                    + (this.add ? "hinzuzufügende" : "zu entfernende") + " Unterquest an.");
            return true;
        }
        
        Quest otherQuest = ChatAndTextUtil.getQuest(sender, args,
                "/cubequest " + (this.add ? "add" : "remove") + "SubQuest ", "", "Quest ",
                (this.add ? "hinzufügen" : "entfernen") + ".");
        
        if (otherQuest == null) {
            return true;
        }
        
        if (this.add) {
            try {
                if (((ComplexQuest) quest).addSubQuest(otherQuest)) {
                    ChatAndTextUtil.sendNormalMessage(sender, "SubQuest hinzugefügt.");
                } else {
                    ChatAndTextUtil.sendWarningMessage(sender, "SubQuest war bereits enthalten.");
                }
            } catch (CircleInQuestGraphException e) {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Diese Unterquest hinzuzufügen würde einen Zirkelschluss im Quest-Graph erzeugen"
                                + " (sprich: die hinzuzufügende Quest ist die selbe Quest oder das gilt für eine ihre Unterquests).");
                return true;
            }
        } else {
            if (quest.isReady() && ((ComplexQuest) quest).getSubQuests()
                    .equals(Collections.singleton(otherQuest))) {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Diese Quest ist bereits auf fertig gesetzt und hat nur diese Unterquest. Sie kann daher nicht entfernt werden.");
                return true;
            }
            
            if (((ComplexQuest) quest).removeSubQuest(otherQuest)) {
                ChatAndTextUtil.sendNormalMessage(sender, "SubQuest entfernt.");
            } else {
                ChatAndTextUtil.sendWarningMessage(sender, "SubQuest war nicht enthalten.");
            }
        }
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
        return "<Quest (Id oder Name)>";
    }
    
}
