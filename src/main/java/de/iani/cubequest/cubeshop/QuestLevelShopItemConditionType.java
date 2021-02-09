package de.iani.cubequest.cubeshop;

import de.iani.cubeshop.CubeShop;
import de.iani.cubeshop.DeserializationException;
import de.iani.cubeshop.shopitemconditions.ShopItemConditionType;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.Collections;
import java.util.Set;
import org.bukkit.command.CommandSender;


public class QuestLevelShopItemConditionType extends ShopItemConditionType<QuestLevelShopItemCondition> {
    
    private static final Set<String> DEPENDENCIES =
            Collections.singleton(CubeShop.PLUGIN_DEPENDENCY_PREFIX + "CubeQuest");
    private static final QuestLevelShopItemConditionType INSTANCE = new QuestLevelShopItemConditionType();
    
    public static QuestLevelShopItemConditionType getInstance() {
        return INSTANCE;
    }
    
    private QuestLevelShopItemConditionType() {
        super("QuestLevel");
    }
    
    @Override
    public Set<String> getDependencies() {
        return DEPENDENCIES;
    }
    
    @Override
    public QuestLevelShopItemCondition deserialize(String serialized) throws DeserializationException {
        return new QuestLevelShopItemCondition(serialized);
    }
    
    @Override
    public QuestLevelShopItemCondition createCondition(CommandSender sender, ArgsParser args, String commandPrefix)
            throws IllegalConditionDataException, DelayedCreationException {
        if (!args.hasNext()) {
            throw new IllegalConditionDataException(
                    "Bitte gib das minimale Questlevel an, dass ein Spieler haben muss.");
        }
        try {
            return new QuestLevelShopItemCondition(args.getNext(-1));
        } catch (IllegalArgumentException e) {
            throw new IllegalConditionDataException("Das minimale Questlevel muss eine nicht-negative Ganzzahl sein.");
        }
    }
    
}
