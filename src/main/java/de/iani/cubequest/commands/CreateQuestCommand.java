package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.quests.QuestType;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class CreateQuestCommand extends SubCommand {
    
    public static final String COMMAND_PATH = "create";
    public static final String FULL_COMMAND = "quest " + COMMAND_PATH;
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString, ArgsParser args) {
        
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib einen Quest-Typ an.");
            return true;
        }
        
        String typeString = args.getNext();
        QuestType type;
        try {
            type = QuestType.valueOf(typeString);
        } catch (IllegalArgumentException e) {
            ChatAndTextUtil.sendWarningMessage(sender, "Unbekannter Quest-Typ " + typeString + ".");
            return true;
        }
        
        Class<? extends Quest> questClass = type.questClass;
        Quest quest = CubeQuest.getInstance().getQuestCreator().createQuest(questClass);
        
        int id = quest.getId();
        ChatAndTextUtil.sendNormalMessage(sender, type + " mit Quest-ID " + id + " erfolgreich erstellt.");
        CubeQuest.getInstance().getQuestEditor().startEdit(sender, quest);
        
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        List<String> result = new ArrayList<>();
        
        for (QuestType type : QuestType.values()) {
            result.add(type.name());
        }
        
        return ChatAndTextUtil.polishTabCompleteList(result, args.getNext(""));
    }
    
    @Override
    public String getUsage() {
        return "<QuestType>";
    }
    
}
