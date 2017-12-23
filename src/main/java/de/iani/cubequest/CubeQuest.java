package de.iani.cubequest;

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

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

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
import de.iani.cubequest.commands.ConsolidateQuestSpecificationsCommand;
import de.iani.cubequest.commands.CreateQuestCommand;
import de.iani.cubequest.commands.EditQuestCommand;
import de.iani.cubequest.commands.GiveOrRemoveQuestForPlayerCommand;
import de.iani.cubequest.commands.ListQuestSpecificationsCommand;
import de.iani.cubequest.commands.ModifyQuestGiverCommand;
import de.iani.cubequest.commands.ModifyQuestGiverCommand.QuestGiverModification;
import de.iani.cubequest.commands.QuestInfoCommand;
import de.iani.cubequest.commands.RemoveQuestSpecificationCommand;
import de.iani.cubequest.commands.SetComplexQuestStructureCommand;
import de.iani.cubequest.commands.SetDeliveryInventoryCommand;
import de.iani.cubequest.commands.SetGotoLocationCommand;
import de.iani.cubequest.commands.SetOrRemoveFailiureQuestCommand;
import de.iani.cubequest.commands.SetOrRemoveFollowupQuestCommand;
import de.iani.cubequest.commands.SetQuestAmountCommand;
import de.iani.cubequest.commands.SetQuestDateOrTimeCommand;
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
import de.iani.cubequest.interaction.InteractorCreator;
import de.iani.cubequest.interaction.NPCInteractor;
import de.iani.cubequest.questGiving.QuestGiver;
import de.iani.cubequest.questStates.QuestStateCreator;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.quests.QuestCreator;
import de.iani.cubequest.quests.WaitForDateQuest;
import de.iani.cubequest.sql.DatabaseFassade;
import de.iani.cubequest.sql.util.SQLConfig;
import de.iani.cubequest.wrapper.NPCEventListener;
import de.iani.treasurechest.TreasureChest;
import de.iani.treasurechest.TreasureChestAPI;
import de.speedy64.globalchat.api.GlobalChatAPI;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

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

    private HashMap<UUID, PlayerData> playerData;

    private Map<String, QuestGiver> questGivers;
    private Map<Integer, QuestGiver> questGiversByNPCId;
    private Set<QuestGiver> dailyQuestGivers;

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
        this.questGiversByNPCId = new HashMap<>();
        this.dailyQuestGivers = new HashSet<>();
        this.questCreator = new QuestCreator();
        this.questStateCreator = new QuestStateCreator();
        this.interactorCreator = new InteractorCreator();
        this.questEditor = new QuestEditor();
        this.waitingForPlayer = new ArrayList<>();

        this.daemonTimer = new Timer("CubeQuest-Timer", true);
    }

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
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
        ConfigurationSerialization.registerClass(DeliveryQuestSpecification.DeliveryQuestPossibilitiesSpecification.class);
        ConfigurationSerialization.registerClass(DeliveryQuestSpecification.DeliveryReceiverSpecification.class);
        ConfigurationSerialization.registerClass(BlockBreakQuestSpecification.BlockBreakQuestPossibilitiesSpecification.class);
        ConfigurationSerialization.registerClass(BlockPlaceQuestSpecification.BlockPlaceQuestPossibilitiesSpecification.class);
        ConfigurationSerialization.registerClass(KillEntitiesQuestSpecification.KillEntitiesQuestPossibilitiesSpecification.class);

        sqlConfig = new SQLConfig(getConfig().getConfigurationSection("database"));
        dbf = new DatabaseFassade();
        if (!dbf.reconnect()) {
            return;
        }

        this.generateDailyQuests = this.getConfig().getBoolean("generateDailyQuests");
        this.payRewards = this.getConfig().getBoolean("payRewards");

        hasCitizens = Bukkit.getPluginManager().getPlugin("Citizens") != null;

        eventListener  = new EventListener(this);
        Bukkit.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        Bukkit.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", eventListener);

        commandExecutor = new CommandRouter(getCommand("quest"));
        commandExecutor.addCommandMapping(new QuestInfoCommand(), "questInfo");
        commandExecutor.addAlias("info", "questInfo");
        commandExecutor.addCommandMapping(new ShowPlayerQuestsCommand(), "showQuests");
        commandExecutor.addAlias("show", "showQuests");
        commandExecutor.addCommandMapping(new ShowQuestGiveMessageCommand(), "showGiveMessage");
        commandExecutor.addCommandMapping(new AcceptQuestCommand(), "acceptQuest");
        commandExecutor.addCommandMapping(new GiveOrRemoveQuestForPlayerCommand(true), "giveToPlayer");
        commandExecutor.addCommandMapping(new GiveOrRemoveQuestForPlayerCommand(false), "removeFromPlayer");
        commandExecutor.addCommandMapping(new CreateQuestCommand(), "create");
        commandExecutor.addCommandMapping(new EditQuestCommand(), "edit");
        commandExecutor.addCommandMapping(new StopEditingQuestCommand(), "edit", "stop");
        commandExecutor.addCommandMapping(new ToggleReadyStatusCommand(), "setReady");
        commandExecutor.addCommandMapping(new SetQuestNameCommand(), "setName");
        commandExecutor.addCommandMapping(new SetQuestMessageCommand(MessageTrigger.GIVE), "setGiveMessage");
        commandExecutor.addCommandMapping(new SetQuestMessageCommand(MessageTrigger.SUCCESS), "setSuccessMessage");
        commandExecutor.addCommandMapping(new SetQuestMessageCommand(MessageTrigger.FAIL), "setFailMessage");
        commandExecutor.addCommandMapping(new SetRewardItemsCommand(true), "setSuccessRewardItems");
        commandExecutor.addCommandMapping(new SetRewardItemsCommand(false), "setFailRewardItems");
        commandExecutor.addCommandMapping(new SetRewardIntCommand(true, Attribute.CUBES), "setSuccessRewardCubes");
        commandExecutor.addCommandMapping(new SetRewardIntCommand(false, Attribute.CUBES), "setFailRewardCubes");
        commandExecutor.addCommandMapping(new SetRewardIntCommand(true, Attribute.QUEST_POINTS), "setSuccessRewardQuestPoints");
        commandExecutor.addCommandMapping(new SetRewardIntCommand(false, Attribute.QUEST_POINTS), "setFailRewardQuestPoints");
        commandExecutor.addCommandMapping(new SetRewardIntCommand(true, Attribute.XP), "setSuccessRewardXP");
        commandExecutor.addCommandMapping(new SetRewardIntCommand(false, Attribute.XP), "setFailRewardXP");
        commandExecutor.addCommandMapping(new SetQuestVisibilityCommand(), "setVisibility");
        commandExecutor.addCommandMapping(new SetComplexQuestStructureCommand(), "setQuestStructure");
        commandExecutor.addCommandMapping(new AddOrRemoveSubQuestCommand(true), "addSubQuest");
        commandExecutor.addCommandMapping(new AddOrRemoveSubQuestCommand(false), "removeSubQuest");
        commandExecutor.addCommandMapping(new SetOrRemoveFailiureQuestCommand(true), "setFailiureQuest");
        commandExecutor.addCommandMapping(new SetOrRemoveFailiureQuestCommand(false), "removeFailiureQuest");
        commandExecutor.addCommandMapping(new SetOrRemoveFollowupQuestCommand(true), "setFollowupQuest");
        commandExecutor.addCommandMapping(new SetOrRemoveFollowupQuestCommand(false), "removeFollowupQuest");
        commandExecutor.addCommandMapping(new ClearSubQuestsCommand(), "clearSubQuests");
        commandExecutor.addCommandMapping(new SetQuestAmountCommand(), "setAmount");
        commandExecutor.addCommandMapping(new AddOrRemoveMaterialCommand(true), "addMaterial");
        commandExecutor.addCommandMapping(new AddOrRemoveMaterialCommand(false), "removeMaterial");
        commandExecutor.addCommandMapping(new ClearMaterialsCommand(), "clearMaterials");
        commandExecutor.addCommandMapping(new AddOrRemoveEntityTypeCommand(true), "addEntityType");
        commandExecutor.addCommandMapping(new AddOrRemoveEntityTypeCommand(false), "removeEntityType");
        commandExecutor.addCommandMapping(new ClearEntityTypesCommand(), "clearEntityTypes");
        commandExecutor.addCommandMapping(new SetGotoLocationCommand(), "setGotoLocation");
        commandExecutor.addCommandMapping(new SetQuestInteractorCommand(), "setInteractor");
        commandExecutor.addCommandMapping(new SetDeliveryInventoryCommand(), "setDelivery");
        commandExecutor.addCommandMapping(new SetQuestRegexCommand(true), "setLiteralMatch");
        commandExecutor.addCommandMapping(new SetQuestDateOrTimeCommand(true), "setQuestDate");
        commandExecutor.addCommandMapping(new SetQuestDateOrTimeCommand(false), "setQuestTime");
        commandExecutor.addCommandMapping(new SetQuestRegexCommand(false), "setRegex");
        commandExecutor.addCommandMapping(new ListQuestSpecificationsCommand(), "listQuestSpecifications");
        commandExecutor.addCommandMapping(new RemoveQuestSpecificationCommand(), "removeQuestSpecification");
        commandExecutor.addCommandMapping(new ConsolidateQuestSpecificationsCommand(), "consolidateQuestSpecifications");
        commandExecutor.addCommandMapping(new AddGotoQuestSpecificationCommand(), "addGotoQuestSpecification");
        for (InteractorRequiredFor requiredFor: InteractorRequiredFor.values()) {
            commandExecutor.addCommandMapping(new AddOrRemoveInteractorForSpecificationCommand(requiredFor), requiredFor.command);
        }
        for (MaterialCombinationRequiredFor requiredFor: MaterialCombinationRequiredFor.values()) {
            commandExecutor.addCommandMapping(new AddOrRemoveMaterialCombinationForSpecificationCommand(true, requiredFor), "add" + requiredFor.command);
            commandExecutor.addCommandMapping(new AddOrRemoveMaterialCombinationForSpecificationCommand(false, requiredFor), "remove" + requiredFor.command);
        }
        for (EntityTypeCombinationRequiredFor requiredFor: EntityTypeCombinationRequiredFor.values()) {
            commandExecutor.addCommandMapping(new AddOrRemoveEntityTypeCombinationForSpecificationCommand(true, requiredFor), "add" + requiredFor.command);
            commandExecutor.addCommandMapping(new AddOrRemoveEntityTypeCombinationForSpecificationCommand(false, requiredFor), "remove" + requiredFor.command);
        }
        commandExecutor.addCommandMapping(new TogglePayRewardsCommand(), "setPayRewards");
        commandExecutor.addCommandMapping(new ToggleGenerateDailyQuestsCommand(), "setGenerateDailyQuests");
        commandExecutor.addCommandMapping(new AddQuestGiverCommand(), "addQuestGiver");
        for (QuestGiverModification m: QuestGiverModification.values()) {
            commandExecutor.addCommandMapping(new ModifyQuestGiverCommand(m), m.command);
        }

        globalChatAPI = (GlobalChatAPI) Bukkit.getPluginManager().getPlugin("GlobalChat");
        loadServerIdAndName();

        if (hasCitizens) {
            loadCitizensAPI();
            Bukkit.getPluginManager().registerEvents(new NPCEventListener(), this);
         }
        loadQuests();

        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            questDependentSetup();
        }, 1L);

        tickTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            tick();
        }, 2L, 1L);
    }

    public boolean hasInteractiveBooksAPI() {
        return Bukkit.getPluginManager().getPlugin("InteractiveBookAPI") != null;
    }

    public boolean hasCitizensPlugin() {
        return hasCitizens;
    }

    private void loadCitizensAPI() {
        loadNPCs();
    }

    private void loadNPCs() {
        /*npcReg = CitizensAPI.getNamedNPCRegistry("CubeQuestNPCReg");
        if (npcReg == null) {
            npcReg = CitizensAPI.createNamedNPCRegistry("CubeQuestNPCReg", SimpleNPCDataStore.create(new YamlStorage(
                    new File(this.getDataFolder().getPath() + File.separator + "npcs.yml"))));
        }*/
        npcReg = CitizensAPI.getNPCRegistry();
    }

    private void loadServerIdAndName() {
        if (getConfig().contains("serverId")) {
            serverId = getConfig().getInt("serverId");
        } else {
            try {
                serverId = dbf.addServerId();

                getConfig().set("serverId", serverId);
                this.saveConfig();
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Could not create serverId!", e);
            }
        }
        if (getConfig().contains("serverName")) {
            serverName = getConfig().getString("serverName");
        } else {
            waitingForPlayer.add(() -> {
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
        questCreator.loadQuests();
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
                YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(questGiverFolder, name));
                QuestGiver giver = (QuestGiver) config.get("giver");
                questGivers.put(giver.getName(), giver);
                questGiversByNPCId.put(giver.getNPC().getId(), giver);
            }
        }

        List<String> dailyQuestGiverNames = (List<String>) this.getConfig().get("dailyQuestGivers");
        if (dailyQuestGiverNames != null) {
            for (String name: dailyQuestGiverNames) {
                QuestGiver giver = questGivers.get(name);
                if (giver != null) {
                    dailyQuestGivers.add(giver);
                }
            }
        }

        this.questGenerator = QuestGenerator.getInstance(); //this.getConfig().contains("questGenerator")? (QuestGenerator) this.getConfig().get("questGenerator") : new QuestGenerator();
    }

    @Override
    public void onDisable() {
        daemonTimer.cancel();
        if (tickTask != null && (Bukkit.getScheduler().isQueued(tickTask) || Bukkit.getScheduler().isCurrentlyRunning(tickTask))) {
            Bukkit.getScheduler().cancelTask(tickTask);
        }
    }

    private void tick() {
        tick ++;
        if (this.generateDailyQuests && (questGenerator.getLastGeneratedForDay() == null || LocalDate.now().isAfter(questGenerator.getLastGeneratedForDay()))) {
            questGenerator.generateDailyQuests();
        }
    }

    public QuestManager getQuestManager() {
        return QuestManager.getInstance();
    }

    public CommandRouter getCommandExecutor() {
        return commandExecutor;
    }

    public QuestCreator getQuestCreator() {
        return questCreator;
    }

    public QuestStateCreator getQuestStateCreator() {
        return questStateCreator;
    }

    public InteractorCreator getInteractorCreator() {
        return interactorCreator;
    }

    public QuestEditor getQuestEditor() {
        return questEditor;
    }

    public QuestGenerator getQuestGenerator() {
        return questGenerator;
    }

    public EventListener getEventListener() {
        return eventListener;
    }

    public GlobalChatAPI getGlobalChatAPI() {
        return globalChatAPI;
    }

    public NPCRegistry getNPCReg() {
        if (!hasCitizensPlugin()) {
            return null;
        }
        return npcReg;
    }

    public boolean isGeneratingDailyQuests() {
        return generateDailyQuests;
    }

    public void setGenerateDailyQuests(boolean val) {
        this.generateDailyQuests = val;
        this.getConfig().set("generateDailyQuests", val);
        if (!val) {
            dailyQuestGivers.clear();
            this.saveDailyQuestGivers();
        } else {
            this.saveConfig();
        }
    }

    public boolean isPayRewards() {
        return payRewards;
    }

    public void setPayRewards(boolean val) {
        this.payRewards = val;
        this.getConfig().set("payRewards", val);
        this.saveConfig();
    }

    public int getServerId() {
        return serverId;
    }

    public String getBungeeServerName() {
        return serverName;
    }

    public void setBungeeServerName(String val) {
        serverName = val;
        try {
            dbf.setServerName();

            getConfig().set("serverName", serverName);
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
            return dbf.getOtherBungeeServerNames();
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Could not get servernames!", e);
            return new String[0];
        }
    }

    public boolean isWaitingForPlayer() {
        return !waitingForPlayer.isEmpty();
    }

    public void addWaitingForPlayer(Runnable r) {
        if (Bukkit.getServer().getOnlinePlayers().isEmpty()) {
            waitingForPlayer.add(r);
        } else {
            r.run();
        }
    }

    public void playerArrived() {
        Iterator<Runnable> it = waitingForPlayer.iterator();
        while (it.hasNext()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(this, it.next(), 1L);
            it.remove();
        }
    }

    public Timer getTimer() {
        return daemonTimer;
    }

    public DatabaseFassade getDatabaseFassade() {
        return dbf;
    }

    public SQLConfig getSQLConfigData() {
        return sqlConfig;
    }

    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    public PlayerData getPlayerData(UUID id) {
        if (id == null) {
            throw new NullPointerException();
        }
        PlayerData pd = playerData.get(id);
        if (pd == null) {
            pd = new PlayerData(id);
            playerData.put(id, pd);
        }
        return pd;
    }

    public void unloadPlayerData(UUID id) {
        playerData.remove(id);
    }

    public QuestGiver getQuestGiver(String name) {
        return questGivers.get(name);
    }

    public QuestGiver getQuestGiver(NPC npc) {
        return questGiversByNPCId.get(npc.getId());
    }

    public void addQuestGiver(QuestGiver giver) {
        if (questGivers.get(giver.getName()) != null) {
            throw new IllegalArgumentException("already has a QuestGiver with that name");
        }
        if (questGiversByNPCId.get(giver.getNPC().getId()) != null) {
            throw new IllegalArgumentException("already has a QuestGiver with that npc");
        }

        questGivers.put(giver.getName(), giver);
        questGiversByNPCId.put(giver.getNPC().getId(), giver);
    }

    public boolean removeQuestGiver(String name) {
        QuestGiver giver = questGivers.get(name);
        if (giver == null) {
            return false;
        }

        return removeQuestGiver(giver);
    }

    public boolean removeQuestGiver(NPC npc) {
        QuestGiver giver = questGiversByNPCId.get(npc.getId());
        if (giver == null) {
            return false;
        }

        return removeQuestGiver(giver);
    }

    private boolean removeQuestGiver(QuestGiver giver) {
        questGivers.remove(giver.getName());
        questGiversByNPCId.remove(giver.getNPC().getId());
        dailyQuestGivers.remove(giver);

        File folder = new File(CubeQuest.getInstance().getDataFolder(), "questGivers");
        File configFile = new File(folder, giver.getName() + ".yml");
        if (!configFile.delete()) {
            getLogger().log(Level.WARNING, "Could not delete config \"" + giver.getName() + ".yml\" for QuestGiver.");
        }

        return true;
    }

    public Collection<QuestGiver> getQuestGivers() {
        return Collections.unmodifiableCollection(questGivers.values());
    }

    public Set<QuestGiver> getDailyQuestGivers() {
        return Collections.unmodifiableSet(dailyQuestGivers);
    }

    public boolean addDailyQuestGiver(String name) {
        QuestGiver giver = questGivers.get(name);
        if (giver == null) {
            throw new IllegalArgumentException("no QuestGiver with that name");
        }

        return addDailyQuestGiver(giver);
    }

    public boolean addDailyQuestGiver(NPC npc) {
        QuestGiver giver = questGiversByNPCId.get(npc.getId());
        if (giver == null) {
            throw new IllegalArgumentException("no QuestGiver with that npc");
        }

        return addDailyQuestGiver(giver);
    }

    private boolean addDailyQuestGiver(QuestGiver giver) {
        if (dailyQuestGivers.add(giver)) {
            if (LocalDate.now().equals(questGenerator.getLastGeneratedForDay())) {
                Quest[] generated = questGenerator.getGeneratedDailyQuests();
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
        QuestGiver giver = questGivers.get(name);
        if (giver == null) {
            throw new IllegalArgumentException("no QuestGiver with that name");
        }

        return removeDailyQuestGiver(giver);
    }

    public boolean removeDailyQuestGiver(NPC npc) {
        QuestGiver giver = questGiversByNPCId.get(npc.getId());
        if (giver == null) {
            throw new IllegalArgumentException("no QuestGiver with that npc");
        }

        return removeDailyQuestGiver(giver);
    }

    private boolean removeDailyQuestGiver(QuestGiver giver) {
        if (dailyQuestGivers.remove(giver)) {
            saveDailyQuestGivers();
            return true;
        }
        return false;
    }

    private void saveDailyQuestGivers() {
        List<String> dailyQuestGiverNames = new ArrayList<>();
        dailyQuestGivers.forEach(qg -> dailyQuestGiverNames.add(qg.getName()));
        this.getConfig().set("dailyQuestGivers", dailyQuestGiverNames);
        this.saveConfig();
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

        TreasureChestAPI tcAPI = TreasureChest.getPlugin(TreasureChest.class);
        tcAPI.addItem(Bukkit.getOfflinePlayer(playerId), display, reward.getItems(), reward.getCubes());
    }

    public void payCubes(Player player, int cubes) {
        payCubes(player.getUniqueId(), cubes);
    }

    public void payCubes(UUID playerId, int cubes) {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().log(Level.SEVERE, "Could not find Economy! Hence, could not pay " + cubes + " cubes to player " + playerId.toString());
            return;
        }
        EconomyResponse response = rsp.getProvider().depositPlayer(Bukkit.getOfflinePlayer(playerId), cubes);
        if (!response.transactionSuccess()) {
            getLogger().log(Level.SEVERE, "Could not pay " + cubes + " cubes to player " + playerId.toString() + " (EconomyResponse not successfull: " + response.errorMessage + ")");
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

}
