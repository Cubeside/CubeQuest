package de.iani.cubequest.generation;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.EventListener.GlobalChatMsgType;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.Reward;
import de.iani.cubequest.quests.ComplexQuest;
import de.iani.cubequest.quests.ComplexQuest.Structure;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.quests.WaitForDateQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.ItemStackUtil;
import javafx.util.Pair;
import net.md_5.bungee.api.ChatColor;

public class QuestGenerator implements ConfigurationSerializable {

    public static final double INITIAL_DIFICULTY_TOLARANCE = 0.1;

    private int questsToGenerate;
    private int questsToGenerateOnThisServer;

    private Set<QuestSpecification> possibleQuests;
    private DailyQuestData currentDailyQuests;

    private LocalDate lastGeneratedForDay;

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
            Map<String, Object> result = new HashMap<String, Object>();

            List<Integer> currentDailyQuestList = new ArrayList<Integer>();
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

    public QuestGenerator() {
        possibleQuests = new HashSet<QuestSpecification>();
        currentDailyQuests = new DailyQuestData();
    }

    @SuppressWarnings("unchecked")
    public QuestGenerator(Map<String, Object> serialized) throws InvalidConfigurationException {
        try {
            questsToGenerate = (Integer) serialized.get("questsToGenerate");
            questsToGenerateOnThisServer = (Integer) serialized.get("questsToGenerateOnThisServer");
            possibleQuests = new HashSet<QuestSpecification>((List<QuestSpecification>) serialized.get("possibleQuests"));
            currentDailyQuests = (DailyQuestData) serialized.get("dailyQuestData");
            lastGeneratedForDay = serialized.get("lastGeneratedForDay") == null? null : LocalDate.ofEpochDay((Long) serialized.get("lastGeneratedForDay"));
        } catch (Exception e) {
            throw new InvalidConfigurationException(e);
        }
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

    public void generateDailyQuests() {
        lastGeneratedForDay = LocalDate.now();
        currentDailyQuests = new DailyQuestData();
        currentDailyQuests.quests = new Quest[questsToGenerate];

        Random ran = new Random(lastGeneratedForDay.toEpochDay());

        List<String> selectedServers = new ArrayList<String>();
        List<String> serversToSelectFrom = new ArrayList<String>();
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

        List<QuestSpecification> qsList = new ArrayList<QuestSpecification>();
        possibleQuests.forEach(qs -> {
            if (qs.isLegal()) {
                qsList.add(qs);
            }
        });
        qsList.sort(QuestSpecification.COMPARATOR);

        List<QuestSpecificationAndDifficultyPair> generatedList = new ArrayList<QuestSpecificationAndDifficultyPair>();
        qsList.forEach(qs ->  generatedList.add(new QuestSpecificationAndDifficultyPair(qs, qs.generateQuest(ran) + 0.1*ran.nextGaussian())));
        generatedList.sort(new DifferenceInDifficultyComparator(difficulty));
        generatedList.subList(1, generatedList.size()-1).forEach(qsdp -> qsdp.getQuestSpecification().clearGeneratedQuest());

        QuestSpecification resultSpecification = generatedList.get(0).getQuestSpecification();
        String questName = "DailyQuest " + ChatAndTextUtil.toRomanNumber(dailyQuestOrdinal+1) + " vom " + dateString;
        Reward reward = generateReward(difficulty, resultSpecification.getRewardModifier(), ran);

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
    public Reward generateReward(double difficulty, double rewardModifier, Random ran) {
        return new Reward(0, 1, 5, new ItemStack[] {ItemStackUtil.getMysteriousSpellBook()});
    }

    public void dailyQuestGenerated(int dailyQuestOrdinal, Quest generatedQuest) {
        WaitForDateQuest timeoutQuest = CubeQuest.getInstance().getQuestCreator().createQuest(WaitForDateQuest.class);
        timeoutQuest.setDate(currentDailyQuests.nextDayDate);

        try {
            int dailyQuestId = CubeQuest.getInstance().getDatabaseFassade().reserveNewQuest();
            currentDailyQuests.quests[dailyQuestOrdinal] = new ComplexQuest(dailyQuestId, "",
                    "",
                    "",
                    CubeQuest.PLUGIN_TAG + " " + ChatColor.RED + "Die Zeit für deine Quest \"" + generatedQuest.getName() + "\" ist leider abgelaufen.",
                    null, null,
                    Structure.ALLTOBEDONE,
                    new HashSet<Quest>(Arrays.asList(generatedQuest)),
                    timeoutQuest, null);
            QuestManager.getInstance().addQuest(currentDailyQuests.quests[dailyQuestOrdinal]);
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not create DailyQuest.", e);
        }

        if (Arrays.stream(currentDailyQuests.quests).allMatch(q -> q != null)) {
            //TODO: result zu quest-giver hinzufügen, etc.
        }
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

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<String, Object>();

        result.put("questsToGenerate", questsToGenerate);
        result.put("questsToGenerateOnThisServer", questsToGenerateOnThisServer);

        List<QuestSpecification> possibleQSList = new ArrayList<QuestSpecification>(possibleQuests);
        result.put("possibleQuests", possibleQSList);

        result.put("currentDailyQuests", currentDailyQuests);
        result.put("lastGeneratedForDay", lastGeneratedForDay == null? null : lastGeneratedForDay.toEpochDay());

        return result;
    }

}
