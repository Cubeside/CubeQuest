package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.bukkit.ChatUtilBukkit;
import de.iani.cubesideutils.bukkit.ChatUtilBukkit.StringMsg;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;


public class ListPlayersWithStateCommand extends SubCommand {
    
    public static final String COMMAND_PATH = "listPlayersByState";
    public static final String FULL_COMMAND = "quest " + COMMAND_PATH;
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib einen Quest-Status an.");
            return true;
        }
        
        String statusString = args.next();
        Status status = Status.match(statusString);
        if (status == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Status \"" + statusString + "\" nicht gefunden.");
            return true;
        }
        
        Quest quest = ChatAndTextUtil.getQuest(sender, args, COMMAND_PATH + " " + status + " ", "",
                "Spieler mit Status " + status + " zu Quest ", " suchen");
        if (quest == null) {
            return true;
        }
        
        int page = args.hasNext() ? args.getNext(0) - 1 : 0;
        
        List<StringMsg> playerList;
        try {
            playerList = CubeQuest.getInstance().getDatabaseFassade().getPlayersWithState(quest.getId(), status)
                    .stream().map(ChatAndTextUtil::getPlayerString).map(StringMsg::new)
                    .collect(Collectors.toCollection(ArrayList::new));
            playerList.sort((m1, m2) -> String.CASE_INSENSITIVE_ORDER.compare(m1.message, m2.message));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        
        ChatUtilBukkit.sendMessagesPaged(sender, playerList, page,
                "Spieler mit Status " + status + " in Quest " + quest.getId(),
                FULL_COMMAND + " " + status + " " + " " + quest.getId());
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.SEE_PLAYER_INFO_PERMISSION;
    }
    
    @Override
    public Collection<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        args.getNext(null);
        if (!args.hasNext()) {
            return Arrays.stream(Status.values()).map(Status::name).toList();
        }
        
        return Collections.emptyList();
    }
    
    @Override
    public String getUsage() {
        return "<Status> <Quest> [Seite]";
    }
    
}
