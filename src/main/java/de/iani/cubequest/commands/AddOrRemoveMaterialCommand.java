package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.MaterialsAndAmountQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class AddOrRemoveMaterialCommand extends SubCommand {
    
    public static final String ADD_COMMAND_PATH = "addMaterial";
    public static final String FULL_ADD_COMMAND = "quest " + ADD_COMMAND_PATH;
    public static final String REMOVE_COMMAND_PATH = "removeMaterial";
    public static final String FULL_REMOVE_COMMAND = "quest " + REMOVE_COMMAND_PATH;
    
    private boolean add;
    
    public AddOrRemoveMaterialCommand(boolean add) {
        this.add = add;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString, ArgsParser args) {
        
        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }
        
        if (!(quest instanceof MaterialsAndAmountQuest)) {
            ChatAndTextUtil.sendWarningMessage(sender, "Diese Quest erfordert keine Materialien.");
            return true;
        }
        Material material = null;
        if (!args.hasNext()) {
            if (sender instanceof Player) {
                ItemStack stack = ((Player) sender).getInventory().getItemInMainHand();
                if (stack != null) {
                    material = stack.getType() == Material.AIR ? null : stack.getType();
                }
            }
            if (material == null) {
                ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib an, welches Material " + (this.add ? "zu" : "von") + " der Quest "
                        + (this.add ? "hinzugefügt" : "entfernt") + " werden soll (oder, als Spieler: Nimm es in die Hand).");
                return true;
            }
        } else {
            String materialName = args.getNext();
            material = Material.matchMaterial(materialName);
            if (material == null) {
                ChatAndTextUtil.sendWarningMessage(sender, "Material " + materialName + " nicht gefunden.");
                return true;
            }
        }
        
        boolean changed = this.add ? ((MaterialsAndAmountQuest) quest).addType(material) : ((MaterialsAndAmountQuest) quest).removeType(material);
        if (changed) {
            ChatAndTextUtil.sendNormalMessage(sender, "Material " + material + (this.add ? " zu " : " von ") + quest.getTypeName() + " ["
                    + quest.getId() + "] " + (this.add ? "hinzugefügt" : "entfernt") + ".");
        } else {
            ChatAndTextUtil.sendWarningMessage(sender, "Das Material " + material + " war in " + quest.getTypeName() + " [" + quest.getId() + "] "
                    + (this.add ? "bereits" : "nicht") + " vorhanden.");
        }
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        List<String> result = new ArrayList<>();
        
        for (Material type : Material.values()) {
            result.add(type.name());
        }
        
        return ChatAndTextUtil.polishTabCompleteList(result, args.getNext(""));
    }
    
    @Override
    public String getUsage() {
        return "<Material>";
    }
    
}
