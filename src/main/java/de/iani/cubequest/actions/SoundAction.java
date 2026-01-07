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
                Integer questId = CubeQuest.getInstance().getQuestCreator().getCurrentlyDeserializing();
                CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                        "Sound " + soundString + " is no longer available! Quest-ID: " + questId);
            }
        } else {
            try {
                sound = Sound.valueOf(soundString);
            } catch (IllegalArgumentException e) {
                this.backwardsIncompatible = true;
                this.soundString = soundString;
                Integer questId = CubeQuest.getInstance().getQuestCreator().getCurrentlyDeserializing();
                sound = null;
                CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                        "Sound " + soundString + " is no longer available! Quest-ID: " + questId);
            }
        }
        init(sound, ((Number) serialized.get("volume")).floatValue(), ((Number) serialized.get("pitch")).floatValue());
    }

    private void init(Sound sound, float volume, float pitch) {
        if (!this.backwardsIncompatible) {
            this.sound = Objects.requireNonNull(sound);
            this.soundString = Registry.SOUNDS.getKey(this.sound).asMinimalString();
        }
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

        Component soundComp = Component.text(this.soundString, NamedTextColor.GREEN);
        if (this.backwardsIncompatible) {
            soundComp = Component.textOfChildren(soundComp, Component.text(" (nicht vorhanden)", NamedTextColor.RED));
        }

        Component locComp = getLocation().getLocationInfo(true);

        return Component
                .textOfChildren(msg, Component.text("Sound: "), soundComp, Component.text(" mit Lautstärke "),
                        Component.text(this.volume, NamedTextColor.GREEN), Component.text(" und Tonhöhe "),
                        Component.text(this.pitch, NamedTextColor.GREEN), Component.space(), locComp)
                .color(NamedTextColor.DARK_AQUA);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();

        result.put("sound", this.soundString);
        result.put("volume", this.volume);
        result.put("pitch", this.pitch);

        return result;
    }

}
