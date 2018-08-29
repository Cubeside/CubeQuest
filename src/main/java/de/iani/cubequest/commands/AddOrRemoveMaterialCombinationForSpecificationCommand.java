package de.iani.cubequest.commands;

import com.google.common.base.Verify;
import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.generation.BlockBreakQuestSpecification.BlockBreakQuestPossibilitiesSpecification;
import de.iani.cubequest.generation.BlockPlaceQuestSpecification.BlockPlaceQuestPossibilitiesSpecification;
import de.iani.cubequest.generation.DeliveryQuestSpecification.DeliveryQuestPossibilitiesSpecification;
import de.iani.cubequest.generation.FishingQuestSpecification.FishingQuestPossibilitiesSpecification;
import de.iani.cubequest.generation.MaterialCombination;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AddOrRemoveMaterialCombinationForSpecificationCommand extends SubCommand {
    
    private boolean add;
    private MaterialCombinationRequiredFor requiredFor;
    
    public enum MaterialCombinationRequiredFor {
        DELIVERY("DeliveryMaterialCombination"),
        BLOCK_BREAK("BlockBreakMaterialCombination"),
        BLOCK_PLACE("BlockPlaceMaterialCombination"),
        FISH("FishingMaterialCombination");
        
        public final String command;
        
        private MaterialCombinationRequiredFor(String command) {
            this.command = command;
        }
    }
    
    public AddOrRemoveMaterialCombinationForSpecificationCommand(boolean add,
            MaterialCombinationRequiredFor requiredFor) {
        Verify.verifyNotNull(requiredFor);
        
        this.add = add;
        this.requiredFor = requiredFor;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args) {
        
        MaterialCombination mc;
        
        if (!args.hasNext()) {
            if (!(sender instanceof Player)) {
                ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Materialien an, die "
                        + (this.add ? "hinzugefügt" : "entfernt") + " werden sollen.");
                return true;
            }
            mc = new MaterialCombination(((Player) sender).getInventory().getContents());
        } else {
            mc = new MaterialCombination();
            while (args.hasNext()) {
                String nextName = args.getNext();
                Material next = Material.matchMaterial(nextName);
                if (next == null) {
                    ChatAndTextUtil.sendWarningMessage(sender,
                            "Material " + nextName + " nicht gefunden.");
                    return true;
                }
                mc.add(next);
            }
        }
        
        boolean result;
        switch (this.requiredFor) {
            case DELIVERY:
                DeliveryQuestPossibilitiesSpecification deliveryInstance =
                        DeliveryQuestPossibilitiesSpecification.getInstance();
                result = this.add ? deliveryInstance.addMaterialCombination(mc)
                        : deliveryInstance.removeMaterialCombination(mc);
                break;
            case BLOCK_BREAK:
                BlockBreakQuestPossibilitiesSpecification blockBreakInstance =
                        BlockBreakQuestPossibilitiesSpecification.getInstance();
                result = this.add ? blockBreakInstance.addMaterialCombination(mc)
                        : blockBreakInstance.removeMaterialCombination(mc);
                break;
            case BLOCK_PLACE:
                BlockPlaceQuestPossibilitiesSpecification blockPlaceInstance =
                        BlockPlaceQuestPossibilitiesSpecification.getInstance();
                result = this.add ? blockPlaceInstance.addMaterialCombination(mc)
                        : blockPlaceInstance.removeMaterialCombination(mc);
                break;
            case FISH:
                FishingQuestPossibilitiesSpecification fishingInstance =
                        FishingQuestPossibilitiesSpecification.getInstance();
                result = this.add ? fishingInstance.addMaterialCombination(mc)
                        : fishingInstance.removeMaterialCombination(mc);
                break;
            default:
                assert (false);
                return false;
        }
        
        if (result) {
            ChatAndTextUtil.sendNormalMessage(sender, "Materialkombination erfolgreich "
                    + (this.add ? "hinzugefügt" : "entfernt") + ".");
        } else {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Materialkombination war " + (this.add ? "bereits" : "nicht") + " enthalten.");
        }
        return true;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUEST_SPECIFICATIONS_PERMISSION;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias,
            ArgsParser args) {
        List<String> result = new ArrayList<>();
        
        for (Material type: Material.values()) {
            result.add(type.name());
        }
        
        return ChatAndTextUtil.polishTabCompleteList(result, args.getNext(""));
    }
    
    @Override
    public String getUsage() {
        return "<Material> [Material...]";
    }
    
}
