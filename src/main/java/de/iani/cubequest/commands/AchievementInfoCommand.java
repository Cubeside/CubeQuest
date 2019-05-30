package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.quests.ComplexQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.Util;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AchievementInfoCommand extends SubCommand {
    
    public static final String COMMAND_PATH = "achievements";
    public static final String FULL_COMMAND = "quest " + COMMAND_PATH;
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args) {
        
        OfflinePlayer player;
        if (args.hasNext()) {
            String playerString = args.next();
            try {
                UUID id = UUID.fromString(playerString);
                player = CubeQuest.getInstance().getPlayerUUIDCache().getPlayer(id);
            } catch (IllegalArgumentException e) {
                player = CubeQuest.getInstance().getPlayerUUIDCache().getPlayer(playerString);
            }
            
            if (player == null) {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Spieler " + playerString + " nicht gefunden.");
                return true;
            }
        } else if (sender instanceof Player) {
            player = (Player) sender;
        } else {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitt gib einen Spieler an.");
            return true;
        }
        
        ChatAndTextUtil.sendNormalMessage(sender,
                sender == player ? ("Deine erreichten Achievements:")
                        : ("Die erreicheten Achievements von " + player.getName() + ":"));
        PlayerData data = CubeQuest.getInstance().getPlayerData(player);
        for (ComplexQuest quest : CubeQuest.getInstance().getAchievementQuests()) {
            if (quest.getFollowupQuest() != null
                    && !data.isGivenTo(quest.getFollowupQuest().getId())) {
                continue;
            }
            if (!Util.isLegalAchievementQuest(quest.getFollowupQuest())) {
                continue;
            }
            
            ComponentBuilder builder = new ComponentBuilder(quest.getDisplayName());
            builder.color(ChatColor.BLUE).append(" (für nächste Stufe ");
            for (BaseComponent[] bc : quest.getSubQuests().iterator().next()
                    .getSpecificStateInfo(data, 0)) {
                builder.append(bc);
            }
            builder.append(")");
            sender.sendMessage(builder.create());
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias,
            ArgsParser args) {
        return Bukkit.getServer().getOnlinePlayers().stream().map(Player::getName)
                .collect(Collectors.toList());
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.ACCEPT_QUESTS_PERMISSION;
    }
    
    @Override
    public String getUsage() {
        return "[player]";
    }
    
}
