package de.iani.cubequest.actions;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;


public class SoundAction extends LocatedAction {

    private boolean backwardsIncompatible = false;
    private Sound sound;
    private String soundString;
    private float volume;
    private float pitch;

    public SoundAction(long delay, Sound sound, float volume, float pitch, ActionLocation location) {
        super(delay, location);

        init(sound, volume, pitch);
    }

    @SuppressWarnings("removal")
    public SoundAction(Map<String, Object> serialized) {
        super(serialized);

        String soundString = (String) serialized.get("sound");
        Sound sound;
        NamespacedKey key = NamespacedKey.fromString(soundString);
        if (key != null) {
            sound = Registry.SOUNDS.get(key);
            if (sound == null) {
                this.backwardsIncompatible = true;
                this.soundString = soundString;
                CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                        "Sound " + soundString + " is no longer available!");
            }
        } else {
            try {
                sound = Sound.valueOf(soundString);
            } catch (IllegalArgumentException e) {
                this.backwardsIncompatible = true;
                this.soundString = soundString;
                sound = null;
                CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                        "Sound " + soundString + " is no longer available!");
            }
        }
        init(sound, ((Number) serialized.get("volume")).floatValue(), ((Number) serialized.get("pitch")).floatValue());
    }

    private void init(Sound sound, float volume, float pitch) {
        this.sound = this.backwardsIncompatible ? null : Objects.requireNonNull(sound);
        this.volume = volume;
        this.pitch = pitch;

        if (volume <= 0) {
            throw new IllegalArgumentException("volume must be positive");
        }

        if (pitch < 0.5 || pitch > 2) {
            throw new IllegalArgumentException("pitch must be between 0.5 and 2");
        }
    }

    @Override
    public SoundAction relocate(ActionLocation location) {
        return new SoundAction(getDelay(), this.sound, this.volume, this.pitch, location);
    }

    @Override
    protected BiConsumer<Player, PlayerData> getActionPerformer() {
        if (this.backwardsIncompatible) {
            return (player, data) -> {
            };
        }

        return (player, data) -> {
            Location loc = getLocation().getLocation(player, data);
            player.playSound(loc, this.sound, this.volume, this.pitch);
        };
    }

    @Override
    public Component getActionInfo() {
        Component msg = Component.empty();

        Component delayComp = getDelayComponent();
        if (delayComp != null) {
            msg = msg.append(delayComp);
        }

        String soundName = this.backwardsIncompatible ? "(nicht vorhanden) " + this.soundString
                : Registry.SOUNDS.getKey(this.sound).asMinimalString();

        Component locComp = getLocation().getLocationInfo(true);

        return msg.append(Component
                .text("Sound: " + soundName + " mit Lautstärke " + this.volume + " und Tonhöhe " + this.pitch + " ")
                .append(locComp)).color(NamedTextColor.DARK_AQUA);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();

        result.put("sound",
                this.backwardsIncompatible ? this.soundString : Registry.SOUNDS.getKey(this.sound).asString());
        result.put("volume", this.volume);
        result.put("pitch", this.pitch);

        return result;
    }

}
