package de.iani.cubequest;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import de.iani.cubequest.events.QuestFailEvent;
import de.iani.cubequest.events.QuestRenameEvent;
import de.iani.cubequest.events.QuestSuccessEvent;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.quests.WaitingQuest;
import net.citizensnpcs.api.event.NPCClickEvent;

public class EventListener implements Listener, PluginMessageListener {

    private CubeQuest plugin;

    public enum MsgType {
        QUEST_UPDATED;

        private static MsgType[] values = values();

        public static MsgType fromOrdinal(int ordinal) {
            return values[ordinal];
        }
    }

    public EventListener(CubeQuest plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();
        if (subchannel.equals("GetServer")) {
            String servername = in.readUTF();
            plugin.setBungeeServerName(servername);
        } else if (subchannel.equals("CubeQuest")) {
            short len = in.readShort();
            byte[] msgbytes = new byte[len];
            in.readFully(msgbytes);

            DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
            try {
                MsgType type = MsgType.fromOrdinal(msgin.readInt());
                switch(type) {
                    case QUEST_UPDATED:
                        int questId = msgin.readInt();
                        Quest quest = QuestManager.getInstance().getQuest(questId);
                        if (quest == null) {
                            CubeQuest.getInstance().getQuestCreator().loadQuest(questId);
                        } else {
                            CubeQuest.getInstance().getQuestCreator().refreshQuest(questId);
                        }
                        break;
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Exception reading incoming PluginMessage!", e);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        CubeQuest.getInstance().unloadPlayerData(event.getPlayer().getUniqueId());

        if (CubeQuest.getInstance().hasTreasureChest()) {
            try {
                for (Reward r: CubeQuest.getInstance().getDatabaseFassade().getAndDeleteRewardsToDeliver(event.getPlayer().getUniqueId())) {
                    CubeQuest.getInstance().addToTreasureChest(event.getPlayer().getUniqueId(), r);
                }
            } catch (SQLException | InvalidConfigurationException e) {
                CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not load rewards to deliver for player " + event.getPlayer().getName() + ":", e);
            }
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(CubeQuest.getInstance(), () -> {
            CubeQuest.getInstance().getPlayerData(event.getPlayer()).loadInitialData();
            for (Quest quest: QuestManager.getInstance().getQuests()) {
                if (quest instanceof WaitingQuest) {
                    ((WaitingQuest) quest).checkPlayer(event.getPlayer());
                }
            }
        }, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        plugin.getQuestEditor().stopEdit(event.getPlayer());
        CubeQuest.getInstance().unloadPlayerData(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            plugin.getQuestEditor().removeFromSelectingNPC(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreakEvent(BlockBreakEvent event) {
        for (Quest q: plugin.getQuestManager().getQuests()) {
            if (q.isReady()) {
                q.onBlockBreakEvent(event);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        for (Quest q: plugin.getQuestManager().getQuests()) {
            if (q.isReady()) {
                q.onBlockPlaceEvent(event);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeathEvent(EntityDeathEvent event) {
        for (Quest q: plugin.getQuestManager().getQuests()) {
            if (q.isReady()) {
                q.onEntityDeathEvent(event);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        for (Quest q: plugin.getQuestManager().getQuests()) {
            if (q.isReady()) {
                q.onPlayerMoveEvent(event);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFishEvent(PlayerFishEvent event) {
        for (Quest q: plugin.getQuestManager().getQuests()) {
            if (q.isReady()) {
                q.onPlayerFishEvent(event);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        for (Quest q: plugin.getQuestManager().getQuests()) {
            if (q.isReady()) {
                q.onPlayerCommandPreprocessEvent(event);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onNPCClickEvent(NPCClickEvent event) {
        if (plugin.getQuestEditor().isSelectingNPC(event.getClicker())) {
            Bukkit.dispatchCommand(event.getClicker(), "/quest setNPC " + event.getNPC().getId());
            return;
        }
        if (!event.isCancelled()) {
            for (Quest q: plugin.getQuestManager().getQuests()) {
                if (q.isReady()) {
                    q.onNPCClickEvent(event);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuestSuccessEvent(QuestSuccessEvent event) {
        for (Quest q: plugin.getQuestManager().getQuests()) {
            if (q.isReady()) {
                q.onQuestSuccessEvent(event);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuestFailEvent(QuestFailEvent event) {
        for (Quest q: plugin.getQuestManager().getQuests()) {
            if (q.isReady()) {
                q.onQuestFailEvent(event);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuestRenameEvent(QuestRenameEvent event) {
        QuestManager.getInstance().onQuestRenameEvent(event);
    }

}
