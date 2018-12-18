package de.iani.cubequest;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import de.iani.cubequest.events.QuestDeleteEvent;
import de.iani.cubequest.events.QuestFailEvent;
import de.iani.cubequest.events.QuestFreezeEvent;
import de.iani.cubequest.events.QuestRenameEvent;
import de.iani.cubequest.events.QuestSetReadyEvent;
import de.iani.cubequest.events.QuestSuccessEvent;
import de.iani.cubequest.events.QuestWouldBeDeletedEvent;
import de.iani.cubequest.generation.DeliveryQuestSpecification;
import de.iani.cubequest.generation.DeliveryQuestSpecification.DeliveryReceiverSpecification;
import de.iani.cubequest.generation.QuestGenerator;
import de.iani.cubequest.generation.QuestSpecification;
import de.iani.cubequest.interaction.BlockInteractor;
import de.iani.cubequest.interaction.BlockInteractorDamagedEvent;
import de.iani.cubequest.interaction.EntityInteractor;
import de.iani.cubequest.interaction.EntityInteractorDamagedEvent;
import de.iani.cubequest.interaction.InteractorDamagedEvent;
import de.iani.cubequest.interaction.InteractorProtecting;
import de.iani.cubequest.interaction.PlayerInteractBlockInteractorEvent;
import de.iani.cubequest.interaction.PlayerInteractEntityInteractorEvent;
import de.iani.cubequest.interaction.PlayerInteractInteractorEvent;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.quests.ComplexQuest;
import de.iani.cubequest.quests.InteractorQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.ParameterizedConsumer;
import de.iani.cubequest.wrapper.NPCEventListener;
import de.speedy64.globalchat.api.GlobalChatDataEvent;
import java.io.DataInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityCreatePortalEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;

public class EventListener implements Listener, PluginMessageListener {
    
    private static class QuestConsumerForInteractorEvent implements Consumer<QuestState> {
        
        private PlayerInteractInteractorEvent<?> param;
        private boolean aggregated;
        
        public QuestConsumerForInteractorEvent() {
            this.param = null;
            this.aggregated = false;
        }
        
        @Override
        public void accept(QuestState state) {
            Quest quest = state.getQuest();
            if (quest.onPlayerInteractInteractorEvent(this.param, state)) {
                this.aggregated = true;
                if (this.param.getInteractor() instanceof EntityInteractor) {
                    this.param.setCancelled(true);
                }
                if (quest instanceof InteractorQuest) {
                    if (((InteractorQuest) quest).isRequireConfirmation()) {
                        CubeQuest.getInstance().getInteractionConfirmationHandler()
                                .addQuestToNextBook((InteractorQuest) quest);
                    } else {
                        ((InteractorQuest) quest).playerConfirmedInteraction(this.param.getPlayer(),
                                state);
                    }
                }
            }
        }
        
        public PlayerInteractInteractorEvent<?> getParam() {
            return this.param;
        }
        
        public void setParam(PlayerInteractInteractorEvent<?> event) {
            this.param = event;
        }
        
        public boolean popAggregated() {
            boolean res = this.aggregated;
            this.aggregated = false;
            return res;
        }
    }
    
    private CubeQuest plugin;
    
    private NPCEventListener npcListener;
    
    private Map<PlayerInteractInteractorEvent<?>, PlayerInteractInteractorEvent<?>> interactsThisTick;
    
    private List<Consumer<Player>> onPlayerJoin;
    private List<Consumer<Player>> onPlayerQuit;
    
    private Consumer<QuestState> forEachActiveQuestAfterPlayerJoinEvent =
            (state -> state.getQuest().afterPlayerJoinEvent(state));
    
    private ParameterizedConsumer<PlayerQuitEvent, QuestState> forEachActiveQuestOnPlayerQuitEvent =
            new ParameterizedConsumer<>(
                    (event, state) -> state.getQuest().onPlayerQuitEvent(event, state));
    
    private ParameterizedConsumer<BlockBreakEvent, QuestState> forEachActiveQuestOnBlockBreakEvent =
            new ParameterizedConsumer<>(
                    (event, state) -> state.getQuest().onBlockBreakEvent(event, state));
    
    private ParameterizedConsumer<BlockPlaceEvent, QuestState> forEachActiveQuestOnBlockPlaceEvent =
            new ParameterizedConsumer<>(
                    (event, state) -> state.getQuest().onBlockPlaceEvent(event, state));
    
    private ParameterizedConsumer<EntityDeathEvent, QuestState> forEachActiveQuestOnEntityKilledByPlayerEvent =
            new ParameterizedConsumer<>(
                    (event, state) -> state.getQuest().onEntityKilledByPlayerEvent(event, state));
    
    private ParameterizedConsumer<EntityTameEvent, QuestState> forEachActiveQuestOnEntityTamedByPlayerEvent =
            new ParameterizedConsumer<>(
                    (event, state) -> state.getQuest().onEntityTamedByPlayerEvent(event, state));
    
    private ParameterizedConsumer<PlayerMoveEvent, QuestState> forEachActiveQuestOnPlayerMoveEvent =
            new ParameterizedConsumer<>(
                    (event, state) -> state.getQuest().onPlayerMoveEvent(event, state));
    
    private ParameterizedConsumer<PlayerFishEvent, QuestState> forEachActiveQuestOnPlayerFishEvent =
            new ParameterizedConsumer<>(
                    (event, state) -> state.getQuest().onPlayerFishEvent(event, state));
    
    private ParameterizedConsumer<PlayerCommandPreprocessEvent, QuestState> forEachActiveQuestOnPlayerCommandPreprocessEvent =
            new ParameterizedConsumer<>((event, state) -> state.getQuest()
                    .onPlayerCommandPreprocessEvent(event, state));
    
    private QuestConsumerForInteractorEvent forEachActiveQuestOnPlayerInteractInteractorEvent =
            new QuestConsumerForInteractorEvent();
    
    private ParameterizedConsumer<QuestSuccessEvent, QuestState> forEachActiveQuestOnQuestSuccessEvent =
            new ParameterizedConsumer<>(
                    (event, state) -> state.getQuest().onQuestSuccessEvent(event, state));
    
    private ParameterizedConsumer<QuestFailEvent, QuestState> forEachActiveQuestOnQuestFailEvent =
            new ParameterizedConsumer<>(
                    (event, state) -> state.getQuest().onQuestFailEvent(event, state));
    
    private ParameterizedConsumer<QuestFreezeEvent, QuestState> forEachActiveQuestOnQuestFreezeEvent =
            new ParameterizedConsumer<>(
                    (event, state) -> state.getQuest().onQuestFreezeEvent(event, state));
    
    public enum GlobalChatMsgType {
        QUEST_UPDATED,
        QUEST_DELETED,
        NPC_QUEST_SETREADY,
        GENERATE_DAILY_QUEST,
        DAILY_QUEST_GENERATED,
        DAILY_QUEST_FINISHED,
        DAILY_QUESTS_REMOVED;
        
        private static GlobalChatMsgType[] values = values();
        
        public static GlobalChatMsgType fromOrdinal(int ordinal) {
            return values[ordinal];
        }
    }
    
    public EventListener(CubeQuest plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
        Bukkit.getServer().getMessenger().registerIncomingPluginChannel(plugin, "BungeeCord", this);
        
        if (CubeQuest.getInstance().hasCitizensPlugin()) {
            this.npcListener = new NPCEventListener();
        }
        
        this.interactsThisTick = new HashMap<>();
        this.onPlayerJoin = new ArrayList<>();
        this.onPlayerQuit = new ArrayList<>();
    }
    
    public void tick() {
        if (!this.interactsThisTick.isEmpty()) {
            this.interactsThisTick.clear();
        }
    }
    
    public void addOnPlayerJoin(Consumer<Player> action) {
        this.onPlayerJoin.add(action);
    }
    
    public void addOnPlayerQuit(Consumer<Player> action) {
        this.onPlayerQuit.add(action);
    }
    
    public void callEventIfDistinct(PlayerInteractInteractorEvent<?> event) {
        PlayerInteractInteractorEvent<?> oldEvent = this.interactsThisTick.put(event, event);
        if (oldEvent == null) {
            Bukkit.getPluginManager().callEvent(event);
        } else if (oldEvent.isCancelled()) {
            event.setCancelled(true);
        }
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
                case QUEST_UPDATED:
                    int questId = msgin.readInt();
                    Quest quest = QuestManager.getInstance().getQuest(questId);
                    if (quest == null) {
                        this.plugin.getQuestCreator().loadQuest(questId);
                    } else {
                        this.plugin.getQuestCreator().refreshQuest(questId);
                    }
                    
                    break;
                
                case QUEST_DELETED:
                    questId = msgin.readInt();
                    quest = QuestManager.getInstance().getQuest(questId);
                    if (quest != null) {
                        QuestManager.getInstance().questDeleted(quest);
                    } else {
                        CubeQuest.getInstance().getLogger().log(Level.WARNING,
                                "Quest deleted on other server not found on this server.");
                    }
                    
                    break;
                
                case NPC_QUEST_SETREADY:
                    questId = msgin.readInt();
                    InteractorQuest npcQuest =
                            (InteractorQuest) QuestManager.getInstance().getQuest(questId);
                    npcQuest.hasBeenSetReady(msgin.readBoolean());
                    
                    break;
                
                case GENERATE_DAILY_QUEST:
                    if (!msgin.readUTF().equals(this.plugin.getBungeeServerName())) {
                        return;
                    }
                    
                    if (!this.plugin.getQuestGenerator().checkForDelegatedGeneration()) {
                        this.plugin.getLogger().log(Level.SEVERE,
                                "No delegated generation found despite global chat message received.");
                    }
                    
                    break;
                
                case DAILY_QUEST_GENERATED:
                    int ordinal = msgin.readInt();
                    questId = msgin.readInt();
                    quest = QuestManager.getInstance().getQuest(questId);
                    if (quest == null) {
                        this.plugin.getQuestCreator().loadQuest(questId);
                        quest = QuestManager.getInstance().getQuest(questId);
                    } else {
                        this.plugin.getQuestCreator().refreshQuest(quest);
                    }
                    this.plugin.getQuestGenerator().dailyQuestGenerated(ordinal, quest);
                    
                    break;
                
                case DAILY_QUEST_FINISHED:
                case DAILY_QUESTS_REMOVED:
                    if (this.plugin.getQuestGenerator() != null) {
                        // possible with to bad timing (event arrives before QuestGenerator is
                        // loaded.
                        this.plugin.getQuestGenerator().refreshDailyQuests();
                    }
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
                for (Reward r : this.plugin.getDatabaseFassade()
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
        
        PlayerData data = this.plugin.getPlayerData(player);
        
        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
            data.getActiveQuests().forEach(this.forEachActiveQuestAfterPlayerJoinEvent);
            
            boolean changed;
            do {
                changed = false;
                
                for (QuestState state : data.getActiveQuests()) {
                    Quest quest = state.getQuest();
                    if (!(quest instanceof ComplexQuest)) {
                        continue;
                    }
                    
                    changed |= ((ComplexQuest) quest).update(player);
                }
            } while (changed);
            
            if (player.hasPermission(CubeQuest.ACCEPT_QUESTS_PERMISSION)) {
                for (Quest quest : CubeQuest.getInstance().getAutoGivenQuests()) {
                    if (data.getPlayerStatus(quest.getId()) == Status.NOTGIVENTO
                            && quest.fulfillsGivingConditions(player, data)) {
                        // fullfillsgivingconditions impliziert ready
                        quest.giveToPlayer(player);
                    }
                }
            }
            
            for (Consumer<Player> c : this.onPlayerJoin) {
                c.accept(event.getPlayer());
            }
        }, 1L);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        for (Consumer<Player> c : this.onPlayerQuit) {
            c.accept(event.getPlayer());
        }
        
        CubeQuest.getInstance().getPlayerData(event.getPlayer()).payDelayedRewards();
        
        this.plugin.getQuestGivers().forEach(qg -> qg.removeMightGetFromHere(event.getPlayer()));
        
        PlayerQuitEvent oldEvent = this.forEachActiveQuestOnPlayerQuitEvent.getParam();
        this.forEachActiveQuestOnPlayerQuitEvent.setParam(event);
        this.plugin.getPlayerData(event.getPlayer()).getActiveQuests()
                .forEach(this.forEachActiveQuestOnPlayerQuitEvent);
        this.forEachActiveQuestOnPlayerQuitEvent.setParam(oldEvent);
        
        this.plugin.unloadPlayerData(event.getPlayer().getUniqueId());
    }
    
    // BlockEvents for security and quests
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void earlierOnBlockBurnEvent(BlockBurnEvent event) {
        BlockInteractor interactor = new BlockInteractor(event.getBlock());
        BlockInteractorDamagedEvent<BlockBurnEvent> newEvent =
                new BlockInteractorDamagedEvent<>(event, interactor);
        Bukkit.getPluginManager().callEvent(newEvent);
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void earlierOnBlockExplodeEvent(BlockExplodeEvent event) {
        BlockInteractor interactor = new BlockInteractor(event.getBlock());
        BlockInteractorDamagedEvent<BlockExplodeEvent> newEvent =
                new BlockInteractorDamagedEvent<>(event, interactor);
        Bukkit.getPluginManager().callEvent(newEvent);
        
        for (Block b : event.blockList()) {
            System.out.println(b);
            if (event.isCancelled()) {
                System.out.println(event.isCancelled());
                return;
            }
            BlockInteractor otherInteractor = new BlockInteractor(b);
            BlockInteractorDamagedEvent<BlockExplodeEvent> otherNewEvent =
                    new BlockInteractorDamagedEvent<>(event, otherInteractor);
            Bukkit.getPluginManager().callEvent(otherNewEvent);
        }
        
        System.out.println(event.isCancelled());
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void earlierOnBlockFadeEvent(BlockFadeEvent event) {
        BlockInteractor interactor = new BlockInteractor(event.getBlock());
        BlockInteractorDamagedEvent<BlockFadeEvent> newEvent =
                new BlockInteractorDamagedEvent<>(event, interactor);
        Bukkit.getPluginManager().callEvent(newEvent);
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void earlierOnBlockFromToEvent(BlockFromToEvent event) {
        BlockInteractor interactor = new BlockInteractor(event.getBlock());
        BlockInteractorDamagedEvent<BlockFromToEvent> newEvent =
                new BlockInteractorDamagedEvent<>(event, interactor);
        Bukkit.getPluginManager().callEvent(newEvent);
        
        if (event.isCancelled()) {
            return;
        }
        
        BlockInteractor secondInteractor = new BlockInteractor(event.getToBlock());
        BlockInteractorDamagedEvent<BlockFromToEvent> secondNewEvent =
                new BlockInteractorDamagedEvent<>(event, secondInteractor);
        Bukkit.getPluginManager().callEvent(secondNewEvent);
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void earlierOnBlockGrowEvent(BlockGrowEvent event) {
        BlockInteractor interactor = new BlockInteractor(event.getBlock());
        BlockInteractorDamagedEvent<BlockGrowEvent> newEvent =
                new BlockInteractorDamagedEvent<>(event, interactor);
        Bukkit.getPluginManager().callEvent(newEvent);
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockIgniteEvent(BlockIgniteEvent event) {
        BlockInteractor interactor = new BlockInteractor(event.getBlock());
        BlockInteractorDamagedEvent<BlockIgniteEvent> newEvent =
                new BlockInteractorDamagedEvent<>(event, interactor);
        Bukkit.getPluginManager().callEvent(newEvent);
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void earlierOnBlockPhysicsEvent(BlockPhysicsEvent event) {
        BlockInteractor interactor = new BlockInteractor(event.getBlock());
        BlockInteractorDamagedEvent<BlockPhysicsEvent> newEvent =
                new BlockInteractorDamagedEvent<>(event, interactor);
        Bukkit.getPluginManager().callEvent(newEvent);
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void earlierOnBlockPistonExtendEvent(BlockPistonExtendEvent event) {
        for (Block b : event.getBlocks()) {
            BlockInteractor interactor = new BlockInteractor(b);
            BlockInteractorDamagedEvent<BlockPistonExtendEvent> newEvent =
                    new BlockInteractorDamagedEvent<>(event, interactor);
            Bukkit.getPluginManager().callEvent(newEvent);
            
            if (event.isCancelled()) {
                return;
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void earlierOnBlockPistonRetractEvent(BlockPistonRetractEvent event) {
        for (Block b : event.getBlocks()) {
            BlockInteractor interactor = new BlockInteractor(b);
            BlockInteractorDamagedEvent<BlockPistonRetractEvent> newEvent =
                    new BlockInteractorDamagedEvent<>(event, interactor);
            Bukkit.getPluginManager().callEvent(newEvent);
            
            if (event.isCancelled()) {
                return;
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void earlierOnLeavesDecayEvent(LeavesDecayEvent event) {
        BlockInteractor interactor = new BlockInteractor(event.getBlock());
        BlockInteractorDamagedEvent<LeavesDecayEvent> newEvent =
                new BlockInteractorDamagedEvent<>(event, interactor);
        Bukkit.getPluginManager().callEvent(newEvent);
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void earlierOnBlockBreakEvent(BlockBreakEvent event) {
        BlockInteractor interactor = new BlockInteractor(event.getBlock());
        BlockInteractorDamagedEvent<BlockBreakEvent> newEvent =
                new BlockInteractorDamagedEvent<>(event, interactor);
        Bukkit.getPluginManager().callEvent(newEvent);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreakEvent(BlockBreakEvent event) {
        BlockBreakEvent oldEvent = this.forEachActiveQuestOnBlockBreakEvent.getParam();
        this.forEachActiveQuestOnBlockBreakEvent.setParam(event);
        this.plugin.getPlayerData(event.getPlayer()).getActiveQuests()
                .forEach(this.forEachActiveQuestOnBlockBreakEvent);
        this.forEachActiveQuestOnBlockBreakEvent.setParam(oldEvent);
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void earlierOnBlockPlaceEvent(BlockPlaceEvent event) {
        BlockInteractor interactor = new BlockInteractor(event.getBlock());
        BlockInteractorDamagedEvent<BlockPlaceEvent> newEvent =
                new BlockInteractorDamagedEvent<>(event, interactor);
        Bukkit.getPluginManager().callEvent(newEvent);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlaceEvent(BlockPlaceEvent event) {
        BlockPlaceEvent oldEvent = this.forEachActiveQuestOnBlockPlaceEvent.getParam();
        this.forEachActiveQuestOnBlockPlaceEvent.setParam(event);
        this.plugin.getPlayerData(event.getPlayer()).getActiveQuests()
                .forEach(this.forEachActiveQuestOnBlockPlaceEvent);
        this.forEachActiveQuestOnBlockPlaceEvent.setParam(oldEvent);
    }
    
    // EntityEvents for security and quests
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void earlierOnHangingBreakEvent(HangingBreakEvent event) {
        EntityInteractor interactor = new EntityInteractor(event.getEntity());
        EntityInteractorDamagedEvent<HangingBreakEvent> newEvent =
                new EntityInteractorDamagedEvent<>(event, interactor);
        Bukkit.getPluginManager().callEvent(newEvent);
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void earlierOnEntityChangeBlockEvent(EntityChangeBlockEvent event) {
        BlockInteractor interactor = new BlockInteractor(event.getBlock());
        BlockInteractorDamagedEvent<EntityChangeBlockEvent> newEvent =
                new BlockInteractorDamagedEvent<>(event, interactor);
        Bukkit.getPluginManager().callEvent(newEvent);
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void earlierOnEntityCreatePortalEvent(EntityCreatePortalEvent event) {
        for (BlockState state : event.getBlocks()) {
            BlockInteractor interactor = new BlockInteractor(state.getBlock());
            BlockInteractorDamagedEvent<EntityCreatePortalEvent> newEvent =
                    new BlockInteractorDamagedEvent<>(event, interactor);
            Bukkit.getPluginManager().callEvent(newEvent);
            
            if (event.isCancelled()) {
                return;
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void earlierOnEntityDamageEvent(EntityDamageEvent event) {
        EntityInteractor interactor = new EntityInteractor(event.getEntity());
        EntityInteractorDamagedEvent<EntityDamageEvent> newEvent =
                new EntityInteractorDamagedEvent<>(event, interactor);
        Bukkit.getPluginManager().callEvent(newEvent);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeathEvent(EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();
        if (player == null) {
            return;
        }
        
        EntityDeathEvent oldEvent = this.forEachActiveQuestOnEntityKilledByPlayerEvent.getParam();
        this.forEachActiveQuestOnEntityKilledByPlayerEvent.setParam(event);
        this.plugin.getPlayerData(player).getActiveQuests()
                .forEach(this.forEachActiveQuestOnEntityKilledByPlayerEvent);
        this.forEachActiveQuestOnEntityKilledByPlayerEvent.setParam(oldEvent);
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void earlierOnEntityExplodeEvent(EntityExplodeEvent event) {
        for (Block b : event.blockList()) {
            BlockInteractor interactor = new BlockInteractor(b);
            BlockInteractorDamagedEvent<EntityExplodeEvent> newEvent =
                    new BlockInteractorDamagedEvent<>(event, interactor);
            Bukkit.getPluginManager().callEvent(newEvent);
            
            if (event.isCancelled()) {
                return;
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityTameEvent(EntityTameEvent event) {
        if (!(event.getOwner() instanceof Player)) {
            return;
        }
        
        EntityTameEvent oldEvent = this.forEachActiveQuestOnEntityTamedByPlayerEvent.getParam();
        this.forEachActiveQuestOnEntityTamedByPlayerEvent.setParam(event);
        this.plugin.getPlayerData((Player) event.getOwner()).getActiveQuests()
                .forEach(this.forEachActiveQuestOnEntityTamedByPlayerEvent);
        this.forEachActiveQuestOnEntityTamedByPlayerEvent.setParam(oldEvent);
    }
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void earlierOnExplosionPrimeEvent(ExplosionPrimeEvent event) {
        EntityInteractor interactor = new EntityInteractor(event.getEntity());
        EntityInteractorDamagedEvent<ExplosionPrimeEvent> newEvent =
                new EntityInteractorDamagedEvent<>(event, interactor);
        Bukkit.getPluginManager().callEvent(newEvent);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMoveEvent(PlayerMoveEvent event) {
        PlayerMoveEvent oldEvent = this.forEachActiveQuestOnPlayerMoveEvent.getParam();
        this.forEachActiveQuestOnPlayerMoveEvent.setParam(event);
        this.plugin.getPlayerData(event.getPlayer()).getActiveQuests()
                .forEach(this.forEachActiveQuestOnPlayerMoveEvent);
        this.forEachActiveQuestOnPlayerMoveEvent.setParam(oldEvent);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerFishEvent(PlayerFishEvent event) {
        PlayerFishEvent oldEvent = this.forEachActiveQuestOnPlayerFishEvent.getParam();
        this.forEachActiveQuestOnPlayerFishEvent.setParam(event);
        this.plugin.getPlayerData(event.getPlayer()).getActiveQuests()
                .forEach(this.forEachActiveQuestOnPlayerFishEvent);
        this.forEachActiveQuestOnPlayerFishEvent.setParam(oldEvent);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        PlayerCommandPreprocessEvent oldEvent =
                this.forEachActiveQuestOnPlayerCommandPreprocessEvent.getParam();
        this.forEachActiveQuestOnPlayerCommandPreprocessEvent.setParam(event);
        this.plugin.getPlayerData(event.getPlayer()).getActiveQuests()
                .forEach(this.forEachActiveQuestOnPlayerCommandPreprocessEvent);
        this.forEachActiveQuestOnPlayerCommandPreprocessEvent.setParam(oldEvent);
    }
    
    // Interaction soll auch dan ggf. Quests auslösen, wenn gecancelled.
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onPlayerInteractEntityEvent(PlayerInteractEntityEvent event) {
        if (this.npcListener != null && this.npcListener.onPlayerInteractEntityEvent(event)) {
            return;
        }
        if (event.getPlayer().isSneaking()) {
            return;
        }
        
        PlayerInteractInteractorEvent<?> newEvent = new PlayerInteractEntityInteractorEvent(event,
                new EntityInteractor(event.getRightClicked()));
        callEventIfDistinct(newEvent);
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onPlayerInteractAtEntityEvent(PlayerInteractAtEntityEvent event) {
        onPlayerInteractEntityEvent(event);
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onPlayerArmorStandManipulateEvent(PlayerArmorStandManipulateEvent event) {
        onPlayerInteractEntityEvent(event);
    }
    
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = false)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }
        if (event.getPlayer().isSneaking()) {
            return;
        }
        
        PlayerInteractInteractorEvent<?> newEvent = new PlayerInteractBlockInteractorEvent(event,
                new BlockInteractor(event.getClickedBlock()));
        callEventIfDistinct(newEvent);
    }
    
    // Wird höchstens vom Plugin gecancelled, dann sollen auch keine Quests etwas machen
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteractInteractorEvent(PlayerInteractInteractorEvent<?> event) {
        PlayerInteractInteractorEvent<?> oldEvent =
                this.forEachActiveQuestOnPlayerInteractInteractorEvent.getParam();
        this.forEachActiveQuestOnPlayerInteractInteractorEvent.setParam(event);
        this.plugin.getPlayerData(event.getPlayer()).getActiveQuests()
                .forEach(this.forEachActiveQuestOnPlayerInteractInteractorEvent);
        boolean ignoreGiver =
                this.forEachActiveQuestOnPlayerInteractInteractorEvent.popAggregated();
        this.forEachActiveQuestOnPlayerInteractInteractorEvent.setParam(oldEvent);
        
        if (CubeQuest.getInstance().getInteractionConfirmationHandler()
                .showBook(event.getPlayer())) {
            event.setCancelled(true);
            ignoreGiver = true;
        }
        
        if (ignoreGiver) {
            return;
        }
        
        QuestGiver giver = this.plugin.getQuestGiver(event.getInteractor());
        if (giver != null) {
            event.setCancelled(true);
            
            // range check also updates location cache
            if (event.getPlayer().getLocation()
                    .distance(giver.getInteractor().getLocation()) <= 7) {
                giver.showQuestsToPlayer(event.getPlayer());
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuestSuccessEvent(QuestSuccessEvent event) {
        QuestSuccessEvent oldEvent = this.forEachActiveQuestOnQuestSuccessEvent.getParam();
        this.forEachActiveQuestOnQuestSuccessEvent.setParam(event);
        this.plugin.getPlayerData(event.getPlayer()).getActiveQuests()
                .forEach(this.forEachActiveQuestOnQuestSuccessEvent);
        this.forEachActiveQuestOnQuestSuccessEvent.setParam(oldEvent);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuestFailEvent(QuestFailEvent event) {
        QuestFailEvent oldEvent = this.forEachActiveQuestOnQuestFailEvent.getParam();
        this.forEachActiveQuestOnQuestFailEvent.setParam(event);
        this.plugin.getPlayerData(event.getPlayer()).getActiveQuests()
                .forEach(this.forEachActiveQuestOnQuestFailEvent);
        this.forEachActiveQuestOnQuestFailEvent.setParam(oldEvent);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuestFreezeEvent(QuestFreezeEvent event) {
        QuestFreezeEvent oldEvent = this.forEachActiveQuestOnQuestFreezeEvent.getParam();
        this.forEachActiveQuestOnQuestFreezeEvent.setParam(event);
        this.plugin.getPlayerData(event.getPlayer()).getActiveQuests()
                .forEach(this.forEachActiveQuestOnQuestFreezeEvent);
        this.forEachActiveQuestOnQuestFreezeEvent.setParam(oldEvent);
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuestRenameEvent(QuestRenameEvent event) {
        QuestManager.getInstance().onQuestRenameEvent(event);
    }
    
    @EventHandler
    public void onQuestSetReadyEvent(QuestSetReadyEvent event) {
        for (ComplexQuest q : QuestManager.getInstance().getQuests(ComplexQuest.class)) {
            q.onQuestSetReadyEvent(event);
        }
        
        if (event.isCancelled()) {
            return;
        }
        if (event.getSetReady()) {
            return;
        }
        
        for (PlayerData data : CubeQuest.getInstance().getLoadedPlayerData()) {
            if (data.isGivenTo(event.getQuest().getId())) {
                event.getQuest().removeFromPlayer(data.getId());
            }
        }
    }
    
    @EventHandler
    public void onQuestDeleteEvent(QuestDeleteEvent event) {
        for (ComplexQuest q : QuestManager.getInstance().getQuests(ComplexQuest.class)) {
            q.onQuestDeleteEvent(event);
        }
    }
    
    @EventHandler
    public void onQuestWouldBeDeletedEvent(QuestWouldBeDeletedEvent event) {
        for (ComplexQuest q : QuestManager.getInstance().getQuests(ComplexQuest.class)) {
            q.onQuestWouldBeDeletedEvent(event);
        }
    }
    
    @EventHandler
    public void onInteractorDamagedEvent(InteractorDamagedEvent<?> event) {
        if (event.getInteractor() instanceof EntityInteractor) {
            if (((EntityInteractor) event.getInteractor()).getEntity() instanceof Player) {
                return;
            }
        }
        Set<InteractorProtecting> protecting =
                CubeQuest.getInstance().getProtectedBy(event.getInteractor());
        for (InteractorProtecting prot : protecting) {
            if (prot.onInteractorDamagedEvent(event)) {
                interactorDamagingCancelled(prot, event);
                return;
            }
        }
    }
    
    @SuppressWarnings({"null", "unlikely-arg-type"})
    private void interactorDamagingCancelled(InteractorProtecting cancelledBy,
            InteractorDamagedEvent<?> event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        
        boolean isQuest = cancelledBy instanceof Quest;
        Quest q = isQuest ? (Quest) cancelledBy : null;
        
        boolean isReceiver = !isQuest && (cancelledBy instanceof DeliveryReceiverSpecification);
        DeliveryReceiverSpecification r =
                isReceiver ? (DeliveryReceiverSpecification) cancelledBy : null;
        
        boolean isGiver = !isQuest && !isReceiver && (cancelledBy instanceof QuestGiver);
        QuestGiver g = isGiver ? (QuestGiver) cancelledBy : null;
        
        if ((isGiver && !player.hasPermission(CubeQuest.EDIT_QUEST_GIVERS_PERMISSION))
                || (isQuest && !player.hasPermission(CubeQuest.EDIT_QUESTS_PERMISSION))
                || ((isReceiver || !isGiver && !isQuest)
                        && !player.hasPermission(CubeQuest.EDIT_QUEST_SPECIFICATIONS_PERMISSION))) {
            ChatAndTextUtil.sendErrorMessage(player, event.getNoPermissionMessage());
            return;
        }
        
        String prefix;
        int index;
        boolean warning;
        if (isQuest) {
            prefix = "Dieser Interactor ist Teil von Quest ";
            index = -1;
            warning = false;
        } else if (isReceiver) {
            prefix = "Dieser Interactor ist Teil folgender DeliveryReceiverSpecification ";
            index = (new ArrayList<>(
                    DeliveryQuestSpecification.DeliveryQuestPossibilitiesSpecification.getInstance()
                            .getTargets())).indexOf(r);
            warning = false;
        } else if (isGiver) {
            prefix = "Dieser Interactor ist QuestGiver ";
            index = -1;
            warning = false;
        } else if (cancelledBy instanceof QuestSpecification) {
            prefix = "Dieser Interactor ist Teil von QuestSpecification ";
            index = QuestGenerator.getInstance().getPossibleQuestsIncludingNulls()
                    .indexOf(cancelledBy);
            warning = false;
        } else {
            prefix = "Dieser Interactor ist Teil des Quest-Systems.";
            index = -1;
            warning = true;
        }
        
        HoverEvent he = isGiver ? null
                : new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder(isQuest ? "Info zu " + q.toString() + " anzeigen"
                                : ("QuestSpecifications auflisten")).create());
        ClickEvent ce = isGiver ? null
                : new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        isQuest ? "/quest info " + q.getId()
                                : isReceiver
                                        ? ("/quest listDeliveryQuestReceiverSpecifications "
                                                + Math.max(0,
                                                        ((index / ChatAndTextUtil.PAGE_LENGTH)
                                                                + 1)))
                                        : ("/quest listQuestSpecifications " + Math.max(0,
                                                ((index / ChatAndTextUtil.PAGE_LENGTH) + 1))));
        
        ComponentBuilder builder = new ComponentBuilder(prefix).color(ChatColor.GOLD);
        
        if (warning) {
            CubeQuest.getInstance().getLogger().log(Level.WARNING,
                    "Unknown InteractorProtector: " + cancelledBy.getClass().getName());
        } else {
            builder.append((isQuest ? q.getId() + " "
                    : isReceiver ? "" : isGiver ? g.getName() + " " : (index + 1 + " ")));
            if (!isGiver) {
                builder.event(he).event(ce);
            }
            
            builder.append("und kann nicht zerstört werden" + (isReceiver ? ": " : ".")).reset()
                    .color(ChatColor.GOLD);
            
            if (isReceiver) {
                builder.append(r.getSpecificationInfo());
            }
        }
        
        ChatAndTextUtil.sendBaseComponent(player, builder.create());
    }
    
}
