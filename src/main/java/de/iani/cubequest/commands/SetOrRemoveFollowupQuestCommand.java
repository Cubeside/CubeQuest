package de.iani.cubequest.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.ComplexQuest;
import de.iani.cubequest.quests.ComplexQuest.CircleInQuestGraphException;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;

public class SetOrRemoveFollowupQuestCommand extends SubCommand {
    
    private boolean set;
    
    public SetOrRemoveFollowupQuestCommand(boolean set) {
        this.set = set;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args) {
        
        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }
        
        if (!(quest instanceof ComplexQuest)) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Diese Quest unterstützt keine Nachfolgequest.");
            return true;
        }
        
        if (!set) {
            ((ComplexQuest) quest).setFollowupQuest(null);
            ChatAndTextUtil.sendNormalMessage(sender, "Nachfolgequest entfernt.");
            return true;
        }
        
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die neue Nachfolgequest an.");
            return true;
        }
        
        // String otherQuestString = args.getNext();
        Quest otherQuest = ChatAndTextUtil.getQuest(sender, args, "/cubequest setFollowupQuest ",
                "", "Quest ", " als Nachfolger festlegen");
        if (otherQuest == null) {
            return true;
        }
        // try {
        // int id = Integer.parseInt(otherQuestString);
        // otherQuest = QuestManager.getInstance().getQuest(id);
        // if (otherQuest == null) {
        // ChatAndTextUtil.sendWarningMessage(sender, "Es gibt keine Quest mit der ID " + id + ".");
        // return true;
        // }
        // } catch (NumberFormatException e) {
        // Set<Quest> quests = QuestManager.getInstance().getQuests(otherQuestString);
        // if (quests.isEmpty()) {
        // ChatAndTextUtil.sendWarningMessage(sender, "Es gibt keine Quest mit dem Namen " +
        // otherQuestString + ".");
        // return true;
        // } else if (quests.size() > 1) {
        // ChatAndTextUtil.sendWarningMessage(sender, "Es gibt mehrere Quests mit diesem Namen,
        // bitte wähle eine aus:");
        // for (Quest q: quests) {
        // if (sender instanceof Player) {
        // HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Quest "
        // + q.getId() + " als Nachfolger festlegen.").create());
        // ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cubequest
        // setFollowupQuest " + q.getId());
        // String msg = CubeQuest.PLUGIN_TAG + ChatColor.GOLD + q.getTypeName() + " " + q.getId();
        // ComponentBuilder cb = new ComponentBuilder("").append(msg).event(ce).event(he);
        // ((Player) sender).spigot().sendMessage(cb.create());
        // } else {
        // ChatAndTextUtil.sendWarningMessage(sender, QuestType.getQuestType(q.getClass()) + " " +
        // q.getId());
        // }
        // }
        // return true;
        // }
        // otherQuest = Iterables.getFirst(quests, null);
        // }
        
        try {
            ((ComplexQuest) quest).setFollowupQuest(otherQuest);
        } catch (CircleInQuestGraphException e) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Diese Unterquest hinzuzufügen würde einen Zirkelschluss im Quest-Graph erzeugen (sprich: die hinzuzufügende Quest ist die selbe Quest oder das gilt für eine ihre Unterquests).");
            return true;
        }
        ChatAndTextUtil.sendNormalMessage(sender, "Nachfolgequest gesetzt.");
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
    
}
