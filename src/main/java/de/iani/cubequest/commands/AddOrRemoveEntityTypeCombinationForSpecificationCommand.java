package de.iani.cubequest.commands;

import com.google.common.base.Verify;
import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.generation.EntityTypeCombination;
import de.iani.cubequest.generation.KillEntitiesQuestSpecification.KillEntitiesQuestPossibilitiesSpecification;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.Util;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;

public class AddOrRemoveEntityTypeCombinationForSpecificationCommand extends SubCommand {
    
    private boolean add;
    private EntityTypeCombinationRequiredFor requiredFor;
    
    public enum EntityTypeCombinationRequiredFor {
        KILL_ENTITIES("KillEntitiesEntityTypeCombination");
        
        public final String command;
        
        private EntityTypeCombinationRequiredFor(String command) {
            this.command = command;
        }
    }
    
    public AddOrRemoveEntityTypeCombinationForSpecificationCommand(boolean add,
            EntityTypeCombinationRequiredFor requiredFor) {
        Verify.verifyNotNull(requiredFor);
        
        this.add = add;
        this.requiredFor = requiredFor;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args) {
        
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Entity-Typen an, die "
                    + (add ? "hinzugefügt" : "entfernt") + " werden sollen.");
            return true;
        }
        
        EntityTypeCombination ec = new EntityTypeCombination();
        while (args.hasNext()) {
            String nextName = args.getNext();
            EntityType next = Util.matchEntityType(nextName);
            if (next == null) {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Entity-Typ " + nextName + " nicht gefunden.");
                return true;
            }
            ec.addMaterial(next);
        }
        
        boolean result;
        switch (requiredFor) {
            case KILL_ENTITIES:
                KillEntitiesQuestPossibilitiesSpecification killEntitiesInstance =
                        KillEntitiesQuestPossibilitiesSpecification.getInstance();
                result = add ? killEntitiesInstance.adEntityTypeCombination(ec)
                        : killEntitiesInstance.removeEntityTypeCombination(ec);
                break;
            default:
                assert (false);
                return false;
        }
        
        if (result) {
            ChatAndTextUtil.sendNormalMessage(sender,
                    "Materialkombination erfolgreich " + (add ? "hinzugefügt" : "entfernt") + ".");
        } else {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Materialkombination war " + (add ? "bereits" : "nicht") + " enthalten.");
        }
        return true;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias,
            ArgsParser args) {
        List<String> result = new ArrayList<>();
        
        for (EntityType type: EntityType.values()) {
            result.add(type.name());
        }
        
        return ChatAndTextUtil.polishTabCompleteList(result, args.getNext(""));
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUEST_SPECIFICATIONS_PERMISSION;
    }
    
}
