package de.iani.cubequest.cubeshop;

import de.iani.cubequest.CubeQuest;
import de.iani.cubeshop.CubeShop;
import de.iani.cubeshop.DeserializationException;
import de.iani.cubeshop.pricecreation.IntegerValuedPriceType;
import java.util.Collections;
import java.util.Set;
import org.bukkit.entity.Player;

public class QuestPointsPriceType extends IntegerValuedPriceType<QuestPointsPrice> {
    
    private static final QuestPointsPriceType INSTANCE = new QuestPointsPriceType();
    private static final Set<String> DEPENDENCIES = Collections
            .singleton(CubeShop.PLUGIN_DEPENDENCY_PREFIX + CubeQuest.getInstance().getName());
    
    public static QuestPointsPriceType getInstance() {
        return INSTANCE;
    }
    
    private QuestPointsPriceType() {
        super("QuestPoints");
    }
    
    @Override
    public String getName() {
        return "Questpunkte";
    }
    
    @Override
    public Set<String> getDependencies() {
        return DEPENDENCIES;
    }
    
    @Override
    public int getAvailable(Player player) {
        return CubeQuest.getInstance().getPlayerData(player).getQuestPoints();
    }
    
    @Override
    public String getAvailableAsString(Player player) {
        int points = getAvailable(player);
        return points + " Questpunkt" + (points == 1 ? "" : "e");
    }
    
    @Override
    public QuestPointsPrice createPrice(int value) {
        return new QuestPointsPrice(value);
    }
    
    @Override
    public QuestPointsPrice deserialize(String serialized) throws DeserializationException {
        return new QuestPointsPrice(serialized);
    }
    
}
