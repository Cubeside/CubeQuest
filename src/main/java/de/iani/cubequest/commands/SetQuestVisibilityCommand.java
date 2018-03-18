package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SetQuestVisibilityCommand extends SubCommand {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args) {
        
        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }
        
        boolean visible = quest.isVisible();
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Bitte gib an, ob die Quest für Spieler sichtbar sein soll (true | false). (Derzeit: "
                            + visible + ")");
            return true;
        }
        
        String arg = args.getNext();
        if (Arrays.asList(new String[] {"t", "true", "y", "yes", "j", "ja"})
                .contains(arg.toLowerCase())) {
            if (visible) {
                ChatAndTextUtil.sendNormalMessage(sender, "Die Quest ist bereits sichtbar.");
            } else {
                quest.setVisible(true);
                ChatAndTextUtil.sendNormalMessage(sender, "Die Quest ist nun sichtbar.");
            }
        } else if (Arrays.asList(new String[] {"f", "false", "n", "no", "nein"})
                .contains(arg.toLowerCase())) {
            CubeQuest.getInstance().setGenerateDailyQuests(true);
            if (visible) {
                quest.setVisible(false);
                ChatAndTextUtil.sendNormalMessage(sender, "Die Quest ist nun unsichtbar.");
            } else {
                ChatAndTextUtil.sendNormalMessage(sender, "Die Quest war bereits unsichtbar.");
            }
        } else {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Bitte gib an, ob die Quest auf sichtbar gesetzt werden soll (true | false).");
        }
        
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias,
            ArgsParser args) {
        String arg = args.getNext("");
        List<String> result = new ArrayList<>(
                Arrays.asList(new String[] {"true", "false", "yes", "no", "ja", "nein"}));
        result.removeIf(s -> {
            return !s.startsWith(arg.toLowerCase());
        });
        return result;
    }
    
}
