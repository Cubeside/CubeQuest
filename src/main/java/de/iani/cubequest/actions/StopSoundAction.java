package de.iani.cubequest.actions;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.entity.Player;


public class StopSoundAction extends DelayableAction {

    private boolean backwardsIncompatible = false;
    private Sound sound;
    private String soundString;

    @SuppressWarnings("removal")
    public StopSoundAction(Map<String, Object> serialized) {
        super(serialized);

        String soundString = (String) serialized.get("sound");
        if (soundString != null) {
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
                    CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                            "Sound " + soundString + " is no longer available! Quest-ID: " + questId);
                }
            }
        }
    }

    public StopSoundAction(long delay, Sound sound) {
        super(delay);

        this.sound = sound;
    }

    @Override
    protected BiConsumer<Player, PlayerData> getActionPerformer() {
        if (this.backwardsIncompatible) {
            return (player, data) -> {
            };
        }

        return this.sound == null ? (p, pd) -> p.stopAllSounds() : (p, pd) -> p.stopSound(this.sound);
    }


    @Override
    public Component getActionInfo() {
        Component msg = Component.empty();

        Component delayComp = getDelayComponent();
        if (delayComp != null) {
            msg = msg.append(delayComp);
        }

        String what = this.backwardsIncompatible ? "(nicht vorhanden) " + this.soundString
                : this.sound == null ? "alle Sounds" : ("Sound " + Registry.SOUNDS.getKey(this.sound));

        return msg.append(Component.text("Stoppe " + what)).color(NamedTextColor.DARK_AQUA);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        if (this.sound != null || (this.backwardsIncompatible && this.soundString != null)) {
            result.put("sound",
                    this.backwardsIncompatible ? this.soundString : Registry.SOUNDS.getKey(this.sound).asString());
        }
        return result;
    }

}
