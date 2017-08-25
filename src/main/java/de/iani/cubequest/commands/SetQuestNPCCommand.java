package de.iani.cubequest.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.NPCQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatUtil;
import net.citizensnpcs.api.npc.NPC;

public class SetQuestNPCCommand extends SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            ChatUtil.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }

        if (!(quest instanceof NPCQuest)) {
            ChatUtil.sendWarningMessage(sender, "Diese Quest erfordert keinen NPC.");
            return true;
        }

        if (!args.hasNext()) {
            if (!(sender instanceof Player)) {
                ChatUtil.sendWarningMessage(sender, "Bitte gib eine NPC-ID an.");
                return true;
            }
            if (CubeQuest.getInstance().getQuestEditor().setSelectingNPC(sender)) {
                ChatUtil.sendNormalMessage(sender, "Bitte wähle durch Rechtsklick einen NPC aus.");
            } else {
                ChatUtil.sendWarningMessage(sender, "Du wählst bereits einen NPC aus.");
            }
            return true;
        }

        int id = args.getNext(-1);
        if (id < 0) {
            ChatUtil.sendWarningMessage(sender, "Die NPC-ID muss eine nicht-negative Ganzzahl sein.");
            return true;
        }

        NPC npc = CubeQuest.getInstance().getNPCReg().getById(id);
        if (npc == null) {
            ChatUtil.sendWarningMessage(sender, "NPC mit der ID " + id + " nicht gefunden.");
            return true;
        }

        ((NPCQuest) quest).setNPC(npc);
        ChatUtil.sendNormalMessage(sender, "NPC gesetzt.");
        CubeQuest.getInstance().getQuestEditor().removeFromSelectingNPC(sender);

        return true;
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }

}
