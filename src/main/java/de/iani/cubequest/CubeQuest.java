package de.iani.cubequest;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.iani.cubequest.bubbles.InteractorBubbleMaker;
import de.iani.cubequest.commands.AcceptQuestCommand;
import de.iani.cubequest.commands.AddGotoQuestSpecificationCommand;
import de.iani.cubequest.commands.AddOrRemoveEntityTypeCombinationForSpecificationCommand;
import de.iani.cubequest.commands.AddOrRemoveEntityTypeCombinationForSpecificationCommand.EntityTypeCombinationRequiredFor;
import de.iani.cubequest.commands.AddOrRemoveEntityTypeCommand;
import de.iani.cubequest.commands.AddOrRemoveInteractorForSpecificationCommand;
import de.iani.cubequest.commands.AddOrRemoveInteractorForSpecificationCommand.InteractorRequiredFor;
import de.iani.cubequest.commands.AddOrRemoveMaterialCombinationForSpecificationCommand;
import de.iani.cubequest.commands.AddOrRemoveMaterialCombinationForSpecificationCommand.MaterialCombinationRequiredFor;
import de.iani.cubequest.commands.AddOrRemoveMaterialCommand;
import de.iani.cubequest.commands.AddOrRemoveSubQuestCommand;
import de.iani.cubequest.commands.AddQuestGiverCommand;
import de.iani.cubequest.commands.ClearEntityTypesCommand;
import de.iani.cubequest.commands.ClearMaterialsCommand;
import de.iani.cubequest.commands.ClearSubQuestsCommand;
import de.iani.cubequest.commands.CommandRouter;
import de.iani.cubequest.commands.ConfirmQuestInteractionCommand;
import de.iani.cubequest.commands.ConsolidateQuestSpecificationsCommand;
import de.iani.cubequest.commands.CreateQuestCommand;
import de.iani.cubequest.commands.DeleteQuestCommand;
import de.iani.cubequest.commands.EditQuestCommand;
import de.iani.cubequest.commands.GiveOrRemoveQuestForPlayerCommand;
import de.iani.cubequest.commands.ListQuestSpecificationsCommand;
import de.iani.cubequest.commands.ModifyQuestGiverCommand;
import de.iani.cubequest.commands.ModifyQuestGiverCommand.QuestGiverModification;
import de.iani.cubequest.commands.QuestInfoCommand;
import de.iani.cubequest.commands.RemoveQuestSpecificationCommand;
import de.iani.cubequest.commands.SetAllowRetryCommand;
import de.iani.cubequest.commands.SetComplexQuestStructureCommand;
import de.iani.cubequest.commands.SetDeliveryInventoryCommand;
import de.iani.cubequest.commands.SetGotoLocationCommand;
import de.iani.cubequest.commands.SetOnDeleteCascadeCommand;
import de.iani.cubequest.commands.SetOrRemoveFailiureQuestCommand;
import de.iani.cubequest.commands.SetOrRemoveFollowupQuestCommand;
import de.iani.cubequest.commands.SetQuestAmountCommand;
import de.iani.cubequest.commands.SetQuestDateOrTimeCommand;
import de.iani.cubequest.commands.SetQuestDisplayMessageCommand;
import de.iani.cubequest.commands.SetQuestInteractorCommand;
import de.iani.cubequest.commands.SetQuestMessageCommand;
import de.iani.cubequest.commands.SetQuestMessageCommand.MessageTrigger;
import de.iani.cubequest.commands.SetQuestNameCommand;
import de.iani.cubequest.commands.SetQuestRegexCommand;
import de.iani.cubequest.commands.SetQuestVisibilityCommand;
import de.iani.cubequest.commands.SetRewardIntCommand;
import de.iani.cubequest.commands.SetRewardIntCommand.Attribute;
import de.iani.cubequest.commands.SetRewardItemsCommand;
import de.iani.cubequest.commands.ShowPlayerQuestsCommand;
import de.iani.cubequest.commands.ShowQuestGiveMessageCommand;
import de.iani.cubequest.commands.StopEditingQuestCommand;
import de.iani.cubequest.commands.ToggleGenerateDailyQuestsCommand;
import de.iani.cubequest.commands.TogglePayRewardsCommand;
import de.iani.cubequest.commands.ToggleReadyStatusCommand;
import de.iani.cubequest.generation.BlockBreakQuestSpecification;
import de.iani.cubequest.generation.BlockPlaceQuestSpecification;
import de.iani.cubequest.generation.ClickInteractorQuestSpecification;
import de.iani.cubequest.generation.DeliveryQuestSpecification;
import de.iani.cubequest.generation.EntityTypeCombination;
import de.iani.cubequest.generation.GotoQuestSpecification;
import de.iani.cubequest.generation.KillEntitiesQuestSpecification;
import de.iani.cubequest.generation.MaterialCombination;
import de.iani.cubequest.generation.QuestGenerator;
import de.iani.cubequest.interaction.EntityInteractor;
import de.iani.cubequest.interaction.Interactor;
import de.iani.cubequest.interaction.InteractorCreator;
import de.iani.cubequest.interaction.NPCInteractor;
import de.iani.cubequest.questGiving.QuestGiver;
import de.iani.cubequest.questStates.QuestStateCreator;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.quests.QuestCreator;
import de.iani.cubequest.quests.WaitForDateQuest;
import de.iani.cubequest.sql.DatabaseFassade;
import de.iani.cubequest.sql.util.SQLConfig;
import de.iani.treasurechest.TreasureChest;
import de.iani.treasurechest.TreasureChestAPI;
import de.speedy64.globalchat.api.GlobalChatAPI;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class CubeQuest extends JavaPlugin {
    
    public static final String PLUGIN_TAG = ChatColor.BLUE + "[CubeQuest]";
    
    public static final String ACCEPT_QUESTS_PERMISSION = "cubequest.use";
    public static final String SEE_PLAYER_INFO_PERMISSION = "cubequest.admin";
    public static final String EDIT_QUESTS_PERMISSION = "cubequest.admin";
    public static final String EDIT_QUEST_STATES_PERMISSION = "cubequest.admin";
    public static final String EDIT_QUEST_GIVERS_PERMISSION = "cubequest.admin";
    public static final String EDIT_QUEST_SPECIFICATIONS_PERMISSION = "cubequest.admin";
    public static final String TOGGLE_SERVER_PROPERTIES_PERMISSION = "cubequest.admin";
    public static final String SEE_EXCEPTIONS_PERMISSION = "cubequest.admin";
    
    private static CubeQuest instance = null;
    
    private CommandRouter commandExecutor;
    private QuestCreator questCreator;
    private QuestStateCreator questStateCreator;
    private InteractorCreator interactorCreator;
    private QuestEditor questEditor;
    private QuestGenerator questGenerator;
    private EventListener eventListener;
    private InteractionConfirmationHandler interactionConfirmationHandler;
    private SQLConfig sqlConfig;
    private DatabaseFassade dbf;
    
    private boolean hasCitizens;
    private NPCRegistry npcReg;
    
    private int serverId;
    private String serverName;
    private boolean generateDailyQuests;
    private boolean payRewards;
    
    private GlobalChatAPI globalChatAPI;
    private ArrayList<Runnable> waitingForPlayer;
    private Integer tickTask;
    private long tick = 0;
    private Timer daemonTimer;
    private InteractorBubbleMaker bubbleMaker;
    
    private HashMap<UUID, PlayerData> playerData;
    
    private Map<String, QuestGiver> questGivers;
    private Map<Interactor, QuestGiver> questGiversByInteractor;
    private Set<QuestGiver> dailyQuestGivers;
    
    private List<String> storedMessages;
    
    public static CubeQuest getInstance() {
        return instance;
    }
    
    public CubeQuest() {
        if (instance != null) {
            throw new IllegalStateException("there already is an instance!");
        }
        instance = this;
        
        this.playerData = new HashMap<>();
        this.questGivers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        this.questGiversByInteractor = new HashMap<>();
        this.dailyQuestGivers = new HashSet<>();
        this.questCreator = new QuestCreator();
        this.questStateCreator = new QuestStateCreator();
        this.interactorCreator = new InteractorCreator();
        this.questEditor = new QuestEditor();
        this.interactionConfirmationHandler = new InteractionConfirmationHandler();
        this.waitingForPlayer = new ArrayList<>();
        this.bubbleMaker = new InteractorBubbleMaker();
        this.storedMessages = new ArrayList<>();
        
        
        this.daemonTimer = new Timer("CubeQuest-Timer", true);
    }
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        ConfigurationSerialization.registerClass(Reward.class);
        ConfigurationSerialization.registerClass(QuestGiver.class);
        ConfigurationSerialization.registerClass(Quest.class);
        
        ConfigurationSerialization.registerClass(NPCInteractor.class);
        ConfigurationSerialization.registerClass(EntityInteractor.class);
        
        ConfigurationSerialization.registerClass(QuestGenerator.class);
        ConfigurationSerialization.registerClass(QuestGenerator.DailyQuestData.class);
        ConfigurationSerialization.registerClass(MaterialCombination.class);
        ConfigurationSerialization.registerClass(EntityTypeCombination.class);
        ConfigurationSerialization.registerClass(GotoQuestSpecification.class);
        ConfigurationSerialization.registerClass(ClickInteractorQuestSpecification.class);
        ConfigurationSerialization.registerClass(
                DeliveryQuestSpecification.DeliveryQuestPossibilitiesSpecification.class);
        ConfigurationSerialization
                .registerClass(DeliveryQuestSpecification.DeliveryReceiverSpecification.class);
        ConfigurationSerialization.registerClass(
                BlockBreakQuestSpecification.BlockBreakQuestPossibilitiesSpecification.class);
        ConfigurationSerialization.registerClass(
                BlockPlaceQuestSpecification.BlockPlaceQuestPossibilitiesSpecification.class);
        ConfigurationSerialization.registerClass(
                KillEntitiesQuestSpecification.KillEntitiesQuestPossibilitiesSpecification.class);
        
        this.sqlConfig = new SQLConfig(getConfig().getConfigurationSection("database"));
        this.dbf = new DatabaseFassade();
        if (!this.dbf.reconnect()) {
            return;
        }
        
        this.generateDailyQuests = getConfig().getBoolean("generateDailyQuests");
        this.payRewards = getConfig().getBoolean("payRewards");
        
        this.hasCitizens = Bukkit.getPluginManager().getPlugin("Citizens") != null;
        
        this.eventListener = new EventListener(this);
        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        Bukkit.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord",
                this.eventListener);
        
        this.commandExecutor = new CommandRouter(getCommand("quest"));
        this.commandExecutor.addCommandMapping(new QuestInfoCommand(), "questInfo");
        this.commandExecutor.addAlias("info", "questInfo");
        this.commandExecutor.addCommandMapping(new ShowPlayerQuestsCommand(), "showQuests");
        this.commandExecutor.addAlias("show", "showQuests");
        this.commandExecutor.addCommandMapping(new ShowQuestGiveMessageCommand(),
                "showGiveMessage");
        this.commandExecutor.addCommandMapping(new AcceptQuestCommand(), "acceptQuest");
        this.commandExecutor.addCommandMapping(new ConfirmQuestInteractionCommand(),
                "confirmQuestInteraction");
        this.commandExecutor.addCommandMapping(new GiveOrRemoveQuestForPlayerCommand(true),
                "giveToPlayer");
        this.commandExecutor.addCommandMapping(new GiveOrRemoveQuestForPlayerCommand(false),
                "removeFromPlayer");
        this.commandExecutor.addCommandMapping(new CreateQuestCommand(), "create");
        this.commandExecutor.addCommandMapping(new DeleteQuestCommand(), "delete");
        this.commandExecutor.addCommandMapping(new EditQuestCommand(), "edit");
        this.commandExecutor.addCommandMapping(new StopEditingQuestCommand(), "edit", "stop");
        this.commandExecutor.addCommandMapping(new ToggleReadyStatusCommand(), "setReady");
        this.commandExecutor.addCommandMapping(new SetQuestNameCommand(), "setName");
        this.commandExecutor.addCommandMapping(new SetQuestDisplayMessageCommand(),
                "setDisplayMessage");
        this.commandExecutor.addCommandMapping(new SetQuestMessageCommand(MessageTrigger.GIVE),
                "setGiveMessage");
        this.commandExecutor.addCommandMapping(new SetQuestMessageCommand(MessageTrigger.SUCCESS),
                "setSuccessMessage");
        this.commandExecutor.addCommandMapping(new SetQuestMessageCommand(MessageTrigger.FAIL),
                "setFailMessage");
        this.commandExecutor.addCommandMapping(new SetRewardItemsCommand(true),
                "setSuccessRewardItems");
        this.commandExecutor.addCommandMapping(new SetRewardItemsCommand(false),
                "setFailRewardItems");
        this.commandExecutor.addCommandMapping(new SetRewardIntCommand(true, Attribute.CUBES),
                "setSuccessRewardCubes");
        this.commandExecutor.addCommandMapping(new SetRewardIntCommand(false, Attribute.CUBES),
                "setFailRewardCubes");
        this.commandExecutor.addCommandMapping(
                new SetRewardIntCommand(true, Attribute.QUEST_POINTS),
                "setSuccessRewardQuestPoints");
        this.commandExecutor.addCommandMapping(
                new SetRewardIntCommand(false, Attribute.QUEST_POINTS), "setFailRewardQuestPoints");
        this.commandExecutor.addCommandMapping(new SetRewardIntCommand(true, Attribute.XP),
                "setSuccessRewardXP");
        this.commandExecutor.addCommandMapping(new SetRewardIntCommand(false, Attribute.XP),
                "setFailRewardXP");
        this.commandExecutor.addCommandMapping(new SetAllowRetryCommand(true),
                "setAllowRetryOnSuccess");
        this.commandExecutor.addCommandMapping(new SetAllowRetryCommand(false),
                "setAllowRetryOnFail");
        this.commandExecutor.addCommandMapping(new SetQuestVisibilityCommand(), "setVisibility");
        this.commandExecutor.addCommandMapping(new SetComplexQuestStructureCommand(),
                "setQuestStructure");
        this.commandExecutor.addCommandMapping(new AddOrRemoveSubQuestCommand(true), "addSubQuest");
        this.commandExecutor.addCommandMapping(new AddOrRemoveSubQuestCommand(false),
                "removeSubQuest");
        this.commandExecutor.addCommandMapping(new SetOrRemoveFailiureQuestCommand(true),
                "setFailiureQuest");
        this.commandExecutor.addCommandMapping(new SetOrRemoveFailiureQuestCommand(false),
                "removeFailiureQuest");
        this.commandExecutor.addCommandMapping(new SetOrRemoveFollowupQuestCommand(true),
                "setFollowupQuest");
        this.commandExecutor.addCommandMapping(new SetOrRemoveFollowupQuestCommand(false),
                "removeFollowupQuest");
        this.commandExecutor.addCommandMapping(new ClearSubQuestsCommand(), "clearSubQuests");
        this.commandExecutor.addCommandMapping(new SetOnDeleteCascadeCommand(),
                "setOnDeleteCascade");
        this.commandExecutor.addCommandMapping(new SetQuestAmountCommand(), "setAmount");
        this.commandExecutor.addCommandMapping(new AddOrRemoveMaterialCommand(true), "addMaterial");
        this.commandExecutor.addCommandMapping(new AddOrRemoveMaterialCommand(false),
                "removeMaterial");
        this.commandExecutor.addCommandMapping(new ClearMaterialsCommand(), "clearMaterials");
        this.commandExecutor.addCommandMapping(new AddOrRemoveEntityTypeCommand(true),
                "addEntityType");
        this.commandExecutor.addCommandMapping(new AddOrRemoveEntityTypeCommand(false),
                "removeEntityType");
        this.commandExecutor.addCommandMapping(new ClearEntityTypesCommand(), "clearEntityTypes");
        this.commandExecutor.addCommandMapping(new SetGotoLocationCommand(), "setGotoLocation");
        this.commandExecutor.addCommandMapping(new SetQuestInteractorCommand(), "setInteractor");
        this.commandExecutor.addCommandMapping(new SetDeliveryInventoryCommand(), "setDelivery");
        this.commandExecutor.addCommandMapping(new SetQuestRegexCommand(true), "setLiteralMatch");
        this.commandExecutor.addCommandMapping(new SetQuestDateOrTimeCommand(true), "setQuestDate");
        this.commandExecutor.addCommandMapping(new SetQuestDateOrTimeCommand(false),
                "setQuestTime");
        this.commandExecutor.addCommandMapping(new SetQuestRegexCommand(false), "setRegex");
        this.commandExecutor.addCommandMapping(new ListQuestSpecificationsCommand(),
                "listQuestSpecifications");
        this.commandExecutor.addCommandMapping(new RemoveQuestSpecificationCommand(),
                "removeQuestSpecification");
        this.commandExecutor.addCommandMapping(new ConsolidateQuestSpecificationsCommand(),
                "consolidateQuestSpecifications");
        this.commandExecutor.addCommandMapping(new AddGotoQuestSpecificationCommand(),
                "addGotoQuestSpecification");
        for (InteractorRequiredFor requiredFor: InteractorRequiredFor.values()) {
            this.commandExecutor.addCommandMapping(
                    new AddOrRemoveInteractorForSpecificationCommand(requiredFor),
                    requiredFor.command);
        }
        for (MaterialCombinationRequiredFor requiredFor: MaterialCombinationRequiredFor.values()) {
            this.commandExecutor.addCommandMapping(
                    new AddOrRemoveMaterialCombinationForSpecificationCommand(true, requiredFor),
                    "add" + requiredFor.command);
            this.commandExecutor.addCommandMapping(
                    new AddOrRemoveMaterialCombinationForSpecificationCommand(false, requiredFor),
                    "remove" + requiredFor.command);
        }
        for (EntityTypeCombinationRequiredFor requiredFor: EntityTypeCombinationRequiredFor
                .values()) {
            this.commandExecutor.addCommandMapping(
                    new AddOrRemoveEntityTypeCombinationForSpecificationCommand(true, requiredFor),
                    "add" + requiredFor.command);
            this.commandExecutor.addCommandMapping(
                    new AddOrRemoveEntityTypeCombinationForSpecificationCommand(false, requiredFor),
                    "remove" + requiredFor.command);
        }
        this.commandExecutor.addCommandMapping(new TogglePayRewardsCommand(), "setPayRewards");
        this.commandExecutor.addCommandMapping(new ToggleGenerateDailyQuestsCommand(),
                "setGenerateDailyQuests");
        this.commandExecutor.addCommandMapping(new AddQuestGiverCommand(), "addQuestGiver");
        for (QuestGiverModification m: QuestGiverModification.values()) {
            this.commandExecutor.addCommandMapping(new ModifyQuestGiverCommand(m), m.command);
        }
        
        this.globalChatAPI = (GlobalChatAPI) Bukkit.getPluginManager().getPlugin("GlobalChat");
        loadServerIdAndName();
        
        if (this.hasCitizens) {
            loadCitizensAPI();
        }
        loadQuests();
        
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            questDependentSetup();
        }, 1L);
        
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            this.bubbleMaker.updateTargets();
        }, 2L);
        
        this.tickTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            tick();
        }, 3L, 1L);
    }
    
    public boolean stillInSetup() {
        return this.tick < 1;
    }
    
    public boolean hasCitizensPlugin() {
        return this.hasCitizens;
    }
    
    private void loadCitizensAPI() {
        loadNPCs();
    }
    
    private void loadNPCs() {
        this.npcReg = CitizensAPI.getNPCRegistry();
    }
    
    private void loadServerIdAndName() {
        if (getConfig().contains("serverId")) {
            this.serverId = getConfig().getInt("serverId");
        } else {
            try {
                this.serverId = this.dbf.addServerId();
                
                getConfig().set("serverId", this.serverId);
                saveConfig();
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Could not create serverId!", e);
            }
        }
        if (getConfig().contains("serverName")) {
            this.serverName = getConfig().getString("serverName");
        } else {
            this.waitingForPlayer.add(() -> {
                Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF("GetServer");
                    Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
                    player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
                }, 1L);
            });
        }
    }
    
    private void loadQuests() {
        this.questCreator.loadQuests();
    }
    
    @SuppressWarnings("unchecked")
    private void questDependentSetup() {
        for (Quest q: QuestManager.getInstance().getQuests()) {
            if (q instanceof WaitForDateQuest && q.isReady()) { // ready impliziert !done
                ((WaitForDateQuest) q).checkTime();
            }
        }
        
        File questGiverFolder = new File(getDataFolder(), "questGivers");
        if (questGiverFolder.exists()) {
            for (String name: questGiverFolder.list()) {
                if (!name.endsWith(".yml")) {
                    continue;
                }
                YamlConfiguration config =
                        YamlConfiguration.loadConfiguration(new File(questGiverFolder, name));
                QuestGiver giver = (QuestGiver) config.get("giver");
                this.questGivers.put(giver.getName(), giver);
                this.questGiversByInteractor.put(giver.getInteractor(), giver);
            }
        }
        
        List<String> dailyQuestGiverNames = (List<String>) getConfig().get("dailyQuestGivers");
        if (dailyQuestGiverNames != null) {
            for (String name: dailyQuestGiverNames) {
                QuestGiver giver = this.questGivers.get(name);
                if (giver != null) {
                    this.dailyQuestGivers.add(giver);
                }
            }
        }
        
        this.questGenerator = QuestGenerator.getInstance();
    }
    
    @Override
    public void onDisable() {
        this.daemonTimer.cancel();
        if (this.tickTask != null && (Bukkit.getScheduler().isQueued(this.tickTask)
                || Bukkit.getScheduler().isCurrentlyRunning(this.tickTask))) {
            Bukkit.getScheduler().cancelTask(this.tickTask);
        }
    }
    
    private void tick() {
        this.tick++;
        
        this.bubbleMaker.tick(this.tick);
        if (this.generateDailyQuests && (this.questGenerator.getLastGeneratedForDay() == null
                || LocalDate.now().isAfter(this.questGenerator.getLastGeneratedForDay()))) {
            this.questGenerator.generateDailyQuests();
        }
    }
    
    public QuestManager getQuestManager() {
        return QuestManager.getInstance();
    }
    
    public CommandRouter getCommandExecutor() {
        return this.commandExecutor;
    }
    
    public QuestCreator getQuestCreator() {
        return this.questCreator;
    }
    
    public QuestStateCreator getQuestStateCreator() {
        return this.questStateCreator;
    }
    
    public InteractorCreator getInteractorCreator() {
        return this.interactorCreator;
    }
    
    public QuestEditor getQuestEditor() {
        return this.questEditor;
    }
    
    public QuestGenerator getQuestGenerator() {
        return this.questGenerator;
    }
    
    public EventListener getEventListener() {
        return this.eventListener;
    }
    
    public InteractionConfirmationHandler getInteractionConfirmationHandler() {
        return this.interactionConfirmationHandler;
    }
    
    public InteractorBubbleMaker getBubbleMaker() {
        return this.bubbleMaker;
    }
    
    public GlobalChatAPI getGlobalChatAPI() {
        return this.globalChatAPI;
    }
    
    public NPCRegistry getNPCReg() {
        if (!hasCitizensPlugin()) {
            return null;
        }
        return this.npcReg;
    }
    
    public boolean isGeneratingDailyQuests() {
        return this.generateDailyQuests;
    }
    
    public void setGenerateDailyQuests(boolean val) {
        this.generateDailyQuests = val;
        getConfig().set("generateDailyQuests", val);
        if (!val) {
            this.dailyQuestGivers.clear();
            saveDailyQuestGivers();
        } else {
            saveConfig();
        }
    }
    
    public boolean isPayRewards() {
        return this.payRewards;
    }
    
    public void setPayRewards(boolean val) {
        this.payRewards = val;
        getConfig().set("payRewards", val);
        saveConfig();
    }
    
    public int getServerId() {
        return this.serverId;
    }
    
    public String getBungeeServerName() {
        return this.serverName;
    }
    
    public void setBungeeServerName(String val) {
        this.serverName = val;
        try {
            this.dbf.setServerName();
            
            getConfig().set("serverName", this.serverName);
            getDataFolder().mkdirs();
            File configFile = new File(getDataFolder(), "config.yml");
            configFile.createNewFile();
            getConfig().save(configFile);
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Could not set servername!", e);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Could not save config!", e);
        }
    }
    
    public String[] getOtherBungeeServers() {
        try {
            return this.dbf.getOtherBungeeServerNames();
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Could not get servernames!", e);
            return new String[0];
        }
    }
    
    public boolean isWaitingForPlayer() {
        return !this.waitingForPlayer.isEmpty();
    }
    
    public void addWaitingForPlayer(Runnable r) {
        if (Bukkit.getServer().getOnlinePlayers().isEmpty()) {
            this.waitingForPlayer.add(r);
        } else {
            r.run();
        }
    }
    
    public void playerArrived() {
        Iterator<Runnable> it = this.waitingForPlayer.iterator();
        while (it.hasNext()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, it.next(), 1L);
            it.remove();
        }
    }
    
    public Timer getTimer() {
        return this.daemonTimer;
    }
    
    public DatabaseFassade getDatabaseFassade() {
        return this.dbf;
    }
    
    public SQLConfig getSQLConfigData() {
        return this.sqlConfig;
    }
    
    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }
    
    public PlayerData getPlayerData(UUID id) {
        if (id == null) {
            throw new NullPointerException();
        }
        PlayerData pd = this.playerData.get(id);
        if (pd == null) {
            pd = new PlayerData(id);
            this.playerData.put(id, pd);
        }
        return pd;
    }
    
    public void unloadPlayerData(UUID id) {
        this.playerData.remove(id);
    }
    
    public QuestGiver getQuestGiver(String name) {
        return this.questGivers.get(name);
    }
    
    public QuestGiver getQuestGiver(Interactor interactor) {
        return this.questGiversByInteractor.get(interactor);
    }
    
    public void addQuestGiver(QuestGiver giver) {
        if (this.questGivers.get(giver.getName()) != null) {
            throw new IllegalArgumentException("already has a QuestGiver with that name");
        }
        if (this.questGiversByInteractor.get(giver.getInteractor()) != null) {
            throw new IllegalArgumentException("already has a QuestGiver with that interactor");
        }
        
        this.questGivers.put(giver.getName(), giver);
        this.questGiversByInteractor.put(giver.getInteractor(), giver);
        
        this.bubbleMaker.updateTargets();
    }
    
    public boolean removeQuestGiver(String name) {
        QuestGiver giver = this.questGivers.get(name);
        if (giver == null) {
            return false;
        }
        
        removeQuestGiver(giver);
        return true;
    }
    
    public boolean removeQuestGiver(Interactor interactor) {
        QuestGiver giver = this.questGiversByInteractor.get(interactor);
        if (giver == null) {
            return false;
        }
        
        removeQuestGiver(giver);
        return true;
    }
    
    private void removeQuestGiver(QuestGiver giver) {
        this.questGivers.remove(giver.getName());
        this.questGiversByInteractor.remove(giver.getInteractor());
        this.dailyQuestGivers.remove(giver);
        
        File folder = new File(CubeQuest.getInstance().getDataFolder(), "questGivers");
        File configFile = new File(folder, giver.getName() + ".yml");
        if (!configFile.delete()) {
            getLogger().log(Level.WARNING,
                    "Could not delete config \"" + giver.getName() + ".yml\" for QuestGiver.");
        }
        
        this.bubbleMaker.updateTargets();
    }
    
    public Collection<QuestGiver> getQuestGivers() {
        return Collections.unmodifiableCollection(this.questGivers.values());
    }
    
    public Set<QuestGiver> getDailyQuestGivers() {
        return Collections.unmodifiableSet(this.dailyQuestGivers);
    }
    
    public boolean addDailyQuestGiver(String name) {
        QuestGiver giver = this.questGivers.get(name);
        if (giver == null) {
            throw new IllegalArgumentException("no QuestGiver with that name");
        }
        
        return addDailyQuestGiver(giver);
    }
    
    public boolean addDailyQuestGiver(Interactor interactor) {
        QuestGiver giver = this.questGiversByInteractor.get(interactor);
        if (giver == null) {
            throw new IllegalArgumentException("no QuestGiver with that interactor");
        }
        
        return addDailyQuestGiver(giver);
    }
    
    private boolean addDailyQuestGiver(QuestGiver giver) {
        if (this.dailyQuestGivers.add(giver)) {
            if (LocalDate.now().equals(this.questGenerator.getLastGeneratedForDay())) {
                Quest[] generated = this.questGenerator.getTodaysDailyQuests();
                if (generated != null) {
                    for (Quest q: generated) {
                        giver.addQuest(q);
                    }
                }
            }
            saveDailyQuestGivers();
            return true;
        }
        return false;
    }
    
    public boolean removeDailyQuestGiver(String name) {
        QuestGiver giver = this.questGivers.get(name);
        if (giver == null) {
            throw new IllegalArgumentException("no QuestGiver with that name");
        }
        
        return removeDailyQuestGiver(giver);
    }
    
    public boolean removeDailyQuestGiver(Interactor interactor) {
        QuestGiver giver = this.questGiversByInteractor.get(interactor);
        if (giver == null) {
            throw new IllegalArgumentException("no QuestGiver with that interactor");
        }
        
        return removeDailyQuestGiver(giver);
    }
    
    private boolean removeDailyQuestGiver(QuestGiver giver) {
        if (this.dailyQuestGivers.remove(giver)) {
            saveDailyQuestGivers();
            return true;
        }
        return false;
    }
    
    private void saveDailyQuestGivers() {
        List<String> dailyQuestGiverNames = new ArrayList<>();
        this.dailyQuestGivers.forEach(qg -> dailyQuestGiverNames.add(qg.getName()));
        getConfig().set("dailyQuestGivers", dailyQuestGiverNames);
        saveConfig();
    }
    
    public boolean hasTreasureChest() {
        return Bukkit.getPluginManager().getPlugin("TreasureChest") != null;
    }
    
    public boolean addTreasureChest(Player player, Reward reward) {
        return addToTreasureChest(player.getUniqueId(), reward);
    }
    
    public boolean addToTreasureChest(UUID playerId, Reward reward) {
        if (!hasTreasureChest()) {
            return false;
        }
        
        addToTreasureChestInternal(playerId, reward);
        return true;
    }
    
    private void addToTreasureChestInternal(UUID playerId, Reward reward) {
        ItemStack display = new ItemStack(Material.BOOK);
        display.addUnsafeEnchantment(Enchantment.LOOT_BONUS_BLOCKS, 1);
        ItemMeta meta = display.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Quest-Belohnung");
        display.setItemMeta(meta);
        
        TreasureChestAPI tcAPI = JavaPlugin.getPlugin(TreasureChest.class);
        tcAPI.addItem(Bukkit.getOfflinePlayer(playerId), display, reward.getItems(),
                reward.getCubes());
    }
    
    public void payCubes(Player player, int cubes) {
        payCubes(player.getUniqueId(), cubes);
    }
    
    public void payCubes(UUID playerId, int cubes) {
        RegisteredServiceProvider<Economy> rsp =
                getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().log(Level.SEVERE, "Could not find Economy! Hence, could not pay " + cubes
                    + " cubes to player " + playerId.toString());
            return;
        }
        EconomyResponse response =
                rsp.getProvider().depositPlayer(Bukkit.getOfflinePlayer(playerId), cubes);
        if (!response.transactionSuccess()) {
            getLogger().log(Level.SEVERE,
                    "Could not pay " + cubes + " cubes to player " + playerId.toString()
                            + " (EconomyResponse not successfull: " + response.errorMessage + ")");
        }
    }
    
    public void addQuestPoints(Player player, int questPoints) {
        addQuestPoints(player.getUniqueId(), questPoints);
    }
    
    public void addQuestPoints(UUID playerId, int questPoints) {
        this.getPlayerData(playerId).changeQuestPoints(questPoints);
    }
    
    public void addXp(Player player, int xp) {
        addQuestPoints(player.getUniqueId(), xp);
    }
    
    public void addXp(UUID playerId, int xp) {
        this.getPlayerData(playerId).changeXP(xp);
    }
    
    public void addStoredMessage(String msg) {
        this.storedMessages.add(msg);
    }
    
    public String[] popStoredMessages() {
        String[] res = new String[this.storedMessages.size()];
        res = this.storedMessages.toArray(res);
        this.storedMessages.clear();
        return res;
    }
    
}
