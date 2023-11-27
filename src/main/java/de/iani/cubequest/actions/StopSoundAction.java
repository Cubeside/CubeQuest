package de.iani.cubequest.actions;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Sound;
import org.bukkit.entity.Player;


public class StopSoundAction extends DelayableAction {

    private boolean backwardsIncompatible = false;
    private Sound sound;

    public StopSoundAction(Map<String, Object> serialized) {
        super(serialized);

        String soundString = (String) serialized.get("sound");
        try {
            if (soundString == null) {
                this.sound = null;
            } else {
                this.sound = Sound.valueOf(soundString);
            }
        } catch (IllegalArgumentException e) {
            this.backwardsIncompatible = true;
            this.sound = null;
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Sound " + soundString + " is no longer available!");
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
    public BaseComponent[] getActionInfo() {
        TextComponent[] resultMsg = new TextComponent[1];
        resultMsg[0] = new TextComponent();

        BaseComponent delayComp = getDelayComponent();
        if (delayComp != null) {
            resultMsg[0].addExtra(delayComp);
        }

        TextComponent tagComp =
                new TextComponent("Stoppe " + (this.sound == null ? "alle Sounds" : ("Sound " + this.sound.name())));
        tagComp.setColor(ChatColor.DARK_AQUA);
        resultMsg[0].addExtra(tagComp);

        return resultMsg;
    }

}
