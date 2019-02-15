package de.iani.cubequest.actions;

import de.iani.cubequest.PlayerData;
import java.util.Map;
import java.util.Objects;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;


public class RemovePotionEffectAction extends QuestAction {
    
    private PotionEffectType potionEffectType;
    
    public RemovePotionEffectAction(PotionEffectType potionEffectType) {
        init(potionEffectType);
    }
    
    public RemovePotionEffectAction(Map<String, Object> serialized) {
        String potionEffectTypeString = (String) serialized.get("potionEffectType");
        PotionEffectType potionEffectType = PotionEffectType.getByName(potionEffectTypeString);
        init(potionEffectType);
    }
    
    private void init(PotionEffectType potionEffectType) {
        this.potionEffectType = Objects.requireNonNull(potionEffectType);
    }
    
    @Override
    public void perform(Player player, PlayerData data) {
        player.removePotionEffect(this.potionEffectType);
    }
    
    @Override
    public BaseComponent[] getActionInfo() {
        return new ComponentBuilder(
                ChatColor.DARK_AQUA + "Trank-Effekt entfernen: " + this.potionEffectType.getName())
                        .create();
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("potionEffectType", this.potionEffectType.getName());
        return result;
    }
    
}
