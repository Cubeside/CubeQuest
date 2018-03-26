package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.questGiving.QuestGiver;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AcceptQuestCommand extends SubCommand {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args) {
        
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Bitte gib den Quest-Giver an, von dem du die Quest annehmen möchtest.");
            ChatAndTextUtil.sendWarningMessage(sender,
                    "(Am besten du nimmst die Quest direkt über den Quest-Giver an, anstatt es über den Befehl zu probieren.)");
            return true;
        }
        
        String giverName = args.getNext();
        QuestGiver giver = CubeQuest.getInstance().getQuestGiver(giverName);
        if (giver == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Dieser Quest-Giver existiert nicht.");
            return true;
        }
        
        if (giver.getInteractor().getLocation() == null || giver.getInteractor().getLocation()
                .distance(((Player) sender).getLocation()) > 7.0) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Du bist zu weit von diesem Quest-Giver entfernt.");
            return true;
        }
        
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Bitte gib die ID der Quest an, die du annehmen möchtest.");
            return true;
        }
        
        int questId = args.getNext(-1);
        if (questId < 1) {
            ChatAndTextUtil.sendWarningMessage(sender, "Es gibt keine Quest mit dieser ID.");
            return true;
        }
        
        Quest quest = QuestManager.getInstance().getQuest(questId);
        if (quest == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Es gibt keine Quest mit dieser ID.");
            return true;
        }
        
        if (!giver.mightGetFromHere(((Player) sender), quest)) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Du kannst diese Quest nicht an diesem Quest-Giver erhalten.");
            return true;
        }
        
        if (!quest.fullfillsGivingConditions((Player) sender)) {
            ChatAndTextUtil.sendWarningMessage(sender, "Diese Quest kannst du nicht annehmen.");
            return true;
        }
        
        quest.giveToPlayer(((Player) sender));
        giver.removeMightGetFromHere((Player) sender, quest);
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.ACCEPT_QUESTS_PERMISSION;
    }
    
    @Override
    public boolean requiresPlayer() {
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias,
            ArgsParser args) {
        return Collections.emptyList();
    }
    
    @Override
    public String getUsage() {
        return "<QuestGiverName> <QuestId>";
    }
    
}
