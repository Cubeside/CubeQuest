package de.iani.cubequest.commands;

import de.cubeside.connection.util.GlobalLocation;
import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.Reward;
import de.iani.cubequest.actions.ActionBarMessageAction;
import de.iani.cubequest.actions.ActionLocation;
import de.iani.cubequest.actions.ActionType;
import de.iani.cubequest.actions.BossBarMessageAction;
import de.iani.cubequest.actions.ChatMessageAction;
import de.iani.cubequest.actions.DelayableAction;
import de.iani.cubequest.actions.EffectAction;
import de.iani.cubequest.actions.EffectAction.EffectData;
import de.iani.cubequest.actions.FixedActionLocation;
import de.iani.cubequest.actions.ParticleAction;
import de.iani.cubequest.actions.ParticleAction.ParticleData;
import de.iani.cubequest.actions.PlayerActionLocation;
import de.iani.cubequest.actions.PotionEffectAction;
import de.iani.cubequest.actions.QuestAction;
import de.iani.cubequest.actions.RedstoneSignalAction;
import de.iani.cubequest.actions.RemovePotionEffectAction;
import de.iani.cubequest.actions.RewardAction;
import de.iani.cubequest.actions.SoundAction;
import de.iani.cubequest.actions.SpawnEntityAction;
import de.iani.cubequest.actions.TeleportationAction;
import de.iani.cubequest.actions.TitleMessageAction;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.SafeLocation;
import de.iani.cubesideutils.Pair;
import de.iani.cubesideutils.StringUtil;
import de.iani.cubesideutils.bukkit.StringUtilBukkit;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.bukkit.items.ItemGroups;
import de.iani.cubesideutils.bukkit.items.ItemStacks;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;


public class AddEditOrRemoveActionCommand extends SubCommand implements Listener {
    
    private static final Set<String> EDIT_ACTION_STRINGS;
    private static final Set<String> DELAY_STRINGS;
    
    static {
        Set<String> editActionStrings = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        editActionStrings.add("edit");
        editActionStrings.add("append");
        EDIT_ACTION_STRINGS = Collections.unmodifiableSet(editActionStrings);
        
        Set<String> delayStrings = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        delayStrings.add("delayed");
        DELAY_STRINGS = Collections.unmodifiableSet(delayStrings);
    }
    
    public static enum ActionTime {
        
        GIVE("Vergabe"), SUCCESS("Erfolgs"), FAIL("Misserfolgs");
        
        public final String commandPath;
        public final String fullCommand;
        
        public final String germanPrefix;
        
        private ActionTime(String germanPrefix) {
            this.commandPath = name().toLowerCase() + "Action";
            this.fullCommand = "quest " + this.commandPath;
            
            this.germanPrefix = germanPrefix;
        }
        
        public List<QuestAction> getQuestActions(Quest quest) {
            switch (this) {
                case GIVE:
                    return quest.getGiveActions();
                case SUCCESS:
                    return quest.getSuccessActions();
                case FAIL:
                    return quest.getFailActions();
            }
            throw new AssertionError("Unknown ActionTime " + this + "!");
        }
        
        public void addAction(Quest quest, QuestAction action) {
            switch (this) {
                case GIVE:
                    quest.addGiveAction(action);
                    return;
                case SUCCESS:
                    quest.addSuccessAction(action);
                    return;
                case FAIL:
                    quest.addFailAction(action);
                    return;
            }
            throw new AssertionError("Unknown ActionTime " + this + "!");
        }
        
        public QuestAction replaceAction(Quest quest, int actionIndex, QuestAction action) {
            switch (this) {
                case GIVE:
                    return quest.replaceGiveAction(actionIndex, action);
                case SUCCESS:
                    return quest.replaceSuccessAction(actionIndex, action);
                case FAIL:
                    return quest.replaceFailAction(actionIndex, action);
            }
            throw new AssertionError("Unknown ActionTime " + this + "!");
        }
        
        public QuestAction removeAction(Quest quest, int actionIndex) {
            switch (this) {
                case GIVE:
                    return quest.removeGiveAction(actionIndex);
                case SUCCESS:
                    return quest.removeSuccessAction(actionIndex);
                case FAIL:
                    return quest.removeFailAction(actionIndex);
            }
            throw new AssertionError("Unknown ActionTime " + this + "!");
        }
    }
    
    public static enum RewardAttribute {
        
        CUBES("Cubes"), QUEST_POINTS("Quest-Points"), XP("XP");
        
        public final String name;
        
        public static RewardAttribute matchAttribute(String arg) {
            try {
                return valueOf(arg.toUpperCase());
            } catch (IllegalArgumentException e) {
                // ingore
            }
            if (arg.toLowerCase().endsWith("points")) {
                return QUEST_POINTS;
            }
            return null;
        }
        
        private RewardAttribute(String name) {
            this.name = name;
        }
        
        public int getValue(Reward reward) {
            switch (this) {
                case CUBES:
                    return reward.getCubes();
                case QUEST_POINTS:
                    return reward.getQuestPoints();
                case XP:
                    return reward.getXp();
            }
            throw new AssertionError("Unknown RewardAttribute " + this + "!");
        }
        
    }
    
    private static class ActionParseException extends RuntimeException {
        
        private static final long serialVersionUID = 1L;
        
    }
    
    private class PreparedReward {
        
        private Quest quest;
        private int editedIndex;
        private int cubes;
        private int questPoints;
        private int xp;
        
        public PreparedReward(Quest quest, int editedIndex, Map<RewardAttribute, Integer> setAttributes) {
            if (editedIndex < 0) {
                init(quest, editedIndex, setAttributes.getOrDefault(RewardAttribute.CUBES, 0),
                        setAttributes.getOrDefault(RewardAttribute.QUEST_POINTS, 0),
                        setAttributes.getOrDefault(RewardAttribute.XP, 0));
            } else {
                List<QuestAction> actions = AddEditOrRemoveActionCommand.this.time.getQuestActions(quest);
                if (actions.size() <= editedIndex) {
                    throw new IllegalArgumentException("Index of edited action out of bounds.");
                }
                
                QuestAction action = actions.get(editedIndex);
                if (!(action instanceof RewardAction)) {
                    throw new IllegalArgumentException("Edited action is no RewardAction.");
                }
                
                Reward edited = ((RewardAction) action).getReward();
                init(quest, editedIndex, setAttributes.getOrDefault(RewardAttribute.CUBES, edited.getCubes()),
                        setAttributes.getOrDefault(RewardAttribute.QUEST_POINTS, edited.getQuestPoints()),
                        setAttributes.getOrDefault(RewardAttribute.XP, edited.getXp()));
            }
        }
        
        private void init(Quest quest, int editedIndex, int cubes, int questPoints, int xp) {
            this.quest = quest;
            this.editedIndex = editedIndex;
            this.cubes = cubes;
            this.questPoints = questPoints;
            this.xp = xp;
        }
        
        public Quest getQuest() {
            return this.quest;
        }
        
        public int getEditedIndex() {
            return this.editedIndex;
        }
        
        public int getCubes() {
            return this.cubes;
        }
        
        public int getQuestPoints() {
            return this.questPoints;
        }
        
        public int getXp() {
            return this.xp;
        }
    }
    
    private ActionTime time;
    
    private Map<UUID, PreparedReward> currentlyEditingReward;
    
    public AddEditOrRemoveActionCommand(ActionTime time) {
        this.time = time;
        this.currentlyEditingReward = new HashMap<>();
        
        Bukkit.getPluginManager().registerEvents(this, CubeQuest.getInstance());
        CubeQuest.getInstance().getEventListener()
                .addOnPlayerQuit(player -> this.currentlyEditingReward.remove(player.getUniqueId()));
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {
        
        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            ChatAndTextUtil.sendNotEditingQuestMessage(sender);
            return true;
        }
        
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Bitte gib an, ob eine Aktion hinzugefügt, bearbeitet oder entfernt werden soll.");
            return true;
        }
        
        String modificationType = args.next();
        if (modificationType.equalsIgnoreCase("add")) {
            addAction(sender, args, quest);
            return true;
        } else if (modificationType.equalsIgnoreCase("remove")) {
            removeAction(sender, args, quest);
            return true;
        } else if (EDIT_ACTION_STRINGS.contains(modificationType)) {
            editAction(sender, args, quest);
            return true;
        }
        
        ChatAndTextUtil.sendWarningMessage(sender,
                "Bitte gib an, ob eine Aktion hinzugefügt (add), bearbeitet (edit/append) oder entfernt (remove) werden soll.");
        return true;
    }
    
    private void addAction(CommandSender sender, ArgsParser args, Quest quest) {
        QuestAction action;
        try {
            action = parseAction(sender, args, quest);
        } catch (ActionParseException e) {
            return;
        }
        
        switch (this.time) {
            case GIVE:
                quest.addGiveAction(action);
                break;
            case SUCCESS:
                quest.addSuccessAction(action);
                break;
            case FAIL:
                quest.addFailAction(action);
                break;
            default:
                throw new AssertionError("Unknown ActionTime " + this.time + "!");
        }
        
        ChatAndTextUtil.sendNormalMessage(sender, this.time.germanPrefix + "aktion hinzugefügt:");
        ChatAndTextUtil.sendBaseComponent(sender, action.getActionInfo());
    }
    
    private void editAction(CommandSender sender, ArgsParser args, Quest quest) {
        int editedIndex = args.getNext(0) - 1;
        if (editedIndex < 0) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Bitte gib den Index der zu beareitenden Aktion als positive Ganzzahl an.");
            return;
        }
        
        List<QuestAction> actions = this.time.getQuestActions(quest);
        if (editedIndex >= actions.size()) {
            ChatAndTextUtil.sendWarningMessage(sender, "So viele Aktionen hat die bearbeitete Quest nicht.");
            return;
        }
        
        try {
            QuestAction edited = actions.get(editedIndex);
            if (edited instanceof ChatMessageAction) {
                QuestAction action = parseChatMessageAction(sender, args, quest, editedIndex,
                        ((ChatMessageAction) edited).getDelay());
                QuestAction old = this.time.replaceAction(quest, editedIndex, action);
                ChatAndTextUtil.sendNormalMessage(sender, this.time.germanPrefix + "aktion bearbeitet. Alt:");
                ChatAndTextUtil.sendBaseComponent(sender, old.getActionInfo());
                ChatAndTextUtil.sendNormalMessage(sender, "Neu:");
                ChatAndTextUtil.sendBaseComponent(sender, action.getActionInfo());
                return;
            } else if (edited instanceof RewardAction) {
                prepareRewardAction(sender, args, quest, editedIndex);
                return;
            } else {
                ChatAndTextUtil.sendWarningMessage(sender, "Die " + (editedIndex + 1) + ". " + this.time.germanPrefix
                        + "aktion kann nicht bearbeited werden.");
                return;
            }
        } catch (ActionParseException e) {
            return;
        }
    }
    
    private void removeAction(CommandSender sender, ArgsParser args, Quest quest) {
        int actionIndex = args.getNext(0) - 1;
        if (actionIndex < 0) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Bitte gib den Index der zu entfernenden Aktion als positive Ganzzahl an.");
            return;
        }
        
        List<QuestAction> actions = this.time.getQuestActions(quest);
        if (actionIndex >= actions.size()) {
            ChatAndTextUtil.sendWarningMessage(sender, "So viele Aktionen hat die bearbeitete Quest nicht.");
            return;
        }
        
        QuestAction removed = this.time.removeAction(quest, actionIndex);
        ChatAndTextUtil.sendNormalMessage(sender, this.time.germanPrefix + "aktion entfernt:");
        ChatAndTextUtil.sendBaseComponent(sender, removed.getActionInfo());
    }
    
    private QuestAction parseAction(CommandSender sender, ArgsParser args, Quest quest) {
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib einen Aktionstyp an.");
            throw new ActionParseException();
        }
        
        long delayTicks = 0;
        if (AddEditOrRemoveActionCommand.DELAY_STRINGS.contains(args.seeNext(null))) {
            args.next();
            if (!args.hasNext()) {
                ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Verzögerung in Ticks an.");
                throw new ActionParseException();
            }
            
            delayTicks = args.getNext(-1);
            if (delayTicks < 0) {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Bitte gib die Verzögerung in Ticks als nicht-negative Ganzzahl an.");
                throw new ActionParseException();
            }
        }
        
        String typeString = args.next();
        ActionType actionType = ActionType.match(typeString);
        if (actionType == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Aktionstyp " + typeString + " nicht gefunden.");
            throw new ActionParseException();
        }
        
        if (delayTicks != 0 && !DelayableAction.class.isAssignableFrom(actionType.concreteClass)) {
            ChatAndTextUtil.sendWarningMessage(sender, "Aktionstyp " + typeString + " kann nicht verzögert werden.");
            throw new ActionParseException();
        }
        
        if (actionType == ActionType.ACTION_BAR_MESSAGE) {
            return parseActionBarMessageAction(sender, args, quest, delayTicks);
        }
        
        if (actionType == ActionType.BOSS_BAR_MESSAGE) {
            return parseBossBarMessageAction(sender, args, quest, delayTicks);
        }
        
        if (actionType == ActionType.CHAT_MESSAGE) {
            return parseChatMessageAction(sender, args, quest, -1, delayTicks);
        }
        
        if (actionType == ActionType.REWARD) {
            prepareRewardAction(sender, args, quest, -1);
            throw new ActionParseException();
        }
        
        if (actionType == ActionType.REDSTONE_SIGNAL) {
            return parseRedstoneSignalAction(sender, args, quest);
        }
        
        if (actionType == ActionType.POTION_EFFECT) {
            return parsePotionEffectAction(sender, args, quest);
        }
        
        if (actionType == ActionType.REMOVE_POTION_EFFECT) {
            return parseRemovePotionEffectAction(sender, args, quest);
        }
        
        if (actionType == ActionType.PARTICLE) {
            return parseParticleAction(sender, args, quest, delayTicks);
        }
        
        if (actionType == ActionType.EFFECT) {
            return parseEffectAction(sender, args, quest, delayTicks);
        }
        
        if (actionType == ActionType.SOUND) {
            return parseSoundAction(sender, args, quest, delayTicks);
        }
        
        if (actionType == ActionType.SPAWN_ENTITY) {
            return parseSpawnEntityAction(sender, args, quest, delayTicks);
        }
        
        if (actionType == ActionType.TELEPORT) {
            return parseTeleportAction(sender, args, quest, delayTicks);
        }
        
        if (actionType == ActionType.TITLE_MESSAGE) {
            return parseTitleMessageAction(sender, args, quest, delayTicks);
        }
        
        throw new AssertionError("Unknown ActionType " + actionType + "!");
    }
    
    private QuestAction parseActionBarMessageAction(CommandSender sender, ArgsParser args, Quest quest,
            long delayTicks) {
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Nachricht an, die angezeigt werden soll.");
            throw new ActionParseException();
        }
        
        String message = StringUtil.convertColors(args.getAll(null));
        return new ActionBarMessageAction(delayTicks, message);
    }
    
    private QuestAction parseBossBarMessageAction(CommandSender sender, ArgsParser args, Quest quest, long delayTicks) {
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Farbe der Boss-Bar an ("
                    + Arrays.stream(BarColor.values()).map(BarColor::name).collect(Collectors.joining(", ")) + ").");
            throw new ActionParseException();
        }
        
        BarColor color = args.getNextEnum(BarColor.class, null);
        if (color == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Unbekannte Farbe.");
            throw new ActionParseException();
        }
        
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib den Stil der Boss-Bar an ("
                    + Arrays.stream(BarStyle.values()).map(BarStyle::name).collect(Collectors.joining(", ")) + ").");
            throw new ActionParseException();
        }
        
        BarStyle style = args.getNextEnum(BarStyle.class, null);
        if (style == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Unbekannter Stil.");
            throw new ActionParseException();
        }
        
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Bitte gib die Dauer in Ticks an, für die die Nachricht angezeigt werden soll.");
            throw new ActionParseException();
        }
        
        long duration = args.getNext(-1);
        if (duration <= 0) {
            ChatAndTextUtil.sendWarningMessage(sender, "Die Dauer muss eine positive Ganzzahl sein.");
            throw new ActionParseException();
        }
        
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Nachricht an, die angezeigt werden soll.");
            throw new ActionParseException();
        }
        
        String message = StringUtil.convertEscaped(StringUtil.convertColors(args.getAll(null)));
        return new BossBarMessageAction(delayTicks, message, color, style, duration);
    }
    
    private QuestAction parseChatMessageAction(CommandSender sender, ArgsParser args, Quest quest, int editedIndex,
            long delayTicks) {
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Nachricht an, die "
                    + (editedIndex < 0 ? "verschickt" : "angehangen") + " werden soll.");
            throw new ActionParseException();
        }
        
        String message = StringUtil.convertEscaped(StringUtil.convertColors(args.getAll(null)));
        if (editedIndex >= 0) {
            ChatMessageAction edited = (ChatMessageAction) this.time.getQuestActions(quest).get(editedIndex);
            message = edited.getMessage() + " " + message;
        }
        
        return new ChatMessageAction(delayTicks, message);
    }
    
    private void prepareRewardAction(CommandSender sender, ArgsParser args, Quest quest, int editedIndex) {
        if (!(sender instanceof Player)) {
            ChatAndTextUtil.sendErrorMessage(sender, "Dieser Befehl kann nur von Spielern ausgeführt werden.");
            throw new ActionParseException();
        }
        Player player = (Player) sender;
        
        if (this.currentlyEditingReward.containsKey(player.getUniqueId())) {
            ChatAndTextUtil.sendWarningMessage(sender, "Du bearbeitest bereits eine Belohnung.");
            throw new ActionParseException();
        }
        
        Map<RewardAttribute, Integer> setAttributes = new EnumMap<>(RewardAttribute.class);
        
        while (args.hasNext()) {
            String combinedString = args.next();
            String[] splitStrings = combinedString.split(Pattern.quote(":"));
            if (splitStrings.length != 2) {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Jedes Attribut muss in der Form Attributsname:Wert angegeben werden.");
                throw new ActionParseException();
            }
            
            String attributeString = splitStrings[0];
            RewardAttribute attribute = RewardAttribute.matchAttribute(attributeString);
            if (attribute == null) {
                ChatAndTextUtil.sendWarningMessage(sender, "Attribut " + attributeString + " nicht gefunden.");
                throw new ActionParseException();
            }
            if (setAttributes.containsKey(attribute)) {
                ChatAndTextUtil.sendWarningMessage(sender, "Ein Attribut kann nur einmal gesetzt werden.");
                throw new ActionParseException();
            }
            
            String valueString = splitStrings[1];
            Integer value;
            try {
                value = Integer.parseInt(valueString);
                if (value < 0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException e) {
                ChatAndTextUtil.sendWarningMessage(sender, "Die Werte müssen nicht-negative Ganzzahlen sein.");
                throw new ActionParseException();
            }
            
            setAttributes.put(attribute, value);
        }
        
        Inventory inventory =
                Bukkit.createInventory(player, 27, this.time.germanPrefix + "belohnung [Quest " + quest.getId() + "]");
        if (editedIndex >= 0) {
            RewardAction edited = (RewardAction) this.time.getQuestActions(quest).get(editedIndex);
            inventory.addItem(edited.getReward().getItems());
        }
        player.openInventory(inventory);
        
        this.currentlyEditingReward.put(player.getUniqueId(), new PreparedReward(quest, editedIndex, setAttributes));
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClosedEvent(InventoryCloseEvent event) {
        PreparedReward prepared = this.currentlyEditingReward.remove(event.getPlayer().getUniqueId());
        if (prepared == null) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        Quest quest = prepared.getQuest();
        
        if (prepared.getEditedIndex() >= 0 && this.time.getQuestActions(quest).size() <= prepared.getEditedIndex()) {
            ChatAndTextUtil.sendWarningMessage(player, "Die zu bearbeitende Aktion existiert nicht mehr.");
            return;
        }
        
        ItemStack[] items = ItemStacks.shrink(event.getInventory().getContents());
        event.getInventory().clear();
        event.getInventory().addItem(items);
        items = event.getInventory().getContents();
        
        Reward reward = new Reward(prepared.getCubes(), prepared.getQuestPoints(), prepared.getXp(), items);
        if (reward.isEmpty()) {
            ChatAndTextUtil.sendWarningMessage(player, "Die Belohnung darf nicht leer sein.");
            return;
        }
        
        RewardAction result = new RewardAction(reward);
        
        if (prepared.getEditedIndex() < 0) {
            this.time.addAction(quest, result);
            ChatAndTextUtil.sendNormalMessage(player, this.time.germanPrefix + "aktion hinzugefügt:");
        } else {
            QuestAction old = this.time.replaceAction(quest, prepared.getEditedIndex(), result);
            ChatAndTextUtil.sendNormalMessage(player, this.time.germanPrefix + "aktion bearbeitet. Alt:");
            ChatAndTextUtil.sendBaseComponent(player, old.getActionInfo());
            ChatAndTextUtil.sendNormalMessage(player, "Neu:");
        }
        ChatAndTextUtil.sendBaseComponent(player, result.getActionInfo());
    }
    
    private QuestAction parseRedstoneSignalAction(CommandSender sender, ArgsParser args, Quest quest) {
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Dauer des Signals in Ticks an.");
            throw new ActionParseException();
        }
        
        int ticks = args.getNext(-1);
        if (ticks <= 0) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Dauer in Ticks als positive Ganzzahl an.");
            throw new ActionParseException();
        }
        
        SafeLocation location = parseSafeLocation(sender, args, quest);
        return new RedstoneSignalAction(location, ticks);
    }
    
    private QuestAction parsePotionEffectAction(CommandSender sender, ArgsParser args, Quest quest) {
        PotionEffectType effectType = parsePotionEffectType(sender, quest, args);
        
        int duration = 1;
        if (!effectType.isInstant()) {
            duration = args.getNext(-1);
            if (duration <= 0) {
                ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Dauer des Effekts in Ticks an.");
                throw new ActionParseException();
            }
        }
        
        int amplifier = args.hasNext() ? args.getNext(-1) : 1;
        amplifier--;
        if (amplifier < 0) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Stärke des Effekts als positive Ganzzahl an.");
            throw new ActionParseException();
        }
        
        boolean ambient;
        if (!args.hasNext()) {
            ambient = false;
        } else {
            String ambientString = args.next();
            if (StringUtil.TRUE_STRINGS.contains(ambientString)) {
                ambient = true;
            } else if (StringUtil.FALSE_STRINGS.contains(ambientString)) {
                ambient = false;
            } else {
                ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib an, ob der Effekt \"ambient\" ist (true/false).");
                throw new ActionParseException();
            }
        }
        
        boolean particles;
        if (!args.hasNext()) {
            particles = true;
        } else {
            String particlesString = args.next();
            if (StringUtil.TRUE_STRINGS.contains(particlesString)) {
                particles = true;
            } else if (StringUtil.FALSE_STRINGS.contains(particlesString)) {
                particles = false;
            } else {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Bitte gib an, ob der Effekt Partikel erzeugen soll (true/false).");
                throw new ActionParseException();
            }
        }
        
        boolean icon;
        if (!args.hasNext()) {
            icon = true;
        } else {
            String iconString = args.next();
            if (StringUtil.TRUE_STRINGS.contains(iconString)) {
                icon = true;
            } else if (StringUtil.FALSE_STRINGS.contains(iconString)) {
                icon = false;
            } else {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Bitte gib an, ob der Effekt ein Icon anzeigen soll (true/false).");
                throw new ActionParseException();
            }
        }
        
        return new PotionEffectAction(new PotionEffect(effectType, duration, amplifier, ambient, particles, icon));
    }
    
    private QuestAction parseRemovePotionEffectAction(CommandSender sender, ArgsParser args, Quest quest) {
        PotionEffectType effectType = parsePotionEffectType(sender, quest, args);
        return new RemovePotionEffectAction(effectType);
    }
    
    private QuestAction parseParticleAction(CommandSender sender, ArgsParser args, Quest quest, long delayTicks) {
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib eine Partikel-Art an.");
            throw new ActionParseException();
        }
        
        String particleString = args.next();
        Particle particle;
        try {
            particle = Particle.valueOf(particleString.toUpperCase());
        } catch (IllegalArgumentException e) {
            ChatAndTextUtil.sendWarningMessage(sender, "Partikel-Art " + particleString + " nicht gefunden.");
            throw new ActionParseException();
        }
        
        double amountPerTick = args.getNext(-1.0);
        if (amountPerTick <= 0) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Bitte gib die Anzahl Partikel je Tick als positive Kommazahl an.");
            throw new ActionParseException();
        }
        
        int numberOfTicks = args.getNext(-1);
        if (numberOfTicks <= 0) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Anzahl an Tick als positive Ganzzahl an.");
            throw new ActionParseException();
        }
        
        double offsetX = args.getNext(-1.0);
        if (offsetX < 0.0) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Bitte gib den Offset in x-Richtung als nicht-negative Kommazahl an.");
            throw new ActionParseException();
        }
        
        double offsetY = args.getNext(-1.0);
        if (offsetY < 0.0) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Bitte gib den Offset in y-Richtung als nicht-negative Kommazahl an.");
            throw new ActionParseException();
        }
        
        double offsetZ = args.getNext(-1.0);
        if (offsetZ < 0.0) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Bitte gib den Offset in z-Richtung als nicht-negative Kommazahl an.");
            throw new ActionParseException();
        }
        
        double extra;
        try {
            if (!args.hasNext()) {
                throw new NumberFormatException();
            }
            extra = Double.parseDouble(args.next());
        } catch (NumberFormatException e) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib den Parameter \"extra\" als Kommazahl an.");
            throw new ActionParseException();
        }
        
        Object data = null;
        ParticleData.Type particleDataType = ParticleData.Type.fromDataType(particle.getDataType());
        if (particleDataType != null) {
            switch (particleDataType) {
                case DUST_OPTIONS:
                    if (!args.hasNext()) {
                        ChatAndTextUtil.sendWarningMessage(sender,
                                "Bitte gib die Farbe der Partikel durch ihren Namen oder als Hexadezimal-RGB-Wert an.");
                        throw new ActionParseException();
                    }
                    
                    Color color;
                    String colorString = args.next();
                    color = StringUtilBukkit.getConstantColor(colorString.replace('_', ' '));
                    if (color == null) {
                        try {
                            int rgb = Integer.parseInt(colorString, 16);
                            if (rgb < 0) {
                                ChatAndTextUtil.sendWarningMessage(sender, "Der RGB-Wert darf nicht negativ sein.");
                                throw new ActionParseException();
                            }
                            if (rgb > 0xFFFFFF) {
                                ChatAndTextUtil.sendWarningMessage(sender,
                                        "Der maximale, gültige RGB-Wert ist FFFFFF.");
                                throw new ActionParseException();
                            }
                            color = Color.fromRGB(rgb);
                        } catch (NumberFormatException e) {
                            ChatAndTextUtil.sendWarningMessage(sender, "Farbe " + colorString + " nicht erkannt.");
                            throw new ActionParseException();
                        }
                    }
                    
                    float size = (float) args.getNext(-1.0);
                    if (size <= 0.0) {
                        ChatAndTextUtil.sendWarningMessage(sender,
                                "Bitte gib die Größe der Partikel als positive Kommazahl an.");
                        throw new ActionParseException();
                    }
                    
                    data = new DustOptions(color, size);
                    break;
                
                case ITEM_STACK:
                    data = new ItemStack(parseMaterial(sender, args, quest), 1);
                    break;
                
                case BLOCK_DATA:
                    Material mat = parseMaterial(sender, args, quest);
                    if (!mat.isBlock()) {
                        ChatAndTextUtil.sendWarningMessage(sender,
                                "Dieser Partikel erfordert einen Block, nicht " + mat + ".");
                        throw new ActionParseException();
                    }
                    data = mat.createBlockData();
                    break;
            }
        }
        
        ActionLocation location = parseActionLocation(sender, args, quest);
        
        return new ParticleAction(delayTicks, particle, amountPerTick, numberOfTicks, location, offsetX, offsetY,
                offsetZ, extra, new ParticleData(data));
    }
    
    private QuestAction parseEffectAction(CommandSender sender, ArgsParser args, Quest quest, long delayTicks) {
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib einne Effekt an.");
            throw new ActionParseException();
        }
        
        String effectString = args.next();
        Effect effect;
        try {
            effect = Effect.valueOf(effectString.toUpperCase());
        } catch (IllegalArgumentException e) {
            ChatAndTextUtil.sendWarningMessage(sender, "Effekt " + effectString + " nicht gefunden.");
            throw new ActionParseException();
        }
        
        if (effect == Effect.POTION_BREAK) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Der Effekt POTION_BREAK kann aus technischen Gründen leider nicht verwendet werden.");
            throw new ActionParseException();
        }
        
        Object data = null;
        EffectData.Type effectDataType = EffectData.Type.fromDataType(effect.getData());
        if (effectDataType != null) {
            switch (effectDataType) {
                case MATERIAL:
                    data = parseMaterial(sender, args, quest);
                    
                    if (effect == Effect.RECORD_PLAY && !ItemGroups.isMusicDisc((Material) data)) {
                        ChatAndTextUtil.sendWarningMessage(sender,
                                "Dieser Effekt erfordert eine Schallplatte, nicht " + data + ".");
                        throw new ActionParseException();
                    }
                    if (effect == Effect.STEP_SOUND && !((Material) data).isBlock()) {
                        ChatAndTextUtil.sendWarningMessage(sender,
                                "Dieser Effekt erfordert einen Block, nicht " + data + ".");
                        throw new ActionParseException();
                    }
                    break;
                
                case BLOCK_FACE:
                    if (!args.hasNext()) {
                        ChatAndTextUtil.sendWarningMessage(sender,
                                "Bitte gib eine Blockseite (z.B. NORTH, DOWN, SOUTH_EAST, ...) als zusätzlichen Parameter für diesen Effekt an.");
                        throw new ActionParseException();
                    }
                    
                    String blockFaceString = args.next();
                    try {
                        data = BlockFace.valueOf(blockFaceString);
                    } catch (IllegalArgumentException e) {
                        ChatAndTextUtil.sendWarningMessage(sender,
                                "Blockseite " + blockFaceString + " nicht gefunden.");
                        throw new ActionParseException();
                    }
                    break;
                
                case INTEGER:
                    if (!args.hasNext()) {
                        ChatAndTextUtil.sendWarningMessage(sender,
                                "Bitte gib eine Ganzzahl als zusätzlichen Parameter für diesen Effekt an.");
                        throw new ActionParseException();
                    }
                    
                    try {
                        data = Integer.parseInt(args.next());
                    } catch (NumberFormatException e) {
                        ChatAndTextUtil.sendWarningMessage(sender,
                                "Bitte gib eine Ganzzahl als zusätzlichen Parameter für diesen Effekt an.");
                        throw new ActionParseException();
                    }
                    
                    if (effect == Effect.VILLAGER_PLANT_GROW && ((Integer) data) <= 0) {
                        ChatAndTextUtil.sendWarningMessage(sender,
                                "Dieser Effekt erfordert eine positive Ganzzahl, keine negative oder 0.");
                        throw new ActionParseException();
                    }
                    break;
            }
        }
        
        ActionLocation location = parseActionLocation(sender, args, quest);
        
        return new EffectAction(delayTicks, effect, location, new EffectData(data));
    }
    
    private QuestAction parseSoundAction(CommandSender sender, ArgsParser args, Quest quest, long delayTicks) {
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib einen Sound an.");
            throw new ActionParseException();
        }
        
        String soundString = args.next();
        Sound sound;
        try {
            sound = Sound.valueOf(soundString.toUpperCase());
        } catch (IllegalArgumentException e) {
            ChatAndTextUtil.sendWarningMessage(sender, "Sound " + soundString + " nicht gefunden.");
            throw new ActionParseException();
        }
        
        float volume = (float) args.getNext(-1.0);
        if (volume <= 0.0) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Bitte gib die Lautstärke des Geräuschs als positive Kommazahl an.");
            throw new ActionParseException();
        }
        
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Tonhöhe des Geräuschs an.");
            throw new ActionParseException();
        }
        float pitch;
        try {
            pitch = Float.parseFloat(args.next());
            if (pitch < 0.5 || pitch > 2.0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Bitte gib die Tonhöhe des Geräuschs als Kommazahl von 0.5 bis 2.0 an.");
            throw new ActionParseException();
        }
        
        ActionLocation location = parseActionLocation(sender, args, quest);
        
        return new SoundAction(delayTicks, sound, volume, pitch, location);
    }
    
    private QuestAction parseSpawnEntityAction(CommandSender sender, ArgsParser args, Quest quest, long delayTicks) {
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib einen Entity-Typ an.");
            throw new ActionParseException();
        }
        
        String entityTypeString = args.next();
        EntityType entityType;
        try {
            entityType = EntityType.valueOf(entityTypeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            ChatAndTextUtil.sendWarningMessage(sender, "Entity-Typ " + entityTypeString + " nicht gefunden.");
            throw new ActionParseException();
        }
        
        if (entityType == EntityType.UNKNOWN || entityType == EntityType.PLAYER) {
            ChatAndTextUtil.sendWarningMessage(sender, "Entity-Typ " + entityType + " nicht erlaut.");
            throw new ActionParseException();
        }
        
        ActionLocation location = parseActionLocation(sender, args, quest);
        
        return new SpawnEntityAction(delayTicks, entityType, location);
    }
    
    private QuestAction parseTeleportAction(CommandSender sender, ArgsParser args, Quest quest, long delayTicks) {
        if ((sender instanceof Player) && !args.hasNext()) {
            return new TeleportationAction(delayTicks, new GlobalLocation(((Player) sender).getLocation()));
        }
        
        if (args.remaining() < 4) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Bitte gib die Welt und die x-, y-, und z-Koordinaten der Position an,"
                            + " zu der der Spieler teleportiert werden soll.");
            throw new ActionParseException();
        }
        
        String worldString = args.next();
        World world = Bukkit.getWorld(worldString);
        if (world == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Welt " + worldString + " nicht gefunden.");
            throw new ActionParseException();
        }
        
        double x, y, z;
        try {
            x = Double.parseDouble(args.next());
            y = Double.parseDouble(args.next());
            z = Double.parseDouble(args.next());
        } catch (NumberFormatException e) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Koordinaten als Kommazahlen an.");
            throw new ActionParseException();
        }
        
        float yaw = 0.0f;
        float pitch = 0.0f;
        if (args.hasNext()) {
            if (args.remaining() < 2) {
                ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib yaw und pitch oder keins von beidem an.");
                throw new ActionParseException();
            }
            
            try {
                yaw = Float.parseFloat(args.next());
                pitch = Float.parseFloat(args.next());
            } catch (NumberFormatException e) {
                ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib yaw und pitch als Kommazahlen an.");
                throw new ActionParseException();
            }
        }
        
        return new TeleportationAction(delayTicks, new GlobalLocation(world.getName(), x, y, z, yaw, pitch));
    }
    
    private QuestAction parseTitleMessageAction(CommandSender sender, ArgsParser args, Quest quest, long delayTicks) {
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Dauer des Fade-Ins der Nachricht in Ticks an.");
            throw new ActionParseException();
        }
        
        int fadeIn = args.getNext(-1);
        if (fadeIn <= 0) {
            ChatAndTextUtil.sendWarningMessage(sender, "Die Dauer muss eine positive Ganzzahl sein.");
            throw new ActionParseException();
        }
        
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Bitte gib die Dauer in Ticks an, für die die Nachricht angezeigt werden soll.");
            throw new ActionParseException();
        }
        
        int stay = args.getNext(-1);
        if (stay <= 0) {
            ChatAndTextUtil.sendWarningMessage(sender, "Die Dauer muss eine positive Ganzzahl sein.");
            throw new ActionParseException();
        }
        
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Dauer des Fade-Outs der Nachricht in Ticks an.");
            throw new ActionParseException();
        }
        
        int fadeOut = args.getNext(-1);
        if (fadeOut <= 0) {
            ChatAndTextUtil.sendWarningMessage(sender, "Die Dauer muss eine positive Ganzzahl sein.");
            throw new ActionParseException();
        }
        
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Bitte gib Titel und Untertitel an, die angezeigt werden sollen, getrennt von einem |.");
            throw new ActionParseException();
        }
        
        Pair<String, String> messages = StringUtil.splitAtPipe(args.getAll(null));
        if (messages == null) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Bitte gib Titel und Untertitel an, die angezeigt werden sollen, getrennt von einem |.");
            throw new ActionParseException();
        }
        
        return new TitleMessageAction(delayTicks, StringUtil.convertColors(messages.first),
                StringUtil.convertColors(messages.second), fadeIn, stay, fadeOut);
    }
    
    private ActionLocation parseActionLocation(CommandSender sender, ArgsParser args, Quest quest) {
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Bitte gib an, ob die Aktion an einer festen Position (fixed) oder der Position des Spielers (player) ausgelöst werden soll.");
            throw new ActionParseException();
        }
        
        String locationTypeString = args.next();
        if (locationTypeString.equalsIgnoreCase("player")) {
            double xOffset, yOffset, zOffset;
            if (!args.hasNext()) {
                xOffset = 0.0;
                yOffset = 0.0;
                zOffset = 0.0;
            } else if (args.remaining() < 3) {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Bitte gib den Offset von der Spielerposition in x-, y- und z-Richtung als Kommazahlen an.");
                throw new ActionParseException();
            } else {
                try {
                    xOffset = Double.parseDouble(args.next());
                    yOffset = Double.parseDouble(args.next());
                    zOffset = Double.parseDouble(args.next());
                } catch (NumberFormatException e) {
                    ChatAndTextUtil.sendWarningMessage(sender,
                            "Bitte gib den Offset von der Spielerposition in x-, y- und z-Richtung als Kommazahlen an.");
                    throw new ActionParseException();
                }
                
            }
            
            return new PlayerActionLocation(xOffset, yOffset, zOffset);
        }
        
        if (!locationTypeString.equalsIgnoreCase("fixed")) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Bitte gib an, ob die Aktion an einer festen Position (fixed) oder der Position des Spielers (player) ausgelöst werden soll.");
            throw new ActionParseException();
        }
        
        SafeLocation location = parseSafeLocation(sender, args, quest);
        return new FixedActionLocation(location);
    }
    
    private SafeLocation parseSafeLocation(CommandSender sender, ArgsParser args, Quest quest) {
        if ((sender instanceof Player) && !args.hasNext()) {
            return new SafeLocation(((Player) sender).getLocation()).stripDirection();
        }
        
        if (args.remaining() < 4) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Bitte gib die Welt und die x-, y-, und z-Koordinaten der Position an,"
                            + " an der die Aktion ausgelöst werden soll.");
            throw new ActionParseException();
        }
        
        String worldString = args.next();
        World world = Bukkit.getWorld(worldString);
        if (world == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Welt " + worldString + " nicht gefunden.");
            throw new ActionParseException();
        }
        
        double x, y, z;
        try {
            x = Double.parseDouble(args.next());
            y = Double.parseDouble(args.next());
            z = Double.parseDouble(args.next());
        } catch (NumberFormatException e) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Koordinaten als Kommazahlen an.");
            throw new ActionParseException();
        }
        
        return new SafeLocation(world.getName(), x, y, z);
    }
    
    @SuppressWarnings("deprecation")
    private Material parseMaterial(CommandSender sender, ArgsParser args, Quest quest) {
        if (!args.hasNext()) {
            if (sender instanceof Player) {
                ItemStack stack = ((Player) sender).getInventory().getItemInMainHand();
                if (stack != null && stack.getType() != Material.AIR) {
                    return stack.getType();
                }
            }
            
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib ein Material an.");
            throw new ActionParseException();
        } else {
            String materialString = args.getNext();
            Material result = Material.matchMaterial(materialString);
            if (result == null) {
                ChatAndTextUtil.sendWarningMessage(sender, "Material " + materialString + " nicht gefunden.");
                throw new ActionParseException();
            }
            if (result.isLegacy()) {
                ChatAndTextUtil.sendWarningMessage(sender, "Legacy-Materialien können nicht verwendet werden.");
                throw new ActionParseException();
            }
            
            return result;
        }
    }
    
    private PotionEffectType parsePotionEffectType(CommandSender sender, Quest quest, ArgsParser args) {
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib einen Trank-Typ an.");
            throw new ActionParseException();
        }
        
        String potionTypeString = args.next();
        PotionEffectType effectType = PotionEffectType.getByName(potionTypeString.toUpperCase());
        if (effectType == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Trank-Typ " + potionTypeString + " nicht gefunden.");
            throw new ActionParseException();
        }
        
        return effectType;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        String modificationType = args.getNext(null);
        if (!args.hasNext()) {
            return Arrays.asList("add", "edit", "append", "remove");
        }
        
        if (modificationType.equalsIgnoreCase("add")) {
            
            
            String actionTypeOrDelayString = args.getNext(null);
            if (!args.hasNext()) {
                List<String> result = Arrays.stream(ActionType.values()).map(ActionType::name)
                        .collect(Collectors.toCollection(ArrayList::new));
                result.addAll(DELAY_STRINGS);
                return result;
            }
            
            String actionTypeString;
            if (DELAY_STRINGS.contains(actionTypeOrDelayString)) {
                args.next();
                if (!args.hasNext()) {
                    return Collections.emptyList();
                }
                
                actionTypeString = args.getNext(null);
            } else {
                actionTypeString = actionTypeOrDelayString;
            }
            
            if (!args.hasNext()) {
                return Arrays.stream(ActionType.values())
                        .filter(t -> DelayableAction.class.isAssignableFrom(t.concreteClass)).map(ActionType::name)
                        .collect(Collectors.toList());
            }
            
            ActionType actionType = ActionType.match(actionTypeString);
            if (actionType == null) {
                return Collections.emptyList();
            }
            
            switch (actionType) {
                case ACTION_BAR_MESSAGE:
                    return Collections.emptyList();
                
                case BOSS_BAR_MESSAGE:
                    args.getNext(null);
                    if (!args.hasNext()) {
                        return Arrays.stream(BarColor.values()).map(BarColor::name).collect(Collectors.toList());
                    }
                    
                    args.getNext(null);
                    if (!args.hasNext()) {
                        return Arrays.stream(BarStyle.values()).map(BarStyle::name).collect(Collectors.toList());
                    }
                    
                    return Collections.emptyList();
                
                case CHAT_MESSAGE:
                    return Collections.emptyList();
                
                case REWARD:
                    return tabCompleteReward(sender, command, alias, args);
                
                case REDSTONE_SIGNAL:
                    args.getNext(null);
                    if (!args.hasNext()) {
                        return Collections.emptyList();
                    }
                    return tabCompleteLocation(sender, command, alias, args);
                
                case POTION_EFFECT:
                    String potionEffectTypeString = args.getNext(null);
                    if (!args.hasNext()) {
                        return Arrays.stream(PotionEffectType.values()).filter(Objects::nonNull)
                                .map(PotionEffectType::getName).collect(Collectors.toList());
                    }
                    
                    PotionEffectType potionEffectType = PotionEffectType.getByName(potionEffectTypeString);
                    if (potionEffectType == null) {
                        return Collections.emptyList();
                    }
                    
                    // duration
                    if (!potionEffectType.isInstant()) {
                        args.getNext(null);
                        if (!args.hasNext()) {
                            return Collections.emptyList();
                        }
                    }
                    
                    // amplifier
                    args.getNext(null);
                    if (!args.hasNext()) {
                        return Collections.emptyList();
                    }
                    
                    List<String> result = new ArrayList<>();
                    result.addAll(StringUtil.TRUE_STRINGS);
                    result.addAll(StringUtil.FALSE_STRINGS);
                    return result;
                
                case REMOVE_POTION_EFFECT:
                    potionEffectTypeString = args.getNext(null);
                    if (!args.hasNext()) {
                        return Arrays.stream(PotionEffectType.values()).filter(Objects::nonNull)
                                .map(PotionEffectType::getName).collect(Collectors.toList());
                    }
                    
                    return Collections.emptyList();
                
                case PARTICLE:
                    String particleString = args.getNext(null);
                    if (!args.hasNext()) {
                        return Arrays.stream(Particle.values()).map(Particle::name).collect(Collectors.toList());
                    }
                    
                    Particle particle;
                    try {
                        particle = Particle.valueOf(particleString.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        return Collections.emptyList();
                    }
                    
                    ParticleData.Type particleDataType = ParticleData.Type.fromDataType(particle.getDataType());
                    if (particleDataType == null) {
                        return Collections.emptyList();
                    }
                    
                    // amountPerTick, numberOfTicks, offset(X, Y, Z), extra
                    args.getNext(null);
                    args.getNext(null);
                    args.getNext(null);
                    args.getNext(null);
                    args.getNext(null);
                    args.getNext(null);
                    
                    if (!args.hasNext()) {
                        return Collections.emptyList();
                    }
                    
                    switch (particleDataType) {
                        case DUST_OPTIONS:
                            args.getNext(null);
                            if (!args.hasNext()) {
                                return StringUtilBukkit.getConstantColors().stream()
                                        .map(StringUtilBukkit::getConstantColorName).map(s -> s.replace(' ', '_'))
                                        .collect(Collectors.toList());
                            }
                            
                            args.getNext(null);
                            if (!args.hasNext()) {
                                return Collections.emptyList();
                            }
                            break;
                        case ITEM_STACK:
                        case BLOCK_DATA:
                            args.getNext(null);
                            if (!args.hasNext()) {
                                return tabCompleteMaterial(sender, command, alias, args);
                            }
                            break;
                        default:
                            throw new AssertionError("Unknown ParticleDataType " + particleDataType + "!");
                    }
                    return tabCompleteActionLocation(sender, command, alias, args);
                
                case EFFECT:
                    String effectString = args.getNext(null);
                    if (!args.hasNext()) {
                        return Arrays.stream(Effect.values()).filter(e -> e != Effect.POTION_BREAK).map(Effect::name)
                                .collect(Collectors.toList());
                    }
                    
                    Effect effect;
                    try {
                        effect = Effect.valueOf(effectString.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        return Collections.emptyList();
                    }
                    
                    if (effect == Effect.POTION_BREAK) {
                        return Collections.emptyList();
                    }
                    
                    EffectData.Type effectDataType = EffectData.Type.fromDataType(effect.getData());
                    if (effectDataType == null) {
                        return Collections.emptyList();
                    }
                    
                    switch (effectDataType) {
                        case MATERIAL:
                            args.getNext(null);
                            if (!args.hasNext()) {
                                if (effect == Effect.RECORD_PLAY) {
                                    return Arrays.stream(Material.values()).filter(ItemGroups::isMusicDisc)
                                            .map(Material::name).collect(Collectors.toList());
                                } else if (effect == Effect.STEP_SOUND) {
                                    return Arrays.stream(Material.values()).filter(Material::isBlock)
                                            .map(Material::name).collect(Collectors.toList());
                                } else {
                                    return Collections.emptyList();
                                }
                            }
                            break;
                        case BLOCK_FACE:
                            args.getNext(null);
                            if (!args.hasNext()) {
                                return Arrays.stream(BlockFace.values()).map(BlockFace::name)
                                        .collect(Collectors.toList());
                            }
                            break;
                        case INTEGER:
                            args.getNext(null);
                            if (!args.hasNext()) {
                                return Collections.emptyList();
                            }
                            break;
                        default:
                            throw new AssertionError("Unknown EffectDataType " + effectDataType + "!");
                    }
                    return tabCompleteActionLocation(sender, command, alias, args);
                
                case SOUND:
                    args.getNext(null);
                    if (!args.hasNext()) {
                        return Arrays.stream(Sound.values()).map(Sound::name).collect(Collectors.toList());
                    }
                    
                    // volume, pitch
                    args.getNext(null);
                    args.getNext(null);
                    if (!args.hasNext()) {
                        return Collections.emptyList();
                    }
                    
                    return tabCompleteActionLocation(sender, command, alias, args);
                
                case SPAWN_ENTITY:
                    args.getNext(null);
                    if (!args.hasNext()) {
                        return Arrays.stream(EntityType.values())
                                .filter(t -> t != EntityType.UNKNOWN && t != EntityType.PLAYER).map(EntityType::name)
                                .collect(Collectors.toList());
                    }
                    
                    return tabCompleteActionLocation(sender, command, alias, args);
                
                case TELEPORT:
                    return Collections.emptyList();
                
                case TITLE_MESSAGE:
                    return Collections.emptyList();
                
                default:
                    throw new AssertionError("Unknown ActionType " + actionType + "!");
                
            }
        } else if (EDIT_ACTION_STRINGS.contains(modificationType)) {
            int editedIndex = args.getNext(0) - 1;
            if (editedIndex < 0) {
                return Collections.emptyList();
            }
            
            Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
            if (quest == null) {
                return Collections.emptyList();
            }
            
            List<QuestAction> actions = this.time.getQuestActions(quest);
            if (editedIndex >= actions.size()) {
                return Collections.emptyList();
            }
            
            QuestAction edited = actions.get(editedIndex);
            if (edited instanceof ChatMessageAction) {
                return Collections.emptyList();
            } else if (edited instanceof RewardAction) {
                return tabCompleteReward(sender, command, alias, args);
            } else {
                return Collections.emptyList();
            }
        } else if (modificationType.equalsIgnoreCase("remove")) {
            return Collections.emptyList();
        } else {
            return Collections.emptyList();
        }
    }
    
    private List<String> tabCompleteReward(CommandSender sender, Command command, String alias, ArgsParser args) {
        return Arrays.stream(RewardAttribute.values()).map(RewardAttribute::name).map(s -> s + ":")
                .collect(Collectors.toList());
    }
    
    private List<String> tabCompleteActionLocation(CommandSender sender, Command command, String alias,
            ArgsParser args) {
        String locationTypeString = args.getNext(null);
        if (!args.hasNext()) {
            return Arrays.asList("player", "fixed");
        }
        
        if (locationTypeString.equalsIgnoreCase("player")) {
            return Collections.emptyList();
        } else if (locationTypeString.equalsIgnoreCase("fixed")) {
            return tabCompleteLocation(sender, command, alias, args);
        } else {
            return Collections.emptyList();
        }
    }
    
    private List<String> tabCompleteLocation(CommandSender sender, Command command, String alias, ArgsParser args) {
        args.getNext(null);
        if (!args.hasNext()) {
            return Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());
        }
        
        return Collections.emptyList();
    }
    
    @SuppressWarnings("deprecation")
    private List<String> tabCompleteMaterial(CommandSender sender, Command command, String alias, ArgsParser args) {
        return Arrays.stream(Material.values()).filter(m -> !m.isLegacy()).map(Material::name).map(s -> s + ":")
                .collect(Collectors.toList());
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
    
    @Override
    public String getUsage() {
        return "<add [delayed] <Action>> | <remove <Index>> | <edit | append <Index> <Action>>";
    }
    
}
