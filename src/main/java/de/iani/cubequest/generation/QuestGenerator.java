package de.iani.cubequest.generation;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.EventListener.GlobalChatMsgType;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.Reward;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.ItemStackUtil;
import de.iani.cubequest.util.Util;
import javafx.util.Pair;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class QuestGenerator implements ConfigurationSerializable {

    private static QuestGenerator instance;

    private int questsToGenerate;
    private int questsToGenerateOnThisServer;

    private List<QuestSpecification> possibleQuests;
    private DailyQuestData currentDailyQuests;

    private Map<Material, Double> materialValues;
    private Map<EntityType, Double> entityValues;
    private double defaultMaterialValue;
    private double defaultEntityValue;

    private LocalDate lastGeneratedForDay;

    //private Object saveLock = new Object();

    public class DailyQuestData implements ConfigurationSerializable {

        private Quest[] quests;
        private String dateString;
        private Date nextDayDate;

        private DailyQuestData() {
            dateString = (new SimpleDateFormat("dd.MM.yyyy")).format(new Date());
            try {
                Date today = (new SimpleDateFormat("dd.MM.yyyy hh:mm:ss")).parse(dateString + " 00:00:00");
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(today);
                calendar.add(Calendar.DATE, 1);
                nextDayDate = calendar.getTime();
            } catch (ParseException e) {
                CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not parse next day.", e);
            }
        }

        @SuppressWarnings("unchecked")
        public DailyQuestData(Map<String, Object> serialized) {
            List<Integer> currentDailyQuestList = (List<Integer>) serialized.get("currentDailyQuests");
            quests = new Quest[currentDailyQuestList.size()];
            for (int i=0; i<quests.length; i++) {
                quests[i] = currentDailyQuestList.get(i) == null? null : QuestManager.getInstance().getQuest(currentDailyQuestList.get(i));
            }
            dateString = (String) serialized.get("dateString");
            nextDayDate = serialized.get("nextDayDate") == null? null : new Date((Long) serialized.get("nextDayDate"));
        }

        @Override
        public Map<String, Object> serialize() {
            Map<String, Object> result = new HashMap<>();

            List<Integer> currentDailyQuestList = new ArrayList<>();
            Arrays.stream(quests).forEach(q -> currentDailyQuestList.add(q == null? null : q.getId()));
            result.put("quests", currentDailyQuestList);

            result.put("dateString", dateString);
            result.put("nextDayDate", nextDayDate == null? null : nextDayDate.getTime());

            return result;
        }

    }

    public class QuestSpecificationAndDifficultyPair extends Pair<QuestSpecification, Double> {

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

    public class DifferenceInDifficultyComparator implements Comparator<QuestSpecificationAndDifficultyPair> {

        private double targetDifficulty;

        public DifferenceInDifficultyComparator(double targetDifficulty) {
            this.targetDifficulty = targetDifficulty;
        }

        @Override
        public int compare(QuestSpecificationAndDifficultyPair o1, QuestSpecificationAndDifficultyPair o2) {
            return Double.compare(Math.abs(targetDifficulty - o1.getDifficulty()), Math.abs(targetDifficulty - o2.getDifficulty()));
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

    private QuestGenerator() {
        this.possibleQuests = new ArrayList<>();
        this.currentDailyQuests = new DailyQuestData();
        this.materialValues = new EnumMap<>(Material.class);
        this.entityValues = new EnumMap<>(EntityType.class);
        this.defaultMaterialValue = 0.0025; // ca. ein Holzblock (Stamm)
        this.defaultEntityValue = 0.1;      // ca. ein Zombie

        // Ein paar voreingestellte Werte

        materialValues.put(Material.DIAMOND, 0.125);
        materialValues.put(Material.GOLD_INGOT, 0.0105);
        materialValues.put(Material.IRON_INGOT, 0.006);
        materialValues.put(Material.COBBLESTONE, 0.002);
        materialValues.put(Material.WHEAT, 0.001);
        materialValues.put(Material.CROPS, 0.0015);
        materialValues.put(Material.CACTUS, 0.005);

        entityValues.put(EntityType.CHICKEN, 0.05);
        entityValues.put(EntityType.PIG, 0.05);
        entityValues.put(EntityType.COW, 0.05);
        entityValues.put(EntityType.MUSHROOM_COW, 0.06);
        entityValues.put(EntityType.LLAMA, 0.07);
        entityValues.put(EntityType.WITCH, 0.5);
        entityValues.put(EntityType.CREEPER, 0.2);
        entityValues.put(EntityType.SKELETON, 0.25);
    }

    @SuppressWarnings("unchecked")
    public QuestGenerator(Map<String, Object> serialized) throws InvalidConfigurationException {
        if (instance != null) {
            throw new IllegalStateException("Can't initilize second instance of singleton!");
        }

        try {
            questsToGenerate = (Integer) serialized.get("questsToGenerate");
            questsToGenerateOnThisServer = (Integer) serialized.get("questsToGenerateOnThisServer");

            possibleQuests = (List<QuestSpecification>) serialized.get("possibleQuests");
            DeliveryQuestSpecification.DeliveryQuestPossibilitiesSpecification.deserialize((Map<String, Object>) serialized.get("deliveryQuestSpecifications"));
            BlockBreakQuestSpecification.BlockBreakQuestPossibilitiesSpecification.deserialize((Map<String, Object>) serialized.get("blockBreakQuestSpecifications"));

            currentDailyQuests = (DailyQuestData) serialized.get("dailyQuestData");
            lastGeneratedForDay = serialized.get("lastGeneratedForDay") == null? null : LocalDate.ofEpochDay((Long) serialized.get("lastGeneratedForDay"));

            Map<String, Double> mValues = (Map<String, Double>) serialized.get("materialValues");
            materialValues = new EnumMap<>(Material.class);
            mValues.forEach((materialName, value) -> materialValues.put(Material.valueOf(materialName), value));

            Map<String, Double> eValues = (Map<String, Double>) serialized.get("entityValues");
            entityValues = new EnumMap<>(EntityType.class);
            eValues.forEach((entityName, value) -> entityValues.put(EntityType.valueOf(entityName), value));

            defaultMaterialValue = (double) serialized.get("defaultMaterialValue");
            defaultEntityValue = (double) serialized.get("defaultEntityValue");
        } catch (Exception e) {
            throw new InvalidConfigurationException(e);
        }
    }

    public List<QuestSpecification> getPossibleQuests() {
        return Collections.unmodifiableList(possibleQuests);
    }

    public void addPossibleQuest(QuestSpecification qs) {
        possibleQuests.add(qs);
    }

    public void removePossibleQuest(int index) {
        possibleQuests.remove(index);
    }

    public int getQuestsToGenerate() {
        return questsToGenerate;
    }

    public void setQuestsToGenerate(int questsToGenerate) {
        this.questsToGenerate = questsToGenerate;
    }

    public int getQuestsToGenerateOnThisServer() {
        return questsToGenerateOnThisServer;
    }

    public void setQuestsToGenerateOnThisServer(int questsToGenerateOnThisServer) {
        this.questsToGenerateOnThisServer = questsToGenerateOnThisServer;
    }

    public LocalDate getLastGeneratedForDay() {
        return lastGeneratedForDay;
    }

    public double getValue(Material m) {
        return materialValues.containsKey(m)? materialValues.get(m) : defaultMaterialValue;
    }

    public void setValue(Material m, double value) {
        materialValues.put(m, value);
    }

    public double getValue(EntityType t) {
        return entityValues.containsKey(t)? entityValues.get(t) : defaultEntityValue;
    }

    public void setValue(EntityType t, double value) {
        entityValues.put(t, value);
    }

    public void generateDailyQuests() {
        lastGeneratedForDay = LocalDate.now();
        currentDailyQuests = new DailyQuestData();
        currentDailyQuests.quests = new Quest[questsToGenerate];

        Random ran = new Random(lastGeneratedForDay.toEpochDay());

        List<String> selectedServers = new ArrayList<>();
        List<String> serversToSelectFrom = new ArrayList<>();
        Map<String, Integer> serversToSelectFromWithAmountOfLegalSpecifications;
        try {
            serversToSelectFromWithAmountOfLegalSpecifications = CubeQuest.getInstance().getDatabaseFassade().getServersToGenerateDailyQuestOn();
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "SQL-Exception while trying to generate daily-quests! No quests generated.", e);
            return;
        }

        serversToSelectFromWithAmountOfLegalSpecifications.remove(CubeQuest.getInstance().getBungeeServerName());
        for (String server: serversToSelectFromWithAmountOfLegalSpecifications.keySet()) {
            for (int i=0; i<serversToSelectFromWithAmountOfLegalSpecifications.get(server); i++) {
                serversToSelectFrom.add(server);
            }
        }

        if (serversToSelectFrom.isEmpty()) {
            serversToSelectFrom.add(null);
        } else {
            Collections.sort(serversToSelectFrom);
            Collections.shuffle(serversToSelectFrom, ran);
        }
        for (int i=0; serversToSelectFrom.size() < questsToGenerate - questsToGenerateOnThisServer; i++) {
            serversToSelectFrom.add(serversToSelectFrom.get(i));
        }
        for (int i=0; i<questsToGenerate; i++) {
            selectedServers.add(i < questsToGenerateOnThisServer? null : serversToSelectFrom.get(i - questsToGenerateOnThisServer));
        }
        Collections.shuffle(selectedServers, ran);

        for (int i=0; i<questsToGenerate; i++) {
            double difficulty = (questsToGenerate > 1? 0.1 + i*0.8/(questsToGenerate-1) : 0.5) + 0.1*ran.nextDouble();
            String server = selectedServers.get(i);

            if (server == null) {
                dailyQuestGenerated(i, generateQuest(i, currentDailyQuests.dateString, difficulty, ran));
            } else {
                ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
                DataOutputStream msgout = new DataOutputStream(msgbytes);
                try {
                    msgout.writeInt(GlobalChatMsgType.GENERATE_DAILY_QUEST.ordinal());
                    msgout.writeUTF(server);
                    msgout.writeInt(i);
                    msgout.writeUTF(currentDailyQuests.dateString);
                    msgout.writeDouble(difficulty);
                    msgout.writeLong(ran.nextLong());
                } catch (IOException e) {
                    CubeQuest.getInstance().getLogger().log(Level.SEVERE, "IOException trying to send GlobalChatMessage!", e);
                    return;
                }

                byte[] msgarry = msgbytes.toByteArray();
                CubeQuest.getInstance().getGlobalChatAPI().sendDataToServers("CubeQuest", msgarry);
            }
        }
    }

    public Quest generateQuest(int dailyQuestOrdinal, String dateString, double difficulty, Random ran) {
        if (possibleQuests.isEmpty()) {
            CubeQuest.getInstance().getLogger().log(Level.WARNING, "Could not generate a DailyQuest for this server as no QuestSpecifications were specified.");
            return null;
        }

        List<QuestSpecification> qsList = new ArrayList<>();
        possibleQuests.forEach(qs -> {
            if (qs.isLegal()) {
                qsList.add(qs);
            }
        });
        qsList.sort(QuestSpecification.COMPARATOR);

        List<QuestSpecificationAndDifficultyPair> generatedList = new ArrayList<>();
        qsList.forEach(qs ->  generatedList.add(new QuestSpecificationAndDifficultyPair(qs, qs.generateQuest(ran) + 0.1*ran.nextGaussian())));

        int weighting = DeliveryQuestSpecification.DeliveryQuestPossibilitiesSpecification.getInstance().getWeighting();
        for (int i=0; i<weighting; i++) {
            DeliveryQuestSpecification qs = new DeliveryQuestSpecification();
            generatedList.add(new QuestSpecificationAndDifficultyPair(qs, qs.generateQuest(ran) + 0.1*ran.nextGaussian()));
        }
        weighting = BlockBreakQuestSpecification.BlockBreakQuestPossibilitiesSpecification.getInstance().getWeighting();
        for (int i=0; i<weighting; i++) {
            BlockBreakQuestSpecification qs = new BlockBreakQuestSpecification();
            generatedList.add(new QuestSpecificationAndDifficultyPair(qs, qs.generateQuest(ran) + 0.1*ran.nextGaussian()));
        }
        weighting = BlockPlaceQuestSpecification.BlockPlaceQuestPossibilitiesSpecification.getInstance().getWeighting();
        for (int i=0; i<weighting; i++) {
            BlockPlaceQuestSpecification qs = new BlockPlaceQuestSpecification();
            generatedList.add(new QuestSpecificationAndDifficultyPair(qs, qs.generateQuest(ran) + 0.1*ran.nextGaussian()));
        }
        weighting = KillEntitiesQuestSpecification.KillEntitiesQuestPossibilitiesSpecification.getInstance().getWeighting();
        for (int i=0; i<weighting; i++) {
            KillEntitiesQuestSpecification qs = new KillEntitiesQuestSpecification();
            generatedList.add(new QuestSpecificationAndDifficultyPair(qs, qs.generateQuest(ran) + 0.1*ran.nextGaussian()));
        }

        generatedList.sort(new DifferenceInDifficultyComparator(difficulty));
        generatedList.subList(1, generatedList.size()-1).forEach(qsdp -> qsdp.getQuestSpecification().clearGeneratedQuest());

        QuestSpecification resultSpecification = generatedList.get(0).getQuestSpecification();
        String questName = "DailyQuest " + ChatAndTextUtil.toRomanNumber(dailyQuestOrdinal+1) + " vom " + dateString;
        Reward reward = generateReward(difficulty, ran);

        Quest result = resultSpecification.createGeneratedQuest(questName, reward);

        return result;
    }

    /**
     * Generiert die Belohnung für eine DailyQuest
     * @param difficulty ignored
     * @param rewardModifier ignored
     * @param ran ignored
     * @return Feste Belohnung (nicht zufällig generiert) aus 1 QuestPoint, 5 XP und 1 Mysteriösen Zauberbuch
     */
    public Reward generateReward(double difficulty, Random ran) {
        return new Reward(0, 1, 5, new ItemStack[] {ItemStackUtil.getMysteriousSpellBook()});
    }

    public void dailyQuestGenerated(int dailyQuestOrdinal, Quest generatedQuest) {
        currentDailyQuests.quests[dailyQuestOrdinal] = Util.addTimeLimit(generatedQuest, currentDailyQuests.nextDayDate);

        if (Arrays.stream(currentDailyQuests.quests).allMatch(q -> q != null)) {
            //TODO: result zu quest-giver hinzufügen, etc.
        }

        saveConfig();
    }

    public Quest[] getGeneratedDailyQuests() {
        return currentDailyQuests == null? null : Arrays.copyOf(currentDailyQuests.quests, currentDailyQuests.quests.length);
    }

    public int countLegalQuestSecifications() {
        int i = 0;
        for (QuestSpecification qs: possibleQuests) {
            if (qs.isLegal()) {
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

        result.add(ChatAndTextUtil.headline1("Liste der Quest-Specificationen"));
        result.add(new ComponentBuilder("").create());
        int index = 1;
        for (QuestSpecification qs: possibleQuests) {
            result.add(new ComponentBuilder(index + ": ").append(qs.getSpecificationInfo()).create());
        }

        result.add(new ComponentBuilder("").create());
        result.addAll(DeliveryQuestSpecification.DeliveryQuestPossibilitiesSpecification.getInstance().getSpecificationInfo());
        result.add(new ComponentBuilder("").create());
        result.addAll(KillEntitiesQuestSpecification.KillEntitiesQuestPossibilitiesSpecification.getInstance().getSpecificationInfo());
        result.add(new ComponentBuilder("").create());
        result.addAll(BlockBreakQuestSpecification.BlockBreakQuestPossibilitiesSpecification.getInstance().getSpecificationInfo());
        result.add(new ComponentBuilder("").create());
        result.addAll(BlockPlaceQuestSpecification.BlockPlaceQuestPossibilitiesSpecification.getInstance().getSpecificationInfo());

        return result;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();

        result.put("questsToGenerate", questsToGenerate);
        result.put("questsToGenerateOnThisServer", questsToGenerateOnThisServer);

        List<QuestSpecification> possibleQSList = new ArrayList<>(possibleQuests);
        result.put("possibleQuests", possibleQSList);
        result.put("deliveryQuestSpecifications", DeliveryQuestSpecification.DeliveryQuestPossibilitiesSpecification.getInstance().serialize());
        result.put("blockBreakQuestSpecifications", BlockBreakQuestSpecification.BlockBreakQuestPossibilitiesSpecification.getInstance().serialize());

        result.put("currentDailyQuests", currentDailyQuests);
        result.put("lastGeneratedForDay", lastGeneratedForDay == null? null : lastGeneratedForDay.toEpochDay());

        Map<String, Double> mValues = new HashMap<>();
        materialValues.forEach((material, value) -> mValues.put(material.name(), value));
        result.put("materialValues", mValues);

        Map<String, Double> eValues = new HashMap<>();
        entityValues.forEach((entity, value) -> eValues.put(entity.name(), value));
        result.put("entityValues", eValues);

        result.put("defaultMaterialValue", defaultMaterialValue);
        result.put("defaultEntityValue", defaultEntityValue);

        return result;
    }

    public void saveConfig() {
        CubeQuest.getInstance().getDataFolder().mkdirs();
        File configFile = new File(CubeQuest.getInstance().getDataFolder(), "generator.yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("generator", this);
        try {
            config.save(configFile);
        } catch (IOException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not save QuestGenerator.", e);
        }
        /*(new Thread() {
            @Override
            public void run() {
                synchronized (saveLock) {

                }
            }
        }).start();*/
    }

}
