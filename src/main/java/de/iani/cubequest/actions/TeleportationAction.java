package de.iani.cubequest.actions;

import de.cubeside.connection.util.GlobalLocation;
import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.speedy64.globalport.GlobalApi;
import de.speedy64.globalport.data.GPLocation;
import de.speedy64.globalport.data.GPPlayer;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;


public class TeleportationAction extends DelayableAction {

    private GlobalLocation target;

    public TeleportationAction(long delay, GlobalLocation target) {
        super(delay);

        this.target = Objects.requireNonNull(target);
    }

    public TeleportationAction(Map<String, Object> serialized) {
        super(serialized);

        this.target = Objects.requireNonNull((GlobalLocation) serialized.get("target"));
    }

    @Override
    protected BiConsumer<Player, PlayerData> getActionPerformer() {
        return (player, data) -> {
            String serverName = GlobalApi.getGlobalPortServerName(this.target.getServer());
            if (serverName == null) {
                CubeQuest.getInstance().getLogger().log(Level.WARNING,
                        "Couldn't perform teleportation action for player " + player.getUniqueId() + ", server "
                                + this.target.getServer() + " unknown.");
                return;
            }

            GPLocation gpTarget = new GPLocation(serverName, this.target.getWorld(), this.target.getX(),
                    this.target.getY(), this.target.getZ(), this.target.getPitch(), this.target.getYaw());
            GPPlayer gpPlayer = GPPlayer.getOnlinePlayer(player.getUniqueId());
            gpPlayer.portPlayerTo(gpTarget);
        };
    }

    @Override
    public Component getActionInfo() {
        Component delayComp = getDelayComponent();
        if (delayComp == null) {
            delayComp = Component.empty();
        }

        return Component
                .textOfChildren(delayComp, Component.text("Teleport: "), ChatAndTextUtil.getLocationInfo(this.target))
                .color(NamedTextColor.DARK_AQUA);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("target", this.target);
        return result;
    }

}
