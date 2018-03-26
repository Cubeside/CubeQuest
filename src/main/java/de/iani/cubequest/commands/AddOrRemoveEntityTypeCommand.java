package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.EntityTypesAndAmountQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.Util;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;

public class AddOrRemoveEntityTypeCommand extends SubCommand {
    
    private boolean add;
    
    public AddOrRemoveEntityTypeCommand(boolean add) {
        this.add = add;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args) {
        
        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }
        
        if (!(quest instanceof EntityTypesAndAmountQuest)) {
            ChatAndTextUtil.sendWarningMessage(sender, "Diese Quest erfordert keine EntityTypes.");
            return true;
        }
        EntityType type = null;
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Bitte gib an, welcher EntityType " + (this.add ? "zu" : "von") + " der Quest "
                            + (this.add ? "hinzugefügt" : "entfernt") + " werden soll.");
            return true;
        } else {
            String typeName = args.getNext();
            type = Util.matchEntityType(typeName);
            if (type == null) {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "EntityType " + typeName + " nicht gefunden.");
                return true;
            }
        }
        
        boolean changed = this.add ? ((EntityTypesAndAmountQuest) quest).addType(type)
                : ((EntityTypesAndAmountQuest) quest).removeType(type);
        if (changed) {
            ChatAndTextUtil.sendNormalMessage(sender,
                    "EntityType " + type + (this.add ? " zu " : " von ") + quest.getTypeName()
                            + " [" + quest.getId() + "] " + (this.add ? "hinzugefügt" : "entfernt")
                            + ".");
        } else {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Der EntityType " + type + " war in " + quest.getTypeName() + " ["
                            + quest.getId() + "] " + (this.add ? "bereits" : "nicht")
                            + " vorhanden.");
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
        List<String> result = new ArrayList<>();
        
        for (EntityType type: EntityType.values()) {
            result.add(type.name());
        }
        
        return ChatAndTextUtil.polishTabCompleteList(result, args.getNext(""));
    }
    
    @Override
    public String getUsage() {
        return "<EntityType>";
    }
}
