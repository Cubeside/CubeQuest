package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.Collections;
import java.util.List;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ShowLevelCommand extends SubCommand {
    
    public static final String COMMAND_PATH = "showLevel";
    public static final String FULL_COMMAND = "quest " + COMMAND_PATH;
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args) {
        
        OfflinePlayer player;
        if (args.hasNext()) {
            String playerName = args.next();
            player = CubeQuest.getInstance().getPlayerUUIDCache().getPlayer(playerName);
            
            if (player == null) {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Spieler " + playerName + " nicht gefunden.");
                return true;
            }
        } else if (!(sender instanceof Player)) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib einen Spieler an.");
            return true;
        } else {
            player = (Player) sender;
        }
        
        PlayerData data = CubeQuest.getInstance().getPlayerData(player);
        
        int level = data.getLevel();
        int xp = data.getXp();
        int requiredXp = PlayerData.getXpRequiredForLevel(level + 1);
        
        ChatAndTextUtil.sendNormalMessage(sender,
                (sender == player ? "Du hast " : (player.getName() + " hat ")) + xp
                        + " Quest-XP und " + (sender == player ? "bist" : "ist") + " damit Level "
                        + level + ".");
        
        if (player == sender || sender.hasPermission(CubeQuest.SEE_PLAYER_INFO_PERMISSION)) {
            ChatAndTextUtil.sendNormalMessage(sender,
                    (player == sender ? "Dir" : player.getName()) + " fehl"
                            + (requiredXp - xp == 1 ? "t" : "en") + " noch " + (requiredXp - xp)
                            + " Quest-XP zum nächsten Level.");
            ChatAndTextUtil.sendNormalMessage(sender,
                    (sender == player ? "Du hast " : (player.getName() + " hat "))
                            + data.getQuestPoints() + " Quest-Punkt"
                            + (data.getQuestPoints() == 1 ? "" : "e") + ".");
        }
        
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.ACCEPT_QUESTS_PERMISSION;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias,
            ArgsParser args) {
        return Collections.emptyList();
    }
    
    @Override
    public String getUsage() {
        return "(zeigt dein aktuelles Quest-Level, deine Quest-XP und deine Quest-Punkte an)";
    }
    
}
