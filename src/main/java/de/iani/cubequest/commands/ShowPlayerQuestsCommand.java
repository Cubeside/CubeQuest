package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.ComponentBuilder.FormatRetention;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class ShowPlayerQuestsCommand extends SubCommand {

    public enum ListType {

        ALL(CubeQuest.SEE_PLAYER_INFO_PERMISSION, "showAllQuests", "", false, (pd, id) -> true),
        NOTGIVENTO(CubeQuest.SEE_PLAYER_INFO_PERMISSION, "showMissingQuests", "fehlende", false,
                (pd, id) -> pd.getPlayerStatus(id) == Status.NOTGIVENTO,
                pd -> QuestManager.getInstance().getQuests().stream().filter(Quest::isReady)),
        ACTIVE(CubeQuest.ACCEPT_QUESTS_PERMISSION, "showQuests", "aktive", true, (pd, id) -> {
            QuestState state = pd.getPlayerState(id);
            return state.getStatus() == Status.GIVENTO && !state.isHidden();
        }, pd -> pd.getActiveQuests().stream().map(QuestState::getQuest)),
        HIDDEN(CubeQuest.ACCEPT_QUESTS_PERMISSION, "showHiddenQuests", "ausgeblendete", true, (pd, id) -> {
            QuestState state = pd.getPlayerState(id);
            return state.getStatus() == Status.GIVENTO && state.isHidden();
        }, pd -> pd.getActiveQuests().stream().map(QuestState::getQuest)),
        SUCCESS(CubeQuest.ACCEPT_QUESTS_PERMISSION, "showSuccessQuests", "abgeschlossene", true,
                (pd, id) -> pd.getPlayerStatus(id) == Status.SUCCESS),
        FAIL(CubeQuest.ACCEPT_QUESTS_PERMISSION, "showFailedQuests", "fehlgeschlagene", true,
                (pd, id) -> pd.getPlayerStatus(id) == Status.FAIL),
        FROZEN(CubeQuest.ACCEPT_QUESTS_PERMISSION, "showFrozenQuests", "eingefrorene", true,
                (pd, id) -> pd.getPlayerStatus(id) == Status.FROZEN);

        public final String permission;
        public final String commandPath;
        public final String fullCommand;
        public final String attribute;
        public final boolean hasState;
        public final BiPredicate<PlayerData, Integer> displayPredicate;
        Function<PlayerData, Stream<Quest>> questSupplier;

        private ListType(String permission, String commandPath, String attribute, boolean hasState,
                BiPredicate<PlayerData, Integer> displayPredicate, Function<PlayerData, Stream<Quest>> questSupplier) {
            this.permission = permission;
            this.commandPath = commandPath;
            this.fullCommand = "quest " + commandPath;
            this.attribute = attribute;
            this.hasState = hasState;
            this.displayPredicate = displayPredicate;
            this.questSupplier = questSupplier;
        }

        private ListType(String permission, String commandPath, String attribute, boolean hasState,
                BiPredicate<PlayerData, Integer> displayPredicate) {
            this(permission, commandPath, attribute, hasState, displayPredicate,
                    pd -> QuestManager.getInstance().getQuests().stream());
        }
    }

    private static final int MAX_NUM_PAGES_QUEST_LIST = 30;

    private ListType type;

    public ShowPlayerQuestsCommand(ListType type) {
        this.type = type;
    }

    @SuppressWarnings("null")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

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
        Stream<Quest> questStream = this.type.questSupplier.apply(playerData);
        questStream = questStream.filter(q -> q.isVisible() && this.type.displayPredicate.test(playerData, q.getId()));
        questStream.forEach(q -> showableQuests.add(q));
        showableQuests.sort(Quest.QUEST_DISPLAY_COMPARATOR);

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        List<BookMeta> books = new ArrayList<>();
        List<String> firstQuestsInBooks = new ArrayList<>();
        BookMeta meta = null;
        boolean oneBookEnough = true;

        boolean isActiveAndHasHidden =
                this.type == ListType.ACTIVE && playerData.getActiveQuests().stream().anyMatch(qs -> qs.isHidden());

        if (showableQuests.isEmpty()) {
            meta = (BookMeta) book.getItemMeta();
            ComponentBuilder builder = new ComponentBuilder("");
            builder.append("Du hast aktuell keine "
                    + (this.type.attribute.isEmpty() ? "" : (this.type.attribute + "n ")) + "Quests.").bold(true)
                    .color(ChatColor.GOLD);
            if (isActiveAndHasHidden) {
                builder.append("\n\n");
                HoverEvent showHiddenHoverEvent =
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Ausgeblendete Quests auflisten"));
                ClickEvent showHiddenClickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/" + ListType.HIDDEN.fullCommand + (player == sender ? "" : player.getName()));
                builder.append("Du hast ausgeblendete Quests. Klicke hier, um sie aufzulisten.").color(ChatColor.GREEN)
                        .event(showHiddenClickEvent).event(showHiddenHoverEvent);
            }
            meta.spigot().addPage(builder.create());
        } else {
            for (Quest q : showableQuests) {
                List<BaseComponent[]> displayMessageList = ChatAndTextUtil.getQuestDescription(q);

                HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Hier klicken"));
                if (this.type.hasState) {
                    ClickEvent stateClickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/quest stateInfo " + (player == sender ? "" : (player.getName() + " ")) + q.getId());
                    ClickEvent giveMessageClickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/quest showGiveMessage " + (player == sender ? "" : (player.getName() + " ")) + q.getId());

                    displayMessageList.add(new ComponentBuilder("").append("Fortschritt anzeigen")
                            .color(ChatColor.DARK_GREEN).bold(true).event(stateClickEvent).event(hoverEvent).create());
                    displayMessageList.add(
                            new ComponentBuilder("").append("Vergabenachricht anzeigen").color(ChatColor.DARK_GREEN)
                                    .bold(true).event(giveMessageClickEvent).event(hoverEvent).create());
                }
                if (this.type == ListType.ACTIVE && player == sender) {
                    ClickEvent hideClickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/" + HideOrRestoreQuestCommand.HIDE_FULL_COMMAND + " " + q.getId());
                    displayMessageList.add(new ComponentBuilder("").append("Quest ausblenden")
                            .color(ChatColor.DARK_GREEN).bold(true).event(hideClickEvent).event(hoverEvent).create());
                } else if (this.type == ListType.HIDDEN && player == sender) {
                    ClickEvent restoreClickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/" + HideOrRestoreQuestCommand.RESTORE_FULL_COMMAND + " " + q.getId());
                    displayMessageList
                            .add(new ComponentBuilder("").append("Quest einblenden").color(ChatColor.DARK_GREEN)
                                    .bold(true).event(restoreClickEvent).event(hoverEvent).create());
                }
                if (sender.hasPermission(CubeQuest.EDIT_QUESTS_PERMISSION)) {
                    ClickEvent infoClickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                            "/" + QuestInfoCommand.FULL_COMMAND + " " + q.getId());
                    displayMessageList.add(new ComponentBuilder("").append("Info anzeigen").color(ChatColor.DARK_GREEN)
                            .bold(true).event(infoClickEvent).event(hoverEvent).create());
                }

                if (meta == null
                        || !ChatAndTextUtil.writeIntoBook(meta, displayMessageList, MAX_NUM_PAGES_QUEST_LIST)) {
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

            List<BaseComponent[]> toc = new ArrayList<>();
            if (isActiveAndHasHidden) {
                ComponentBuilder builder = new ComponentBuilder("");
                HoverEvent showHiddenHoverEvent =
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Ausgeblendete Quests auflisten"));
                ClickEvent showHiddenClickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/" + ListType.HIDDEN.fullCommand + (player == sender ? "" : player.getName()));
                builder.append("Du hast ausgeblendete Quests. Klicke hier, um sie aufzulisten.").color(ChatColor.GREEN)
                        .event(showHiddenClickEvent).event(showHiddenHoverEvent);
                if (oneBookEnough && meta.getPageCount() < MAX_NUM_PAGES_QUEST_LIST) {
                    List<BaseComponent[]> pages = new ArrayList<>(meta.spigot().getPages());
                    pages.add(0, builder.create());
                    meta.spigot().setPages(pages);
                } else {
                    oneBookEnough = false;
                    toc.add(builder.create());
                    toc.add(null);
                }
            }

            if (!oneBookEnough) {
                int bookIndex = -1;
                if (args.hasNext()) {
                    String indexString = args.next();
                    if (!indexString.startsWith(".")) {
                        ChatAndTextUtil.sendWarningMessage(sender,
                                "Der Buchindex muss aus technischen Gründen mit einem Punkt beginnen."
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
                    toc.add(new ComponentBuilder("Buchliste:").bold(true).create());
                    for (int i = 0; i < books.size(); i++) {
                        ClickEvent clickEvent =
                                new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + this.type.fullCommand
                                        + (player != sender ? " " + player.getName() : "") + " ." + (i + 1));
                        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                new Text("Quests ab hier auflisten (Buch " + (i + 1) + ")"));
                        toc.add(null);
                        toc.add(new ComponentBuilder("Quests ab \"").reset().event(clickEvent).event(hoverEvent)
                                .append(TextComponent.fromLegacy(firstQuestsInBooks.get(i)))
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
        ((Player) sender).openBook(book);

        return true;
    }

    @Override
    public String getRequiredPermission() {
        return this.type.permission;
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

        return ChatAndTextUtil.polishTabCompleteList(Bukkit.getOnlinePlayers().stream().map(p -> p.getName())
                .collect(Collectors.toCollection(() -> new ArrayList<>())), args.getNext(""));
    }

    @Override
    public String getUsage() {
        return "(zeigt deine " + (this.type.attribute.isEmpty() ? "" : (this.type.attribute + "n ")) + "Quests an)";
    }

}
