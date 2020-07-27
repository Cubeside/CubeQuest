package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import de.iani.playerUUIDCache.CachedPlayer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetQuestStatusForPlayerCommand extends SubCommand {
    
    public static String commandPath(Status status) {
        switch (status) {
            case FAIL:
                return "failForPlayer";
            case FROZEN:
                return "freezeForPlayer";
            case GIVENTO:
                return "giveToPlayer";
            case NOTGIVENTO:
                return "removeFromPlayer";
            case SUCCESS:
                return "succeedForPlayer";
            default:
                throw new NullPointerException();
            
        }
    }
    
    private Status status;
    
    public static String fullCommand(Status status) {
        return "quest " + commandPath(status);
    }
    
    public SetQuestStatusForPlayerCommand(Status status) {
        this.status = status;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString, ArgsParser args) {
        
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib einen Spieler an.");
            return true;
        }
        
        String playerName = args.getNext();
        OfflinePlayer player = Bukkit.getPlayerExact(playerName);
        UUID playerId = player != null ? player.getUniqueId() : null;
        if (player == null) {
            if (this.status == Status.NOTGIVENTO) {
                CachedPlayer cached = CubeQuest.getInstance().getPlayerUUIDCache().getPlayer(playerName);
                if (cached != null) {
                    player = cached;
                    playerId = cached.getUUID();
                    playerName = cached.getName();
                }
            }
            if (playerId == null) {
                ChatAndTextUtil.sendWarningMessage(sender, "Spieler " + playerName + " nicht gefunden.");
                return true;
            }
        } else {
            playerName = player.getName();
        }
        
        Quest quest = ChatAndTextUtil.getQuest(sender, args, fullCommand(this.status) + " " + playerName + " ", "", "Quest ",
                (this.status == Status.GIVENTO ? "an " + playerName + " geben"
                        : this.status == Status.NOTGIVENTO ? "von " + playerName + " entfernen"
                                : "für " + playerName + " auf " + (this.status == Status.SUCCESS ? "erfolgreich"
                                        : this.status == Status.FAIL ? "fehlgeschlagen" : "eingefrohren") + " setzen"));
        if (quest == null) {
            return true;
        }
        
        if (CubeQuest.getInstance().getPlayerData(player).getPlayerStatus(quest.getId()) == this.status) {
            ChatAndTextUtil.sendWarningMessage(sender, "Dieser Spieler hat für diese Quest bereits den Status " + this.status + ".");
            return true;
        }
        if (this.status != Status.GIVENTO && this.status != Status.NOTGIVENTO
                && CubeQuest.getInstance().getPlayerData(player).getPlayerStatus(quest.getId()) != Status.GIVENTO) {
            ChatAndTextUtil.sendWarningMessage(sender, "Diesem Spieler ist diese Quest nicht gegeben.");
            return true;
        }
        
        switch (this.status) {
            case GIVENTO:
                if (!quest.isReady()) {
                    ChatAndTextUtil.sendWarningMessage(sender, "Diese Quest ist nicht auf \"fertig\" gesetzt.");
                    return true;
                }
                
                quest.giveToPlayer((Player) player);
                ChatAndTextUtil.sendNormalMessage(sender, quest.getTypeName() + " [" + quest.getId() + "] an " + playerName + " vergegeben.");
                return true;
            case NOTGIVENTO:
                quest.removeFromPlayer(playerId);
                ChatAndTextUtil.sendNormalMessage(sender, quest.getTypeName() + " [" + quest.getId() + "] von " + playerName + " entfernt.");
                return true;
            case SUCCESS:
                quest.onSuccess((Player) player);
                ChatAndTextUtil.sendNormalMessage(sender,
                        quest.getTypeName() + " [" + quest.getId() + "] für " + playerName + " auf erfolgreich gesetzt.");
                return true;
            case FAIL:
                quest.onFail((Player) player);
                ChatAndTextUtil.sendNormalMessage(sender,
                        quest.getTypeName() + " [" + quest.getId() + "] für " + playerName + " auf fehlgeschlagen gesetzt.");
                return true;
            case FROZEN:
                quest.onFreeze((Player) player);
                ChatAndTextUtil.sendNormalMessage(sender,
                        quest.getTypeName() + " [" + quest.getId() + "] für " + playerName + " auf eingefrohren gesetzt.");
                return true;
            default:
                throw new NullPointerException();
            
        }
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUEST_STATES_PERMISSION;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        if (args.remaining() > 1) {
            return Collections.emptyList();
        }
        
        List<String> result = new ArrayList<>();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            result.add(player.getName());
        }
        
        return ChatAndTextUtil.polishTabCompleteList(result, args.getNext(""));
    }
    
    @Override
    public String getUsage() {
        return "<Spieler> <Quest (Id oder Name)>";
    }
}
