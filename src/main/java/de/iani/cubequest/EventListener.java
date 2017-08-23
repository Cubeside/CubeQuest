package de.iani.cubequest;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
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
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.quests.Quest;
import net.citizensnpcs.api.event.NPCClickEvent;

public class EventListener implements Listener, PluginMessageListener {

    private CubeQuest plugin;

    private Consumer<QuestState> forEachActiveQuestAfterPlayerJoinEvent
            = (state -> state.getQuest().afterPlayerJoinEvent(state));
    private QuestStateConsumerOnTEvent<PlayerQuitEvent> forEachActiveQuestOnPlayerQuitEvent
            = new QuestStateConsumerOnTEvent<PlayerQuitEvent>((event, state) -> state.getQuest().onPlayerQuitEvent(event, state));
    private QuestStateConsumerOnTEvent<BlockBreakEvent> forEachActiveQuestOnBlockBreakEvent
            = new QuestStateConsumerOnTEvent<BlockBreakEvent>((event, state) -> state.getQuest().onBlockBreakEvent(event, state));
    private QuestStateConsumerOnTEvent<BlockPlaceEvent> forEachActiveQuestOnBlockPlaceEvent
            = new QuestStateConsumerOnTEvent<BlockPlaceEvent>((event, state) -> state.getQuest().onBlockPlaceEvent(event, state));
    private QuestStateConsumerOnTEvent<EntityDeathEvent> forEachActiveQuestOnEntityKilledByPlayerEvent
            = new QuestStateConsumerOnTEvent<EntityDeathEvent>((event, state) -> state.getQuest().onEntityKilledByPlayerEvent(event, state));
    private QuestStateConsumerOnTEvent<PlayerMoveEvent> forEachActiveQuestOnPlayerMoveEvent
            = new QuestStateConsumerOnTEvent<PlayerMoveEvent>((event, state) -> state.getQuest().onPlayerMoveEvent(event, state));
    private QuestStateConsumerOnTEvent<PlayerFishEvent> forEachActiveQuestOnPlayerFishEvent
            = new QuestStateConsumerOnTEvent<PlayerFishEvent>((event, state) -> state.getQuest().onPlayerFishEvent(event, state));
    private QuestStateConsumerOnTEvent<PlayerCommandPreprocessEvent> forEachActiveQuestOnPlayerCommandPreprocessEvent
            = new QuestStateConsumerOnTEvent<PlayerCommandPreprocessEvent>((event, state) -> state.getQuest().onPlayerCommandPreprocessEvent(event, state));
    private QuestStateConsumerOnTEvent<NPCClickEvent> forEachActiveQuestOnNPCClickEvent
            = new QuestStateConsumerOnTEvent<NPCClickEvent>((event, state) -> state.getQuest().onNPCClickEvent(event, state));
    private QuestStateConsumerOnTEvent<QuestSuccessEvent> forEachActiveQuestOnQuestSuccessEvent
            = new QuestStateConsumerOnTEvent<QuestSuccessEvent>((event, state) -> state.getQuest().onQuestSuccessEvent(event, state));
    private QuestStateConsumerOnTEvent<QuestFailEvent> forEachActiveQuestOnQuestFailEvent
            = new QuestStateConsumerOnTEvent<QuestFailEvent>((event, state) -> state.getQuest().onQuestFailEvent(event, state));

    public enum MsgType {
        QUEST_UPDATED;

        private static MsgType[] values = values();

        public static MsgType fromOrdinal(int ordinal) {
            return values[ordinal];
        }
    }

    private class QuestStateConsumerOnTEvent<T extends Event> implements Consumer<QuestState> {

        private T event = null;
        private BiConsumer<T, QuestState> action;

        public QuestStateConsumerOnTEvent(BiConsumer<T, QuestState> action) {
            this.action = action;
        }

        @Override
        public void accept(QuestState state) {
            action.accept(event, state);
        }

        public void setEvent(T event) {
            this.event = event;
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
        final Player player = event.getPlayer();

        CubeQuest.getInstance().unloadPlayerData(player.getUniqueId());

        if (CubeQuest.getInstance().hasTreasureChest()) {
            try {
                for (Reward r: CubeQuest.getInstance().getDatabaseFassade().getAndDeleteRewardsToDeliver(player.getUniqueId())) {
                    CubeQuest.getInstance().addToTreasureChest(player.getUniqueId(), r);
                }
            } catch (SQLException | InvalidConfigurationException e) {
                CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not load rewards to deliver for player " + event.getPlayer().getName() + ":", e);
            }
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(CubeQuest.getInstance(), () -> {
            CubeQuest.getInstance().getPlayerData(player).loadInitialData();
            CubeQuest.getInstance().getPlayerData(player).getActiveQuests().forEach(forEachActiveQuestAfterPlayerJoinEvent);
        }, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        plugin.getQuestEditor().stopEdit(event.getPlayer());
        CubeQuest.getInstance().unloadPlayerData(event.getPlayer().getUniqueId());

        forEachActiveQuestOnPlayerQuitEvent.setEvent(event);
        CubeQuest.getInstance().getPlayerData(event.getPlayer()).getActiveQuests().forEach(forEachActiveQuestOnPlayerQuitEvent);
        forEachActiveQuestOnPlayerQuitEvent.setEvent(null);
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            plugin.getQuestEditor().removeFromSelectingNPC(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreakEvent(BlockBreakEvent event) {
        forEachActiveQuestOnBlockBreakEvent.setEvent(event);
        CubeQuest.getInstance().getPlayerData(event.getPlayer()).getActiveQuests().forEach(forEachActiveQuestOnBlockBreakEvent);
        forEachActiveQuestOnBlockBreakEvent.setEvent(null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        forEachActiveQuestOnBlockPlaceEvent.setEvent(event);
        CubeQuest.getInstance().getPlayerData(event.getPlayer()).getActiveQuests().forEach(forEachActiveQuestOnBlockPlaceEvent);
        forEachActiveQuestOnBlockPlaceEvent.setEvent(null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeathEvent(EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();
        if (player == null) {
            return;
        }
        forEachActiveQuestOnEntityKilledByPlayerEvent.setEvent(event);
        CubeQuest.getInstance().getPlayerData(player).getActiveQuests().forEach(forEachActiveQuestOnEntityKilledByPlayerEvent);
        forEachActiveQuestOnEntityKilledByPlayerEvent.setEvent(null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        forEachActiveQuestOnPlayerMoveEvent.setEvent(event);
        CubeQuest.getInstance().getPlayerData(event.getPlayer()).getActiveQuests().forEach(forEachActiveQuestOnPlayerMoveEvent);
        forEachActiveQuestOnPlayerMoveEvent.setEvent(null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFishEvent(PlayerFishEvent event) {
        forEachActiveQuestOnPlayerFishEvent.setEvent(event);
        CubeQuest.getInstance().getPlayerData(event.getPlayer()).getActiveQuests().forEach(forEachActiveQuestOnPlayerFishEvent);
        forEachActiveQuestOnPlayerFishEvent.setEvent(null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        forEachActiveQuestOnPlayerCommandPreprocessEvent.setEvent(event);
        CubeQuest.getInstance().getPlayerData(event.getPlayer()).getActiveQuests().forEach(forEachActiveQuestOnPlayerCommandPreprocessEvent);
        forEachActiveQuestOnPlayerCommandPreprocessEvent.setEvent(null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onNPCClickEvent(NPCClickEvent event) {
        if (plugin.getQuestEditor().isSelectingNPC(event.getClicker())) {
            Bukkit.dispatchCommand(event.getClicker(), "quest setNPC " + event.getNPC().getId());
            event.setCancelled(true);
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        forEachActiveQuestOnNPCClickEvent.setEvent(event);
        CubeQuest.getInstance().getPlayerData(event.getClicker()).getActiveQuests().forEach(forEachActiveQuestOnNPCClickEvent);
        forEachActiveQuestOnNPCClickEvent.setEvent(null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuestSuccessEvent(QuestSuccessEvent event) {
        forEachActiveQuestOnQuestSuccessEvent.setEvent(event);
        CubeQuest.getInstance().getPlayerData(event.getPlayer()).getActiveQuests().forEach(forEachActiveQuestOnQuestSuccessEvent);
        forEachActiveQuestOnQuestSuccessEvent.setEvent(null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuestFailEvent(QuestFailEvent event) {
        forEachActiveQuestOnQuestFailEvent.setEvent(event);
        CubeQuest.getInstance().getPlayerData(event.getPlayer()).getActiveQuests().forEach(forEachActiveQuestOnQuestFailEvent);
        forEachActiveQuestOnQuestFailEvent.setEvent(null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuestRenameEvent(QuestRenameEvent event) {
        QuestManager.getInstance().onQuestRenameEvent(event);
    }

}
