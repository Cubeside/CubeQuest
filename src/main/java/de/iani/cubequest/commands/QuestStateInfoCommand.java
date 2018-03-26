package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.chat.BaseComponent;
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
        
        Player player = (Player) sender;
        PlayerData data = CubeQuest.getInstance().getPlayerData(player);
        
        Quest quest = ChatAndTextUtil.getQuest(sender, args, q -> {
            return q.isVisible() && data.getPlayerStatus(q.getId()) != Status.NOTGIVENTO;
        }, true, "quest state ", "", "Quest ", " auswählen");
        
        if (quest == null) {
            return true;
        }
        
        for (BaseComponent[] bc: quest.getStateInfo(data)) {
            player.spigot().sendMessage(bc);
        }
        
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
        List<String> result = new ArrayList<>();
        
        for (QuestState state: CubeQuest.getInstance().getPlayerData((Player) sender)
                .getActiveQuests()) {
            result.add(Integer.toString(state.getQuest().getId()));
        }
        
        return ChatAndTextUtil.polishTabCompleteList(result, args.getNext(""));
    }
    
    @Override
    public String getUsage() {
        return "<Spieler> <Quest (Id oder Name)>";
    }
    
}
