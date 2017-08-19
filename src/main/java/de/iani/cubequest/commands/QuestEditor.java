package de.iani.cubequest.commands;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.Quest;

public class QuestEditor {

    private Map<CommandSender, Quest> editors;

    public QuestEditor() {
        this.editors = new HashMap<CommandSender, Quest>();
    }

    public void startEdit(CommandSender sender, Quest quest) {
        if (editors.containsKey(sender)) {
            CubeQuest.sendWarningMessage(sender, "Du bearbeitest bereits eine Quest.");
            return;
        }
        if (editors.containsValue(quest)) {
            CubeQuest.sendWarningMessage(sender, "Vorsicht, diese Quest wird bereits von folgenden Spielern bearbeitet:");
            for (CommandSender other: editors.keySet()) {
                if (editors.get(other) == quest) {
                    CubeQuest.sendWarningMessage(sender, other.getName());
                }
            }
        }
        editors.put(sender, quest);
        CubeQuest.sendNormalMessage(sender, "Bearbeitung von " + quest.getTypeName() + " \"" + quest.getName() + "\" [" + quest.getId() + "] gestartet.");
    }

    public boolean stopEdit(CommandSender sender) {
        if (editors.remove(sender) != null) {
            CubeQuest.sendNormalMessage(sender, "Quest-Bearbeitung geschlossen.");
            return true;
        }
        return false;
    }

    public Quest getEditingQuest(CommandSender sender) {
        return editors.get(sender);
    }

}
