package de.iani.cubequest;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;

import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;

public class QuestEditor {

    private Map<CommandSender, Quest> editors;

    public QuestEditor() {
        this.editors = new HashMap<>();
    }

    public void startEdit(CommandSender sender, Quest quest) {
        if (editors.containsKey(sender)) {
            ChatAndTextUtil.sendWarningMessage(sender, "Du bearbeitest bereits eine Quest.");
            return;
        }
        if (editors.containsValue(quest)) {
            ChatAndTextUtil.sendWarningMessage(sender, "Vorsicht, diese Quest wird bereits von folgenden Spielern bearbeitet:");
            for (CommandSender other: editors.keySet()) {
                if (editors.get(other) == quest) {
                    ChatAndTextUtil.sendWarningMessage(sender, other.getName());
                }
            }
        }
        editors.put(sender, quest);
        ChatAndTextUtil.sendNormalMessage(sender, "Bearbeitung von " + quest.getTypeName() + " \"" + quest.getName() + "\" [" + quest.getId() + "] gestartet.");
    }

    public boolean stopEdit(CommandSender sender) {
        if (editors.remove(sender) != null) {
            ChatAndTextUtil.sendNormalMessage(sender, "Quest-Bearbeitung geschlossen.");
            return true;
        }
        return false;
    }

    public Quest getEditingQuest(CommandSender sender) {
        return editors.get(sender);
    }

}
