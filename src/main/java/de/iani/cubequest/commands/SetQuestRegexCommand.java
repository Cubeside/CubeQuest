package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.CommandQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.regex.PatternSyntaxException;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SetQuestRegexCommand extends SubCommand {
    
    private boolean quote;
    
    public SetQuestRegexCommand(boolean quote) {
        this.quote = quote;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args) {
        
        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }
        
        if (!(quest instanceof CommandQuest)) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Diese Quest erfordert keinen Regulären Ausdruck.");
            return true;
        }
        
        String regex = "";
        while (args.hasNext()) {
            regex += args.getNext() + " ";
        }
        regex = regex.equals("") ? null : regex.substring(0, regex.length() - " ".length());
        
        if (quote) {
            ((CommandQuest) quest).setLiteralMatch(regex);
        } else {
            try {
                ((CommandQuest) quest).setRegex(regex);
            } catch (PatternSyntaxException e) {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Kein gültiger regulärer Ausdruck! (" + e.getDescription() + ")");
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Hier werden reguläre Ausdrücke spezifiziert: https://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html");
                return true;
            }
        }
        ChatAndTextUtil.sendNormalMessage(sender, (quote ? "Gültiger Befehl" : "Regulärer Ausdruck")
                + " für " + quest.getTypeName() + " [" + quest.getId() + "] gesetzt.");
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
    
}
