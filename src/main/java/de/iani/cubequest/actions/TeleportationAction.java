package de.iani.cubequest.actions;

import de.cubeside.connection.util.GlobalLocation;
import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.speedy64.globalport.GlobalApi;
import de.speedy64.globalport.data.GPLocation;
import de.speedy64.globalport.data.GPPlayer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;


public class TeleportationAction extends QuestAction {
    
    private GlobalLocation target;
    
    public TeleportationAction(GlobalLocation target) {
        this.target = Objects.requireNonNull(target);
    }
    
    public TeleportationAction(Map<String, Object> serialized) {
        this((GlobalLocation) serialized.get("target"));
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
    public BaseComponent[] getActionInfo() {
        TextComponent result = new TextComponent("Ziel: ");
        result.setColor(ChatColor.DARK_AQUA);
        result.addExtra(new TextComponent(TextComponent.fromLegacyText(ChatAndTextUtil.getLocationInfo(this.target))));
        return new BaseComponent[] {result};
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        result.put("target", this.target);
        return result;
    }
    
}
