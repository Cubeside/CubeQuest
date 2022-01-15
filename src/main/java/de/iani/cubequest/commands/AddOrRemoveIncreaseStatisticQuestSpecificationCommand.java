package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.generation.IncreaseStatisticQuestSpecification.IncreaseStatisticQuestPossibilitiesSpecification;
import de.iani.cubequest.generation.IncreaseStatisticQuestSpecification.IncreaseStatisticQuestPossibility;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesidestats.api.StatisticKey;
import de.iani.cubesideutils.Pair;
import de.iani.cubesideutils.StringUtil;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;


public class AddOrRemoveIncreaseStatisticQuestSpecificationCommand extends SubCommand {
    
    public static final String ADD_COMMAND_PATH = "addIncreaseStatisticQuestSpecification";
    public static final String REMOVE_COMMAND_PATH = "removeIncreaseStatisticQuestSpecification";
    
    private boolean add;
    
    public AddOrRemoveIncreaseStatisticQuestSpecificationCommand(boolean add) {
        this.add = add;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {
        
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib den Statistik-Key an.");
            return true;
        }
        
        String statistic = args.getNext();
        StatisticKey key = CubeQuest.getInstance().getCubesideStatistics().getStatisticKey(statistic, false);
        if (key == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Statistik " + statistic + " nicht gefunden.");
            return true;
        }
        
        Boolean maxOnce = args.getNext(false);
        if (maxOnce == null) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Bitte gib an (true|false), ob höchstens eine Stufe der Statistik verlangt werden soll.");
            return true;
        }
        
        String rest = args.getAll(null);
        if (rest == null) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Bitte gib Beschreibungen für Fliestext (Buch) und Fortschrittsanzeige an, getrennt von durch \" | \".");
            return true;
        }
        
        Pair<String, String> descriptions = StringUtil.splitAtPipe(rest);
        if (descriptions == null || descriptions.second().isEmpty()) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Bitte gib Beschreibungen für Fliestext (Buch) und Fortschrittsanzeige an, getrennt von durch \" | \".");
            return true;
        }
        
        IncreaseStatisticQuestPossibility statisticPossibility =
                new IncreaseStatisticQuestPossibility(key, maxOnce, descriptions.first(), descriptions.second());
        boolean result = this.add
                ? IncreaseStatisticQuestPossibilitiesSpecification.getInstance().addStatistic(statisticPossibility)
                : IncreaseStatisticQuestPossibilitiesSpecification.getInstance().removeStatistic(statisticPossibility);
        
        if (result) {
            ChatAndTextUtil.sendNormalMessage(sender,
                    "Statistikmöglichkeit erfolgreich " + (this.add ? "hinzugefügt" : "entfernt") + ".");
        } else {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Statistikmöglichkeit war " + (this.add ? "bereits" : "nicht") + " enthalten.");
        }
        
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUEST_SPECIFICATIONS_PERMISSION;
    }
    
    @Override
    public Collection<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        args.getNext(null);
        if (!args.hasNext()) {
            return CubeQuest.getInstance().getCubesideStatistics().getAllStatisticKeys().stream()
                    .map(StatisticKey::getName).toList();
        }
        
        args.getNext(null);
        if (!args.hasNext()) {
            return Arrays.asList("true", "false");
        }
        
        return Collections.emptyList();
    }
    
    @Override
    public String getUsage() {
        return "<Statistic> <Max Once> <Text Description> | <Progress Description>";
    }
    
}
