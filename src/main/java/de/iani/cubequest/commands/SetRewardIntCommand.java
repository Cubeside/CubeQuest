package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.Reward;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SetRewardIntCommand extends SubCommand {
    
    private boolean success;
    private Attribute attribute;
    
    public enum Attribute {
        CUBES("Cubes"), QUEST_POINTS("Quest-Points"), XP("XP");
        
        public final String name;
        public final String successCommandPath;
        public final String fullSuccessCommand;
        public final String failCommandPath;
        public final String fullFailCommand;
        
        private Attribute(String name) {
            this.name = name;
            
            this.successCommandPath = "setSuccessReward" + name;
            this.fullSuccessCommand = "quest " + this.successCommandPath;
            this.failCommandPath = "setFailReward" + name;
            this.fullFailCommand = "quest " + this.failCommandPath;
        }
    }
    
    public SetRewardIntCommand(boolean success, Attribute attribute) {
        this.success = success;
        this.attribute = attribute;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args) {
        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }
        
        int newValue = args.getNext(-1);
        if (newValue < 0) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Anzahl an "
                    + this.attribute.name + " als nicht-negative Ganzzahl an.");
            return true;
        }
        
        Reward formerReward = this.success ? quest.getSuccessReward() : quest.getFailReward();
        int newCubes = this.attribute == Attribute.CUBES ? newValue
                : formerReward == null ? 0 : formerReward.getCubes();
        int newQuestPoints = this.attribute == Attribute.QUEST_POINTS ? newValue
                : formerReward == null ? 0 : formerReward.getQuestPoints();
        int newXp = this.attribute == Attribute.XP ? newValue
                : formerReward == null ? 0 : formerReward.getXp();
        
        Reward resultReward = new Reward(newCubes, newQuestPoints, newXp,
                formerReward == null ? null : formerReward.getItems());
        if (this.success) {
            quest.setSuccessReward(resultReward);
        } else {
            quest.setFailReward(resultReward);
        }
        
        if (resultReward.isEmpty()) {
            ChatAndTextUtil.sendNormalMessage(sender,
                    (this.success ? "Erfolgsbelohnung" : "Trostpreis") + " für "
                            + quest.getTypeName() + " [" + quest.getId() + "] entfernt.");
        } else {
            ChatAndTextUtil.sendNormalMessage(sender,
                    this.attribute.name + " in "
                            + (this.success ? "Erfolgsbelohnung" : "Trostpreis") + " für "
                            + quest.getTypeName() + " [" + quest.getId() + "] gesetzt.");
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
        return "<" + this.attribute.name.replace(Pattern.quote("-"), "") + ">";
    }
    
}
