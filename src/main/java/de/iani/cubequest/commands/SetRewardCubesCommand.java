package de.iani.cubequest.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.Reward;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;

public class SetRewardCubesCommand extends SubCommand {

    private boolean success;

    public SetRewardCubesCommand(boolean success) {
        this.success = success;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {
        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }

        int newCubes = args.getNext(-1);
        if (newCubes < 0) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Anzahl an Cubes als nicht-negative Ganzzahl an.");
            return true;
        }

        Reward formerReward = success? quest.getSuccessReward() : quest.getFailReward();

        Reward resultReward = formerReward == null? new Reward(newCubes) : new Reward(newCubes, formerReward.getItems());
        if (success) {
            quest.setSuccessReward(resultReward);
        } else {
            quest.setFailReward(resultReward);
        }

        if (resultReward.isEmpty()) {
            ChatAndTextUtil.sendNormalMessage(sender, (success? "Erfolgsbelohnung" : "Trostpreis") + " für " + quest.getTypeName() + " [" + quest.getId() + "] entfernt.");
        } else {
            ChatAndTextUtil.sendNormalMessage(sender, "Cubes in " + (success? "Erfolgsbelohnung" : "Trostpreis") + " für " + quest.getTypeName() + " [" + quest.getId() + "] gesetzt.");
        }

        return true;
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }

}
