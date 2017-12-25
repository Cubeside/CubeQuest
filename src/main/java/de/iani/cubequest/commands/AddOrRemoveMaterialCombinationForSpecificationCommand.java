package de.iani.cubequest.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.google.common.base.Verify;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.generation.BlockBreakQuestSpecification.BlockBreakQuestPossibilitiesSpecification;
import de.iani.cubequest.generation.BlockPlaceQuestSpecification.BlockPlaceQuestPossibilitiesSpecification;
import de.iani.cubequest.generation.DeliveryQuestSpecification.DeliveryQuestPossibilitiesSpecification;
import de.iani.cubequest.generation.MaterialCombination;
import de.iani.cubequest.util.ChatAndTextUtil;

public class AddOrRemoveMaterialCombinationForSpecificationCommand extends SubCommand {

    private boolean add;
    private MaterialCombinationRequiredFor requiredFor;

    public enum MaterialCombinationRequiredFor {
        BLOCK_BREAK("BlockBreakMaterialCombination"),
        BLOCK_PLACE("BlockPlaceMaterialCombination"),
        DELIVERY("DeliveryMaterialCombination");

        public final String command;

        private MaterialCombinationRequiredFor(String command) {
            this.command = command;
        }
    }

    public AddOrRemoveMaterialCombinationForSpecificationCommand(boolean add, MaterialCombinationRequiredFor requiredFor) {
        Verify.verifyNotNull(requiredFor);

        this.add = add;
        this.requiredFor = requiredFor;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

        MaterialCombination mc;

        if (!args.hasNext()) {
            if (!(sender instanceof Player)) {
                ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Materialien an, die " + (add? "hinzugefügt" : "entfernt") + " werden sollen.");
                return true;
            }
            mc = new MaterialCombination(((Player) sender).getInventory().getContents());
        } else {
            mc = new MaterialCombination();
            while (args.hasNext()) {
                String nextName = args.getNext();
                Material next = Material.matchMaterial(nextName);
                if (next == null) {
                    ChatAndTextUtil.sendWarningMessage(sender, "Material " + nextName + " nicht gefunden.");
                    return true;
                }
                mc.addMaterial(next);
            }
        }

        boolean result;
        switch (requiredFor) {
            case BLOCK_BREAK:
                BlockBreakQuestPossibilitiesSpecification blockBreakInstance = BlockBreakQuestPossibilitiesSpecification.getInstance();
                result = add? blockBreakInstance.addMaterialCombination(mc) : blockBreakInstance.removeMaterialCombination(mc);
                break;
            case BLOCK_PLACE:
                BlockPlaceQuestPossibilitiesSpecification blockPlaceInstance = BlockPlaceQuestPossibilitiesSpecification.getInstance();
                result = add? blockPlaceInstance.addMaterialCombination(mc) : blockPlaceInstance.removeMaterialCombination(mc);
                break;
            case DELIVERY:
                DeliveryQuestPossibilitiesSpecification deliveryInstance = DeliveryQuestPossibilitiesSpecification.getInstance();
                result = add? deliveryInstance.addMaterialCombination(mc) : deliveryInstance.removeMaterialCombination(mc);
                break;
            default:
                assert(false);
                return false;
        }

        if (result) {
            ChatAndTextUtil.sendNormalMessage(sender, "Materialkombination erfolgreich " + (add? "hinzugefügt" : "entfernt") + ".");
        } else {
            ChatAndTextUtil.sendWarningMessage(sender, "Materialkombination war " + (add? "bereits" : "nicht") + " enthalten.");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        List<String> result = new ArrayList<>();
        
        for (Material type: Material.values()) {
            result.add(type.name());
        }
        
        return ChatAndTextUtil.polishTabCompleteList(result, args.getNext(""));
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUEST_SPECIFICATIONS_PERMISSION;
    }

}
