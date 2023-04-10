package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.bukkit.StringUtilBukkit;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class QuestStateInfoCommand extends SubCommand {

    public static final String NORMAL_COMMAND_PATH = "stateInfo";
    public static final String NORMAL_FULL_COMMAND = "quest " + NORMAL_COMMAND_PATH;

    public static final String UNMASKED_COMMAND_PATH = "stateInfoUnmasked";
    public static final String UNMASKED_FULL_COMMAND = "quest " + UNMASKED_COMMAND_PATH;

    private boolean unmasked;

    public QuestStateInfoCommand(boolean unmasked) {
        this.unmasked = unmasked;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Bitte gib die ID oder den Namen der Quest an, zu der du deinen Fortschritt einsehen möchtest.");
            return true;
        }

        OfflinePlayer player;

        if (sender.hasPermission(CubeQuest.SEE_PLAYER_INFO_PERMISSION) && args.remaining() >= 2) {
            String playerString = args.getNext("");
            player = StringUtilBukkit.parseOfflinePlayer(playerString);
            if (player == null) {
                ChatAndTextUtil.sendWarningMessage(sender, "Spieler " + playerString + " nicht gefunden.");
                return true;
            }
        } else if (!(sender instanceof Player)) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib einen Spieler an.");
            return true;
        } else {
            player = (Player) sender;
        }

        PlayerData data = CubeQuest.getInstance().getPlayerData(player);

        Quest quest = ChatAndTextUtil.getQuest(sender, args, q -> {
            return (q.isVisible() && data.getPlayerStatus(q.getId()) != Status.NOTGIVENTO)
                    || sender.hasPermission(CubeQuest.SEE_PLAYER_INFO_PERMISSION);
        }, true, (this.unmasked ? UNMASKED_FULL_COMMAND : NORMAL_FULL_COMMAND) + " "
                + (player == sender ? "" : (player.getName() + " ")), "", "Quest ", " auswählen");

        if (quest == null) {
            return true;
        }

        ChatAndTextUtil.sendBaseComponent(sender, quest.getStateInfo(data, this.unmasked));

        TextComponent refreshComponent = new TextComponent("Aktualisieren");
        refreshComponent.setColor(ChatColor.GREEN);
        refreshComponent.setUnderlined(true);
        refreshComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                commandString + " " + (player != sender ? (player.getUniqueId() + " ") : "") + quest.getId()));
        refreshComponent.setHoverEvent(
                new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Fortschrittsanzeige aktualisieren")));
        ChatAndTextUtil.sendBaseComponent(sender, refreshComponent);

        return true;
    }

    @Override
    public String getRequiredPermission() {
        return this.unmasked ? CubeQuest.SEE_PLAYER_INFO_PERMISSION : CubeQuest.ACCEPT_QUESTS_PERMISSION;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        if (!(sender instanceof Player) || sender.hasPermission(CubeQuest.SEE_PLAYER_INFO_PERMISSION)) {
            return ChatAndTextUtil.polishTabCompleteList(Bukkit.getOnlinePlayers().stream().map(p -> p.getName())
                    .collect(Collectors.toCollection(() -> new ArrayList<>())), args.getNext(""));
        }

        List<String> result = new ArrayList<>();

        for (QuestState state : CubeQuest.getInstance().getPlayerData((Player) sender).getActiveQuests()) {
            if (state.getQuest().isVisible()) {
                result.add(Integer.toString(state.getQuest().getId()));
            }
        }

        return ChatAndTextUtil.polishTabCompleteList(result, args.getNext(""));
    }

    @Override
    public String getUsage() {
        return "<Quest (Id oder Name)>";
    }

}
