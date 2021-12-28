package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.IncreaseStatisticQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesidestats.api.StatisticKey;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class AddOrRemoveStatisticCommand extends SubCommand {
    
    public static final String ADD_COMMAND_PATH = "addStatistic";
    public static final String FULL_ADD_COMMAND = "quest " + ADD_COMMAND_PATH;
    public static final String REMOVE_COMMAND_PATH = "removeStatistic";
    public static final String FULL_REMOVE_COMMAND = "quest " + REMOVE_COMMAND_PATH;
    
    private boolean add;
    
    public AddOrRemoveStatisticCommand(boolean add) {
        this.add = add;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {
        
        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }
        
        if (!(quest instanceof IncreaseStatisticQuest isq)) {
            ChatAndTextUtil.sendWarningMessage(sender, "Diese Quest erfordert keine Statistiken.");
            return true;
        }
        
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib an, welche Statistik " + (this.add ? "zu" : "von")
                    + " der Quest " + (this.add ? "hinzugefügt" : "entfernt") + " werden soll.");
            return true;
        }
        String keyName = args.getNext();
        StatisticKey key = CubeQuest.getInstance().getCubesideStatistics().getStatisticKey(keyName, false);
        if (key == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Statistik " + keyName + " nicht gefunden.");
            return true;
        }
        
        boolean changed = this.add ? isq.addStatistic(key) : isq.removeStatistic(key);
        if (changed) {
            ChatAndTextUtil.sendNormalMessage(sender,
                    "Statistik " + key.getName() + (this.add ? " zu " : " von ") + quest.getTypeName() + " ["
                            + quest.getId() + "] " + (this.add ? "hinzugefügt" : "entfernt") + ".");
        } else {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Die Statistik " + key.getName() + " war in " + quest.getTypeName() + " [" + quest.getId() + "] "
                            + (this.add ? "bereits" : "nicht") + " vorhanden.");
        }
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        List<String> result = CubeQuest.getInstance().getCubesideStatistics().getAllStatisticKeys().stream()
                .map(StatisticKey::getName).toList();
        return ChatAndTextUtil.polishTabCompleteList(result, args.getNext(""));
    }
    
    @Override
    public String getUsage() {
        return "<Statistic>";
    }
    
}
