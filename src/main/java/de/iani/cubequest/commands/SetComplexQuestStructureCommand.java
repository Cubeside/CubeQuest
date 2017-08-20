package de.iani.cubequest.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.ComplexQuest;
import de.iani.cubequest.quests.ComplexQuest.Structure;
import de.iani.cubequest.quests.Quest;

public class SetComplexQuestStructureCommand extends SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            CubeQuest.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }

        if (!(quest instanceof ComplexQuest)) {
            CubeQuest.sendWarningMessage(sender, "Diese Quest hat keine Struktur.");
            return true;
        }

        if (!args.hasNext()) {
            CubeQuest.sendWarningMessage(sender, "Bitte gibt eine Quest-Struktur an.");
            return true;
        }

        String structureString = args.getNext();
        Structure structure = Structure.match(structureString);
        if (structure == null) {
            CubeQuest.sendWarningMessage(sender, "Quest-Struktur " + structureString + " nicht gefunden.");
            return true;
        }

        ((ComplexQuest) quest).setStructure(structure);
        CubeQuest.sendNormalMessage(sender, "Quest-Struktur auf " + structure + " gesetzt.");
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
        for (Structure s: Structure.values()) {
            if (s.toString().toLowerCase(Locale.ENGLISH).startsWith(arg)) {
                result.add(s.toString());
            }
        }
        return result;
    }

}
