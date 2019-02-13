package de.iani.cubequest.actions;

import de.iani.cubequest.PlayerData;
import java.util.Map;
import java.util.Objects;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;


public class SoundAction extends LocatedAction {
    
    private Sound sound;
    private float volume;
    private float pitch;
    
    public SoundAction(Sound sound, float volume, float pitch, ActionLocation location) {
        super(location);
        
        init(sound, volume, pitch);
    }
    
    public SoundAction(Map<String, Object> serialized) {
        super(serialized);
        
        String soundString = (String) serialized.get("sound");
        Sound sound = Sound.valueOf(soundString);
        init(sound, ((Number) serialized.get("volume")).floatValue(),
                ((Number) serialized.get("pitch")).floatValue());
    }
    
    private void init(Sound sound, float volume, float pitch) {
        this.sound = Objects.requireNonNull(sound);
        this.volume = volume;
        this.pitch = pitch;
        
        if (volume <= 0) {
            throw new IllegalArgumentException("volume must be positive");
        }
    }
    
    @Override
    public void perform(Player player, PlayerData data) {
        Location loc = getLocation().getLocation(player, data);
        player.playSound(loc, this.sound, this.volume, this.pitch);
    }
    
    @Override
    public BaseComponent[] getActionInfo() {
        return new ComponentBuilder(ChatColor.DARK_AQUA + "Sound: " + this.sound
                + " mit Lautstärke " + this.volume + " und Tonhöhe " + this.pitch + " ")
                        .append(getLocation().getLocationInfo(true)).create();
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        
        result.put("sound", this.sound.name());
        result.put("volume", this.volume);
        result.put("pitch", this.pitch);
        
        return result;
    }
    
}
