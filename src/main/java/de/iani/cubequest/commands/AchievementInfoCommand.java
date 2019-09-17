package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.questStates.AmountQuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.quests.AmountQuest;
import de.iani.cubequest.quests.ComplexQuest;
import de.iani.cubequest.quests.EntityTypesAndAmountQuest;
import de.iani.cubequest.quests.MaterialsAndAmountQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.Util;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AchievementInfoCommand extends SubCommand {
    
    public static final String COMMAND_PATH = "achievements";
    public static final String FULL_COMMAND = "quest " + COMMAND_PATH;
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString, ArgsParser args) {
        
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
                ChatAndTextUtil.sendWarningMessage(sender, "Spieler " + playerString + " nicht gefunden.");
                return true;
            }
        } else if (sender instanceof Player) {
            player = (Player) sender;
        } else {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitt gib einen Spieler an.");
            return true;
        }
        
        ChatAndTextUtil.sendNormalMessage(sender,
                sender == player ? ("Deine erreichten Achievements:") : ("Die erreicheten Achievements von " + player.getName() + ":"));
        PlayerData data = CubeQuest.getInstance().getPlayerData(player);
        boolean none = true;
        
        List<ComplexQuest> achievementQuests = QuestManager.getInstance().getQuests(ComplexQuest.class).stream()
                .filter(ComplexQuest::isAchievementQuest).collect(Collectors.toCollection(ArrayList::new));
        achievementQuests.sort(Quest.QUEST_DISPLAY_COMPARATOR);
        
        for (ComplexQuest quest : achievementQuests) {
            if (quest.getFollowupQuest() != null && !data.isGivenTo(quest.getFollowupQuest().getId())) {
                continue;
            }
            if (quest.getFollowupQuest() == null && data.getPlayerStatus(quest.getId()) != Status.SUCCESS) {
                continue;
            }
            if (quest.getFollowupQuest() != null && !Util.isLegalAchievementQuest(quest.getFollowupQuest())) {
                continue;
            }
            
            none = false;
            ComponentBuilder builder = new ComponentBuilder(quest.getDisplayName());
            builder.color(ChatColor.GOLD);
            if (quest.getFollowupQuest() != null) {
                AmountQuest inner = (AmountQuest) ((ComplexQuest) quest.getFollowupQuest()).getSubQuests().iterator().next();
                
                String possibilities = null;
                if (inner instanceof MaterialsAndAmountQuest) {
                    possibilities = ChatAndTextUtil.multipleMaterialsString(((MaterialsAndAmountQuest) inner).getTypes());
                } else if (inner instanceof EntityTypesAndAmountQuest) {
                    possibilities = ChatAndTextUtil.multipleMobsString(((EntityTypesAndAmountQuest) inner).getTypes());
                }
                
                builder.append(" (für nächste Stufe: ").color(ChatColor.BLUE);
                if (possibilities != null) {
                    builder.event(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder(possibilities).create()));
                }
                
                builder.append(String.valueOf(((AmountQuestState) data.getPlayerState(inner.getId())).getAmount())).color(ChatColor.AQUA)
                        .append(" / ").append(String.valueOf(inner.getAmount())).append(")").color(ChatColor.BLUE);
            } else {
                builder.append(" (höchste Stufe)").color(ChatColor.BLUE);
            }
            sender.sendMessage(builder.create());
        }
        
        if (none)
        
        {
            ChatAndTextUtil.sendNormalMessage(sender, "- keine -");
        }
        
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        return Bukkit.getServer().getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList());
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
