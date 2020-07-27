package de.iani.cubequest.actions;

import de.iani.cubequest.PlayerData;
import de.iani.cubesideutils.bukkit.PotionEffects;
import java.util.Map;
import java.util.Objects;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;


public class PotionEffectAction extends QuestAction {
    
    private PotionEffect effect;
    
    public PotionEffectAction(PotionEffect effect) {
        init(effect);
    }
    
    public PotionEffectAction(Map<String, Object> serialized) {
        init((PotionEffect) serialized.get("effect"));
    }
    
    private void init(PotionEffect effect) {
        this.effect = Objects.requireNonNull(effect);
    }
    
    @Override
    public void perform(Player player, PlayerData data) {
        this.effect.apply(player);
    }
    
    @Override
    public BaseComponent[] getActionInfo() {
        return new ComponentBuilder(
                ChatColor.DARK_AQUA + "Trank-Effekt: " + PotionEffects.toNiceString(this.effect))
                        .create();
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("effect", this.effect);
        return result;
    }
    
}
