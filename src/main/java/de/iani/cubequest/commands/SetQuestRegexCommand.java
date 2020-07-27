package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.CommandQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.PatternSyntaxException;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetQuestRegexCommand extends SubCommand {
    
    public static final String REGEX_COMMAND_PATH = "setRegex";
    public static final String FULL_REGEX_COMMAND = "quest " + REGEX_COMMAND_PATH;
    public static final String QUOTE_COMMAND_PATH = "setLiteralMatch";
    public static final String FULL_QUOTE_COMMAND = "quest " + QUOTE_COMMAND_PATH;
    
    private boolean quote;
    
    public SetQuestRegexCommand(boolean quote) {
        this.quote = quote;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString, ArgsParser args) {
        
        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }
        
        if (!(quest instanceof CommandQuest)) {
            ChatAndTextUtil.sendWarningMessage(sender, "Diese Quest erfordert keinen Regulären Ausdruck.");
            return true;
        }
        
        String regex = args.getAll(null);
        
        if (this.quote) {
            ((CommandQuest) quest).setLiteralMatch(regex);
        } else {
            try {
                ((CommandQuest) quest).setRegex(regex);
            } catch (PatternSyntaxException e) {
                ChatAndTextUtil.sendWarningMessage(sender, "Kein gültiger regulärer Ausdruck! (" + e.getDescription() + ")");
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Hier werden reguläre Ausdrücke spezifiziert: https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html");
                return true;
            }
        }
        ChatAndTextUtil.sendNormalMessage(sender,
                (this.quote ? "Gültiger Befehl" : "Regulärer Ausdruck") + " für " + quest.getTypeName() + " [" + quest.getId() + "] gesetzt.");
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        if (args.remaining() > 1) {
            return Collections.emptyList();
        }
        
        List<String> result = new ArrayList<>();
        
        for (Player player : Bukkit.getOnlinePlayers()) {
            result.add(player.getName());
        }
        
        return ChatAndTextUtil.polishTabCompleteList(result, args.getNext(""));
    }
    
    @Override
    public String getUsage() {
        if (this.quote) {
            return "<Befehl>";
        } else {
            return "<Regulärer Ausdruck>";
        }
    }
    
}
