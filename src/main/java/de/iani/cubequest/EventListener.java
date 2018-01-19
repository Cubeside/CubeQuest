package de.iani.cubequest;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import de.iani.cubequest.events.QuestDeleteEvent;
import de.iani.cubequest.events.QuestFailEvent;
import de.iani.cubequest.events.QuestRenameEvent;
import de.iani.cubequest.events.QuestSuccessEvent;
import de.iani.cubequest.events.QuestWouldBeDeletedEvent;
import de.iani.cubequest.interaction.EntityInteractor;
import de.iani.cubequest.interaction.PlayerInteractEntityInteractorEvent;
import de.iani.cubequest.interaction.PlayerInteractInteractorEvent;
import de.iani.cubequest.questGiving.QuestGiver;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.quests.InteractorQuest;
import de.iani.cubequest.quests.Quest;
import de.speedy64.globalchat.api.GlobalChatDataEvent;

public class EventListener implements Listener, PluginMessageListener {
    
    private CubeQuest plugin;
    
    private Consumer<QuestState> forEachActiveQuestAfterPlayerJoinEvent =
            (state -> state.getQuest().afterPlayerJoinEvent(state));
    
    private QuestStateConsumerOnEvent<PlayerQuitEvent> forEachActiveQuestOnPlayerQuitEvent =
            new QuestStateConsumerOnEvent<>(
                    (event, state) -> state.getQuest().onPlayerQuitEvent(event, state));
    
    private QuestStateConsumerOnEvent<BlockBreakEvent> forEachActiveQuestOnBlockBreakEvent =
            new QuestStateConsumerOnEvent<>(
                    (event, state) -> state.getQuest().onBlockBreakEvent(event, state));
    
    private QuestStateConsumerOnEvent<BlockPlaceEvent> forEachActiveQuestOnBlockPlaceEvent =
            new QuestStateConsumerOnEvent<>(
                    (event, state) -> state.getQuest().onBlockPlaceEvent(event, state));
    
    private QuestStateConsumerOnEvent<EntityDeathEvent> forEachActiveQuestOnEntityKilledByPlayerEvent =
            new QuestStateConsumerOnEvent<>(
                    (event, state) -> state.getQuest().onEntityKilledByPlayerEvent(event, state));
    
    private QuestStateConsumerOnEvent<EntityTameEvent> forEachActiveQuestOnEntityTamedByPlayerEvent =
            new QuestStateConsumerOnEvent<>(
                    (event, state) -> state.getQuest().onEntityTamedByPlayerEvent(event, state));
    
    private QuestStateConsumerOnEvent<PlayerMoveEvent> forEachActiveQuestOnPlayerMoveEvent =
            new QuestStateConsumerOnEvent<>(
                    (event, state) -> state.getQuest().onPlayerMoveEvent(event, state));
    
    private QuestStateConsumerOnEvent<PlayerFishEvent> forEachActiveQuestOnPlayerFishEvent =
            new QuestStateConsumerOnEvent<>(
                    (event, state) -> state.getQuest().onPlayerFishEvent(event, state));
    
    private QuestStateConsumerOnEvent<PlayerCommandPreprocessEvent> forEachActiveQuestOnPlayerCommandPreprocessEvent =
            new QuestStateConsumerOnEvent<>((event, state) -> state.getQuest()
                    .onPlayerCommandPreprocessEvent(event, state));
    
    private QuestStateConsumerOnEvent<PlayerInteractInteractorEvent> forEachActiveQuestOnPlayerInteractInteractorEvent =
            new QuestStateConsumerOnEvent<>((event, state) -> state.getQuest()
                    .onPlayerInteractInteractorEvent(event, state));
    
    // Buggy wegen indirekt rekursivem Aufruf der onEvent-Methode
    // private QuestStateConsumerOnEvent<QuestSuccessEvent> forEachActiveQuestOnQuestSuccessEvent
    // = new QuestStateConsumerOnEvent<>((event, state) ->
    // state.getQuest().onQuestSuccessEvent(event, state));
    //
    // private QuestStateConsumerOnEvent<QuestFailEvent> forEachActiveQuestOnQuestFailEvent
    // = new QuestStateConsumerOnEvent<>((event, state) -> state.getQuest().onQuestFailEvent(event,
    // state));
    
    public enum BugeeMsgType {
        QUEST_UPDATED, NPC_QUEST_SETREADY;
        
        private static BugeeMsgType[] values = values();
        
        public static BugeeMsgType fromOrdinal(int ordinal) {
            return values[ordinal];
        }
    }
    
    public enum GlobalChatMsgType {
        GENERATE_DAILY_QUEST, DAILY_QUEST_GENERATED;
        
        private static GlobalChatMsgType[] values = values();
        
        public static GlobalChatMsgType fromOrdinal(int ordinal) {
            return values[ordinal];
        }
    }
    
    private class QuestStateConsumerOnEvent<T> implements Consumer<QuestState> {
        
        private T event = null;
        private BiConsumer<T, QuestState> action;
        
        public QuestStateConsumerOnEvent(BiConsumer<T, QuestState> action) {
            this.action = action;
        }
        
        @Override
        public void accept(QuestState state) {
            this.action.accept(this.event, state);
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
            this.plugin.setBungeeServerName(servername);
        } else if (subchannel.equals("CubeQuest")) {
            short len = in.readShort();
            byte[] msgbytes = new byte[len];
            in.readFully(msgbytes);
            
            DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
            try {
                BugeeMsgType type = BugeeMsgType.fromOrdinal(msgin.readInt());
                switch (type) {
                    case QUEST_UPDATED:
                        int questId = msgin.readInt();
                        Quest quest = QuestManager.getInstance().getQuest(questId);
                        if (quest == null) {
                            this.plugin.getQuestCreator().loadQuest(questId);
                        } else {
                            this.plugin.getQuestCreator().refreshQuest(questId);
                        }
                        
                        break;
                    
                    case NPC_QUEST_SETREADY:
                        questId = msgin.readInt();
                        InteractorQuest npcQuest =
                                (InteractorQuest) QuestManager.getInstance().getQuest(questId);
                        npcQuest.hasBeenSetReady(msgin.readBoolean());
                        
                        break;
                    
                    default:
                        this.plugin.getLogger().log(Level.WARNING, "Unknown BungeeMsgType " + type
                                + ". Msg-bytes: " + Arrays.toString(msgbytes));
                }
            } catch (IOException e) {
                this.plugin.getLogger().log(Level.SEVERE,
                        "Exception reading incoming PluginMessage!", e);
                return;
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGlobalChatDataEvent(GlobalChatDataEvent event) {
        if (!event.getChannel().equals("CubeQuest")) {
            return;
        }
        
        try {
            DataInputStream msgin = event.getData();
            GlobalChatMsgType type = GlobalChatMsgType.fromOrdinal(msgin.readInt());
            switch (type) {
                case GENERATE_DAILY_QUEST:
                    if (!msgin.readUTF().equals(this.plugin.getBungeeServerName())) {
                        return;
                    }
                    
                    int dailyQuestOrdinal = msgin.readInt();
                    String dateString = msgin.readUTF();
                    double difficulty = msgin.readDouble();
                    long seed = msgin.readLong();
                    Quest result = this.plugin.getQuestGenerator().generateQuest(dailyQuestOrdinal,
                            dateString, difficulty, new Random(seed));
                    
                    ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
                    DataOutputStream msgout = new DataOutputStream(msgbytes);
                    msgout.writeInt(GlobalChatMsgType.DAILY_QUEST_GENERATED.ordinal());
                    msgout.writeInt(dailyQuestOrdinal);
                    msgout.writeInt(result.getId());
                    
                    byte[] msgarry = msgbytes.toByteArray();
                    this.plugin.getGlobalChatAPI().sendDataToServers("CubeQuest", msgarry);
                    
                    break;
                
                case DAILY_QUEST_GENERATED:
                    int ordinal = msgin.readInt();
                    int questId = msgin.readInt();
                    Quest quest = QuestManager.getInstance().getQuest(questId);
                    if (quest == null) {
                        this.plugin.getQuestCreator().loadQuest(questId);
                        quest = QuestManager.getInstance().getQuest(questId);
                    } else {
                        this.plugin.getQuestCreator().refreshQuest(quest);
                    }
                    this.plugin.getQuestGenerator().dailyQuestGenerated(ordinal, quest);
                    
                    break;
                
                default:
                    this.plugin.getLogger().log(Level.WARNING,
                            "Unknown GlobalChatMsgType " + type + ".");
            }
        } catch (IOException e) {
            this.plugin.getLogger().log(Level.SEVERE,
                    "Exception reading incoming GlobalChatMessage!", e);
            return;
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        
        this.plugin.unloadPlayerData(player.getUniqueId());
        
        this.plugin.playerArrived();
        
        if (this.plugin.hasTreasureChest()) {
            try {
                for (Reward r: this.plugin.getDatabaseFassade()
                        .getAndDeleteRewardsToDeliver(player.getUniqueId())) {
                    this.plugin.addToTreasureChest(player.getUniqueId(), r);
                }
            } catch (SQLException | InvalidConfigurationException e) {
                this.plugin.getLogger().log(Level.SEVERE,
                        "Could not load rewards to deliver for player "
                                + event.getPlayer().getName() + ":",
                        e);
            }
        }
        
        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
            this.plugin.getPlayerData(player).loadInitialData();
            this.plugin.getPlayerData(player).getActiveQuests()
                    .forEach(this.forEachActiveQuestAfterPlayerJoinEvent);
        }, 1L);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        this.plugin.getQuestEditor().stopEdit(event.getPlayer());
        this.plugin.getQuestGivers().forEach(qg -> qg.removeMightGetFromHere(event.getPlayer()));
        this.plugin.unloadPlayerData(event.getPlayer().getUniqueId());
        
        this.forEachActiveQuestOnPlayerQuitEvent.setEvent(event);
        this.plugin.getPlayerData(event.getPlayer()).getActiveQuests()
                .forEach(this.forEachActiveQuestOnPlayerQuitEvent);
        this.forEachActiveQuestOnPlayerQuitEvent.setEvent(null);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreakEvent(BlockBreakEvent event) {
        this.forEachActiveQuestOnBlockBreakEvent.setEvent(event);
        this.plugin.getPlayerData(event.getPlayer()).getActiveQuests()
                .forEach(this.forEachActiveQuestOnBlockBreakEvent);
        this.forEachActiveQuestOnBlockBreakEvent.setEvent(null);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        this.forEachActiveQuestOnBlockPlaceEvent.setEvent(event);
        this.plugin.getPlayerData(event.getPlayer()).getActiveQuests()
                .forEach(this.forEachActiveQuestOnBlockPlaceEvent);
        this.forEachActiveQuestOnBlockPlaceEvent.setEvent(null);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeathEvent(EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();
        if (player == null) {
            return;
        }
        this.forEachActiveQuestOnEntityKilledByPlayerEvent.setEvent(event);
        this.plugin.getPlayerData(player).getActiveQuests()
                .forEach(this.forEachActiveQuestOnEntityKilledByPlayerEvent);
        this.forEachActiveQuestOnEntityKilledByPlayerEvent.setEvent(null);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityTameEvent(EntityTameEvent event) {
        if (!(event.getOwner() instanceof Player)) {
            return;
        }
        this.forEachActiveQuestOnEntityTamedByPlayerEvent.setEvent(event);
        this.plugin.getPlayerData((Player) event.getOwner()).getActiveQuests()
                .forEach(this.forEachActiveQuestOnEntityTamedByPlayerEvent);
        this.forEachActiveQuestOnEntityTamedByPlayerEvent.setEvent(null);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        this.forEachActiveQuestOnPlayerMoveEvent.setEvent(event);
        this.plugin.getPlayerData(event.getPlayer()).getActiveQuests()
                .forEach(this.forEachActiveQuestOnPlayerMoveEvent);
        this.forEachActiveQuestOnPlayerMoveEvent.setEvent(null);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFishEvent(PlayerFishEvent event) {
        this.forEachActiveQuestOnPlayerFishEvent.setEvent(event);
        this.plugin.getPlayerData(event.getPlayer()).getActiveQuests()
                .forEach(this.forEachActiveQuestOnPlayerFishEvent);
        this.forEachActiveQuestOnPlayerFishEvent.setEvent(null);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        this.forEachActiveQuestOnPlayerCommandPreprocessEvent.setEvent(event);
        this.plugin.getPlayerData(event.getPlayer()).getActiveQuests()
                .forEach(this.forEachActiveQuestOnPlayerCommandPreprocessEvent);
        this.forEachActiveQuestOnPlayerCommandPreprocessEvent.setEvent(null);
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        Bukkit.getPluginManager().callEvent(new PlayerInteractEntityInteractorEvent(event,
                new EntityInteractor(event.getRightClicked())));
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onPlayerInteractAtEntityEvent(PlayerInteractAtEntityEvent event) {
        Bukkit.getPluginManager().callEvent(new PlayerInteractEntityInteractorEvent(event,
                new EntityInteractor(event.getRightClicked())));
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerInteractInteractorEvent(PlayerInteractInteractorEvent event) {
        QuestGiver giver = this.plugin.getQuestGiver(event.getInteractor());
        if (giver != null) {
            giver.showQuestsToPlayer(event.getPlayer());
        }
        
        this.forEachActiveQuestOnPlayerInteractInteractorEvent.setEvent(event);
        this.plugin.getPlayerData(event.getPlayer()).getActiveQuests()
                .forEach(this.forEachActiveQuestOnPlayerInteractInteractorEvent);
        this.forEachActiveQuestOnPlayerInteractInteractorEvent.setEvent(null);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuestSuccessEvent(QuestSuccessEvent event) {
        // forEachActiveQuestOnQuestSuccessEvent.setEvent(event);
        // plugin.getPlayerData(event.getPlayer()).getActiveQuests().forEach(forEachActiveQuestOnQuestSuccessEvent);
        // forEachActiveQuestOnQuestSuccessEvent.setEvent(null);
        this.plugin.getPlayerData(event.getPlayer()).getActiveQuests()
                .forEach((state -> state.getQuest().onQuestSuccessEvent(event, state)));
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuestFailEvent(QuestFailEvent event) {
        // forEachActiveQuestOnQuestFailEvent.setEvent(event);
        // plugin.getPlayerData(event.getPlayer()).getActiveQuests().forEach(forEachActiveQuestOnQuestFailEvent);
        // forEachActiveQuestOnQuestFailEvent.setEvent(null);
        this.plugin.getPlayerData(event.getPlayer()).getActiveQuests()
                .forEach((state -> state.getQuest().onQuestFailEvent(event, state)));
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuestRenameEvent(QuestRenameEvent event) {
        QuestManager.getInstance().onQuestRenameEvent(event);
    }
    
    @EventHandler
    public void onQuestDeleteEvent(QuestDeleteEvent event) {
        for (Quest q: QuestManager.getInstance().getQuests()) {
            q.onQuestDeleteEvent(event);
        }
    }
    
    @EventHandler
    public void onQuestWouldBeDeletedEvent(QuestWouldBeDeletedEvent event) {
        for (Quest q: QuestManager.getInstance().getQuests()) {
            q.onQuestWouldBeDeletedEvent(event);
        }
    }
    
}
