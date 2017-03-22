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
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import de.iani.cubequest.events.QuestFailEvent;
import de.iani.cubequest.events.QuestRenameEvent;
import de.iani.cubequest.events.QuestSuccessEvent;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.quests.QuestManager;
import net.citizensnpcs.api.event.NPCClickEvent;

public class EventListener implements Listener {

    private CubeQuest plugin;

    public EventListener(CubeQuest plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockBreakEvent(BlockBreakEvent event) {
        for (Quest q: plugin.getQuestManager().getQuests()) {
            q.onBlockBreakEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        for (Quest q: plugin.getQuestManager().getQuests()) {
            q.onBlockPlaceEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeathEvent(EntityDeathEvent event) {
        for (Quest q: plugin.getQuestManager().getQuests()) {
            q.onEntityDeathEvent(event);
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        plugin.getCommandExecutor().getQuestEditor().playerQuit(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        for (Quest q: plugin.getQuestManager().getQuests()) {
            q.onPlayerMoveEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerFishEvent(PlayerFishEvent event) {
        for (Quest q: plugin.getQuestManager().getQuests()) {
            q.onPlayerFishEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        for (Quest q: plugin.getQuestManager().getQuests()) {
            q.onPlayerCommandPreprocessEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onNPCClickEvent(NPCClickEvent event) {
        for (Quest q: plugin.getQuestManager().getQuests()) {
            q.onNPCClickEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuestSuccessEvent(QuestSuccessEvent event) {
        for (Quest q: plugin.getQuestManager().getQuests()) {
            q.onQuestSuccessEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuestFailEvent(QuestFailEvent event) {
        for (Quest q: plugin.getQuestManager().getQuests()) {
            q.onQuestFailEvent(event);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuestRenameEvent(QuestRenameEvent event) {
        QuestManager.getInstance().onQuestRenameEvent(event);
    }

}
