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
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
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
    public void perform(Player player, PlayerData data) {
        String serverName = GlobalApi.getGlobalPortServerName(this.target.getServer());
        if (serverName == null) {
            CubeQuest.getInstance().getLogger().log(Level.WARNING, "Couldn't perform teleportation action for player "
                    + player.getUniqueId() + ", server " + this.target.getServer() + " unknown.");
            return;
        }
        
        GPLocation gpTarget = new GPLocation(serverName, this.target.getWorld(), this.target.getX(), this.target.getY(),
                this.target.getZ(), this.target.getPitch(), this.target.getYaw());
        GPPlayer gpPlayer = GPPlayer.getOnlinePlayer(player.getUniqueId());
        gpPlayer.portPlayerTo(gpTarget);
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
    public BaseComponent[] getActionInfo() {
        TextComponent[] resultMsg = new TextComponent[1];
        resultMsg[0] = new TextComponent();
        
        BaseComponent delayComp = getDelayComponent();
        if (delayComp != null) {
            resultMsg[0].addExtra(delayComp);
        }
        
        TextComponent tagComp = new TextComponent("Ziel: ");
        tagComp.setColor(ChatColor.DARK_AQUA);
        
        TextComponent locComp =
                new TextComponent(TextComponent.fromLegacyText(ChatAndTextUtil.getLocationInfo(this.target)));
        tagComp.addExtra(locComp);
        
        resultMsg[0].addExtra(tagComp);
        
        return resultMsg;
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("target", this.target);
        return result;
    }
    
}
