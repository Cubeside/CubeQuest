package de.iani.cubequest.commands;

import java.util.HashMap;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.quests.Quest;

public class QuestEditor {

    private CubeQuest plugin;
    private HashMap<CommandSender, Quest> editors;

    public QuestEditor(CubeQuest plugin) {
        this.plugin = plugin;
        this.editors = new HashMap<CommandSender, Quest>();
    }

    public void startEdit(CommandSender sender, String[] args) {
        if (editors.containsKey(sender)) {
            CubeQuest.sendWarningMessage(sender, "Du bearbeitest bereits eine Quest.");
            return;
        }
        if (args.length < 2) {
            CubeQuest.sendWarningMessage(sender, "Bitte gib eine Quest-Nummer an.");
            return;
        }
        int id;
        try {
            id = Integer.parseInt(args[1]);
            if (id < 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            CubeQuest.sendNormalMessage(sender, "\"" + args[1] + "\" ist keine gültige Quest-Nummer.");
            return;
        }
        Quest quest = QuestManager.getInstance().getQuest(id);
        if (quest == null) {
            CubeQuest.sendWarningMessage(sender, "Es gibt keine Quest mit dieser Id.");
            return;
        }
        if (editors.containsValue(quest)) {
            CubeQuest.sendWarningMessage(sender, "Vorsicht, diese Quest wird bereits bearbeitet.");
        }
        editors.put(sender, quest);
        CubeQuest.sendNormalMessage(sender, "Bearbeitung von Quest " + id + " - \"" + quest.getName() + "\" gestartet.");
    }

    public void playerQuit(Player player) {
        editors.remove(player);
    }

    public void setName(CommandSender sender, String[] args) {
        Quest quest = editors.get(sender);
        if (quest == null) {
            CubeQuest.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest.");
            return;
        }
        if (args.length < 2) {
            CubeQuest.sendWarningMessage(sender, "Bitte gib einen neuen Namen ein.");
        }
        String name = "";
        for (int i=1; i<args.length; i++) {
            name += (i == 1? "" : " ") + args[i];
        }
        quest.setName(name);
        if (quest.getName().equals(name)) {
            CubeQuest.sendNormalMessage(sender, "Name in \"" + name + "\" geändert.");
        } else {
            CubeQuest.sendWarningMessage(sender, "Umbenennung aus unbekannten Gründen gescheitert.");
        }
    }

}
