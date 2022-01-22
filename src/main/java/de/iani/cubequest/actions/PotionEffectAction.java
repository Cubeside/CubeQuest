package de.iani.cubequest.actions;

import de.iani.cubequest.PlayerData;
import de.iani.cubesideutils.bukkit.PotionEffects;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;


public class PotionEffectAction extends DelayableAction {
    
    private PotionEffect effect;
    
    public PotionEffectAction(long delay, PotionEffect effect) {
        super(delay);
        
        init(effect);
    }
    
    public PotionEffectAction(Map<String, Object> serialized) {
        super(serialized);
        
        init((PotionEffect) serialized.get("effect"));
    }
    
    private void init(PotionEffect effect) {
        this.effect = Objects.requireNonNull(effect);
    }
    
    @Override
    protected BiConsumer<Player, PlayerData> getActionPerformer() {
        return (player, data) -> this.effect.apply(player);
    }
    
    @Override
    public BaseComponent[] getActionInfo() {
        TextComponent[] resultMsg = new TextComponent[1];
        resultMsg[0] = new TextComponent();
        
        BaseComponent delayComp = getDelayComponent();
        if (delayComp != null) {
            resultMsg[0].addExtra(delayComp);
        }
        
        TextComponent tagComp = new TextComponent("Trank-Effekt: " + PotionEffects.toNiceString(this.effect));
        tagComp.setColor(ChatColor.DARK_AQUA);
        resultMsg[0].addExtra(tagComp);
        
        return resultMsg;
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("effect", this.effect);
        return result;
    }
    
}
