package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SetOrAddQuestMessageCommand extends SubCommand {
    
    private boolean set;
    private MessageTrigger when;
    
    public enum MessageTrigger {
        DISPLAY("Display"), GIVE("Give"), SUCCESS("Success"), FAIL("Fail");
        
        public final String commandPathInfix;
        public final String setCommandPath;
        public final String fullSetCommand;
        public final String addCommandPath;
        public final String fullAddCommand;
        
        private MessageTrigger(String commandPathInfix) {
            this.commandPathInfix = commandPathInfix;
            
            this.setCommandPath = "set" + commandPathInfix + "Message";
            this.fullSetCommand = "quest " + this.setCommandPath;
            this.addCommandPath = "add" + commandPathInfix + "Message";
            this.fullAddCommand = "quest " + this.addCommandPath;
        }
        
    }
    
    public SetOrAddQuestMessageCommand(boolean set, MessageTrigger when) {
        this.set = set;
        this.when = when;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args) {
        
        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }
        
        String msg = args.hasNext() ? ChatAndTextUtil.convertColors(args.getAll(null)) : null;
        
        if (this.set) {
            switch (this.when) {
                case DISPLAY:
                    quest.setDisplayMessage(msg);
                    break;
                case GIVE:
                    quest.setGiveMessage(msg);
                    break;
                case SUCCESS:
                    quest.setSuccessMessage(msg);
                    break;
                case FAIL:
                    quest.setFailMessage(msg);
                    break;
            }
        } else {
            if (msg == null) {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Bitte gib den Text an, den du zur Nachricht hinzufügen möchtest.");
                return true;
            }
            switch (this.when) {
                case DISPLAY:
                    quest.addDisplayMessage(msg);
                    msg = quest.getDisplayMessage();
                    break;
                case GIVE:
                    quest.addGiveMessage(msg);
                    msg = quest.getGiveMessage();
                    break;
                case SUCCESS:
                    quest.addSuccessMessage(msg);
                    msg = quest.getSuccessMessage();
                    break;
                case FAIL:
                    quest.addFailMessage(msg);
                    msg = quest.getFailMessage();
                    break;
            }
        }
        
        if (msg == null) {
            ChatAndTextUtil.sendNormalMessage(sender, this.when.commandPathInfix + "Message für "
                    + quest.getTypeName() + " [" + quest.getId() + "] gelöscht.");
        } else {
            ChatAndTextUtil.sendNormalMessage(sender, this.when.commandPathInfix + "Message für "
                    + quest.getTypeName() + " [" + quest.getId() + "] lautet jetzt:");
            sender.sendMessage(msg);
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
        return "<Nachricht>";
    }
    
}
