package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.exceptions.QuestDeletionFailedException;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;


public class DeleteQuestCommand extends SubCommand {

    public static final String COMMAND_PATH = "delete";
    public static final String FULL_COMMAND = "quest " + COMMAND_PATH;

    public static final String CASCADING_OPTION = "cascading";

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

        boolean cascading = false;
        if (args.seeNext("").equalsIgnoreCase(CASCADING_OPTION)) {
            cascading = true;
            args.next();
        }

        if (!args.hasNext()) {
            Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
            if (quest != null) {
                Bukkit.dispatchCommand(sender,
                        FULL_COMMAND + " " + (cascading ? CASCADING_OPTION + " " : "") + quest.getId());
                return true;
            }
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib eine Quest an.");
            return true;
        }

        String questString = args.seeAll("");
        Quest quest = ChatAndTextUtil.getQuest(sender, args,
                "/" + FULL_COMMAND + " " + (cascading ? CASCADING_OPTION + " " : ""), "", "Quest ", " löschen");

        if (quest == null) {
            return true;
        }

        if (!questString.equals(quest.getId() + " DELETE")) {
            String infoCommandString = QuestInfoCommand.COMMAND_PATH + " " + quest.getId();
            CubeQuest.getInstance().getCommandExecutor().onCommand(sender, CubeQuest.getInstance().getCommand("quest"),
                    "quest", infoCommandString.split(" "));
            String finalDeletionCommand =
                    "/" + FULL_COMMAND + " " + (cascading ? CASCADING_OPTION + " " : "") + quest.getId() + " DELETE";
            TextComponent msgComponent = new TextComponent("Soll die Quest " + quest.getId()
                    + " wirklich unwiderruflich gelöscht werden? Dann nutze den Befehl " + finalDeletionCommand + ".");
            msgComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Befehl einfügen")));
            msgComponent.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, finalDeletionCommand));
            ChatAndTextUtil.sendWarningMessage(sender, msgComponent);
            return true;
        }

        String[] confirmations;
        try {
            confirmations = QuestManager.getInstance().deleteQuest(quest, cascading);
        } catch (QuestDeletionFailedException e) {
            ChatAndTextUtil.sendWarningMessage(sender, "Quest konnte nicht gelöscht werden. Fehlermeldung:");
            ChatAndTextUtil.sendWarningMessage(sender, e.getLocalizedMessage());

            Throwable reason = e;
            while (reason instanceof QuestDeletionFailedException) {
                if (reason == reason.getCause()) {
                    break;
                }
                reason = reason.getCause();
            }

            if (reason != null) {
                CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                        "An unexpected exception occured while trying to delete quest with id " + quest.getId(), e);
            }

            return true;
        }

        for (String confirmation : confirmations) {
            ChatAndTextUtil.sendNormalMessage(sender, confirmation);
        }
        return true;
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        args.getNext(null);
        if (!args.hasNext()) {
            return List.of(CASCADING_OPTION);
        }
        return Collections.emptyList();
    }

    @Override
    public String getUsage() {
        return "[CASCADING] <Quest> [DELETE]";
    }

}
