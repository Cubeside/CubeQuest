package de.iani.cubequest.generation;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.EventListener.MsgType;
import de.iani.cubequest.quests.Quest;
import javafx.util.Pair;

public class QuestGenerator implements ConfigurationSerializable {

    public static final double INITIAL_DIFICULTY_TOLARANCE = 0.1;

    private int questsToGenerate;
    private int questsToGenerateOnThisServer;

    private Set<QuestSpecification> possibleQuests;

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
            return -1 * Double.compare(Math.abs(targetDifficulty - o1.getDifficulty()), Math.abs(targetDifficulty - o2.getDifficulty()));
        }

    }

    public QuestGenerator() {
        possibleQuests = new HashSet<QuestSpecification>();
    }

    public QuestGenerator(Map<String, Object> serialized) throws InvalidConfigurationException {
        try {
            //TODO
        } catch (Exception e) {
            throw new InvalidConfigurationException(e);
        }
    }

    public void generateDailyQuests() {
        Calendar calendar = Calendar.getInstance();
        Random ran = new Random(calendar.get(Calendar.DATE) + 31*(calendar.get(Calendar.MONTH) + 12*calendar.get(Calendar.YEAR)));

        List<String> servers = new ArrayList<String>();
        List<String> otherLegalServers;
        try {
            otherLegalServers = CubeQuest.getInstance().getDatabaseFassade().getServersToGenerateDailyQuestOn();
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "SQL-Exception while trying to generate daily-quests! No quests generated.", e);
            return;
        }

        otherLegalServers.removeIf(name -> name.equals(CubeQuest.getInstance().getBungeeServerName()));
        if (otherLegalServers.isEmpty()) {
            otherLegalServers.add(null);
        } else {
            Collections.sort(otherLegalServers);
            Collections.shuffle(otherLegalServers, ran);
        }
        for (int i=0; otherLegalServers.size() < questsToGenerate - questsToGenerateOnThisServer; i++) {
            otherLegalServers.add(otherLegalServers.get(i));
        }
        for (int i=0; i<questsToGenerate; i++) {
            servers.add(i < questsToGenerateOnThisServer? null : otherLegalServers.get(i - questsToGenerateOnThisServer));
        }
        Collections.shuffle(servers, ran);

        for (int i=0; i<questsToGenerate; i++) {
            double difficulty = (questsToGenerate > 1? 0.1 + i*0.8/(questsToGenerate-1) : 0.5) + 0.1*ran.nextDouble();
            String server = servers.get(i);

            if (server == null) {
                generateQuest(difficulty, ran);
            } else {
                ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
                DataOutputStream msgout = new DataOutputStream(msgbytes);
                try {
                    msgout.writeInt(MsgType.GENERATE_QUEST.ordinal());
                    msgout.writeDouble(difficulty);
                    msgout.writeLong(ran.nextLong());
                } catch (IOException e) {
                    CubeQuest.getInstance().getLogger().log(Level.SEVERE, "IOException trying to send PluginMessage!", e);
                    return;
                }
                byte[] msgarry = msgbytes.toByteArray();

                CubeQuest.getInstance().addWaitingForPlayer(() -> {
                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                    out.writeUTF("Forward");
                    out.writeUTF(server);
                    out.writeUTF("CubeQuest");
                    out.writeShort(msgarry.length);
                    out.write(msgarry);

                    Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
                    player.sendPluginMessage(CubeQuest.getInstance(), "BungeeCord", out.toByteArray());
                });
            }
        }
    }

    public Quest generateQuest(double difficulty, Random ran) {
        if (possibleQuests.isEmpty()) {
            CubeQuest.getInstance().getLogger().log(Level.WARNING, "Could not generate a DailyQuest for this server as no QuestSpecifications were specified.");
            return null;
        }

        List<QuestSpecification> qsList = new ArrayList<QuestSpecification>(possibleQuests);
        qsList.sort(QuestSpecification.COMPARATOR);

        List<QuestSpecificationAndDifficultyPair> generatedList = new ArrayList<QuestSpecificationAndDifficultyPair>();
        qsList.forEach(qs ->  generatedList.add(new QuestSpecificationAndDifficultyPair(qs, qs.generateQuest(ran))));
        generatedList.sort(new DifferenceInDifficultyComparator(difficulty));

        Quest result = generatedList.get(0).getQuestSpecification().createGeneratedQuest();
        generatedList.subList(1, generatedList.size()-1).forEach(qsdp -> qsdp.getQuestSpecification().clearGeneratedQuest());

        //TODO: result zu quest-giver hinzufügen, evtl aus verschiedenen results complex-quests bilden, etc.
        return result;
    }

    @Override
    public Map<String, Object> serialize() {
        //TODO
        return null;
    }

}
