package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import de.iani.interactiveBookAPI.InteractiveBookAPI;
import de.iani.interactiveBookAPI.InteractiveBookAPIPlugin;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class ShowPlayerQuestsCommand extends SubCommand {
    
    private static final int MAX_NUM_PAGES_QUEST_LIST = 30;
    
    public static String getCommandPath(Status status) {
        if (status == null) {
            return "showAllQuests";
        }
        switch (status) {
            case NOTGIVENTO:
                return "showMissingQuests";
            case GIVENTO:
                return "showQuests";
            case SUCCESS:
                return "showSuccessQuests";
            case FAIL:
                return "showFailedQuests";
            case FROZEN:
                return "showFrozenQuests";
        }
        return null;
    }
    
    public static String getFullCommand(Status status) {
        return "quest " + getCommandPath(status);
    }
    
    public static String getAttribute(Status status) {
        if (status == null) {
            return "";
        }
        switch (status) {
            case NOTGIVENTO:
                return "fehlende";
            case GIVENTO:
                return "aktive";
            case SUCCESS:
                return "abgeschlossene";
            case FAIL:
                return "fehlgeschlagene";
            case FROZEN:
                return "eingefrorene";
        }
        return null;
    }
    
    private Status status;
    
    public ShowPlayerQuestsCommand(Status status) {
        this.status = status;
    }
    
    @SuppressWarnings("null")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString, ArgsParser args) {
        
        OfflinePlayer player;
        
        if (args.remaining() > 0 && !args.seeNext("").startsWith(".")) {
            if (!sender.hasPermission(CubeQuest.SEE_PLAYER_INFO_PERMISSION)) {
                ChatAndTextUtil.sendNoPermissionMessage(sender);
                return true;
            }
            
            String playerName = args.next();
            player = CubeQuest.getInstance().getPlayerUUIDCache().getPlayer(playerName);
            
            if (player == null) {
                ChatAndTextUtil.sendWarningMessage(sender, "Spieler " + playerName + " nicht gefunden.");
                return true;
            }
        } else {
            player = (Player) sender;
        }
        
        List<Quest> showableQuests = new ArrayList<>();
        PlayerData playerData = CubeQuest.getInstance().getPlayerData(player);
        Stream<Quest> questStream;
        if (this.status == Status.GIVENTO) {
            questStream = playerData.getActiveQuests().stream().map(q -> q.getQuest());
        } else {
            questStream = QuestManager.getInstance().getQuests().stream();
            if (this.status == Status.NOTGIVENTO) {
                questStream = questStream.filter(q -> q.isReady());
            }
        }
        questStream = questStream.filter(q -> q.isVisible() && (this.status == null || this.status == playerData.getPlayerStatus(q.getId())));
        questStream.forEach(q -> showableQuests.add(q));
        showableQuests.sort(Quest.QUEST_DISPLAY_COMPARATOR);
        
        InteractiveBookAPI bookAPI = JavaPlugin.getPlugin(InteractiveBookAPIPlugin.class);
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        List<BookMeta> books = new ArrayList<>();
        List<String> firstQuestsInBooks = new ArrayList<>();
        BookMeta meta = null;
        boolean oneBookEnough = true;
        
        if (showableQuests.isEmpty()) {
            meta = (BookMeta) book.getItemMeta();
            ComponentBuilder builder = new ComponentBuilder("");
            builder.append("Du hast aktuell keine " + getAttribute(this.status) + "n" + (this.status == null ? "" : " ") + "Quests.").bold(true)
                    .color(ChatColor.GOLD);
            bookAPI.addPage(meta, builder.create());
        } else {
            for (Quest q : showableQuests) {
                List<BaseComponent[]> displayMessageList = ChatAndTextUtil.getQuestDescription(q);
                
                HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Hier klicken"));
                if (this.status != null && this.status != Status.NOTGIVENTO) {
                    ClickEvent stateClickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/quest stateInfo " + (player == sender ? "" : (player.getName() + " ")) + q.getId());
                    ClickEvent giveMessageClickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/quest showGiveMessage " + (player == sender ? "" : (player.getName() + " ")) + q.getId());
                    
                    displayMessageList.add(new ComponentBuilder("").append("Fortschritt anzeigen").color(ChatColor.DARK_GREEN).bold(true)
                            .event(stateClickEvent).event(hoverEvent).create());
                    displayMessageList.add(null);
                    displayMessageList.add(new ComponentBuilder("").append("Vergabenachricht anzeigen").color(ChatColor.DARK_GREEN).bold(true)
                            .event(giveMessageClickEvent).event(hoverEvent).create());
                    if (sender.hasPermission(CubeQuest.EDIT_QUESTS_PERMISSION)) {
                        displayMessageList.add(null);
                    }
                }
                if (sender.hasPermission(CubeQuest.EDIT_QUESTS_PERMISSION)) {
                    ClickEvent infoClickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + QuestInfoCommand.FULL_COMMAND + " " + q.getId());
                    displayMessageList.add(new ComponentBuilder("").append("Info anzeigen").color(ChatColor.DARK_GREEN).bold(true)
                            .event(infoClickEvent).event(hoverEvent).create());
                }
                
                if (meta == null || !ChatAndTextUtil.writeIntoBook(meta, displayMessageList,
                        MAX_NUM_PAGES_QUEST_LIST - (this.status != null && this.status != Status.GIVENTO && books.size() == 1 ? 1 : 0))) {
                    if (this.status != null && this.status != Status.GIVENTO && books.size() == 1
                            && ChatAndTextUtil.writeIntoBook(meta, displayMessageList, MAX_NUM_PAGES_QUEST_LIST)) {
                        oneBookEnough = false;
                        continue;
                    }
                    meta = (BookMeta) book.getItemMeta();
                    ChatAndTextUtil.writeIntoBook(meta, displayMessageList);
                    books.add(meta);
                    firstQuestsInBooks.add(q.getDisplayName());
                    oneBookEnough &= books.size() == 1;
                }
            }
            
            if (!oneBookEnough) {
                meta = (BookMeta) book.getItemMeta();
            }
            
            if (this.status != null && this.status != Status.GIVENTO) {
                ComponentBuilder builder = new ComponentBuilder("Du hast insgesamt " + showableQuests.size() + " " + getAttribute(this.status) + " "
                        + (showableQuests.size() == 1 ? "Quest" : "Quests") + ".");
                bookAPI.insertPage(meta, 0, builder.color(ChatColor.DARK_GREEN).create());
            }
            
            if (!oneBookEnough) {
                int bookIndex = -1;
                if (args.hasNext()) {
                    String indexString = args.next();
                    if (!indexString.startsWith(".")) {
                        ChatAndTextUtil.sendWarningMessage(sender, "Der Buchindex muss aus technischen Gründen mit einem Punkt beginnen."
                                + " Du kannst auch einfach im Inhaltsverzeichnis auf den entsprechenden Eintrag klicken.");
                        return true;
                    }
                    indexString = indexString.substring(1);
                    try {
                        bookIndex = Integer.parseInt(indexString) - 1;
                        if (bookIndex < -1) {
                            throw new NumberFormatException();
                        }
                    } catch (NumberFormatException e) {
                        ChatAndTextUtil.sendWarningMessage(sender,
                                "Bitte gib den Buchindex nach dem Punkt als positive ganze Zahl an (oder 0 für das Inhaltsverzeichnis).");
                        return true;
                    }
                }
                
                if (bookIndex == -1) {
                    List<BaseComponent[]> toc = new ArrayList<>();
                    toc.add(new ComponentBuilder("Buchliste:").bold(true).create());
                    for (int i = 0; i < books.size(); i++) {
                        ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                "/" + getFullCommand(this.status) + (player != sender ? " " + player.getName() : "") + " ." + (i + 1));
                        HoverEvent hoverEvent =
                                new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Quests ab hier auflisten (Buch " + (i + 1) + ")"));
                        toc.add(null);
                        toc.add(new ComponentBuilder("Quests ab \"").reset().event(clickEvent).event(hoverEvent).append(firstQuestsInBooks.get(i))
                                .append(ChatColor.RESET + "\"").retain(FormatRetention.EVENTS).create());
                    }
                    ChatAndTextUtil.writeIntoBook(meta, toc);
                } else if (bookIndex >= books.size()) {
                    ChatAndTextUtil.sendWarningMessage(sender, "So viele Bücher hat deine Quest-Liste nicht.");
                    return true;
                } else {
                    meta = books.get(bookIndex);
                }
            } else if (args.hasNext()) {
                ChatAndTextUtil.sendWarningMessage(sender, "Deine Quest-Liste hat nur eine Seite.");
            }
        }
        
        meta.setTitle("Quests");
        meta.setAuthor(CubeQuest.PLUGIN_TAG);
        book.setItemMeta(meta);
        bookAPI.showBookToPlayer((Player) sender, book);
        
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        if (this.status == null || this.status == Status.NOTGIVENTO) {
            return CubeQuest.SEE_PLAYER_INFO_PERMISSION;
        }
        return CubeQuest.ACCEPT_QUESTS_PERMISSION;
    }
    
    @Override
    public boolean requiresPlayer() {
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        if (!sender.hasPermission(CubeQuest.SEE_PLAYER_INFO_PERMISSION)) {
            return Collections.emptyList();
        }
        
        return ChatAndTextUtil.polishTabCompleteList(
                Bukkit.getOnlinePlayers().stream().map(p -> p.getName()).collect(Collectors.toCollection(() -> new ArrayList<>())), args.getNext(""));
    }
    
    @Override
    public String getUsage() {
        return "(zeigt deine " + getAttribute(this.status) + "n" + (this.status == null ? "" : " ") + "Quests an)";
    }
    
}
