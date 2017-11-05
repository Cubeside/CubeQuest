package de.iani.cubequest.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.Reward;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;

public class SetRewardIntCommand extends SubCommand {

    private boolean success;
    private Attribute attribute;

    public enum Attribute {
        CUBES("Cubes"),
        QUEST_POINTS("Quest-Points"),
        XP("XP");

        public final String name;

        private Attribute(String name) {
            this.name = name;
        }
    }

    public SetRewardIntCommand(boolean success, Attribute attribute) {
        this.success = success;
        this.attribute = attribute;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {
        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }

        int newValue = args.getNext(-1);
        if (newValue < 0) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Anzahl an " + attribute.name + " als nicht-negative Ganzzahl an.");
            return true;
        }

        Reward formerReward = success? quest.getSuccessReward() : quest.getFailReward();
        int newCubes = attribute == Attribute.CUBES? newValue : formerReward == null? 0 : formerReward.getCubes();
        int newQuestPoints = attribute == Attribute.QUEST_POINTS? newValue : formerReward == null? 0 : formerReward.getQuestPoints();
        int newXp = attribute == Attribute.XP? newValue : formerReward == null? 0 : formerReward.getXp();

        Reward resultReward = new Reward(newCubes, newQuestPoints, newXp, formerReward == null? null : formerReward.getItems());
        if (success) {
            quest.setSuccessReward(resultReward);
        } else {
            quest.setFailReward(resultReward);
        }

        if (resultReward.isEmpty()) {
            ChatAndTextUtil.sendNormalMessage(sender, (success? "Erfolgsbelohnung" : "Trostpreis") + " für " + quest.getTypeName() + " [" + quest.getId() + "] entfernt.");
        } else {
            ChatAndTextUtil.sendNormalMessage(sender, attribute.name + " in " + (success? "Erfolgsbelohnung" : "Trostpreis") + " für " + quest.getTypeName() + " [" + quest.getId() + "] gesetzt.");
        }

        return true;
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }

}
