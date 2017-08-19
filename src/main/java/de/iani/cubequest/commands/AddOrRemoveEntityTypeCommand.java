package de.iani.cubequest.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.EntityTypesAndAmountQuest;
import de.iani.cubequest.quests.Quest;

public class AddOrRemoveEntityTypeCommand extends SubCommand {

    private boolean add;

    public AddOrRemoveEntityTypeCommand(boolean add) {
        this.add = add;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            CubeQuest.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }

        if (!(quest instanceof EntityTypesAndAmountQuest)) {
            CubeQuest.sendWarningMessage(sender, "Diese Quest erfordert keine EntityTypes.");
            return true;
        }
        EntityType type = null;
        if (!args.hasNext()) {
            CubeQuest.sendWarningMessage(sender, "Bitte gib an, welcher EntityType " + (add? "zu" : "von") + " der Quest " + (add? "hinzugefügt" : "entfernt") + " werden soll.");
            return true;
        } else {
            String typeName = args.getNext();
            type = CubeQuest.matchEnum(typeName);
            if (type == null) {
                CubeQuest.sendWarningMessage(sender, "EntityType " + typeName + " nicht gefunden.");
                return true;
            }
        }

        boolean changed = add? ((EntityTypesAndAmountQuest) quest).addType(type) : ((EntityTypesAndAmountQuest) quest).removeType(type);
        if (changed) {
            CubeQuest.sendNormalMessage(sender, "EntityType " + type + (add? "zu" : "von") + quest.getTypeName() + " [" + quest.getId() + "] " + (add? "hinzugefügt" : "entfernt") + ".");
        } else {
            CubeQuest.sendWarningMessage(sender, "Der EntityType " + type + " war in " + quest.getTypeName() + " [" + quest.getId() + "] " + (add? "bereits" : "nicht") + " vorhanden.");
        }
        return true;
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        String arg = args.getNext("").toLowerCase(Locale.ENGLISH);
        List<String> result = new ArrayList<String>();
        for (EntityType e: EntityType.values()) {
            if (e.toString().toLowerCase(Locale.ENGLISH).startsWith(arg)) {
                result.add(e.toString());
            }
        }
        return result;
    }

}
