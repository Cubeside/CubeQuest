package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.generation.GotoQuestSpecification;
import de.iani.cubequest.generation.QuestGenerator;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.StringUtil;
import de.iani.cubesideutils.commands.ArgsParser;
import de.iani.cubesideutils.commands.SubCommand;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AddGotoQuestSpecificationCommand extends SubCommand {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString, ArgsParser args) {
        
        if (args.remaining() < 2) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Schwierigkeit und die Toleranz an des Ortes an.");
            return true;
        }
        
        double difficulty = args.getNext(Double.MIN_VALUE);
        if (difficulty <= 0.0 || difficulty > 1.0) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Schwierigkeit als Kommazahl echt größer 0 und kleiner gleich 1 an.");
            return true;
        }
        
        double tolerance = args.getNext(Double.MIN_VALUE);
        if (tolerance <= 0.0) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Toleranz als Kommazahl echt größer 0 an.");
            return true;
        }
        
        String[] messages = args.getAll("").split("\\|");
        if (messages.length != 2) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Ortsbeschreibung und Vergabenachricht an, getrennt von einem |.");
            return true;
        }
        
        Player player = (Player) sender;
        GotoQuestSpecification specification = new GotoQuestSpecification();
        specification.setLocation(player.getLocation());
        specification.setTolerance(tolerance);
        specification.setDifficulty(difficulty);
        specification.setLocationName(StringUtil.convertColors(messages[0]));
        specification.setGiveMessage(StringUtil.convertColors(messages[1]));
        
        QuestGenerator.getInstance().addPossibleQuest(specification);
        ChatAndTextUtil.sendNormalMessage(sender, "Neue Goto-Quest-Spezifikation erfolgreich erstellt.");
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUEST_SPECIFICATIONS_PERMISSION;
    }
    
    @Override
    public boolean requiresPlayer() {
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        return Collections.emptyList();
    }
    
    @Override
    public String getUsage() {
        return "<Schwierigkeit> <Toleranz> <Ortsbeschreibung>|<Vergabenachricht>";
    }
    
}
