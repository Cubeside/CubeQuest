package de.iani.cubequest.cubeshop;

import de.iani.cubequest.CubeQuest;
import de.iani.cubeshop.DeserializationException;
import de.iani.cubeshop.Price;
import org.bukkit.entity.Player;

public class QuestPointsPrice extends Price {
    
    private int amount;
    
    public QuestPointsPrice(String serialized) throws DeserializationException {
        try {
            this.amount = Integer.parseInt(serialized);
        } catch (NumberFormatException e) {
            throw new DeserializationException(e);
        }
        if (this.amount <= 0) {
            throw new DeserializationException("amount may not be negative");
        }
    }
    
    public QuestPointsPrice(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount may not be negative");
        }
        this.amount = amount;
    }
    
    @Override
    public QuestPointsPriceType getType() {
        return QuestPointsPriceType.getInstance();
    }
    
    @Override
    public boolean canAfford(Player player) {
        return CubeQuest.getInstance().getPlayerData(player).getQuestPoints() >= this.amount;
    }
    
    @Override
    public void pay(Player player) {
        CubeQuest.getInstance().getPlayerData(player).changeQuestPoints(-1 * this.amount);
    }
    
    @Override
    public String toString() {
        return this.amount + " Quest-Punkt" + (this.amount == 1 ? "" : "e");
    }
    
    @Override
    public String serialize() {
        return String.valueOf(this.amount);
    }
    
}
