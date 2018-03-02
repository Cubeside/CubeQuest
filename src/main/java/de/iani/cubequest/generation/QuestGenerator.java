package de.iani.cubequest.generation;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.EventListener.GlobalChatMsgType;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.Reward;
import de.iani.cubequest.exceptions.QuestDeletionFailedException;
import de.iani.cubequest.questGiving.QuestGiver;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.ItemStackUtil;
import de.iani.cubequest.util.Util;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import javafx.util.Pair;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.apache.commons.lang.time.DateUtils;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class QuestGenerator implements ConfigurationSerializable {
    
    private static final int DAYS_TO_KEEP_DAILY_QUESTS = 7;
    
    private static QuestGenerator instance;
    
    private int questsToGenerate;
    private int questsToGenerateOnThisServer;
    
    private List<QuestSpecification> possibleQuests;
    private Deque<DailyQuestData> currentDailyQuests;
    
    private Map<Material, Double> materialValues;
    private Map<EntityType, Double> entityValues;
    private double defaultMaterialValue;
    private double defaultEntityValue;
    
    private LocalDate lastGeneratedForDay;
    
    // private Object saveLock = new Object();
    
    public static class DailyQuestData implements ConfigurationSerializable {
        
        private Quest[] quests;
        private String dateString;
        private Date nextDayDate;
        
        public static DailyQuestData deserialize(Map<String, Object> serialized) {
            return new DailyQuestData(serialized);
        }
        
        private DailyQuestData() {
            Calendar today = DateUtils.truncate(Calendar.getInstance(), Calendar.DATE);
            this.dateString =
                    (new SimpleDateFormat(Util.DATE_FORMAT_STRING)).format(today.getTime());
            today.add(Calendar.DATE, 1);
            this.nextDayDate = today.getTime();
        }
        
        @SuppressWarnings("unchecked")
        private DailyQuestData(Map<String, Object> serialized) {
            List<Integer> currentDailyQuestList = (List<Integer>) serialized.get("quests");
            this.quests = new Quest[currentDailyQuestList.size()];
            for (int i = 0; i < this.quests.length; i++) {
                this.quests[i] = currentDailyQuestList.get(i) == null ? null
                        : QuestManager.getInstance().getQuest(currentDailyQuestList.get(i));
            }
            this.dateString = (String) serialized.get("dateString");
            this.nextDayDate = serialized.get("nextDayDate") == null ? null
                    : new Date((Long) serialized.get("nextDayDate"));
        }
        
        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> result = new HashMap<>();
            
            List<Integer> currentDailyQuestList = new ArrayList<>();
            Arrays.stream(this.quests)
                    .forEach(q -> currentDailyQuestList.add(q == null ? null : q.getId()));
            result.put("quests", currentDailyQuestList);
            
            result.put("dateString", this.dateString);
            result.put("nextDayDate", this.nextDayDate == null ? null : this.nextDayDate.getTime());
            
            return result;
        }
        
    }
    
    public static class QuestSpecificationAndDifficultyPair
            extends Pair<QuestSpecification, Double> {
        
        private static final long serialVersionUID = 1L;
        
        public QuestSpecificationAndDifficultyPair(QuestSpecification key, Double value) {
            super(key, value);
        }
        
        public QuestSpecification getQuestSpecification() {
            return super.getKey();
        }
        
        public double getDifficulty() {
            return super.getValue();
        }
        
    }
    
    public static class DifferenceInDifficultyComparator
            implements Comparator<QuestSpecificationAndDifficultyPair> {
        
        private double targetDifficulty;
        
        public DifferenceInDifficultyComparator(double targetDifficulty) {
            this.targetDifficulty = targetDifficulty;
        }
        
        @Override
        public int compare(QuestSpecificationAndDifficultyPair o1,
                QuestSpecificationAndDifficultyPair o2) {
            return Double.compare(Math.abs(this.targetDifficulty - o1.getDifficulty()),
                    Math.abs(this.targetDifficulty - o2.getDifficulty()));
        }
        
    }
    
    public static QuestGenerator getInstance() {
        if (instance == null) {
            File configFile = new File(CubeQuest.getInstance().getDataFolder(), "generator.yml");
            if (!configFile.exists()) {
                instance = new QuestGenerator();
            } else {
                YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
                if (!config.contains("generator")) {
                    CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                            "Could not load QuestGenerator.");
                    instance = new QuestGenerator();
                } else {
                    instance = (QuestGenerator) config.get("generator");
                }
            }
        }
        
        return instance;
    }
    
    private QuestGenerator() {
        this.possibleQuests = new ArrayList<>();
        this.currentDailyQuests = new ArrayDeque<>(DAYS_TO_KEEP_DAILY_QUESTS);
        this.materialValues = new EnumMap<>(Material.class);
        this.entityValues = new EnumMap<>(EntityType.class);
        this.defaultMaterialValue = 0.0025; // ca. ein Holzblock (Stamm)
        this.defaultEntityValue = 0.1; // ca. ein Zombie
        
        // Ein paar voreingestellte Werte
        
        this.materialValues.put(Material.DIAMOND, 0.125);
        this.materialValues.put(Material.GOLD_INGOT, 0.0105);
        this.materialValues.put(Material.IRON_INGOT, 0.006);
        this.materialValues.put(Material.COBBLESTONE, 0.002);
        this.materialValues.put(Material.WHEAT, 0.001);
        this.materialValues.put(Material.CROPS, 0.0015);
        this.materialValues.put(Material.CACTUS, 0.005);
        
        this.entityValues.put(EntityType.CHICKEN, 0.05);
        this.entityValues.put(EntityType.PIG, 0.05);
        this.entityValues.put(EntityType.COW, 0.05);
        this.entityValues.put(EntityType.MUSHROOM_COW, 0.06);
        this.entityValues.put(EntityType.LLAMA, 0.07);
        this.entityValues.put(EntityType.WITCH, 0.5);
        this.entityValues.put(EntityType.CREEPER, 0.2);
        this.entityValues.put(EntityType.SKELETON, 0.25);
    }
    
    @SuppressWarnings("unchecked")
    public QuestGenerator(Map<String, Object> serialized) throws InvalidConfigurationException {
        if (instance != null) {
            throw new IllegalStateException("Can't initilize second instance of singleton!");
        }
        
        try {
            this.questsToGenerate = (Integer) serialized.get("questsToGenerate");
            this.questsToGenerateOnThisServer =
                    (Integer) serialized.get("questsToGenerateOnThisServer");
            
            this.possibleQuests = (List<QuestSpecification>) serialized.get("possibleQuests");
            if (CubeQuest.getInstance().hasCitizensPlugin()) {
                DeliveryQuestSpecification.DeliveryQuestPossibilitiesSpecification.deserialize(
                        (Map<String, Object>) serialized.get("deliveryQuestSpecifications"));
            }
            KillEntitiesQuestSpecification.KillEntitiesQuestPossibilitiesSpecification.deserialize(
                    (Map<String, Object>) serialized.get("killEntitiesQuestSpecifications"));
            BlockBreakQuestSpecification.BlockBreakQuestPossibilitiesSpecification.deserialize(
                    (Map<String, Object>) serialized.get("blockBreakQuestSpecifications"));
            BlockPlaceQuestSpecification.BlockPlaceQuestPossibilitiesSpecification.deserialize(
                    (Map<String, Object>) serialized.get("blockPlaceQuestSpecifications"));
            
            this.currentDailyQuests =
                    new ArrayDeque<>((List<DailyQuestData>) serialized.get("currentDailyQuests"));
            this.lastGeneratedForDay = serialized.get("lastGeneratedForDay") == null ? null
                    : LocalDate.ofEpochDay(
                            ((Number) serialized.get("lastGeneratedForDay")).longValue());
            
            Map<String, Double> mValues = (Map<String, Double>) serialized.get("materialValues");
            this.materialValues = new EnumMap<>(Material.class);
            mValues.forEach((materialName, value) -> this.materialValues
                    .put(Material.valueOf(materialName), value));
            
            Map<String, Double> eValues = (Map<String, Double>) serialized.get("entityValues");
            this.entityValues = new EnumMap<>(EntityType.class);
            eValues.forEach((entityName, value) -> this.entityValues
                    .put(EntityType.valueOf(entityName), value));
            
            this.defaultMaterialValue = (double) serialized.get("defaultMaterialValue");
            this.defaultEntityValue = (double) serialized.get("defaultEntityValue");
        } catch (Exception e) {
            throw new InvalidConfigurationException(e);
        }
    }
    
    public List<QuestSpecification> getPossibleQuestsIncludingNulls() {
        return Collections.unmodifiableList(this.possibleQuests);
    }
    
    public void addPossibleQuest(QuestSpecification qs) {
        this.possibleQuests.add(qs);
        saveConfig();
    }
    
    public void removePossibleQuest(int index) {
        this.possibleQuests.set(index, null);
        saveConfig();
    }
    
    public void consolidatePossibleQuests() {
        this.possibleQuests.removeIf(qs -> qs == null);
        this.possibleQuests.sort(QuestSpecification.COMPARATOR);
    }
    
    public int getQuestsToGenerate() {
        return this.questsToGenerate;
    }
    
    public void setQuestsToGenerate(int questsToGenerate) {
        this.questsToGenerate = questsToGenerate;
        saveConfig();
    }
    
    public int getQuestsToGenerateOnThisServer() {
        return this.questsToGenerateOnThisServer;
    }
    
    public void setQuestsToGenerateOnThisServer(int questsToGenerateOnThisServer) {
        this.questsToGenerateOnThisServer = questsToGenerateOnThisServer;
        saveConfig();
    }
    
    public LocalDate getLastGeneratedForDay() {
        return this.lastGeneratedForDay;
    }
    
    public double getValue(Material m) {
        return this.materialValues.containsKey(m) ? this.materialValues.get(m)
                : this.defaultMaterialValue;
    }
    
    public void setValue(Material m, double value) {
        this.materialValues.put(m, value);
        saveConfig();
    }
    
    public double getValue(EntityType t) {
        return this.entityValues.containsKey(t) ? this.entityValues.get(t)
                : this.defaultEntityValue;
    }
    
    public void setValue(EntityType t, double value) {
        this.entityValues.put(t, value);
        saveConfig();
    }
    
    public void generateDailyQuests() {
        CubeQuest.getInstance().getLogger().log(Level.INFO, "Starting to generate DailyQuests.");
        
        if (!this.currentDailyQuests.isEmpty()) {
            // DailyQuests von gestern aus QuestGivern austragen
            for (QuestGiver giver: CubeQuest.getInstance().getDailyQuestGivers()) {
                for (Quest q: this.currentDailyQuests.getLast().quests) {
                    giver.removeQuest(q);
                }
            }
            
            // Ggf. über eine Woche alte DailyQuests löschen
            if (this.currentDailyQuests.size() >= DAYS_TO_KEEP_DAILY_QUESTS) {
                DailyQuestData dqData = this.currentDailyQuests.removeFirst();
                List<Quest> toDeleteList = new LinkedList<>();
                for (Quest q: dqData.quests) {
                    toDeleteList.add(q);
                }
                
                int oldSize;
                do {
                    oldSize = toDeleteList.size();
                    
                    Iterator<Quest> it = toDeleteList.iterator();
                    while (it.hasNext()) {
                        // Kann normal gelöscht werden, da sie jetzt nicht mehr in
                        // getAllDailyQuests() auftaucht.
                        try {
                            QuestManager.getInstance().deleteQuest(it.next());
                            it.remove();
                        } catch (QuestDeletionFailedException e) {
                            // ignore
                        }
                    }
                    
                    // Die Quests müssen ggf. in einer bestimmten Reihenfolge gelöscht werden.
                    // Damit diese nicht ermittelt werden muss, machen wir trial and error,
                    // solange die zu löschenden Quests weniger werden.
                } while (!toDeleteList.isEmpty() && toDeleteList.size() < oldSize);
                
                // Logge ggf. Fehlermeldungen von Quests, die wirklich nicht gelöscht werden können
                for (Quest q: toDeleteList) {
                    try {
                        QuestManager.getInstance().deleteQuest(q);
                    } catch (QuestDeletionFailedException e) {
                        CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                                "Could not delete DailyQuest " + q + ":", e);
                    }
                }
            }
        }
        
        this.lastGeneratedForDay = LocalDate.now();
        DailyQuestData dqData = new DailyQuestData();
        this.currentDailyQuests.addLast(dqData);
        dqData.quests = new Quest[this.questsToGenerate];
        
        Random ran = new Random(this.lastGeneratedForDay.toEpochDay());
        
        List<String> selectedServers = new ArrayList<>();
        List<String> serversToSelectFrom = new ArrayList<>();
        Map<String, Integer> serversToSelectFromWithAmountOfLegalSpecifications;
        try {
            serversToSelectFromWithAmountOfLegalSpecifications =
                    CubeQuest.getInstance().getDatabaseFassade().getServersToGenerateDailyQuestOn();
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                    "SQL-Exception while trying to generate daily-quests! No quests generated.", e);
            return;
        }
        
        serversToSelectFromWithAmountOfLegalSpecifications
                .remove(CubeQuest.getInstance().getBungeeServerName());
        for (String server: serversToSelectFromWithAmountOfLegalSpecifications.keySet()) {
            for (int i = 0; i < serversToSelectFromWithAmountOfLegalSpecifications
                    .get(server); i++) {
                serversToSelectFrom.add(server);
            }
        }
        
        if (serversToSelectFrom.isEmpty()) {
            serversToSelectFrom.add(null);
        } else {
            Collections.sort(serversToSelectFrom);
            Collections.shuffle(serversToSelectFrom, ran);
        }
        for (int i = 0; serversToSelectFrom.size() < this.questsToGenerate
                - this.questsToGenerateOnThisServer; i++) {
            serversToSelectFrom.add(serversToSelectFrom.get(i));
        }
        for (int i = 0; i < this.questsToGenerate; i++) {
            selectedServers.add(i < this.questsToGenerateOnThisServer ? null
                    : serversToSelectFrom.get(i - this.questsToGenerateOnThisServer));
        }
        Collections.shuffle(selectedServers, ran);
        
        for (int i = 0; i < this.questsToGenerate; i++) {
            double difficulty =
                    (this.questsToGenerate > 1 ? 0.1 + i * 0.8 / (this.questsToGenerate - 1) : 0.5)
                            + 0.1 * ran.nextDouble();
            String server = selectedServers.get(i);
            
            if (server == null) {
                dailyQuestGenerated(i, generateQuest(i, dqData.dateString, difficulty, ran));
            } else {
                ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
                DataOutputStream msgout = new DataOutputStream(msgbytes);
                try {
                    msgout.writeInt(GlobalChatMsgType.GENERATE_DAILY_QUEST.ordinal());
                    msgout.writeUTF(server);
                    msgout.writeInt(i);
                    msgout.writeUTF(dqData.dateString);
                    msgout.writeDouble(difficulty);
                    msgout.writeLong(ran.nextLong());
                } catch (IOException e) {
                    CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                            "IOException trying to send GlobalChatMessage!", e);
                    return;
                }
                
                byte[] msgarry = msgbytes.toByteArray();
                CubeQuest.getInstance().getGlobalChatAPI().sendDataToServers("CubeQuest", msgarry);
            }
        }
    }
    
    public Quest generateQuest(int dailyQuestOrdinal, String dateString, double difficulty,
            Random ran) {
        if (this.possibleQuests.stream().noneMatch(qs -> qs != null && qs.isLegal())) {
            CubeQuest.getInstance().getLogger().log(Level.WARNING,
                    "Could not generate a DailyQuest for this server as no QuestSpecifications were specified.");
            return null;
        }
        
        List<QuestSpecification> qsList = new ArrayList<>();
        this.possibleQuests.forEach(qs -> {
            if (qs != null && qs.isLegal()) {
                qsList.add(qs);
            }
        });
        qsList.sort(QuestSpecification.COMPARATOR);
        
        List<QuestSpecificationAndDifficultyPair> generatedList = new ArrayList<>();
        qsList.forEach(qs -> generatedList.add(new QuestSpecificationAndDifficultyPair(qs,
                qs.generateQuest(ran) + 0.1 * ran.nextGaussian())));
        
        int weighting = DeliveryQuestSpecification.DeliveryQuestPossibilitiesSpecification
                .getInstance().getWeighting();
        for (int i = 0; i < weighting; i++) {
            DeliveryQuestSpecification qs = new DeliveryQuestSpecification();
            generatedList.add(new QuestSpecificationAndDifficultyPair(qs,
                    qs.generateQuest(ran) + 0.1 * ran.nextGaussian()));
        }
        weighting = BlockBreakQuestSpecification.BlockBreakQuestPossibilitiesSpecification
                .getInstance().getWeighting();
        for (int i = 0; i < weighting; i++) {
            BlockBreakQuestSpecification qs = new BlockBreakQuestSpecification();
            generatedList.add(new QuestSpecificationAndDifficultyPair(qs,
                    qs.generateQuest(ran) + 0.1 * ran.nextGaussian()));
        }
        weighting = BlockPlaceQuestSpecification.BlockPlaceQuestPossibilitiesSpecification
                .getInstance().getWeighting();
        for (int i = 0; i < weighting; i++) {
            BlockPlaceQuestSpecification qs = new BlockPlaceQuestSpecification();
            generatedList.add(new QuestSpecificationAndDifficultyPair(qs,
                    qs.generateQuest(ran) + 0.1 * ran.nextGaussian()));
        }
        weighting = KillEntitiesQuestSpecification.KillEntitiesQuestPossibilitiesSpecification
                .getInstance().getWeighting();
        for (int i = 0; i < weighting; i++) {
            KillEntitiesQuestSpecification qs = new KillEntitiesQuestSpecification();
            generatedList.add(new QuestSpecificationAndDifficultyPair(qs,
                    qs.generateQuest(ran) + 0.1 * ran.nextGaussian()));
        }
        
        generatedList.sort(new DifferenceInDifficultyComparator(difficulty));
        generatedList.subList(1, generatedList.size() - 1)
                .forEach(qsdp -> qsdp.getQuestSpecification().clearGeneratedQuest());
        
        QuestSpecification resultSpecification = generatedList.get(0).getQuestSpecification();
        String questName = "DailyQuest " + ChatAndTextUtil.toRomanNumber(dailyQuestOrdinal + 1)
                + " vom " + dateString;
        Reward reward = generateReward(difficulty, ran);
        
        Quest result = resultSpecification.createGeneratedQuest(questName, reward);
        result.setReady(true);
        
        return result;
    }
    
    /**
     * Generiert die Belohnung für eine DailyQuest
     * 
     * @param difficulty ignored
     * @param rewardModifier ignored
     * @param ran ignored
     * @return Feste Belohnung (nicht zufällig generiert) aus 1 QuestPoint, 5 XP und 1 Mysteriösen
     *         Zauberbuch
     */
    public Reward generateReward(double difficulty, Random ran) {
        return new Reward(0, 1, 5, new ItemStack[] {ItemStackUtil.getMysteriousSpellBook()});
    }
    
    public void dailyQuestGenerated(int dailyQuestOrdinal, Quest generatedQuest) {
        if (generatedQuest.getSuccessMessage() == null) {
            generatedQuest.setSuccessMessage(CubeQuest.PLUGIN_TAG + ChatColor.GOLD + " Du hast die "
                    + generatedQuest.getName() + " abgeschlossen!");
        }
        
        DailyQuestData dqData = this.currentDailyQuests.getLast();
        
        dqData.quests[dailyQuestOrdinal] = Util.addTimeLimit(generatedQuest, dqData.nextDayDate);
        generatedQuest.setVisible(true);
        
        if (Arrays.stream(dqData.quests).allMatch(q -> q != null)) {
            for (QuestGiver giver: CubeQuest.getInstance().getDailyQuestGivers()) {
                for (Quest q: dqData.quests) {
                    giver.addQuest(q);
                }
            }
            
            CubeQuest.getInstance().getLogger().log(Level.INFO, "DailyQuests generated.");
        }
        
        saveConfig();
    }
    
    public Quest[] getTodaysDailyQuests() {
        DailyQuestData dqData = this.currentDailyQuests.getLast();
        return this.currentDailyQuests == null || dqData.quests == null ? null
                : Arrays.copyOf(dqData.quests, dqData.quests.length);
    }
    
    public Collection<Quest> getAllDailyQuests() {
        Set<Quest> result = new LinkedHashSet<>();
        for (DailyQuestData dqData: this.currentDailyQuests) {
            for (Quest q: dqData.quests) {
                result.add(q);
            }
        }
        
        return result;
    }
    
    public int countLegalQuestSecifications() {
        int i = 0;
        for (QuestSpecification qs: this.possibleQuests) {
            if (qs != null && qs.isLegal()) {
                i++;
            }
        }
        
        try {
            CubeQuest.getInstance().getDatabaseFassade().setLegalQuestSpecificationCount(i);
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                    "Could not update count of legal QuestSpecificaitons in database.", e);
        }
        
        return i;
    }
    
    public List<BaseComponent[]> getSpecificationInfo() {
        List<BaseComponent[]> result = new ArrayList<>();
        
        result.add(ChatAndTextUtil.headline1("Liste der Quest-Specificationen"));
        result.add(new ComponentBuilder("").create());
        int index = 1;
        for (QuestSpecification qs: this.possibleQuests) {
            if (qs != null) {
                ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/quest removeQuestSpecification " + index);
                HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("Spezifikation an Index " + index + " entfernen.")
                                .create());
                result.add(new ComponentBuilder(index + ": ").append(qs.getSpecificationInfo())
                        .append(" ").append("[Löschen]").color(ChatColor.RED).event(clickEvent)
                        .event(hoverEvent).create());
            }
            index++;
        }
        
        if (CubeQuest.getInstance().hasCitizensPlugin()) {
            result.add(new ComponentBuilder("").create());
            result.addAll(DeliveryQuestSpecification.DeliveryQuestPossibilitiesSpecification
                    .getInstance().getSpecificationInfo());
        }
        result.add(new ComponentBuilder("").create());
        result.addAll(KillEntitiesQuestSpecification.KillEntitiesQuestPossibilitiesSpecification
                .getInstance().getSpecificationInfo());
        result.add(new ComponentBuilder("").create());
        result.addAll(BlockBreakQuestSpecification.BlockBreakQuestPossibilitiesSpecification
                .getInstance().getSpecificationInfo());
        result.add(new ComponentBuilder("").create());
        result.addAll(BlockPlaceQuestSpecification.BlockPlaceQuestPossibilitiesSpecification
                .getInstance().getSpecificationInfo());
        
        return result;
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        
        result.put("questsToGenerate", this.questsToGenerate);
        result.put("questsToGenerateOnThisServer", this.questsToGenerateOnThisServer);
        
        List<QuestSpecification> possibleQSList = new ArrayList<>(this.possibleQuests);
        possibleQSList.removeIf(qs -> qs == null);
        possibleQSList.sort(QuestSpecification.COMPARATOR);
        result.put("possibleQuests", possibleQSList);
        if (CubeQuest.getInstance().hasCitizensPlugin()) {
            result.put("deliveryQuestSpecifications",
                    DeliveryQuestSpecification.DeliveryQuestPossibilitiesSpecification.getInstance()
                            .serialize());
        }
        result.put("killEntitiesQuestSpecifications",
                KillEntitiesQuestSpecification.KillEntitiesQuestPossibilitiesSpecification
                        .getInstance().serialize());
        result.put("blockBreakQuestSpecifications",
                BlockBreakQuestSpecification.BlockBreakQuestPossibilitiesSpecification.getInstance()
                        .serialize());
        result.put("blockPlaceQuestSpecifications",
                BlockPlaceQuestSpecification.BlockPlaceQuestPossibilitiesSpecification.getInstance()
                        .serialize());
        
        result.put("currentDailyQuests", new ArrayList<>(this.currentDailyQuests));
        result.put("lastGeneratedForDay",
                this.lastGeneratedForDay == null ? null : this.lastGeneratedForDay.toEpochDay());
        
        Map<String, Double> mValues = new HashMap<>();
        this.materialValues.forEach((material, value) -> mValues.put(material.name(), value));
        result.put("materialValues", mValues);
        
        Map<String, Double> eValues = new HashMap<>();
        this.entityValues.forEach((entity, value) -> eValues.put(entity.name(), value));
        result.put("entityValues", eValues);
        
        result.put("defaultMaterialValue", this.defaultMaterialValue);
        result.put("defaultEntityValue", this.defaultEntityValue);
        
        return result;
    }
    
    public void saveConfig() {
        countLegalQuestSecifications();
        CubeQuest.getInstance().getDataFolder().mkdirs();
        File configFile = new File(CubeQuest.getInstance().getDataFolder(), "generator.yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("generator", this);
        try {
            config.save(configFile);
        } catch (IOException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not save QuestGenerator.",
                    e);
        }
    }
    
}
