package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestGiver;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.quests.ComplexQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;


public class ListReferencesCommand extends SubCommand {

    public static final String COMMAND_PATH = "references";
    public static final String FULL_COMMAND = "quest " + COMMAND_PATH;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {
        Quest quest =
                ChatAndTextUtil.getQuest(sender, args, FULL_COMMAND + " ", "", "Vorkommen von Quest ", " anzeigen");
        if (quest == null) {
            return true;
        }


        List<Component> results = new ArrayList<>();
        for (QuestGiver giver : CubeQuest.getInstance().getQuestGivers()) {
            if (giver.hasQuest(quest)) {
                Component line = Component.text("Quest-Giver ", NamedTextColor.BLUE)
                        .append(giver.getName().colorIfAbsent(NamedTextColor.GREEN)).color(NamedTextColor.BLUE);
                results.add(line);
            }
        }

        for (ComplexQuest other : QuestManager.getInstance().getQuests(ComplexQuest.class)) {
            if (other.getSubQuests().contains(quest)) {
                Component line = Component.text("Complex-Quest ", NamedTextColor.LIGHT_PURPLE)
                        .append(Component.text(String.valueOf(other.getId()), NamedTextColor.GREEN))
                        .hoverEvent(HoverEvent.showText(Component.text("Info anzeigen")))
                        .clickEvent(ClickEvent.runCommand("/" + QuestInfoCommand.FULL_COMMAND + " " + other.getId()));

                if (!other.getInternalName().isEmpty()) {
                    line = line.append(Component.text(" (" + other.getInternalName() + ")"));
                }

                results.add(line.color(NamedTextColor.LIGHT_PURPLE));
            }
        }

        Component header = Component.text("Vorkommen von Quest " + quest.getId()
                + (quest.getInternalName().isEmpty() ? "" : (" (" + quest.getInternalName() + ")")) + ":");
        ChatAndTextUtil.sendNormalMessage(sender, header);

        if (results.isEmpty()) {
            sender.sendMessage(Component.text("-- KEINE --", NamedTextColor.GOLD));
        }

        for (Component c : results) {
            sender.sendMessage(c);
        }

        return true;
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }

}
