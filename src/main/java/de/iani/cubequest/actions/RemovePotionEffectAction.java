package de.iani.cubequest.actions;

import de.iani.cubequest.PlayerData;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;


public class RemovePotionEffectAction extends DelayableAction {
    
    private PotionEffectType potionEffectType;
    
    public RemovePotionEffectAction(long delay, PotionEffectType potionEffectType) {
        super(delay);
        
        init(potionEffectType);
    }
    
    public RemovePotionEffectAction(Map<String, Object> serialized) {
        super(serialized);
        
        String potionEffectTypeString = (String) serialized.get("potionEffectType");
        PotionEffectType potionEffectType = PotionEffectType.getByName(potionEffectTypeString);
        init(potionEffectType);
    }
    
    private void init(PotionEffectType potionEffectType) {
        this.potionEffectType = Objects.requireNonNull(potionEffectType);
    }
    
    @Override
    protected BiConsumer<Player, PlayerData> getActionPerformer() {
        return (player, data) -> player.removePotionEffect(this.potionEffectType);
    }
    
    @Override
    public BaseComponent[] getActionInfo() {
        TextComponent[] resultMsg = new TextComponent[1];
        resultMsg[0] = new TextComponent();
        
        BaseComponent delayComp = getDelayComponent();
        if (delayComp != null) {
            resultMsg[0].addExtra(delayComp);
        }
        
        TextComponent tagComp = new TextComponent("Trank-Effekt entfernen: " + this.potionEffectType.getName());
        tagComp.setColor(ChatColor.DARK_AQUA);
        resultMsg[0].addExtra(tagComp);
        
        return resultMsg;
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("potionEffectType", this.potionEffectType.getName());
        return result;
    }
    
}
