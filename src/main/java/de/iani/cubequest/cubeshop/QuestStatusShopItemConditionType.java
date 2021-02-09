package de.iani.cubequest.cubeshop;

import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubeshop.CubeShop;
import de.iani.cubeshop.DeserializationException;
import de.iani.cubeshop.shopitemconditions.ShopItemConditionType;
import de.iani.cubeshop.utils.ArgsParserWrapper;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.command.CommandSender;


public class QuestStatusShopItemConditionType extends ShopItemConditionType<QuestStatusShopItemCondition> {
    
    private static final QuestStatusShopItemConditionType INSTANCE = new QuestStatusShopItemConditionType();
    private static final Set<String> DEPENDENCIES =
            Collections.singleton(CubeShop.PLUGIN_DEPENDENCY_PREFIX + "CubeQuest");
    
    public static QuestStatusShopItemConditionType getInstance() {
        return INSTANCE;
    }
    
    private QuestStatusShopItemConditionType() {
        super("QuestStatus");
    }
    
    @Override
    public Set<String> getDependencies() {
        return DEPENDENCIES;
    }
    
    @Override
    public QuestStatusShopItemCondition deserialize(String serialized) throws DeserializationException {
        return new QuestStatusShopItemCondition(serialized);
    }
    
    @Override
    public QuestStatusShopItemCondition createCondition(CommandSender sender, ArgsParserWrapper args,
            String commandPrefix) throws IllegalConditionDataException, DelayedCreationException {
        if (args.remaining() < 2) {
            throw new IllegalConditionDataException(commandUsage());
        }
        
        boolean negated = false;
        if (args.seeNext(null).equalsIgnoreCase("NOT")) {
            negated = true;
            args.next();
        }
        
        if (args.remaining() < 2) {
            throw new IllegalConditionDataException(commandUsage());
        }
        
        Status status = Status.match(args.next());
        if (status == null) {
            throw new IllegalConditionDataException(commandUsage());
        }
        
        int questId = args.getNext(-1);
        if (questId <= 0) {
            throw new IllegalConditionDataException("Ungültige Quest-ID.");
        }
        
        String description = args.getAll(null);
        return new QuestStatusShopItemCondition(negated, status, questId, description);
    }
    
    private String commandUsage() {
        return "Syntax: [NOT] "
                + Arrays.stream(Status.values()).map(Status::name).collect(Collectors.joining(" | ", "<", ">"))
                + " <QuestId> [Beschreibung]";
    }
    
}
