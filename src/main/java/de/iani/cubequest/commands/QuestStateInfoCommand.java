package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class QuestStateInfoCommand extends SubCommand {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args) {
        
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Bitte gib die ID oder den Namen der Quest an, zu der du deinen Fortschritt einsehen möchtest.");
            return true;
        }
        
        OfflinePlayer player;
        
        if (sender.hasPermission(CubeQuest.SEE_PLAYER_INFO_PERMISSION) && args.remaining() >= 2) {
            String playerName = args.seeNext("");
            player = Bukkit.getPlayer(playerName);
            if (player == null) {
                player = CubeQuest.getInstance().getPlayerUUIDCache().getPlayer(playerName);
            }
            if (player == null) {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Spieler " + playerName + " nicht gefunden.");
                return true;
            } else {
                args.next();
            }
        } else if (!(sender instanceof Player)) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib einen Spieler an.");
            return true;
        } else {
            player = (Player) sender;
        }
        
        PlayerData data = CubeQuest.getInstance().getPlayerData(player);
        
        OfflinePlayer fPlayer = player;
        Quest quest = ChatAndTextUtil.getQuest(sender, args, q -> {
            return (fPlayer != sender)
                    || (q.isVisible() && data.getPlayerStatus(q.getId()) != Status.NOTGIVENTO);
        }, true, "quest state " + (player == sender ? "" : (player.getName() + " ")), "", "Quest ",
                " auswählen");
        
        if (quest == null) {
            return true;
        }
        
        ChatAndTextUtil.sendBaseComponent(sender, quest.getStateInfo(data));
        
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.ACCEPT_QUESTS_PERMISSION;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias,
            ArgsParser args) {
        if (!(sender instanceof Player)
                || sender.hasPermission(CubeQuest.SEE_PLAYER_INFO_PERMISSION)) {
            return ChatAndTextUtil.polishTabCompleteList(Bukkit.getOnlinePlayers().stream()
                    .map(p -> p.getName()).collect(Collectors.toList()), args.getNext(""));
        }
        
        List<String> result = new ArrayList<>();
        
        for (QuestState state: CubeQuest.getInstance().getPlayerData((Player) sender)
                .getActiveQuests()) {
            if (state.getQuest().isVisible()) {
                result.add(Integer.toString(state.getQuest().getId()));
            }
        }
        
        return ChatAndTextUtil.polishTabCompleteList(result, args.getNext(""));
    }
    
    @Override
    public String getUsage() {
        return "<Quest (Id oder Name)>";
    }
    
}
