package de.iani.cubequest.generation;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.EventListener.GlobalChatMsgType;
import de.iani.cubequest.QuestGiver;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.Reward;
import de.iani.cubequest.actions.MessageAction;
import de.iani.cubequest.actions.QuestAction;
import de.iani.cubequest.exceptions.QuestDeletionFailedException;
import de.iani.cubequest.interaction.InteractorProtecting;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.ItemStackUtil;
import de.iani.cubequest.util.Pair;
import de.iani.cubequest.util.Util;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

public class QuestGenerator implements ConfigurationSerializable {
    
    private static final int DAYS_TO_KEEP_DAILY_QUESTS = 3;
    
    private static QuestGenerator instance;
    
    private int questsToGenerate;
    private int questsToGenerateOnThisServer;
    
    private ItemStack mysteriousSpellingBook;
    
    private List<QuestSpecification> possibleQuests;
    private Set<QuestSpecification> lastUsedPossibilities;
    private Set<QuestSpecification> currentlyUsedPossibilities;
    
    private Deque<DailyQuestData> currentDailyQuests;
    
    private Map<MaterialValueOption, ValueMap<Material>> materialValues;
    private Map<EntityValueOption, ValueMap<EntityType>> entityValues;
    
    private LocalDate lastGeneratedForDay;
    
    // private Object saveLock = new Object();
    
    public enum MaterialValueOption {
        DELIVER, PLACE, BREAK, FISH;
    }
    
    public enum EntityValueOption {
        KILL;
    }
    
    public static class QuestSpecificationAndDifficultyPair extends Pair<QuestSpecification, Double> {
        
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
    
    public static class QuestSpeficicationBestFitComparator implements Comparator<QuestSpecificationAndDifficultyPair> {
        
        private double targetDifficulty;
        private Set<QuestSpecification> avoid1;
        private Set<QuestSpecification> avoid2;
        
        public QuestSpeficicationBestFitComparator(double targetDifficulty, Set<QuestSpecification> avoid1, Set<QuestSpecification> avoid2) {
            this.targetDifficulty = targetDifficulty;
            this.avoid1 = avoid1;
            this.avoid2 = avoid2;
        }
        
        @Override
        public int compare(QuestSpecificationAndDifficultyPair o1, QuestSpecificationAndDifficultyPair o2) {
            int result = 0;
            
            if (this.avoid1.contains(o1.getQuestSpecification())) {
                result += 1;
            }
            if (this.avoid1.contains(o2.getQuestSpecification())) {
                result -= 1;
            }
            if (result != 0) {
                return result;
            }
            
            if (this.avoid2.contains(o1.getQuestSpecification())) {
                result += 1;
            }
            if (this.avoid2.contains(o2.getQuestSpecification())) {
                result -= 1;
            }
            if (result != 0) {
                return result;
            }
            
            result = Double.compare(Math.abs(this.targetDifficulty - o1.getDifficulty()), Math.abs(this.targetDifficulty - o2.getDifficulty()));
            return (int) Math.signum(result);
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
                    CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not load QuestGenerator.");
                    instance = new QuestGenerator();
                } else {
                    instance = (QuestGenerator) config.get("generator");
                }
            }
        }
        
        return instance;
    }
    
    public static void reloadConfig() {
        instance = null;
        BlockBreakQuestSpecification.BlockBreakQuestPossibilitiesSpecification.resetInstance();
        BlockPlaceQuestSpecification.BlockPlaceQuestPossibilitiesSpecification.resetInstance();
        DeliveryQuestSpecification.DeliveryQuestPossibilitiesSpecification.resetInstance();
        FishingQuestSpecification.FishingQuestPossibilitiesSpecification.resetInstance();
        KillEntitiesQuestSpecification.KillEntitiesQuestPossibilitiesSpecification.resetInstance();
        getInstance();
        CubeQuest.getInstance().updateQuestGenerator();
    }
    
    @SuppressWarnings("deprecation")
    private QuestGenerator() {
        this.possibleQuests = new ArrayList<>();
        this.lastUsedPossibilities = new TreeSet<>(QuestSpecification.SIMILAR_SPECIFICATIONS_COMPARATOR);
        this.currentlyUsedPossibilities = new TreeSet<>(QuestSpecification.SIMILAR_SPECIFICATIONS_COMPARATOR);
        this.materialValues = new EnumMap<>(MaterialValueOption.class);
        this.entityValues = new EnumMap<>(EntityValueOption.class);
        refreshDailyQuests();
        
        for (MaterialValueOption option : MaterialValueOption.values()) {
            ValueMap<Material> map = new ValueMap<>(Material.class, 0.0025);
            this.materialValues.put(option, map);
        }
        
        for (EntityValueOption option : EntityValueOption.values()) {
            ValueMap<EntityType> map = new ValueMap<>(EntityType.class, 0.1);
            this.entityValues.put(option, map);
        }
        
        this.mysteriousSpellingBook = ItemStackUtil.getMysteriousSpellBook();
    }
    
    @SuppressWarnings({"unchecked", "deprecation"})
    public QuestGenerator(Map<String, Object> serialized) throws InvalidConfigurationException {
        if (instance != null) {
            throw new IllegalStateException("Can't initilize second instance of singleton!");
        }
        
        try {
            this.questsToGenerate = (Integer) serialized.get("questsToGenerate");
            this.questsToGenerateOnThisServer = (Integer) serialized.get("questsToGenerateOnThisServer");
            this.mysteriousSpellingBook = (ItemStack) serialized.get("mysteriousSpellingBook");
            if (this.mysteriousSpellingBook == null) {
                this.mysteriousSpellingBook = ItemStackUtil.getMysteriousSpellBook();
            }
            
            this.possibleQuests = (List<QuestSpecification>) serialized.get("possibleQuests");
            this.lastUsedPossibilities = new TreeSet<>(QuestSpecification.SIMILAR_SPECIFICATIONS_COMPARATOR);
            if (serialized.containsKey("lastUsedPossibilities")) {
                this.lastUsedPossibilities.addAll((List<QuestSpecification>) serialized.get("lastUsedPossibilities"));
            }
            this.currentlyUsedPossibilities = new TreeSet<>(QuestSpecification.SIMILAR_SPECIFICATIONS_COMPARATOR);
            if (serialized.containsKey("currentlyUsedPossibilities")) {
                this.currentlyUsedPossibilities.addAll((List<QuestSpecification>) serialized.get("currentlyUsedPossibilities"));
            }
            
            DeliveryQuestSpecification.DeliveryQuestPossibilitiesSpecification
                    .deserialize((Map<String, Object>) serialized.get("deliveryQuestSpecifications"));
            BlockBreakQuestSpecification.BlockBreakQuestPossibilitiesSpecification
                    .deserialize((Map<String, Object>) serialized.get("blockBreakQuestSpecifications"));
            BlockPlaceQuestSpecification.BlockPlaceQuestPossibilitiesSpecification
                    .deserialize((Map<String, Object>) serialized.get("blockPlaceQuestSpecifications"));
            FishingQuestSpecification.FishingQuestPossibilitiesSpecification
                    .deserialize((Map<String, Object>) serialized.get("fishingQuestSpecifications"));
            KillEntitiesQuestSpecification.KillEntitiesQuestPossibilitiesSpecification
                    .deserialize((Map<String, Object>) serialized.get("killEntitiesQuestSpecifications"));
            
            Map<String, Object> mValues = (Map<String, Object>) serialized.get("materialValues");
            this.materialValues = (Map<MaterialValueOption, ValueMap<Material>>) Util.deserializeEnumMap(MaterialValueOption.class, mValues);
            
            Map<String, Object> eValues = (Map<String, Object>) serialized.get("entityValues");
            this.entityValues = (Map<EntityValueOption, ValueMap<EntityType>>) Util.deserializeEnumMap(EntityValueOption.class, eValues);
            
            refreshDailyQuests();
            this.lastGeneratedForDay = serialized.get("lastGeneratedForDay") == null ? null
                    : LocalDate.ofEpochDay(((Number) serialized.get("lastGeneratedForDay")).longValue());
        } catch (Exception e) {
            throw new InvalidConfigurationException(e);
        }
        
        for (QuestSpecification spec : this.possibleQuests) {
            if (spec instanceof InteractorProtecting) {
                CubeQuest.getInstance().addProtecting((InteractorProtecting) spec);
            }
        }
    }
    
    public void refreshDailyQuests() {
        try {
            this.currentDailyQuests = new ArrayDeque<>(CubeQuest.getInstance().getDatabaseFassade().getDailyQuestData());
        } catch (SQLException e) {
            this.currentDailyQuests =
                    this.currentDailyQuests == null ? new ArrayDeque<>(QuestGenerator.DAYS_TO_KEEP_DAILY_QUESTS) : this.currentDailyQuests;
            
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not refresh current dailyQuests:", e);
        }
    }
    
    public List<QuestSpecification> getPossibleQuestsIncludingNulls() {
        return Collections.unmodifiableList(this.possibleQuests);
    }
    
    public void addPossibleQuest(QuestSpecification qs) {
        this.possibleQuests.add(qs);
        if (qs instanceof InteractorProtecting) {
            CubeQuest.getInstance().addProtecting((InteractorProtecting) qs);
        }
        saveConfig();
    }
    
    public void removePossibleQuest(int index) {
        QuestSpecification spec = this.possibleQuests.set(index, null);
        if (spec != null) {
            if (spec instanceof InteractorProtecting) {
                CubeQuest.getInstance().removeProtecting((InteractorProtecting) spec);
            }
            saveConfig();
        }
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
    
    public ItemStack getMysteriousSpellingBook() {
        return new ItemStack(this.mysteriousSpellingBook);
    }
    
    public void setMysteriousSpellingBook(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            throw new IllegalArgumentException("item may neither be null nor air");
        }
        this.mysteriousSpellingBook = new ItemStack(item);
        saveConfig();
    }
    
    public LocalDate getLastGeneratedForDay() {
        return this.lastGeneratedForDay;
    }
    
    public double getValue(MaterialValueOption o, Material m) {
        return this.materialValues.get(o).getValue(m);
    }
    
    public void setValue(MaterialValueOption o, Material m, double value) {
        this.materialValues.get(o).setValue(m, value);
        saveConfig();
    }
    
    public double getValue(EntityValueOption o, EntityType t) {
        return this.entityValues.get(o).getValue(t);
    }
    
    public void setValue(EntityValueOption o, EntityType t, double value) {
        this.entityValues.get(o).setValue(t, value);
        saveConfig();
    }
    
    public void generateDailyQuests() {
        CubeQuest.getInstance().getLogger().log(Level.INFO, "Starting to generate DailyQuests.");
        
        deleteOldDailyQuests();
        
        DailyQuestData dqData;
        try {
            int dataId = CubeQuest.getInstance().getDatabaseFassade().reserveNewDailyQuestData();
            dqData = new DailyQuestData(dataId, this.questsToGenerate);
            this.currentDailyQuests.addLast(dqData);
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not create new DailyQuestData.", e);
            return;
        }
        
        this.lastGeneratedForDay = LocalDate.now();
        this.lastUsedPossibilities = this.currentlyUsedPossibilities;
        this.currentlyUsedPossibilities = new TreeSet<>(QuestSpecification.SIMILAR_SPECIFICATIONS_COMPARATOR);
        
        Random ran;
        try {
            ran = new Random(Util.fromBytes(MessageDigest.getInstance("MD5").digest(Util.byteArray(this.lastGeneratedForDay.toEpochDay()))));
        } catch (NoSuchAlgorithmException e) {
            throw new AssertionError(e);
        }
        List<String> selectedServers = getServersToGenerateOn(ran, dqData);
        
        try {
            for (int i = 0; i < this.questsToGenerate; i++) {
                double difficulty = (this.questsToGenerate > 1 ? 0.1 + i * 0.8 / (this.questsToGenerate - 1) : 0.5) + 0.1 * ran.nextDouble();
                String server = selectedServers.get(i);
                
                if (server == null) {
                    dailyQuestGenerated(i, generateQuest(i, dqData.getDateString(), difficulty, ran));
                } else {
                    delegateDailyQuestGeneration(server, i, dqData, difficulty, ran);
                }
            }
        } catch (Exception e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                    "QuestGeneration failed with an exception and was aborted (side effects may persist).");
            return;
        }
        saveConfig();
    }
    
    private void deleteOldDailyQuests() {
        if (!this.currentDailyQuests.isEmpty()) {
            // DailyQuests von gestern aus QuestGivern austragen
            for (QuestGiver giver : CubeQuest.getInstance().getDailyQuestGivers()) {
                for (Quest q : this.currentDailyQuests.getLast().getQuests()) {
                    giver.removeQuest(q);
                }
            }
            
            for (Quest q : this.currentDailyQuests.getLast().getQuests()) {
                q.setReady(false);
            }
            
            // Ggf. über eine Woche alte DailyQuests löschen
            while (this.currentDailyQuests.size() >= DAYS_TO_KEEP_DAILY_QUESTS) {
                DailyQuestData dqData = this.currentDailyQuests.removeFirst();
                
                try {
                    CubeQuest.getInstance().getDatabaseFassade().deleteDailyQuestData(dqData);
                } catch (SQLException e) {
                    CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not delete DailyQuests " + dqData + ":", e);
                    return;
                }
                
                ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
                DataOutputStream msgout = new DataOutputStream(msgbytes);
                try {
                    msgout.writeInt(GlobalChatMsgType.DAILY_QUESTS_REMOVED.ordinal());
                    byte[] msgarry = msgbytes.toByteArray();
                    CubeQuest.getInstance().getGlobalChatAPI().sendDataToServers("CubeQuest", msgarry);
                } catch (IOException e) {
                    CubeQuest.getInstance().getLogger().log(Level.SEVERE, "IOException trying to send GlobalChatMessage!", e);
                    return;
                }
                
                // Logge ggf. Fehlermeldungen von Quests, die nicht gelöscht werden können
                for (Quest q : dqData.getQuests()) {
                    try {
                        QuestManager.getInstance().deleteQuest(q);
                    } catch (QuestDeletionFailedException e) {
                        CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not delete DailyQuest " + q + ":", e);
                    }
                }
            }
        }
    }
    
    private List<String> getServersToGenerateOn(Random ran, DailyQuestData dqData) {
        List<String> selectedServers = new ArrayList<>();
        List<String> serversToSelectFrom = new ArrayList<>();
        Map<String, Integer> serversToSelectFromWithAmountOfLegalSpecifications;
        try {
            serversToSelectFromWithAmountOfLegalSpecifications = CubeQuest.getInstance().getDatabaseFassade().getServersToGenerateDailyQuestOn();
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "SQL-Exception while trying to generate daily-quests! No quests generated.", e);
            return Collections.emptyList();
        }
        
        serversToSelectFromWithAmountOfLegalSpecifications.remove(CubeQuest.getInstance().getBungeeServerName());
        for (String server : serversToSelectFromWithAmountOfLegalSpecifications.keySet()) {
            for (int i = 0; i < serversToSelectFromWithAmountOfLegalSpecifications.get(server); i++) {
                serversToSelectFrom.add(server);
            }
        }
        
        if (serversToSelectFrom.isEmpty()) {
            serversToSelectFrom.add(null);
        } else {
            Collections.sort(serversToSelectFrom);
            Collections.shuffle(serversToSelectFrom, ran);
        }
        for (int i = 0; serversToSelectFrom.size() < this.questsToGenerate - this.questsToGenerateOnThisServer; i++) {
            serversToSelectFrom.add(serversToSelectFrom.get(i));
        }
        for (int i = 0; i < this.questsToGenerate; i++) {
            selectedServers.add(i < this.questsToGenerateOnThisServer ? null : serversToSelectFrom.get(i - this.questsToGenerateOnThisServer));
        }
        Collections.shuffle(selectedServers, ran);
        
        return selectedServers;
    }
    
    private void delegateDailyQuestGeneration(String server, int questOrdinal, DailyQuestData dqData, double difficulty, Random ran) {
        try {
            DelegatedGenerationData data = new DelegatedGenerationData(dqData.getDateString(), questOrdinal, difficulty, ran.nextLong());
            CubeQuest.getInstance().getDatabaseFassade().addDelegatedQuestGeneration(server, data);
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "IOException trying to save DelegatedGenerationData!", e);
            return;
        }
        
        ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
        DataOutputStream msgout = new DataOutputStream(msgbytes);
        try {
            msgout.writeInt(GlobalChatMsgType.GENERATE_DAILY_QUEST.ordinal());
            msgout.writeUTF(server);
            byte[] msgarry = msgbytes.toByteArray();
            CubeQuest.getInstance().getGlobalChatAPI().sendDataToServers("CubeQuest", msgarry);
        } catch (IOException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "IOException trying to send GlobalChatMessage!", e);
            return;
        }
    }
    
    public Quest generateQuest(int dailyQuestOrdinal, String dateString, double difficulty, Random ran) {
        
        long diff = this.lastGeneratedForDay == null ? Long.MAX_VALUE : (LocalDate.now().toEpochDay() - this.lastGeneratedForDay.toEpochDay());
        if (diff > 0) {
            this.lastGeneratedForDay = LocalDate.now();
            this.lastUsedPossibilities =
                    diff == 1 ? this.currentlyUsedPossibilities : new TreeSet<>(QuestSpecification.SIMILAR_SPECIFICATIONS_COMPARATOR);
            this.currentlyUsedPossibilities = new TreeSet<>(QuestSpecification.SIMILAR_SPECIFICATIONS_COMPARATOR);
        }
        
        if (this.possibleQuests.stream().noneMatch(qs -> qs != null && qs.isLegal())
                && !DeliveryQuestSpecification.DeliveryQuestPossibilitiesSpecification.getInstance().isLegal()
                && !BlockBreakQuestSpecification.BlockBreakQuestPossibilitiesSpecification.getInstance().isLegal()
                && !BlockPlaceQuestSpecification.BlockPlaceQuestPossibilitiesSpecification.getInstance().isLegal()
                && !FishingQuestSpecification.FishingQuestPossibilitiesSpecification.getInstance().isLegal()
                && !KillEntitiesQuestSpecification.KillEntitiesQuestPossibilitiesSpecification.getInstance().isLegal()) {
            throw new IllegalStateException("Could not generate a DailyQuest for this server as no QuestSpecifications were specified.");
        }
        
        List<QuestSpecification> qsList = new ArrayList<>();
        this.possibleQuests.forEach(qs -> {
            if (qs != null && qs.isLegal()) {
                qsList.add(qs);
            }
        });
        qsList.sort(QuestSpecification.COMPARATOR);
        
        List<QuestSpecificationAndDifficultyPair> generatedList = new ArrayList<>();
        qsList.forEach(qs -> generatedList.add(new QuestSpecificationAndDifficultyPair(qs, qs.generateQuest(ran) + 0.1 * ran.nextGaussian())));
        
        int weighting = DeliveryQuestSpecification.DeliveryQuestPossibilitiesSpecification.getInstance().getWeighting();
        for (int i = 0; i < weighting; i++) {
            DeliveryQuestSpecification qs = new DeliveryQuestSpecification();
            generatedList.add(new QuestSpecificationAndDifficultyPair(qs, qs.generateQuest(ran) + 0.1 * ran.nextGaussian()));
        }
        weighting = BlockBreakQuestSpecification.BlockBreakQuestPossibilitiesSpecification.getInstance().getWeighting();
        for (int i = 0; i < weighting; i++) {
            BlockBreakQuestSpecification qs = new BlockBreakQuestSpecification();
            generatedList.add(new QuestSpecificationAndDifficultyPair(qs, qs.generateQuest(ran) + 0.1 * ran.nextGaussian()));
        }
        weighting = BlockPlaceQuestSpecification.BlockPlaceQuestPossibilitiesSpecification.getInstance().getWeighting();
        for (int i = 0; i < weighting; i++) {
            BlockPlaceQuestSpecification qs = new BlockPlaceQuestSpecification();
            generatedList.add(new QuestSpecificationAndDifficultyPair(qs, qs.generateQuest(ran) + 0.1 * ran.nextGaussian()));
        }
        weighting = FishingQuestSpecification.FishingQuestPossibilitiesSpecification.getInstance().getWeighting();
        for (int i = 0; i < weighting; i++) {
            FishingQuestSpecification qs = new FishingQuestSpecification();
            generatedList.add(new QuestSpecificationAndDifficultyPair(qs, qs.generateQuest(ran) + 0.1 * ran.nextGaussian()));
        }
        weighting = KillEntitiesQuestSpecification.KillEntitiesQuestPossibilitiesSpecification.getInstance().getWeighting();
        for (int i = 0; i < weighting; i++) {
            KillEntitiesQuestSpecification qs = new KillEntitiesQuestSpecification();
            generatedList.add(new QuestSpecificationAndDifficultyPair(qs, qs.generateQuest(ran) + 0.1 * ran.nextGaussian()));
        }
        
        generatedList.sort(new QuestSpeficicationBestFitComparator(difficulty, this.currentlyUsedPossibilities, this.lastUsedPossibilities));
        generatedList.subList(1, generatedList.size() - 1).forEach(qsdp -> qsdp.getQuestSpecification().clearGeneratedQuest());
        
        QuestSpecification resultSpecification = generatedList.get(0).getQuestSpecification();
        this.currentlyUsedPossibilities.add(resultSpecification);
        
        String questName = ChatColor.GOLD + "DailyQuest " + ChatAndTextUtil.toRomanNumber(dailyQuestOrdinal + 1) + " vom " + dateString;
        Reward reward = generateReward(difficulty, ran);
        
        Quest result = resultSpecification.createGeneratedQuest(questName, reward);
        result.setReady(true);
        
        if (!CubeQuest.getInstance().isGeneratingDailyQuests()) {
            saveConfig();
        }
        
        return result;
    }
    
    public boolean checkForDelegatedGeneration() {
        List<DelegatedGenerationData> dataList;
        try {
            dataList = CubeQuest.getInstance().getDatabaseFassade().popDelegatedQuestGenerations();
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not pop delegated quest generations.", e);
            return false;
        }
        
        if (dataList.isEmpty()) {
            return false;
        }
        
        for (DelegatedGenerationData data : dataList) {
            Quest generated = generateQuest(data.questOrdinal, data.dateString, data.difficulty, new Random(data.ranSeed));
            
            try {
                ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
                DataOutputStream msgout = new DataOutputStream(msgbytes);
                msgout.writeInt(GlobalChatMsgType.DAILY_QUEST_GENERATED.ordinal());
                msgout.writeInt(data.questOrdinal);
                msgout.writeInt(generated.getId());
                
                byte[] msgarry = msgbytes.toByteArray();
                CubeQuest.getInstance().getGlobalChatAPI().sendDataToServers("CubeQuest", msgarry);
            } catch (IOException e) {
                CubeQuest.getInstance().getLogger().log(Level.SEVERE, "IOException trying to send GlobalChatMessage!", e);
            }
        }
        
        return true;
    }
    
    /**
     * Generiert die Belohnung für eine DailyQuest
     * 
     * @param difficulty ignored
     * @param rewardModifier ignored
     * @param ran ignored
     * @return Feste Belohnung (nicht zufällig generiert) aus 10 QuestPoint, 5 XP und 1 Mysteriösen
     *         Zauberbuch
     */
    public Reward generateReward(double difficulty, Random ran) {
        return new Reward(0, 10, 5, new ItemStack[] {getMysteriousSpellingBook()});
    }
    
    public void dailyQuestGenerated(int dailyQuestOrdinal, Quest generatedQuest) {
        if (!CubeQuest.getInstance().isGeneratingDailyQuests()) {
            return;
        }
        
        boolean hasSuccessMessage = false;
        for (QuestAction action : generatedQuest.getSuccessActions()) {
            if (action instanceof MessageAction) {
                hasSuccessMessage = true;
                break;
            }
        }
        if (!hasSuccessMessage) {
            generatedQuest.addSuccessAction(new MessageAction(ChatColor.GOLD + "Du hast die " + generatedQuest.getDisplayName() + " abgeschlossen!"));
        }
        
        DailyQuestData dqData = this.currentDailyQuests.getLast();
        
        generatedQuest.setVisible(true);
        dqData.setQuest(dailyQuestOrdinal, Util.addTimeLimit(generatedQuest, dqData.getNextDayDate()));
        
        try {
            dqData.saveToDatabase();
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not save DailyQuestData.", e);
            return;
        }
        
        try {
            ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
            DataOutputStream msgout = new DataOutputStream(msgbytes);
            msgout.writeInt(GlobalChatMsgType.DAILY_QUEST_FINISHED.ordinal());
            
            byte[] msgarry = msgbytes.toByteArray();
            CubeQuest.getInstance().getGlobalChatAPI().sendDataToServers("CubeQuest", msgarry);
        } catch (IOException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "IOException trying to send GlobalChatMessage!", e);
        }
        
        if (dqData.getQuests().stream().allMatch(q -> q != null)) {
            for (QuestGiver giver : CubeQuest.getInstance().getDailyQuestGivers()) {
                for (Quest q : dqData.getQuests()) {
                    giver.addQuest(q);
                }
            }
            
            CubeQuest.getInstance().getLogger().log(Level.INFO, "DailyQuests generated.");
        }
    }
    
    public List<Quest> getTodaysDailyQuests() {
        DailyQuestData dqData = this.currentDailyQuests.getLast();
        return this.currentDailyQuests == null ? null : dqData.getQuests();
    }
    
    public Collection<Quest> getAllDailyQuests() {
        Set<Quest> result = new LinkedHashSet<>();
        for (DailyQuestData dqData : this.currentDailyQuests) {
            for (Quest q : dqData.getQuests()) {
                result.add(q);
            }
        }
        
        return result;
    }
    
    public int countLegalQuestSecifications() {
        int i = 0;
        for (QuestSpecification qs : this.possibleQuests) {
            if (qs != null && qs.isLegal()) {
                i++;
            }
        }
        
        try {
            CubeQuest.getInstance().getDatabaseFassade().setLegalQuestSpecificationCount(i);
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not update count of legal QuestSpecificaitons in database.", e);
        }
        
        return i;
    }
    
    public List<BaseComponent[]> getSpecificationInfo() {
        List<BaseComponent[]> result = new ArrayList<>();
        
        int index = 1;
        for (QuestSpecification qs : this.possibleQuests) {
            if (qs != null) {
                ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/quest removeQuestSpecification " + index);
                HoverEvent hoverEvent =
                        new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Spezifikation an Index " + index + " entfernen.").create());
                result.add(new ComponentBuilder(index + ": ").append(qs.getSpecificationInfo()).append(" ").append("[Löschen]").color(ChatColor.RED)
                        .event(clickEvent).event(hoverEvent).create());
            }
            index++;
        }
        
        return result;
    }
    
    public List<BaseComponent[]> getDeliveryReceiverSpecificationInfo() {
        return DeliveryQuestSpecification.DeliveryQuestPossibilitiesSpecification.getInstance().getReceiverSpecificationInfo();
    }
    
    public List<BaseComponent[]> getDeliveryContentSpecificationInfo() {
        return DeliveryQuestSpecification.DeliveryQuestPossibilitiesSpecification.getInstance().getContentSpecificationInfo();
    }
    
    public List<BaseComponent[]> getBlockBreakSpecificationInfo() {
        return BlockBreakQuestSpecification.BlockBreakQuestPossibilitiesSpecification.getInstance().getSpecificationInfo();
    }
    
    public List<BaseComponent[]> getBlockPlaceSpecificationInfo() {
        return BlockPlaceQuestSpecification.BlockPlaceQuestPossibilitiesSpecification.getInstance().getSpecificationInfo();
    }
    
    public List<BaseComponent[]> getFishingSpecificationInfo() {
        return FishingQuestSpecification.FishingQuestPossibilitiesSpecification.getInstance().getSpecificationInfo();
    }
    
    public List<BaseComponent[]> getKillEntitiesSpecificationInfo() {
        return KillEntitiesQuestSpecification.KillEntitiesQuestPossibilitiesSpecification.getInstance().getSpecificationInfo();
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        
        result.put("questsToGenerate", this.questsToGenerate);
        result.put("questsToGenerateOnThisServer", this.questsToGenerateOnThisServer);
        result.put("mysteriousSpellingBook", this.mysteriousSpellingBook);
        
        List<QuestSpecification> possibleQSList = new ArrayList<>(this.possibleQuests);
        possibleQSList.removeIf(qs -> qs == null);
        possibleQSList.sort(QuestSpecification.COMPARATOR);
        result.put("possibleQuests", possibleQSList);
        
        List<QuestSpecification> lastUsedQSList = new ArrayList<>(this.lastUsedPossibilities);
        result.put("lastUsedPossibilities", lastUsedQSList);
        List<QuestSpecification> currentlyUsedQSList = new ArrayList<>(this.currentlyUsedPossibilities);
        result.put("currentlyUsedPossibilities", currentlyUsedQSList);
        
        result.put("deliveryQuestSpecifications", DeliveryQuestSpecification.DeliveryQuestPossibilitiesSpecification.getInstance().serialize());
        result.put("blockBreakQuestSpecifications", BlockBreakQuestSpecification.BlockBreakQuestPossibilitiesSpecification.getInstance().serialize());
        result.put("blockPlaceQuestSpecifications", BlockPlaceQuestSpecification.BlockPlaceQuestPossibilitiesSpecification.getInstance().serialize());
        result.put("fishingQuestSpecifications", FishingQuestSpecification.FishingQuestPossibilitiesSpecification.getInstance().serialize());
        result.put("killEntitiesQuestSpecifications",
                KillEntitiesQuestSpecification.KillEntitiesQuestPossibilitiesSpecification.getInstance().serialize());
        
        result.put("materialValues", Util.serializedEnumMap(this.materialValues));
        result.put("entityValues", Util.serializedEnumMap(this.entityValues));
        
        result.put("lastGeneratedForDay", this.lastGeneratedForDay == null ? null : this.lastGeneratedForDay.toEpochDay());
        
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
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not save QuestGenerator.", e);
        }
    }
    
}
