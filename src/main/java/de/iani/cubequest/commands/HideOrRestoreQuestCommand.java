package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class HideOrRestoreQuestCommand extends SubCommand {

    public static final String HIDE_COMMAND_PATH = "hideQuest";
    public static final String HIDE_FULL_COMMAND = "quest " + HIDE_COMMAND_PATH;

    public static final String RESTORE_COMMAND_PATH = "restoreQuest";
    public static final String RESTORE_FULL_COMMAND = "quest " + RESTORE_COMMAND_PATH;

    boolean hide;

    public HideOrRestoreQuestCommand(boolean hide) {
        this.hide = hide;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {
        PlayerData data = CubeQuest.getInstance().getPlayerData((Player) sender);
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Quest an, die du",
                    this.hide ? " ausblenden" : " wiederherstellen", " möchtest.");
            return true;
        }

        Quest quest = ChatAndTextUtil.getQuest(sender, args,
                q -> q.isVisible() && data.getPlayerStatus(q.getId()) == Status.GIVENTO, true,
                this.hide ? HIDE_FULL_COMMAND : RESTORE_FULL_COMMAND, "", "Quest ",
                this.hide ? " ausblenden" : " wiederherstellen");
        if (quest == null) {
            return true;
        }

        QuestState state = data.getPlayerState(quest.getId());
        if (state == null || state.getStatus() != Status.GIVENTO) {
            ChatAndTextUtil.sendWarningMessage(sender, "Diese Quest ist bei dir derzeit nicht aktiv.");
            return true;
        }
        if (state.isHidden() == this.hide) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Diese Quest ist für dich bereits " + (this.hide ? "aus" : "ein") + "geblendet.");
            return true;
        }

        state.setHidden(this.hide);
        ChatAndTextUtil.sendNormalMessage(sender, "Quest " + (this.hide ? "aus" : "ein") + "geblendet.");
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

}
