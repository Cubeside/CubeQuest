package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.interactiveBookAPI.InteractiveBookAPI;
import de.iani.interactiveBookAPI.InteractiveBookAPIPlugin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class ShowPlayerQuestsCommand extends SubCommand {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args) {
        
        OfflinePlayer player;
        
        if (args.remaining() > 0) {
            if (!sender.hasPermission(CubeQuest.SEE_PLAYER_INFO_PERMISSION)) {
                ChatAndTextUtil.sendNoPermissionMessage(sender);
                return true;
            }
            
            String playerName = args.next();
            player = CubeQuest.getInstance().getPlayerUUIDCache().getPlayer(playerName);
            
            if (player == null) {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Spieler " + playerName + " nicht gefunden.");
                return true;
            }
        } else {
            player = (Player) sender;
        }
        
        List<Quest> showableQuests = new ArrayList<>();
        PlayerData playerData = CubeQuest.getInstance().getPlayerData(player);
        playerData.getActiveQuests().stream().map(q -> q.getQuest()).filter(q -> q.isVisible())
                .forEach(q -> showableQuests.add(q));
        showableQuests.sort(Quest.QUEST_DISPLAY_COMPARATOR);
        
        InteractiveBookAPI bookAPI = JavaPlugin.getPlugin(InteractiveBookAPIPlugin.class);
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setDisplayName("Quests");
        
        if (showableQuests.isEmpty()) {
            ComponentBuilder builder = new ComponentBuilder("");
            builder.append("Du hast aktuell keine offenen Quests.").bold(true)
                    .color(ChatColor.GOLD);
            bookAPI.addPage(meta, builder.create());
        } else {
            for (Quest q: showableQuests) {
                List<BaseComponent[]> displayMessageList = ChatAndTextUtil.getQuestDescription(q);
                
                HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("Hier klicken").create());
                ClickEvent stateClickEvent =
                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/quest stateInfo "
                                + (player == sender ? "" : (player.getName() + " ")) + q.getId());
                ClickEvent giveMessageClickEvent =
                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/quest showGiveMessage "
                                + (player == sender ? "" : (player.getName() + " ")) + q.getId());
                
                displayMessageList.add(new ComponentBuilder("").append("Fortschritt anzeigen")
                        .color(ChatColor.DARK_GREEN).bold(true).event(stateClickEvent)
                        .event(hoverEvent).create());
                displayMessageList.add(null);
                displayMessageList.add(new ComponentBuilder("")
                        .append("Vergabe-Nachricht erneut anzeigen").color(ChatColor.DARK_GREEN)
                        .bold(true).event(giveMessageClickEvent).event(hoverEvent).create());
                
                ChatAndTextUtil.writeIntoBook(meta, displayMessageList);
            }
        }
        
        meta.setAuthor(CubeQuest.PLUGIN_TAG);
        book.setItemMeta(meta);
        bookAPI.showBookToPlayer((Player) sender, book);
        
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
        return Collections.emptyList();
    }
    
    @Override
    public String getUsage() {
        return "(Zeigt dein aktiven Quests an.)";
    }
    
}
