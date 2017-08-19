package de.iani.cubequest.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestType;
import de.iani.cubequest.quests.AmountQuest;
import de.iani.cubequest.quests.MaterialsAndAmountQuest;
import de.iani.cubequest.quests.Quest;

public class AddOrRemoveMaterialCommand extends SubCommand {

    private boolean add;

    public AddOrRemoveMaterialCommand(boolean add) {
        this.add = add;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            CubeQuest.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }

        if (!(quest instanceof AmountQuest)) {
            CubeQuest.sendWarningMessage(sender, "Diese Quest erfordert keine Materialien.");
            return true;
        }
        Material material = null;
        if (!args.hasNext()) {
            if (sender instanceof Player) {
                ItemStack stack = ((Player) sender).getInventory().getItemInMainHand();
                if (stack != null) {
                    material = stack.getType() == Material.AIR? null : stack.getType();
                }
            }
            if (material == null) {
                CubeQuest.sendWarningMessage(sender, "Bitte gib an, welches Material " + (add? "zu" : "von") + " der Quest " + (add? "hinzugefügt" : "entfernt") + " werden soll (oder, als Spieler: Nimm es in die Hand).");
                return true;
            }
        } else {
            String materialName = args.getNext();
            material = Material.matchMaterial(materialName);
            if (material == null) {
                CubeQuest.sendWarningMessage(sender, "Material " + materialName + " nicht gefunden.");
                return true;
            }
        }

        boolean changed = add? ((MaterialsAndAmountQuest) quest).addType(material) : ((MaterialsAndAmountQuest) quest).removeType(material);
        if (changed) {
            CubeQuest.sendNormalMessage(sender, "Material " + material + (add? "zu" : "von") + QuestType.getQuestType(quest.getClass()) + " [" + quest.getId() + "] " + (add? "hinzugefügt" : "entfernt") + ".");
        } else {
            CubeQuest.sendWarningMessage(sender, "Das Material " + material + " war in " + QuestType.getQuestType(quest.getClass()) + (add? " bereits" : " nicht") + " vorhanden.");
        }
        return true;
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }

}
