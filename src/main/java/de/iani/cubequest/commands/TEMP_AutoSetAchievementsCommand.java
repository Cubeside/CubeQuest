package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.quests.ComplexQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.Util;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class TEMP_AutoSetAchievementsCommand extends SubCommand {
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args) {
        
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib einen Prefix an.");
        }
        
        String prefix = ChatColor.stripColor(args.seeAll(null));
        int num = 0;
        for (ComplexQuest quest : QuestManager.getInstance().getQuests(ComplexQuest.class)) {
            if (ChatColor.stripColor(quest.getInternalName()).equals(prefix)) {
                if (!Util.isLegalAchievementQuest(quest)) {
                    ChatAndTextUtil.sendWarningMessage(sender,
                            "Could not activate quest " + quest.getId());
                    continue;
                }
                quest.getSubQuests().iterator().next().setDisplayName("");
                quest.setAchievementQuest(true);
                num++;
            }
        }
        
        ChatAndTextUtil.sendNormalMessage(sender, "Added " + num + " Achievements.");
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
    
}
