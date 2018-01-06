package de.iani.cubequest.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;

public class EditQuestCommand extends SubCommand {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args) {
        
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib eine Quest an.");
            return true;
        }
        
        Quest quest = ChatAndTextUtil.getQuest(sender, args, "/cubequest edit ", "", "Quest ",
                " editieren");
        if (quest == null) {
            return true;
        }
        
        // String questString = args.getAll("");
        // try {
        // int id = Integer.parseInt(questString);
        // quest = QuestManager.getInstance().getQuest(id);
        // if (quest == null) {
        // ChatAndTextUtil.sendWarningMessage(sender, "Es gibt keine Quest mit der ID " + id + ".");
        // return true;
        // }
        // } catch (NumberFormatException e) {
        // Set<Quest> quests = QuestManager.getInstance().getQuests(questString);
        // if (quests.isEmpty()) {
        // ChatAndTextUtil.sendWarningMessage(sender, "Es gibt keine Quest mit dem Namen " +
        // questString + ".");
        // return true;
        // } else if (quests.size() > 1) {
        // ChatAndTextUtil.sendWarningMessage(sender, "Es gibt mehrere Quests mit diesem Namen,
        // bitte wähle eine aus:");
        // for (Quest q: quests) {
        // if (sender instanceof Player) {
        // HoverEvent he = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Quest "
        // + q.getId() + " editieren").create());
        // ClickEvent ce = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/cubequest edit " +
        // q.getId());
        // String msg = CubeQuest.PLUGIN_TAG + " " + ChatColor.GOLD + q.getTypeName() + " " +
        // q.getId();
        // ComponentBuilder cb = new ComponentBuilder("").append(msg).event(ce).event(he);
        // ((Player) sender).spigot().sendMessage(cb.create());
        // } else {
        // ChatAndTextUtil.sendWarningMessage(sender, QuestType.getQuestType(q.getClass()) + " " +
        // q.getId());
        // }
        // }
        // return true;
        // }
        // quest = Iterables.getFirst(quests, null);
        // }
        
        if (quest.isReady()) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Diese Quest ist bereits auf \"fertig\" gesetzt. Sie zu bearbeiten kann unbekannte Nebenwirkungen haben, es wird davon abgeraten.");
        }
        
        CubeQuest.getInstance().getQuestEditor().startEdit(sender, quest);
        
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
    
}
