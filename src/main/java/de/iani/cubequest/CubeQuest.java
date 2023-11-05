package de.iani.cubequest;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.SetMultimap;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.cubeside.connection.ConnectionAPI;
import de.cubeside.nmsutils.NMSUtils;
import de.cubeside.npcs.CubesideNPCs;
import de.cubeside.npcs.data.SpawnedNPCs;
import de.iani.cubequest.actions.ActionBarMessageAction;
import de.iani.cubequest.actions.ActionType;
import de.iani.cubequest.actions.BossBarMessageAction;
import de.iani.cubequest.actions.ChatMessageAction;
import de.iani.cubequest.actions.EffectAction;
import de.iani.cubequest.actions.EffectAction.EffectData;
import de.iani.cubequest.actions.FixedActionLocation;
import de.iani.cubequest.actions.ParticleAction;
import de.iani.cubequest.actions.ParticleAction.ParticleData;
import de.iani.cubequest.actions.PlayerActionLocation;
import de.iani.cubequest.actions.PotionEffectAction;
import de.iani.cubequest.actions.RedstoneSignalAction;
import de.iani.cubequest.actions.RemovePotionEffectAction;
import de.iani.cubequest.actions.RewardAction;
import de.iani.cubequest.actions.SoundAction;
import de.iani.cubequest.actions.SpawnEntityAction;
import de.iani.cubequest.actions.TeleportationAction;
import de.iani.cubequest.actions.TitleMessageAction;
import de.iani.cubequest.bubbles.InteractorBubbleMaker;
import de.iani.cubequest.bubbles.QuestGiverBubbleTarget;
import de.iani.cubequest.commands.AcceptQuestCommand;
import de.iani.cubequest.commands.AchievementInfoCommand;
import de.iani.cubequest.commands.AddConditionCommand;
import de.iani.cubequest.commands.AddEditMoveOrRemoveActionCommand;
import de.iani.cubequest.commands.AddEditMoveOrRemoveActionCommand.ActionTime;
import de.iani.cubequest.commands.AddGotoQuestSpecificationCommand;
import de.iani.cubequest.commands.AddOrRemoveDamageCauseCommand;
import de.iani.cubequest.commands.AddOrRemoveEntityTypeCombinationForSpecificationCommand;
import de.iani.cubequest.commands.AddOrRemoveEntityTypeCombinationForSpecificationCommand.EntityTypeCombinationRequiredFor;
import de.iani.cubequest.commands.AddOrRemoveEntityTypeCommand;
import de.iani.cubequest.commands.AddOrRemoveGameEventCommand;
import de.iani.cubequest.commands.AddOrRemoveIncreaseStatisticQuestSpecificationCommand;
import de.iani.cubequest.commands.AddOrRemoveInteractorForSpecificationCommand;
import de.iani.cubequest.commands.AddOrRemoveInteractorForSpecificationCommand.InteractorRequiredFor;
import de.iani.cubequest.commands.AddOrRemoveMaterialCombinationForSpecificationCommand;
import de.iani.cubequest.commands.AddOrRemoveMaterialCombinationForSpecificationCommand.MaterialCombinationRequiredFor;
import de.iani.cubequest.commands.AddOrRemoveMaterialCommand;
import de.iani.cubequest.commands.AddOrRemoveServerFlagCommand;
import de.iani.cubequest.commands.AddOrRemoveStatisticCommand;
import de.iani.cubequest.commands.AddOrRemoveSubQuestCommand;
import de.iani.cubequest.commands.AddQuestGiverCommand;
import de.iani.cubequest.commands.AddRemoveOrSetXpOrQuestPointsCommand;
import de.iani.cubequest.commands.AddRemoveOrSetXpOrQuestPointsCommand.PointAction;
import de.iani.cubequest.commands.ClearDamageCausesCommand;
import de.iani.cubequest.commands.ClearEntityTypesCommand;
import de.iani.cubequest.commands.ClearMaterialsCommand;
import de.iani.cubequest.commands.ClearStatisticsCommand;
import de.iani.cubequest.commands.ClearSubQuestsCommand;
import de.iani.cubequest.commands.CloneQuestCommand;
import de.iani.cubequest.commands.ConfirmQuestInteractionCommand;
import de.iani.cubequest.commands.ConsolidateQuestSpecificationsCommand;
import de.iani.cubequest.commands.CreateQuestCommand;
import de.iani.cubequest.commands.DeleteQuestCommand;
import de.iani.cubequest.commands.EditQuestCommand;
import de.iani.cubequest.commands.GiveBackQuestCommand;
import de.iani.cubequest.commands.ListBlockBreakQuestSpecificationsCommand;
import de.iani.cubequest.commands.ListBlockPlaceQuestSpecificationsCommand;
import de.iani.cubequest.commands.ListDeliveryQuestContentSpecificationsCommand;
import de.iani.cubequest.commands.ListDeliveryQuestReceiverSpecificationsCommand;
import de.iani.cubequest.commands.ListFishingQuestSpecificationsCommand;
import de.iani.cubequest.commands.ListIncreaseStatisticQuestSpecificationsCommand;
import de.iani.cubequest.commands.ListKillEntitiesQuestSpecificationsCommand;
import de.iani.cubequest.commands.ListPlayersWithStateCommand;
import de.iani.cubequest.commands.ListQuestSpecificationsCommand;
import de.iani.cubequest.commands.ListReferencesCommand;
import de.iani.cubequest.commands.ListServerFlagsCommand;
import de.iani.cubequest.commands.ModifyQuestGiverCommand;
import de.iani.cubequest.commands.ModifyQuestGiverCommand.QuestGiverModification;
import de.iani.cubequest.commands.MoveQuestInteractorCommand;
import de.iani.cubequest.commands.QuestInfoCommand;
import de.iani.cubequest.commands.QuestStateInfoCommand;
import de.iani.cubequest.commands.RemoveConditionCommand;
import de.iani.cubequest.commands.RemoveQuestSpecificationCommand;
import de.iani.cubequest.commands.SaveOrReloadGeneratorCommand;
import de.iani.cubequest.commands.SetAchievementQuestCommand;
import de.iani.cubequest.commands.SetAllowGiveBackCommand;
import de.iani.cubequest.commands.SetAllowRetryCommand;
import de.iani.cubequest.commands.SetAutoGivingCommand;
import de.iani.cubequest.commands.SetAutoRemoveCommand;
import de.iani.cubequest.commands.SetCancelCommandCommand;
import de.iani.cubequest.commands.SetComplexQuestStructureCommand;
import de.iani.cubequest.commands.SetDeliveryInventoryCommand;
import de.iani.cubequest.commands.SetDoBubbleCommand;
import de.iani.cubequest.commands.SetFailAfterSemiSuccessCommand;
import de.iani.cubequest.commands.SetFollowupRequiredForSuccessCommand;
import de.iani.cubequest.commands.SetGotoInvertedCommand;
import de.iani.cubequest.commands.SetGotoLocationCommand;
import de.iani.cubequest.commands.SetGotoToleranceCommand;
import de.iani.cubequest.commands.SetIgnoreOppositeCommand;
import de.iani.cubequest.commands.SetInteractorQuestConfirmationMessageCommand;
import de.iani.cubequest.commands.SetMysteriousSpellingBookCommand;
import de.iani.cubequest.commands.SetOnDeleteCascadeCommand;
import de.iani.cubequest.commands.SetOrAppendDisplayMessageCommand;
import de.iani.cubequest.commands.SetOrRemoveFailureQuestCommand;
import de.iani.cubequest.commands.SetOrRemoveFollowupQuestCommand;
import de.iani.cubequest.commands.SetOrRemoveInteractorAliasCommand;
import de.iani.cubequest.commands.SetOrRemoveQuestInteractorCommand;
import de.iani.cubequest.commands.SetOverwrittenNameForSthCommand;
import de.iani.cubequest.commands.SetOverwrittenNameForSthCommand.SpecificSth;
import de.iani.cubequest.commands.SetQuestAmountCommand;
import de.iani.cubequest.commands.SetQuestBlockCommand;
import de.iani.cubequest.commands.SetQuestDateOrTimeCommand;
import de.iani.cubequest.commands.SetQuestIgnoreCancelledEventsCommand;
import de.iani.cubequest.commands.SetQuestNameCommand;
import de.iani.cubequest.commands.SetQuestRegexCommand;
import de.iani.cubequest.commands.SetQuestStatusForPlayerCommand;
import de.iani.cubequest.commands.SetQuestVisibilityCommand;
import de.iani.cubequest.commands.SetRequireConfirmationCommand;
import de.iani.cubequest.commands.SetTakeDamageQuestPropertyCommand;
import de.iani.cubequest.commands.SetTakeDamageQuestPropertyCommand.TakeDamageQuestPropertyType;
import de.iani.cubequest.commands.ShowLevelCommand;
import de.iani.cubequest.commands.ShowPlayerQuestsCommand;
import de.iani.cubequest.commands.ShowQuestGiveMessageCommand;
import de.iani.cubequest.commands.StopEditingQuestCommand;
import de.iani.cubequest.commands.TestCommand;
import de.iani.cubequest.commands.ToggleGenerateDailyQuestsCommand;
import de.iani.cubequest.commands.TogglePayRewardsCommand;
import de.iani.cubequest.commands.ToggleReadyStatusCommand;
import de.iani.cubequest.commands.TransferPlayerCommand;
import de.iani.cubequest.commands.VersionCommand;
import de.iani.cubequest.conditions.ConditionType;
import de.iani.cubequest.conditions.HaveQuestStatusCondition;
import de.iani.cubequest.conditions.MinimumQuestLevelCondition;
import de.iani.cubequest.cubeshop.Registrator;
import de.iani.cubequest.generation.BlockBreakQuestSpecification;
import de.iani.cubequest.generation.BlockBreakQuestSpecification.BlockBreakQuestPossibilitiesSpecification;
import de.iani.cubequest.generation.BlockPlaceQuestSpecification;
import de.iani.cubequest.generation.BlockPlaceQuestSpecification.BlockPlaceQuestPossibilitiesSpecification;
import de.iani.cubequest.generation.ClickInteractorQuestSpecification;
import de.iani.cubequest.generation.DeliveryQuestSpecification;
import de.iani.cubequest.generation.DeliveryQuestSpecification.DeliveryQuestPossibilitiesSpecification;
import de.iani.cubequest.generation.EntityTypeCombination;
import de.iani.cubequest.generation.FishingQuestSpecification;
import de.iani.cubequest.generation.FishingQuestSpecification.FishingQuestPossibilitiesSpecification;
import de.iani.cubequest.generation.GotoQuestSpecification;
import de.iani.cubequest.generation.IncreaseStatisticQuestSpecification;
import de.iani.cubequest.generation.IncreaseStatisticQuestSpecification.IncreaseStatisticQuestPossibilitiesSpecification;
import de.iani.cubequest.generation.IncreaseStatisticQuestSpecification.IncreaseStatisticQuestPossibility;
import de.iani.cubequest.generation.KeyedValueMap;
import de.iani.cubequest.generation.KillEntitiesQuestSpecification;
import de.iani.cubequest.generation.KillEntitiesQuestSpecification.KillEntitiesQuestPossibilitiesSpecification;
import de.iani.cubequest.generation.MaterialCombination;
import de.iani.cubequest.generation.QuestGenerator;
import de.iani.cubequest.generation.StatisticValueMap;
import de.iani.cubequest.interaction.BlockInteractor;
import de.iani.cubequest.interaction.EntityInteractor;
import de.iani.cubequest.interaction.Interactor;
import de.iani.cubequest.interaction.InteractorCreator;
import de.iani.cubequest.interaction.InteractorProtecting;
import de.iani.cubequest.interaction.NPCInteractor;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.questStates.QuestStateCreator;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.quests.QuestCreator;
import de.iani.cubequest.quests.WaitForDateQuest;
import de.iani.cubequest.sql.DatabaseFassade;
import de.iani.cubequest.sql.util.SQLConfig;
import de.iani.cubequest.util.BlockLocation;
import de.iani.cubequest.util.SafeLocation;
import de.iani.cubesidestats.api.CubesideStatisticsAPI;
import de.iani.cubesideutils.Pair;
import de.iani.cubesideutils.bukkit.commands.CommandRouter;
import de.iani.cubesideutils.bukkit.serialization.SerializablePair;
import de.iani.cubesideutils.commands.ArgsParser;
import de.iani.cubesideutils.plugin.CubesideUtils;
import de.iani.playerUUIDCache.PlayerUUIDCache;
import de.iani.treasurechest.TreasureChest;
import de.iani.treasurechest.TreasureChestAPI;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Timer;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class CubeQuest extends JavaPlugin {

    public static final String PLUGIN_TAG = ChatColor.BLUE + "[Quest]" + ChatColor.RESET;
    public static final String DATA_KEY_PREFIX = "CubeQuest_";

    public static final String ACCEPT_QUESTS_PERMISSION = "cubequest.use";
    public static final String SEE_PLAYER_INFO_PERMISSION = "cubequest.player_info";
    public static final String EDIT_QUESTS_PERMISSION = "cubequest.edit_quests";
    public static final String CONFIRM_QUESTS_PERMISSION = "cubequest.confirm_quests";
    public static final String EDIT_QUEST_STATES_PERMISSION = "cubequest.edit_states";
    public static final String EDIT_QUEST_GIVERS_PERMISSION = "cubequest.edit_givers";
    public static final String EDIT_QUEST_SPECIFICATIONS_PERMISSION = "cubequest.edit_specifications";
    public static final String TOGGLE_SERVER_PROPERTIES_PERMISSION = "cubequest.server_properties";
    public static final String TRANSFER_PLAYERS_PERMISSION = "cubequest.transfer_players";
    public static final String SEE_EXCEPTIONS_PERMISSION = "cubequest.dev";

    private static CubeQuest instance = null;

    private CommandRouter commandExecutor;
    private QuestCreator questCreator;
    private QuestStateCreator questStateCreator;
    private InteractorCreator interactorCreator;
    private QuestEditor questEditor;
    private EventListener eventListener;
    private InteractionConfirmationHandler interactionConfirmationHandler;
    private QuestGenerator questGenerator;

    private LogHandler logHandler;
    private SQLConfig sqlConfig;
    private DatabaseFassade databaseFassade;
    private PlayerUUIDCache playerUUIDCache;
    private CubesideStatisticsAPI statistics;
    private NMSUtils nmsUtils;

    private boolean hasCubesideNPCs;
    private SpawnedNPCs npcReg;

    private boolean hasVault;
    private Economy economy;

    private int serverId;
    private String serverName;
    private boolean generateDailyQuests;
    private boolean payRewards;
    private Set<String> serverFlags;

    private ConnectionAPI connectionAPI;
    private ArrayList<Runnable> waitingForPlayer;
    private Integer tickTask;
    private volatile long tick = 0;
    private Timer daemonTimer;
    private InteractorBubbleMaker bubbleMaker;

    private HashMap<UUID, PlayerData> playerData;

    private Map<String, QuestGiver> questGivers;
    private Map<Interactor, QuestGiver> questGiversByInteractor;
    private Set<QuestGiver> dailyQuestGivers;
    private Set<Quest> autoGivenQuests;

    private List<String> storedMessages;
    private Set<Integer> updateOnDisable;

    private Map<Interactor, Interactor> interactorAliases;
    private SetMultimap<Interactor, InteractorProtecting> interactorProtecting;

    private String globalDataChannelName = "CubeQuest";

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
        this.autoGivenQuests = new HashSet<>();
        this.waitingForPlayer = new ArrayList<>();
        this.storedMessages = new ArrayList<>();
        this.updateOnDisable = new HashSet<>();
        this.interactorAliases = new LinkedHashMap<>();
        this.interactorProtecting = HashMultimap.create();

        this.daemonTimer = new Timer("CubeQuest-Timer", true);

        Pair.class.toString(); // Load class, triggering ConfigurationSerialization registration.
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onEnable() {
        saveDefaultConfig();
        ConfigurationSerialization.registerClass(Reward.class);
        ConfigurationSerialization.registerClass(QuestGiver.class);
        ConfigurationSerialization.registerClass(QuestGiver.class, "de.iani.cubequest.questGiving.QuestGiver");
        ConfigurationSerialization.registerClass(Quest.class);

        ConfigurationSerialization.registerClass(SafeLocation.class);
        ConfigurationSerialization.registerClass(BlockLocation.class);
        ConfigurationSerialization.registerClass(BlockLocation.class, "de.iani.cubequest.interaction.BlockLocation");

        for (ConditionType type : ConditionType.values()) {
            ConfigurationSerialization.registerClass(type.concreteClass);
        }
        ConfigurationSerialization.registerClass(MinimumQuestLevelCondition.class,
                "de.iani.cubequest.questGiving.MinimumQuestLevelCondition");
        ConfigurationSerialization.registerClass(HaveQuestStatusCondition.class,
                "de.iani.cubequest.questGiving.HaveQuestStatusCondition");

        ConfigurationSerialization.registerClass(FixedActionLocation.class);
        ConfigurationSerialization.registerClass(PlayerActionLocation.class);
        ConfigurationSerialization.registerClass(ParticleData.class);
        ConfigurationSerialization.registerClass(EffectData.class);

        ConfigurationSerialization.registerClass(ActionBarMessageAction.class);
        ConfigurationSerialization.registerClass(BossBarMessageAction.class);
        ConfigurationSerialization.registerClass(ChatMessageAction.class);
        ConfigurationSerialization.registerClass(ChatMessageAction.class, "de.iani.cubequest.actions.MessageAction");
        ConfigurationSerialization.registerClass(RewardAction.class);
        ConfigurationSerialization.registerClass(RedstoneSignalAction.class);
        ConfigurationSerialization.registerClass(PotionEffectAction.class);
        ConfigurationSerialization.registerClass(RemovePotionEffectAction.class);
        ConfigurationSerialization.registerClass(ParticleAction.class);
        ConfigurationSerialization.registerClass(EffectAction.class);
        ConfigurationSerialization.registerClass(SoundAction.class);
        ConfigurationSerialization.registerClass(SpawnEntityAction.class);
        ConfigurationSerialization.registerClass(TeleportationAction.class);
        ConfigurationSerialization.registerClass(TitleMessageAction.class);

        for (ActionType type : ActionType.values()) {
            ConfigurationSerialization.registerClass(type.concreteClass);
        }

        ConfigurationSerialization.registerClass(NPCInteractor.class);
        ConfigurationSerialization.registerClass(EntityInteractor.class);
        ConfigurationSerialization.registerClass(BlockInteractor.class);

        ConfigurationSerialization.registerClass(QuestGenerator.class);
        ConfigurationSerialization.registerClass(KeyedValueMap.class);
        ConfigurationSerialization.registerClass(KeyedValueMap.class, "de.iani.cubequest.generation.EnumValueMap");
        ConfigurationSerialization.registerClass(KeyedValueMap.class, "de.iani.cubequest.generation.ValueMap");
        ConfigurationSerialization.registerClass(StatisticValueMap.class);
        ConfigurationSerialization.registerClass(MaterialCombination.class);
        ConfigurationSerialization.registerClass(EntityTypeCombination.class);

        ConfigurationSerialization.registerClass(GotoQuestSpecification.class);
        ConfigurationSerialization.registerClass(ClickInteractorQuestSpecification.class);
        ConfigurationSerialization.registerClass(DeliveryQuestSpecification.class);
        ConfigurationSerialization.registerClass(DeliveryQuestPossibilitiesSpecification.class);
        ConfigurationSerialization.registerClass(DeliveryQuestSpecification.DeliveryReceiverSpecification.class);
        ConfigurationSerialization.registerClass(FishingQuestSpecification.class);
        ConfigurationSerialization.registerClass(FishingQuestPossibilitiesSpecification.class);
        ConfigurationSerialization.registerClass(IncreaseStatisticQuestPossibility.class);
        ConfigurationSerialization.registerClass(IncreaseStatisticQuestPossibilitiesSpecification.class);
        ConfigurationSerialization.registerClass(IncreaseStatisticQuestSpecification.class);
        ConfigurationSerialization.registerClass(BlockBreakQuestSpecification.class);
        ConfigurationSerialization.registerClass(BlockBreakQuestPossibilitiesSpecification.class);
        ConfigurationSerialization.registerClass(BlockPlaceQuestSpecification.class);
        ConfigurationSerialization.registerClass(BlockPlaceQuestPossibilitiesSpecification.class);
        ConfigurationSerialization.registerClass(KillEntitiesQuestSpecification.class);
        ConfigurationSerialization.registerClass(KillEntitiesQuestPossibilitiesSpecification.class);

        this.sqlConfig = new SQLConfig(getConfig().getConfigurationSection("database"));
        this.databaseFassade = new DatabaseFassade();
        if (!this.databaseFassade.reconnect()) {
            return;
        }

        this.playerUUIDCache = JavaPlugin.getPlugin(PlayerUUIDCache.class);
        this.statistics = Bukkit.getServer().getServicesManager().load(CubesideStatisticsAPI.class);
        this.nmsUtils = NMSUtils.createInstance(this);

        this.hasCubesideNPCs = Bukkit.getPluginManager().getPlugin("Citizens") != null;
        this.hasVault = Bukkit.getPluginManager().getPlugin("Vault") != null;

        this.globalDataChannelName = getConfig().getString("globalDataChannelName");

        this.eventListener = new EventListener(this);
        this.questCreator = new QuestCreator();
        this.questStateCreator = new QuestStateCreator();
        this.interactorCreator = new InteractorCreator();
        this.questEditor = new QuestEditor();
        this.interactionConfirmationHandler = new InteractionConfirmationHandler();
        this.logHandler = new LogHandler();
        this.bubbleMaker = new InteractorBubbleMaker();

        this.interactorAliases.clear();
        getConfig().getList("interactorAliases", Collections.emptyList()).stream()
                .map(o -> (Pair<Interactor, Interactor>) o)
                .forEach(pair -> this.interactorAliases.put(pair.first, pair.second));

        this.commandExecutor = new CommandRouter(getCommand("quest"), true, new CubeQuestCommandExceptionHandler());

        this.commandExecutor.addCommandMapping(new VersionCommand(), VersionCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new QuestInfoCommand(), QuestInfoCommand.COMMAND_PATH);
        this.commandExecutor.addAlias("info", QuestInfoCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new ListReferencesCommand(), ListReferencesCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new ShowLevelCommand(), ShowLevelCommand.COMMAND_PATH);
        this.commandExecutor.addAlias("level", ShowLevelCommand.COMMAND_PATH);
        AchievementInfoCommand achievementInfoCommand = new AchievementInfoCommand();
        this.commandExecutor.addCommandMapping(achievementInfoCommand, AchievementInfoCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new ShowPlayerQuestsCommand(null),
                ShowPlayerQuestsCommand.getCommandPath(null));
        ShowPlayerQuestsCommand showActiveQuestsCommand = new ShowPlayerQuestsCommand(Status.GIVENTO);
        for (Status status : Status.values()) {
            ShowPlayerQuestsCommand cmd =
                    status == Status.GIVENTO ? showActiveQuestsCommand : new ShowPlayerQuestsCommand(status);
            this.commandExecutor.addCommandMapping(cmd, ShowPlayerQuestsCommand.getCommandPath(status));
        }
        this.commandExecutor.addAlias("show", ShowPlayerQuestsCommand.getCommandPath(null));
        this.commandExecutor.addAlias("list", ShowPlayerQuestsCommand.getCommandPath(null));
        this.commandExecutor.addCommandMapping(new QuestStateInfoCommand(false),
                QuestStateInfoCommand.NORMAL_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new QuestStateInfoCommand(true),
                QuestStateInfoCommand.UNMASKED_COMMAND_PATH);
        this.commandExecutor.addAlias("state", QuestStateInfoCommand.NORMAL_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new ShowQuestGiveMessageCommand(),
                ShowQuestGiveMessageCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new ListPlayersWithStateCommand(),
                ListPlayersWithStateCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new AcceptQuestCommand(), AcceptQuestCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new GiveBackQuestCommand(), GiveBackQuestCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new ConfirmQuestInteractionCommand(),
                ConfirmQuestInteractionCommand.COMMAND_PATH);
        for (Status status : Status.values()) {
            this.commandExecutor.addCommandMapping(new SetQuestStatusForPlayerCommand(status),
                    SetQuestStatusForPlayerCommand.commandPath(status));
        }
        for (PointAction action : PointAction.values()) {
            this.commandExecutor.addCommandMapping(new AddRemoveOrSetXpOrQuestPointsCommand(action, true),
                    action.xpCommandPath);
            this.commandExecutor.addCommandMapping(new AddRemoveOrSetXpOrQuestPointsCommand(action, false),
                    action.pointsCommandPath);
        }
        this.commandExecutor.addCommandMapping(new TransferPlayerCommand(), TransferPlayerCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetOrRemoveInteractorAliasCommand(true),
                SetOrRemoveInteractorAliasCommand.SET_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetOrRemoveInteractorAliasCommand(false),
                SetOrRemoveInteractorAliasCommand.REMOVE_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new CreateQuestCommand(), CreateQuestCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new DeleteQuestCommand(), DeleteQuestCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new EditQuestCommand(), EditQuestCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new StopEditingQuestCommand(), StopEditingQuestCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new CloneQuestCommand(), CloneQuestCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new ToggleReadyStatusCommand(), ToggleReadyStatusCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetQuestNameCommand(true, true),
                SetQuestNameCommand.INTERNAL_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetQuestNameCommand(false, true),
                SetQuestNameCommand.DISPLAY_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetQuestNameCommand(false, false),
                SetQuestNameCommand.REMOVE_DISPLAY_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetOrAppendDisplayMessageCommand(true),
                SetOrAppendDisplayMessageCommand.SET_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetOrAppendDisplayMessageCommand(false),
                SetOrAppendDisplayMessageCommand.APPEND_COMMAND_PATH);
        this.commandExecutor.addAlias(SetOrAppendDisplayMessageCommand.APPEND_PATH_ALIAS,
                SetOrAppendDisplayMessageCommand.APPEND_COMMAND_PATH);
        for (ActionTime time : ActionTime.values()) {
            this.commandExecutor.addCommandMapping(new AddEditMoveOrRemoveActionCommand(time), time.commandPath);
        }
        this.commandExecutor.addCommandMapping(new SetAllowRetryCommand(true),
                SetAllowRetryCommand.SUCCESS_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetAllowGiveBackCommand(), SetAllowGiveBackCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetAllowRetryCommand(false), SetAllowRetryCommand.FAIL_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetQuestVisibilityCommand(), SetQuestVisibilityCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetAutoGivingCommand(), SetAutoGivingCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetAutoRemoveCommand(), SetAutoRemoveCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetAchievementQuestCommand(),
                SetAchievementQuestCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new AddConditionCommand(true), AddConditionCommand.GIVING_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new AddConditionCommand(false),
                AddConditionCommand.PROGRESS_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new RemoveConditionCommand(true),
                RemoveConditionCommand.GIVING_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new RemoveConditionCommand(false),
                RemoveConditionCommand.PROGRESS_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetComplexQuestStructureCommand(),
                SetComplexQuestStructureCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new AddOrRemoveSubQuestCommand(true),
                AddOrRemoveSubQuestCommand.ADD_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new AddOrRemoveSubQuestCommand(false),
                AddOrRemoveSubQuestCommand.REMOVE_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetOrRemoveFailureQuestCommand(true),
                SetOrRemoveFailureQuestCommand.SET_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetOrRemoveFailureQuestCommand(false),
                SetOrRemoveFailureQuestCommand.REMOVE_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetOrRemoveFollowupQuestCommand(true),
                SetOrRemoveFollowupQuestCommand.SET_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetOrRemoveFollowupQuestCommand(false),
                SetOrRemoveFollowupQuestCommand.REMOVE_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new ClearSubQuestsCommand(), ClearSubQuestsCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetFollowupRequiredForSuccessCommand(),
                SetFollowupRequiredForSuccessCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetFailAfterSemiSuccessCommand(),
                SetFailAfterSemiSuccessCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetOnDeleteCascadeCommand(), SetOnDeleteCascadeCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetQuestAmountCommand(), SetQuestAmountCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetQuestBlockCommand(), SetQuestBlockCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetQuestIgnoreCancelledEventsCommand(),
                SetQuestIgnoreCancelledEventsCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetIgnoreOppositeCommand(), SetIgnoreOppositeCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new AddOrRemoveMaterialCommand(true),
                AddOrRemoveMaterialCommand.ADD_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new AddOrRemoveMaterialCommand(false),
                AddOrRemoveMaterialCommand.REMOVE_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new ClearMaterialsCommand(), ClearMaterialsCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new AddOrRemoveEntityTypeCommand(true),
                AddOrRemoveEntityTypeCommand.ADD_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new AddOrRemoveEntityTypeCommand(false),
                AddOrRemoveEntityTypeCommand.REMOVE_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new ClearEntityTypesCommand(), ClearEntityTypesCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new AddOrRemoveGameEventCommand(true),
                AddOrRemoveGameEventCommand.ADD_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new AddOrRemoveGameEventCommand(false),
                AddOrRemoveGameEventCommand.REMOVE_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new AddOrRemoveStatisticCommand(true),
                AddOrRemoveStatisticCommand.ADD_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new AddOrRemoveStatisticCommand(false),
                AddOrRemoveStatisticCommand.REMOVE_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new ClearStatisticsCommand(), ClearStatisticsCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetGotoLocationCommand(), SetGotoLocationCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetGotoToleranceCommand(), SetGotoToleranceCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetGotoInvertedCommand(), SetGotoInvertedCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetOrRemoveQuestInteractorCommand(true),
                SetOrRemoveQuestInteractorCommand.SET_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetOrRemoveQuestInteractorCommand(false),
                SetOrRemoveQuestInteractorCommand.REMOVE_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new MoveQuestInteractorCommand(),
                MoveQuestInteractorCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetDoBubbleCommand(), SetDoBubbleCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetInteractorQuestConfirmationMessageCommand(),
                SetInteractorQuestConfirmationMessageCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetRequireConfirmationCommand(),
                SetRequireConfirmationCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetDeliveryInventoryCommand(),
                SetDeliveryInventoryCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new AddOrRemoveDamageCauseCommand(true),
                AddOrRemoveDamageCauseCommand.ADD_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new AddOrRemoveDamageCauseCommand(false),
                AddOrRemoveDamageCauseCommand.REMOVE_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new ClearDamageCausesCommand(), ClearDamageCausesCommand.COMMAND_PATH);
        for (TakeDamageQuestPropertyType type : TakeDamageQuestPropertyType.values()) {
            this.commandExecutor.addCommandMapping(new SetTakeDamageQuestPropertyCommand(type), type.commandPath);
        }
        this.commandExecutor.addCommandMapping(new SetQuestDateOrTimeCommand(true),
                SetQuestDateOrTimeCommand.DATE_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetQuestDateOrTimeCommand(false),
                SetQuestDateOrTimeCommand.TIME_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetQuestRegexCommand(true), SetQuestRegexCommand.QUOTE_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetQuestRegexCommand(false),
                SetQuestRegexCommand.REGEX_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SetCancelCommandCommand(), SetCancelCommandCommand.COMMAND_PATH);
        for (SpecificSth sth : SpecificSth.values()) {
            this.commandExecutor.addCommandMapping(new SetOverwrittenNameForSthCommand(sth, true), sth.setCommandPath);
            this.commandExecutor.addCommandMapping(new SetOverwrittenNameForSthCommand(sth, false),
                    sth.resetCommandPath);
        }
        this.commandExecutor.addCommandMapping(new ListQuestSpecificationsCommand(), "listQuestSpecifications");
        this.commandExecutor.addCommandMapping(new ListBlockBreakQuestSpecificationsCommand(),
                "listBlockBreakQuestSpecifications");
        this.commandExecutor.addCommandMapping(new ListBlockPlaceQuestSpecificationsCommand(),
                "listBlockPlaceQuestSpecifications");
        this.commandExecutor.addCommandMapping(new ListDeliveryQuestReceiverSpecificationsCommand(),
                "listDeliveryQuestReceiverSpecifications");
        this.commandExecutor.addCommandMapping(new ListDeliveryQuestContentSpecificationsCommand(),
                "listDeliveryQuestContentSpecifications");
        this.commandExecutor.addCommandMapping(new ListFishingQuestSpecificationsCommand(),
                "listFishingQuestSpecifications");
        this.commandExecutor.addCommandMapping(new ListIncreaseStatisticQuestSpecificationsCommand(),
                ListIncreaseStatisticQuestSpecificationsCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new ListKillEntitiesQuestSpecificationsCommand(),
                "listKillEntitiesQuestSpecifications");
        this.commandExecutor.addCommandMapping(new RemoveQuestSpecificationCommand(), "removeQuestSpecification");
        this.commandExecutor.addCommandMapping(new ConsolidateQuestSpecificationsCommand(),
                "consolidateQuestSpecifications");
        this.commandExecutor.addCommandMapping(new SaveOrReloadGeneratorCommand(true),
                SaveOrReloadGeneratorCommand.SAVE_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new SaveOrReloadGeneratorCommand(false),
                SaveOrReloadGeneratorCommand.RELOAD_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new AddGotoQuestSpecificationCommand(), "addGotoQuestSpecification");
        for (InteractorRequiredFor requiredFor : InteractorRequiredFor.values()) {
            this.commandExecutor.addCommandMapping(new AddOrRemoveInteractorForSpecificationCommand(requiredFor),
                    requiredFor.command);
        }
        for (MaterialCombinationRequiredFor requiredFor : MaterialCombinationRequiredFor.values()) {
            this.commandExecutor.addCommandMapping(
                    new AddOrRemoveMaterialCombinationForSpecificationCommand(true, requiredFor),
                    "add" + requiredFor.command);
            this.commandExecutor.addCommandMapping(
                    new AddOrRemoveMaterialCombinationForSpecificationCommand(false, requiredFor),
                    "remove" + requiredFor.command);
        }
        for (EntityTypeCombinationRequiredFor requiredFor : EntityTypeCombinationRequiredFor.values()) {
            this.commandExecutor.addCommandMapping(
                    new AddOrRemoveEntityTypeCombinationForSpecificationCommand(true, requiredFor),
                    "add" + requiredFor.command);
            this.commandExecutor.addCommandMapping(
                    new AddOrRemoveEntityTypeCombinationForSpecificationCommand(false, requiredFor),
                    "remove" + requiredFor.command);
        }
        this.commandExecutor.addCommandMapping(new AddOrRemoveIncreaseStatisticQuestSpecificationCommand(true),
                AddOrRemoveIncreaseStatisticQuestSpecificationCommand.ADD_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new AddOrRemoveIncreaseStatisticQuestSpecificationCommand(false),
                AddOrRemoveIncreaseStatisticQuestSpecificationCommand.REMOVE_COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new TogglePayRewardsCommand(), "setPayRewards");
        this.commandExecutor.addCommandMapping(new ToggleGenerateDailyQuestsCommand(), "setGenerateDailyQuests");
        this.commandExecutor.addCommandMapping(new SetMysteriousSpellingBookCommand(),
                SetMysteriousSpellingBookCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new ListServerFlagsCommand(), ListServerFlagsCommand.COMMAND_PATH);
        this.commandExecutor.addCommandMapping(new AddOrRemoveServerFlagCommand(true),
                AddOrRemoveServerFlagCommand.ADD_SERVER_FLAG_COMMAND);
        this.commandExecutor.addCommandMapping(new AddOrRemoveServerFlagCommand(false),
                AddOrRemoveServerFlagCommand.REMOVE_SERVER_FLAG_COMMAND);
        this.commandExecutor.addCommandMapping(new AddQuestGiverCommand(), "addQuestGiver");
        for (QuestGiverModification m : QuestGiverModification.values()) {
            this.commandExecutor.addCommandMapping(new ModifyQuestGiverCommand(m), m.command);
        }

        this.commandExecutor.addCommandMapping(new TestCommand(), "test");

        Bukkit.getPluginCommand("q").setExecutor((sender, command, label, args) -> showActiveQuestsCommand
                .onCommand(sender, command, "q", "/q", new ArgsParser(args)));
        Bukkit.getPluginCommand("achievements").setExecutor((sender, command, label, args) -> achievementInfoCommand
                .onCommand(sender, command, "achievements", "/achievements", new ArgsParser(args)));

        this.connectionAPI = CubesideUtils.getInstance().getConnectionApi();
        loadServerIdAndName();

        if (Bukkit.getPluginManager().getPlugin("CubeShop") != null) {
            registerWithCubeShop();
        }

        if (this.hasCubesideNPCs) {
            loadCitizensAPI();
        }
        if (this.hasVault) {
            loadVault();
        }

        loadQuests();
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
            questDependentSetup();
            this.bubbleMaker.setup();
        }, 2L);

        this.tickTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            tick();
        }, 3L, 1L);
    }

    private void registerWithCubeShop() {
        new Registrator().register();
    }

    private void loadServerIdAndName() {
        if (getConfig().contains("serverId")) {
            this.serverId = getConfig().getInt("serverId");
        } else {
            try {
                this.serverId = this.databaseFassade.addServerId();

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

        this.generateDailyQuests = getConfig().getBoolean("generateDailyQuests", false);
        this.payRewards = getConfig().getBoolean("payRewards", false);
        this.serverFlags = getConfig().getStringList("serverFlags").stream().map(s -> s.toLowerCase())
                .collect(Collectors.toCollection(() -> new HashSet<>()));
    }

    public boolean hasCubesideNPCsPlugin() {
        return this.hasCubesideNPCs;
    }

    private void loadCitizensAPI() {
        loadNPCs();
    }

    private void loadNPCs() {
        this.npcReg = JavaPlugin.getPlugin(CubesideNPCs.class).getSpawnedNPCs();
    }

    private void loadVault() {
        this.economy = getServer().getServicesManager().getRegistration(Economy.class).getProvider();
    }

    private void loadQuests() {
        this.questCreator.loadQuests();
    }

    @SuppressWarnings("unchecked")
    private void questDependentSetup() {
        for (WaitForDateQuest q : QuestManager.getInstance().getQuests(WaitForDateQuest.class)) {
            if (q.isLegal() && !q.isDone()) {
                // checkTime() is called during deserialization and thus durin quest loading.
                // However, that call is ignored, because the quest is not registered in the
                // QuestManager at that point. Because of this, this call is necessary.
                q.checkTime();
            }
        }

        File questGiverFolder = new File(getDataFolder(), "questGivers");
        if (questGiverFolder.exists()) {
            for (String name : questGiverFolder.list()) {
                if (!name.endsWith(".yml")) {
                    continue;
                }
                YamlConfiguration config = YamlConfiguration.loadConfiguration(new File(questGiverFolder, name));
                QuestGiver giver = (QuestGiver) config.get("giver");
                this.questGivers.put(giver.getName(), giver);
                this.questGiversByInteractor.put(giver.getInteractor(), giver);
                addProtecting(giver);
            }
        }

        List<String> dailyQuestGiverNames = (List<String>) getConfig().get("dailyQuestGivers");
        if (dailyQuestGiverNames != null) {
            for (String name : dailyQuestGiverNames) {
                QuestGiver giver = this.questGivers.get(name);
                if (giver != null) {
                    this.dailyQuestGivers.add(giver);
                } else {
                    getLogger().log(Level.WARNING, "Unknown dailyQuestGiver: " + name);
                }
            }
        }

        List<Integer> autoGivenQuestIds = getConfig().getIntegerList("autoGivenQuests");
        if (autoGivenQuestIds != null) {
            for (int questId : autoGivenQuestIds) {
                Quest quest = QuestManager.getInstance().getQuest(questId);
                if (quest != null) {
                    this.autoGivenQuests.add(quest);
                } else {
                    getLogger().log(Level.WARNING, "Unknown autGivenQuest: " + questId);
                }
            }
        }

        this.questGenerator = QuestGenerator.getInstance();
        this.questGenerator.checkForDelegatedGeneration();
    }

    @Override
    public void onDisable() {
        for (Integer id : this.updateOnDisable) {
            Quest q = QuestManager.getInstance().getQuest(id);
            if (q != null) {
                q.updateIfReal();
            }
        }

        this.daemonTimer.cancel();
        if (this.tickTask != null && (Bukkit.getScheduler().isQueued(this.tickTask)
                || Bukkit.getScheduler().isCurrentlyRunning(this.tickTask))) {
            Bukkit.getScheduler().cancelTask(this.tickTask);
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData data = getPlayerData(player);
            data.updateCachedStates();
        }
    }

    private void tick() {
        this.tick++;

        this.eventListener.tick();
        this.bubbleMaker.tick(this.tick);
        if (this.generateDailyQuests && (this.questGenerator.getLastGeneratedForDay() == null
                || LocalDate.now().isAfter(this.questGenerator.getLastGeneratedForDay()))) {
            this.questGenerator.generateDailyQuests();
        }
    }

    public long getTickCount() {
        return this.tick;
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

    public void updateQuestGenerator() {
        this.questGenerator = QuestGenerator.getInstance();
    }

    public EventListener getEventListener() {
        return this.eventListener;
    }

    public InteractionConfirmationHandler getInteractionConfirmationHandler() {
        return this.interactionConfirmationHandler;
    }

    public LogHandler getLogHandler() {
        return this.logHandler;
    }

    public InteractorBubbleMaker getBubbleMaker() {
        return this.bubbleMaker;
    }

    public ConnectionAPI getConnectionAPI() {
        return this.connectionAPI;
    }

    public SpawnedNPCs getNPCReg() {
        if (!hasCubesideNPCsPlugin()) {
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

    public Set<String> getServerFlags() {
        return Collections.unmodifiableSet(this.serverFlags);
    }

    public boolean hasServerFlag(String flag) {
        return this.serverFlags.contains(flag.toLowerCase());
    }

    public boolean addServerFlag(String flag) {
        if (this.serverFlags.add(flag.toLowerCase())) {
            getConfig().set("serverFlags", new ArrayList<>(this.serverFlags));
            saveConfig();
            return true;
        }
        return false;
    }

    public boolean removeServerFlag(String flag) {
        if (this.serverFlags.remove(flag.toLowerCase())) {
            getConfig().set("serverFlags", new ArrayList<>(this.serverFlags));
            saveConfig();
            return true;
        }
        return false;
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
            this.databaseFassade.setServerName();

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
            return this.databaseFassade.getOtherBungeeServerNames();
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
        return this.databaseFassade;
    }

    public SQLConfig getSQLConfigData() {
        return this.sqlConfig;
    }

    public PlayerUUIDCache getPlayerUUIDCache() {
        return this.playerUUIDCache;
    }

    public CubesideStatisticsAPI getCubesideStatistics() {
        return this.statistics;
    }

    public NMSUtils getNmsUtils() {
        return this.nmsUtils;
    }

    public PlayerData getPlayerData(OfflinePlayer player) {
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
            pd.loadInitialData();
        }
        return pd;
    }

    public void unloadPlayerData(UUID id) {
        this.playerData.remove(id);
    }

    public Collection<PlayerData> getLoadedPlayerData() {
        return this.playerData.values();
    }

    public void transferPlayer(UUID oldId, UUID newId) {
        if (CubesideUtils.getInstance().getGlobalDataHelper().isOnAnyServer(oldId)
                || CubesideUtils.getInstance().getGlobalDataHelper().isOnAnyServer(newId)) {
            throw new IllegalStateException("Neither player may be online.");
        }

        unloadPlayerData(oldId);
        unloadPlayerData(newId);

        try {
            this.databaseFassade.transferPlayer(oldId, newId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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

        addProtecting(giver);

        this.bubbleMaker.registerBubbleTarget(new QuestGiverBubbleTarget(giver));
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

        removeProtecting(giver);

        File folder = new File(CubeQuest.getInstance().getDataFolder(), "questGivers");
        File configFile = new File(folder, giver.getName() + ".yml");
        if (!configFile.delete()) {
            getLogger().log(Level.WARNING, "Could not delete config \"" + giver.getName() + ".yml\" for QuestGiver.");
        }

        this.bubbleMaker.unregisterBubbleTarget(new QuestGiverBubbleTarget(giver));
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
                List<Quest> generated = this.questGenerator.getTodaysDailyQuests();
                if (generated != null) {
                    for (Quest q : generated) {
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

    public Set<Quest> getAutoGivenQuests() {
        return Collections.unmodifiableSet(this.autoGivenQuests);
    }

    public boolean isAutoGivenQuest(Quest quest) {
        return this.autoGivenQuests.contains(quest);
    }

    public boolean addAutoGivenQuest(int questId) {
        return addAutoGivenQuest(QuestManager.getInstance().getQuest(questId));
    }

    public boolean addAutoGivenQuest(Quest quest) {
        if (this.autoGivenQuests.add(quest)) {
            saveAutoGivenQuests();
            return true;
        }
        return false;
    }

    public boolean removeAutoGivenQuest(int questId) {
        return removeAutoGivenQuest(QuestManager.getInstance().getQuest(questId));
    }

    public boolean removeAutoGivenQuest(Quest quest) {
        if (this.autoGivenQuests.remove(quest)) {
            saveAutoGivenQuests();
            return true;
        }
        return false;
    }

    private void saveAutoGivenQuests() {
        List<Integer> autoGivenQuestIds = new ArrayList<>();
        this.autoGivenQuests.forEach(aq -> autoGivenQuestIds.add(aq.getId()));
        getConfig().set("autoGivenQuests", autoGivenQuestIds);
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
        tcAPI.addItem(Bukkit.getOfflinePlayer(playerId), display, reward.getItems(), reward.getCubes());
    }

    public void payCubes(Player player, int cubes) {
        payCubes(player.getUniqueId(), cubes);
    }

    public void payCubes(UUID playerId, int cubes) {
        if (!this.hasVault) {
            getLogger().log(Level.SEVERE,
                    "Could not pay " + cubes + " to player with id " + playerId.toString() + ": Vault not found.");
            return;
        }

        payCubesInternal(playerId, cubes);
    }

    private void payCubesInternal(UUID playerId, int cubes) {
        EconomyResponse response = this.economy.depositPlayer(Bukkit.getOfflinePlayer(playerId), cubes);
        if (!response.transactionSuccess()) {
            getLogger().log(Level.SEVERE, "Could not pay " + cubes + " cubes to player " + playerId.toString()
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
        this.getPlayerData(playerId).changeXp(xp);
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

    public Interactor getAliased(Interactor interactor) {
        return this.interactorAliases.getOrDefault(interactor, interactor);
    }

    public Interactor getAliasedOrNull(Interactor interactor) {
        return this.interactorAliases.get(interactor);
    }

    public Interactor setAlias(Interactor alias, Interactor original) {
        Interactor result = this.interactorAliases.put(alias, Objects.requireNonNull(original));
        saveInteractorAliases();
        return result;
    }

    public boolean removeAlias(Interactor alias) {
        boolean result = this.interactorAliases.remove(alias) != null;
        saveInteractorAliases();
        return result;
    }

    public void saveInteractorAliases() {
        List<Pair<Interactor, Interactor>> serialized = this.interactorAliases.entrySet().stream()
                .map(entry -> new SerializablePair<>(entry.getKey(), entry.getValue())).collect(Collectors.toList());
        getConfig().set("interactorAliases", serialized);
        saveConfig();
    }

    public Set<InteractorProtecting> getProtectedBy(Interactor interactor) {
        return this.interactorProtecting.get(interactor);
    }

    public void addProtecting(InteractorProtecting protecting) {
        Interactor interactor = protecting.getInteractor();
        if (interactor == null) {
            return;
        }
        this.interactorProtecting.put(interactor, protecting);
    }

    public void removeProtecting(InteractorProtecting protecting) {
        this.interactorProtecting.remove(protecting.getInteractor(), protecting);
    }

    public void addUpdateOnDisable(Quest quest) {
        this.updateOnDisable.add(quest.getId());
    }

    public void sendToGlobalDataChannel(byte[] msgarry) {
        getConnectionAPI().sendData(getGlobalDataChannelName(), msgarry);
    }

    public String getGlobalDataChannelName() {
        return this.globalDataChannelName;
    }
}
