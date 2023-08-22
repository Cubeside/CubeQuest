package de.iani.cubequest;

import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.bukkit.command.CommandSender;

public class QuestEditor {

    private Map<CommandSender, Quest> editors;

    public QuestEditor() {
        this.editors = new HashMap<>();
        CubeQuest.getInstance().getEventListener().addOnPlayerQuit(player -> stopEdit(player));
    }

    public void startEdit(CommandSender sender, Quest quest) {
        stopEdit(sender);
        if (this.editors.containsValue(quest)) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Vorsicht, diese Quest wird bereits von folgenden Spielern bearbeitet:");
            for (CommandSender other : this.editors.keySet()) {
                if (this.editors.get(other) == quest) {
                    ChatAndTextUtil.sendWarningMessage(sender, other.getName());
                }
            }
        }
        this.editors.put(sender, quest);
        ChatAndTextUtil.sendNormalMessage(sender, "Bearbeitung von " + quest.getTypeName() + " \""
                + quest.getInternalName() + "\" [" + quest.getId() + "] gestartet.");
    }

    public boolean stopEdit(CommandSender sender) {
        if (this.editors.remove(sender) != null) {
            ChatAndTextUtil.sendNormalMessage(sender, "Quest-Bearbeitung geschlossen.");
            return true;
        }
        return false;
    }

    public Quest getEditingQuest(CommandSender sender) {
        return this.editors.get(sender);
    }

    public void terminateNonPermittedEdits(Quest quest) {
        if (!quest.isReady()) {
            return;
        }

        Iterator<Entry<CommandSender, Quest>> it = this.editors.entrySet().iterator();
        while (it.hasNext()) {
            Entry<CommandSender, Quest> editor = it.next();
            if (editor.getValue() != quest) {
                continue;
            }

            if (!editor.getKey().hasPermission(CubeQuest.CONFIRM_QUESTS_PERMISSION)) {
                it.remove();
                ChatAndTextUtil.sendNormalMessage(editor.getKey(),
                        "Quest-Bearbeitung geschlossen, da die Quest auf \"fertig\" gesetzt wurde.");
            }
        }
    }

    public void terminateAllEdits(Quest quest) {
        Iterator<Entry<CommandSender, Quest>> it = this.editors.entrySet().iterator();
        while (it.hasNext()) {
            Entry<CommandSender, Quest> editor = it.next();
            if (editor.getValue() != quest) {
                continue;
            }

            it.remove();
            ChatAndTextUtil.sendNormalMessage(editor.getKey(),
                    "Quest-Bearbeitung geschlossen, da die Quest gelöscht wurde.");
        }
    }

}
