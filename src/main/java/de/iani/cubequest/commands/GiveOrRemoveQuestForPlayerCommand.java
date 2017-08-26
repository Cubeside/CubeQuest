package de.iani.cubequest.commands;

import static de.iani.cubequest.util.ChatAndTextUtil.ID_PLACEHOLDER;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;;

public class GiveOrRemoveQuestForPlayerCommand extends SubCommand {

    private boolean give;

    public GiveOrRemoveQuestForPlayerCommand(boolean give) {
        this.give = give;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib einen Spieler an.");
            return true;
        }

        String playerName = args.getNext();
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Spieler " + playerName + " nicht gefunden.");
            return true;
        }

        Quest quest = ChatAndTextUtil.getQuest(sender, args, "cubequest " + (give? "giveTo" : "removeFrom") + "Player " + player.getName() + " " + ID_PLACEHOLDER,
                "Quest " + ID_PLACEHOLDER + (give? " an " + playerName + " geben" : " von " + playerName + " entfernen"));
        if (quest == null) {
            return true;
        }

        if (give) {
            if (!quest.isReady()) {
                ChatAndTextUtil.sendWarningMessage(sender, "Diese Quest ist nicht auf \"fertig\" gesetzt.");
                return true;
            }
            if (CubeQuest.getInstance().getPlayerData(player).getPlayerStatus(quest.getId()) == Status.GIVENTO) {
                ChatAndTextUtil.sendWarningMessage(sender, "Diesem Spieler wurde diese Quest bereits gegeben.");
                return true;
            }

            quest.giveToPlayer(player);
            ChatAndTextUtil.sendNormalMessage(sender, quest.getTypeName() + " [" + quest.getId() + "] an " + playerName + " vergegeben.");
            return true;
        } else {
            if (CubeQuest.getInstance().getPlayerData(player).getPlayerStatus(quest.getId()) != Status.GIVENTO) {
                ChatAndTextUtil.sendWarningMessage(sender, "Diesem Spieler war diese Quest nicht gegeben.");
                return true;
            }
            quest.removeFromPlayer(player.getUniqueId());
            ChatAndTextUtil.sendNormalMessage(sender, quest.getTypeName() + " [" + quest.getId() + "] von " + playerName + " entfernt.");
            return true;
        }
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUEST_STATES_PERMISSION;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        String arg = args.getNext("").toLowerCase(Locale.ENGLISH);
        List<String> result = new ArrayList<String>();
        for (Player p: Bukkit.getOnlinePlayers()) {
            if (p.getName().toLowerCase(Locale.ENGLISH).startsWith(arg)) {
                result.add(p.getName());
            }
        }
        return result;
    }

}
