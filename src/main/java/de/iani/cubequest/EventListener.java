package de.iani.cubequest;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.iani.cubequest.events.QuestFailEvent;
import de.iani.cubequest.events.QuestRenameEvent;
import de.iani.cubequest.events.QuestSuccessEvent;
import de.iani.cubequest.quests.Quest;
import net.citizensnpcs.api.event.NPCClickEvent;

public class EventListener implements Listener {

    private CubeQuest plugin;

    public EventListener(CubeQuest plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        CubeQuest.getInstance().unloadPlayerData(event.getPlayer().getUniqueId());
        Bukkit.getScheduler().scheduleSyncDelayedTask(CubeQuest.getInstance(), () -> {
            CubeQuest.getInstance().getPlayerData(event.getPlayer()).loadInitialData();
        }, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        plugin.getCommandExecutor().getQuestEditor().playerQuit(event.getPlayer());
        CubeQuest.getInstance().unloadPlayerData(event.getPlayer().getUniqueId());
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

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onNPCClickEvent(NPCClickEvent event) {
        for (Quest q: plugin.getQuestManager().getQuests()) {
            if (q.isReady()) {
                q.onNPCClickEvent(event);
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
