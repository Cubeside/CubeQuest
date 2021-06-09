package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class GiveBackQuestCommand extends SubCommand {
    
    public static final String COMMAND_PATH = "giveBack";
    public static final String FULL_COMMAND = "quest " + COMMAND_PATH;
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Bitte gib die ID oder den Namen der Quest an, die du zurückgeben möchtest.");
            return true;
        }
        
        Player player = (Player) sender;
        PlayerData data = CubeQuest.getInstance().getPlayerData(player);
        
        Quest quest = ChatAndTextUtil.getQuest(sender, args, q -> {
            return (q.isAllowGiveBack() && data.getPlayerStatus(q.getId()) == Status.GIVENTO);
        }, true, FULL_COMMAND + " " + player.getName() + " ", "", "Quest ", " auswählen");
        
        if (quest == null) {
            return true;
        }
        
        quest.removeFromPlayer(player.getUniqueId());
        ChatAndTextUtil.sendNormalMessage(player, "Quest zurückgegeben.");
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
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        return Collections.emptyList();
    }
    
    @Override
    public String getUsage() {
        return "<Quest>";
    }
    
}
