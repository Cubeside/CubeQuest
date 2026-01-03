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
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AchievementInfoCommand extends SubCommand {

    public static final String COMMAND_PATH = "achievements";
    public static final String FULL_COMMAND = "quest " + COMMAND_PATH;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

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

        ChatAndTextUtil.sendNormalMessage(sender, sender == player ? ("Deine erreichten Achievements:")
                : ("Die erreicheten Achievements von " + player.getName() + ":"));
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
            Component msg = quest.getDisplayName().colorIfAbsent(NamedTextColor.GOLD);
            if (quest.getFollowupQuest() != null) {
                AmountQuest inner =
                        (AmountQuest) ((ComplexQuest) quest.getFollowupQuest()).getSubQuests().iterator().next();

                Component possibilities = null;
                if (inner instanceof MaterialsAndAmountQuest) {
                    possibilities =
                            ChatAndTextUtil.multipleMaterialsComponent(((MaterialsAndAmountQuest) inner).getTypes());
                } else if (inner instanceof EntityTypesAndAmountQuest) {
                    possibilities = ChatAndTextUtil
                            .multipleEntityTypesComponent(((EntityTypesAndAmountQuest) inner).getTypes());
                }

                Component suffix = Component.text(" (für nächste Stufe: ")
                        .append(Component.text(
                                String.valueOf(((AmountQuestState) data.getPlayerState(inner.getId())).getAmount()),
                                NamedTextColor.AQUA))
                        .append(Component.text(" / ")).append(Component.text(String.valueOf(inner.getAmount())))
                        .append(Component.text(")")).color(NamedTextColor.BLUE);

                if (possibilities != null) {
                    suffix = suffix.hoverEvent(HoverEvent.showText(possibilities));
                }

                msg = msg.append(suffix);
            } else {
                msg = msg.append(Component.text(" (höchste Stufe)", NamedTextColor.BLUE));
            }
            sender.sendMessage(msg);
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
