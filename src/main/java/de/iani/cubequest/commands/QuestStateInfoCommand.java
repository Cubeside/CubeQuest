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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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

            if (args.remaining() >= 2) {
                String name = args.next();
                if (name.equalsIgnoreCase(player.getName()) || !name.equals(player.getUniqueId().toString())) {
                    args.next();
                }
            }
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

        List<Component> msg = quest.getStateInfo(data, this.unmasked);
        Component refreshComponent = Component.text("Aktualisieren").decorate(TextDecoration.UNDERLINED)
                .clickEvent(ClickEvent.runCommand(
                        commandString + " " + (player != sender ? (player.getUniqueId() + " ") : "") + quest.getId()))
                .hoverEvent(HoverEvent.showText(Component.text("Fortschrittsanzeige aktualisieren")))
                .color(NamedTextColor.GREEN);
        msg.add(refreshComponent);

        sender.sendMessage(Component.empty());
        ChatAndTextUtil.sendMessage(sender, msg);

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
