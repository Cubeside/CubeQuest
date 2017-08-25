package de.iani.cubequest;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.command.CommandSender;

import de.iani.cubequest.quests.NPCQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;

public class QuestEditor {

    private Map<CommandSender, Quest> editors;
    private Set<CommandSender> selectingNPC;

    public QuestEditor() {
        this.editors = new HashMap<CommandSender, Quest>();
        this.selectingNPC = new HashSet<CommandSender>();
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
            selectingNPC.remove(sender);
            ChatAndTextUtil.sendNormalMessage(sender, "Quest-Bearbeitung geschlossen.");
            return true;
        }
        return false;
    }

    public Quest getEditingQuest(CommandSender sender) {
        return editors.get(sender);
    }

    public boolean setSelectingNPC(CommandSender sender) {
        Quest q = editors.get(sender);
        if (q == null) {
            throw new IllegalArgumentException("CommandSender isn't editing Quest");
        }
        if (!(q instanceof NPCQuest)) {
            throw new IllegalArgumentException("CommandSender isn't editing NPCQuest");
        }
        return selectingNPC.add(sender);
    }

    public boolean isSelectingNPC(CommandSender sender) {
        return selectingNPC.contains(sender);
    }

    public boolean removeFromSelectingNPC(CommandSender sender) {
        if (selectingNPC.remove(sender)) {
            ChatAndTextUtil.sendNormalMessage(sender, "NPC-Auswahl beendet.");
            return true;
        }
        return false;
    }

}
