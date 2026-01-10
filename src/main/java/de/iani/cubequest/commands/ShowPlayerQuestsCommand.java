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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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
        questStream.forEach(showableQuests::add);
        showableQuests.sort(Quest.QUEST_DISPLAY_COMPARATOR);

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        List<BookMeta> books = new ArrayList<>();
        List<Component> firstQuestsInBooks = new ArrayList<>();
        BookMeta meta = null;
        boolean oneBookEnough = true;

        boolean isActiveAndHasHidden =
                this.type == ListType.ACTIVE && playerData.getActiveQuests().stream().anyMatch(qs -> qs.isHidden());

        if (showableQuests.isEmpty()) {
            meta = (BookMeta) book.getItemMeta();

            Component page =
                    Component.text(
                            "Du hast aktuell keine "
                                    + (this.type.attribute.isEmpty() ? "" : (this.type.attribute + "n ")) + "Quests.",
                            NamedTextColor.GOLD).decorate(TextDecoration.BOLD);

            if (isActiveAndHasHidden) {
                Component showHidden = Component.text("Du hast ausgeblendete Quests. Klicke hier, um sie aufzulisten.")
                        .color(NamedTextColor.DARK_GREEN)
                        .hoverEvent(HoverEvent.showText(Component.text("Ausgeblendete Quests auflisten")))
                        .clickEvent(ClickEvent.runCommand(
                                "/" + ListType.HIDDEN.fullCommand + (player == sender ? "" : player.getName())));

                page = page.append(Component.text("\n\n")).append(showHidden);
            }

            meta.addPages(page);
        } else {
            for (Quest q : showableQuests) {
                List<Component> displayMessageList = ChatAndTextUtil.getQuestDescription(q);

                if (this.type.hasState) {
                    ClickEvent stateClickEvent = ClickEvent.runCommand(
                            "/quest stateInfo " + (player == sender ? "" : (player.getName() + " ")) + q.getId());
                    HoverEvent<Component> stateHoverEvent = HoverEvent.showText(Component.text("Fortschritt anzeigen"));

                    ClickEvent giveMessageClickEvent = ClickEvent.runCommand(
                            "/quest showGiveMessage " + (player == sender ? "" : (player.getName() + " ")) + q.getId());
                    HoverEvent<Component> giveMessageHoverEvent =
                            HoverEvent.showText(Component.text("Vergabenachricht anzeigen"));

                    displayMessageList.add(Component.text("Fortschritt anzeigen\n", NamedTextColor.DARK_GREEN)
                            .decorate(TextDecoration.BOLD).clickEvent(stateClickEvent).hoverEvent(stateHoverEvent));

                    displayMessageList.add(Component.text("Vergabenachricht anzeigen\n", NamedTextColor.DARK_GREEN)
                            .decorate(TextDecoration.BOLD).clickEvent(giveMessageClickEvent)
                            .hoverEvent(giveMessageHoverEvent));
                }

                if (this.type == ListType.ACTIVE && player == sender) {
                    ClickEvent hideClickEvent =
                            ClickEvent.runCommand("/" + HideOrRestoreQuestCommand.HIDE_FULL_COMMAND + " " + q.getId());
                    HoverEvent<Component> hideHoverEvent = HoverEvent.showText(Component.text("Quest ausblenden"));

                    displayMessageList.add(Component.text("Quest ausblenden\n", NamedTextColor.DARK_GREEN)
                            .decorate(TextDecoration.BOLD).clickEvent(hideClickEvent).hoverEvent(hideHoverEvent));
                } else if (this.type == ListType.HIDDEN && player == sender) {
                    ClickEvent restoreClickEvent = ClickEvent
                            .runCommand("/" + HideOrRestoreQuestCommand.RESTORE_FULL_COMMAND + " " + q.getId());
                    HoverEvent<Component> restoreHoverEvent = HoverEvent.showText(Component.text("Quest einblenden"));

                    displayMessageList.add(Component.text("Quest einblenden\n", NamedTextColor.DARK_GREEN)
                            .decorate(TextDecoration.BOLD).clickEvent(restoreClickEvent).hoverEvent(restoreHoverEvent));
                }

                if (sender.hasPermission(CubeQuest.EDIT_QUESTS_PERMISSION)) {
                    ClickEvent infoClickEvent =
                            ClickEvent.runCommand("/" + QuestInfoCommand.FULL_COMMAND + " " + q.getId());
                    HoverEvent<Component> infoHoverEvent = HoverEvent.showText(Component.text("Info anzeigen"));

                    displayMessageList.add(Component.text("Info anzeigen\n", NamedTextColor.DARK_GREEN)
                            .decorate(TextDecoration.BOLD).clickEvent(infoClickEvent).hoverEvent(infoHoverEvent));
                }

                if (meta == null || !ChatAndTextUtil.writeIntoBook(meta, displayMessageList, (Player) sender,
                        MAX_NUM_PAGES_QUEST_LIST)) {
                    meta = (BookMeta) book.getItemMeta();
                    ChatAndTextUtil.writeIntoBook(meta, displayMessageList, (Player) sender);
                    books.add(meta);
                    firstQuestsInBooks.add(q.getDisplayName());
                    oneBookEnough &= books.size() == 1;
                }
            }

            if (!oneBookEnough) {
                meta = (BookMeta) book.getItemMeta();
            }

            List<Component> toc = new ArrayList<>();
            if (isActiveAndHasHidden) {
                Component line = Component.text("Du hast ausgeblendete Quests. Klicke hier, um sie aufzulisten.")
                        .color(NamedTextColor.DARK_GREEN)
                        .hoverEvent(HoverEvent.showText(Component.text("Ausgeblendete Quests auflisten")))
                        .clickEvent(ClickEvent.runCommand(
                                "/" + ListType.HIDDEN.fullCommand + (player == sender ? "" : player.getName())));

                if (oneBookEnough && meta.getPageCount() < MAX_NUM_PAGES_QUEST_LIST) {
                    List<Component> pages = new ArrayList<>(meta.pages());
                    pages.add(line);
                    meta.pages(pages);
                } else {
                    oneBookEnough = false;
                    toc.add(line);
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
                    toc.add(Component.text("Buchliste:").decorate(TextDecoration.BOLD));

                    for (int i = 0; i < books.size(); i++) {
                        ClickEvent clickEvent = ClickEvent.runCommand("/" + this.type.fullCommand
                                + (player != sender ? " " + player.getName() : "") + " ." + (i + 1));
                        HoverEvent<Component> hoverEvent =
                                HoverEvent.showText(Component.text("Quests ab hier auflisten (Buch " + (i + 1) + ")"));

                        toc.add(null);

                        Component entry = Component.text("Quests ab \"").append(firstQuestsInBooks.get(i))
                                .append(Component.text("\"")).clickEvent(clickEvent).hoverEvent(hoverEvent);

                        toc.add(entry);
                    }

                    ChatAndTextUtil.writeIntoBook(meta, toc, (Player) sender);
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
        meta.author(CubeQuest.PLUGIN_TAG);
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
