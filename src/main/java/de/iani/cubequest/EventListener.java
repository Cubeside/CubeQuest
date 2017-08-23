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
    private QuestStateConsumerOnTEvent<PlayerMoveEvent> forEachActiveQuestOnPlayerMoveEvent
            = new QuestStateConsumerOnTEvent<PlayerMoveEvent>((event, state) -> state.getQuest().onPlayerMoveEvent(event, state));

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
            CubeQuest.getInstance().getPlayerData(player).getActiveQuests().forEach(state -> {
                state.getQuest().afterPlayerJoinEvent(state);
            });
        }, 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        plugin.getQuestEditor().stopEdit(event.getPlayer());
        CubeQuest.getInstance().unloadPlayerData(event.getPlayer().getUniqueId());
        CubeQuest.getInstance().getPlayerData(event.getPlayer()).getActiveQuests().forEach(state -> {
            state.getQuest().onPlayerQuitEvent(event, state);
        });
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            plugin.getQuestEditor().removeFromSelectingNPC(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreakEvent(BlockBreakEvent event) {
        CubeQuest.getInstance().getPlayerData(event.getPlayer()).getActiveQuests().forEach(state -> {
            state.getQuest().onBlockBreakEvent(event, state);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        CubeQuest.getInstance().getPlayerData(event.getPlayer()).getActiveQuests().forEach(state -> {
            state.getQuest().onBlockPlaceEvent(event, state);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeathEvent(EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();
        if (player == null) {
            return;
        }
        CubeQuest.getInstance().getPlayerData(player).getActiveQuests().forEach(state -> {
            state.getQuest().onEntityKilledByPlayerEvent(event, state);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        forEachActiveQuestOnPlayerMoveEvent.setEvent(event);
        CubeQuest.getInstance().getPlayerData(event.getPlayer()).getActiveQuests().forEach(forEachActiveQuestOnPlayerMoveEvent);
        forEachActiveQuestOnPlayerMoveEvent.setEvent(null);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFishEvent(PlayerFishEvent event) {
        CubeQuest.getInstance().getPlayerData(event.getPlayer()).getActiveQuests().forEach(state -> {
            state.getQuest().onPlayerFishEvent(event, state);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        CubeQuest.getInstance().getPlayerData(event.getPlayer()).getActiveQuests().forEach(state -> {
            state.getQuest().onPlayerCommandPreprocessEvent(event, state);
        });
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

        CubeQuest.getInstance().getPlayerData(event.getClicker()).getActiveQuests().forEach(state -> {
            state.getQuest().onNPCClickEvent(event, state);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuestSuccessEvent(QuestSuccessEvent event) {
        CubeQuest.getInstance().getPlayerData(event.getPlayer()).getActiveQuests().forEach(state -> {
            state.getQuest().onQuestSuccessEvent(event, state);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuestFailEvent(QuestFailEvent event) {
        CubeQuest.getInstance().getPlayerData(event.getPlayer()).getActiveQuests().forEach(state -> {
            state.getQuest().onQuestFailEvent(event, state);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuestRenameEvent(QuestRenameEvent event) {
        QuestManager.getInstance().onQuestRenameEvent(event);
    }

}
