package de.iani.cubequest;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import de.iani.cubequest.EventListener.MsgType;
import de.iani.cubequest.quests.BlockBreakQuest;
import de.iani.cubequest.quests.BlockPlaceQuest;
import de.iani.cubequest.quests.CommandQuest;
import de.iani.cubequest.quests.ComplexQuest;
import de.iani.cubequest.quests.DeliveryQuest;
import de.iani.cubequest.quests.FishingQuest;
import de.iani.cubequest.quests.GotoQuest;
import de.iani.cubequest.quests.KillEntitiesQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.quests.TalkQuest;
import de.iani.cubequest.quests.WaitForTimeQuest;

public class QuestCreator {

    public enum QuestType {
        BLOCK_BREAK_QUEST, BLOCK_PLACE_QUEST, COMMAND_QUEST, COMPLEX_QUEST, DELIVERY_QUEST, FISHING_QUEST, GOTO_QUEST,
        KILL_ENTITIES_QUEST, TALK_QUEST, WAIT_FOR_DATE_QUEST, WAIT_FOR_TIME_QUEST;

        private static EnumMap<QuestType, Class<? extends Quest>> classes;
        private static HashMap<Class<? extends Quest>, QuestType> types;

        static {
            classes = new EnumMap<QuestType, Class<? extends Quest>>(QuestType.class);
            classes.put(BLOCK_BREAK_QUEST, BlockBreakQuest.class);
            classes.put(BLOCK_PLACE_QUEST, BlockPlaceQuest.class);
            classes.put(COMMAND_QUEST, CommandQuest.class);
            classes.put(COMPLEX_QUEST, ComplexQuest.class);
            classes.put(DELIVERY_QUEST, DeliveryQuest.class);
            classes.put(FISHING_QUEST, FishingQuest.class);
            classes.put(GOTO_QUEST, GotoQuest.class);
            classes.put(KILL_ENTITIES_QUEST, KillEntitiesQuest.class);
            classes.put(TALK_QUEST, TalkQuest.class);
            classes.put(WAIT_FOR_DATE_QUEST, WaitForTimeQuest.class);
            classes.put(WAIT_FOR_TIME_QUEST, WaitForTimeQuest.class);

            types = new HashMap<Class<? extends Quest>, QuestType>();
            types.put(BlockBreakQuest.class, BLOCK_BREAK_QUEST);
            types.put(BlockPlaceQuest.class, BLOCK_PLACE_QUEST);
            types.put(CommandQuest.class, COMMAND_QUEST);
            types.put(ComplexQuest.class, COMPLEX_QUEST);
            types.put(DeliveryQuest.class, DELIVERY_QUEST);
            types.put(FishingQuest.class, FISHING_QUEST);
            types.put(GotoQuest.class, GOTO_QUEST);
            types.put(KillEntitiesQuest.class, KILL_ENTITIES_QUEST);
            types.put(TalkQuest.class, TALK_QUEST);
            types.put(WaitForTimeQuest.class, WAIT_FOR_DATE_QUEST);
            types.put(WaitForTimeQuest.class, WAIT_FOR_TIME_QUEST);
        }

        public static QuestType getQuestType(Class<? extends Quest> c) {
            return types.get(c);
        }

        public Class<? extends Quest> getQuestClass() {
            return classes.get(this);
        }

    }

    private YamlConfiguration deserialize(String serialized) {
        if (serialized == null) {
            throw new NullPointerException();
        }
        YamlConfiguration yc = new YamlConfiguration();
        try {
            yc.loadFromString(serialized);
        } catch (InvalidConfigurationException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not deserialize quest:\n" + serialized, e);
            return null;
        }
        QuestType type = QuestType.valueOf(yc.getString("type"));
        if (type == null) {
            throw new IllegalArgumentException("Invalid type!");
        }
        return yc;
    }

    private Quest create(int id, String serialized) {
        YamlConfiguration yc = deserialize(serialized);
        QuestType type = QuestType.valueOf(yc.getString("type"));
        Quest result;
        try {
            result = type.getQuestClass().getConstructor(int.class).newInstance(id);
            result.deserialize(yc);
        } catch (InvalidConfigurationException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not deserialize quest with id " + id + ":\n" + serialized, e);
            return null;
        }
        CubeQuest.getInstance().getQuestManager().addQuest(result);
        return result;
    }

    private void refresh(Quest quest, String serialized) {
        YamlConfiguration yc = deserialize(serialized);
        try {
            quest.deserialize(yc);
        } catch (InvalidConfigurationException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not deserialize quest with id " + quest.getId() + ":\n" + serialized, e);
        }
    }

    public <T extends Quest> T createQuest(Class<T> type) {
        if (Modifier.isAbstract(type.getModifiers())) {
            throw new IllegalArgumentException("Cannot instantiate abstract QuestClasses!");
        }
        int id;
        try {
            id = CubeQuest.getInstance().getDatabaseFassade().reserveNewQuest();
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not reserve new QuestId!", e);
            return null;
        }
        T result;
        try {
            result = type.getConstructor(int.class).newInstance(id);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not create new Quest of type " + type.getName() + "!", e);
            try {
                CubeQuest.getInstance().getDatabaseFassade().deleteQuest(id);
            } catch (SQLException f) {
                CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not free reserved questId " + id + " after QuestCreation failed:", f);
            }
            return null;
        }
        QuestManager.getInstance().addQuest(result);
        updateQuest(result);
        return result;
    }

    public Quest loadQuest(int id) {
        if (CubeQuest.getInstance().getQuestManager().getQuest(id) != null) {
            throw new IllegalStateException("Quest already loaded!");
        }

        String serialized;
        try {
            serialized = CubeQuest.getInstance().getDatabaseFassade().getSerializedQuest(id);
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not load quest with id " + id, e);
            return null;
        }

        return create(id, serialized);
    }


    public void loadQuests() {
        Map<Integer, String> serializedQuests;
        try {
            serializedQuests = CubeQuest.getInstance().getDatabaseFassade().getSerializedQuests();
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not load quests!", e);
            return;
        }
        for (int id: serializedQuests.keySet()) {
            Quest quest = QuestManager.getInstance().getQuest(id);
            if (quest == null) {
                quest = create(id, serializedQuests.get(id));
            } else {
                refresh(quest, serializedQuests.get(id));
            }
        }
    }

    public void refreshQuest(Quest quest) {
        if (quest == null) {
            throw new NullPointerException();
        }

        int id = quest.getId();

        String serialized;
        try {
            serialized = CubeQuest.getInstance().getDatabaseFassade().getSerializedQuest(id);
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not load quest with id " + id, e);
            return;
        }
        if (serialized == null) {
            CubeQuest.getInstance().getQuestManager().removeQuest(id);
        }

        refresh(quest, serialized);

    }

    public void refreshQuest(int id) {
        Quest quest = CubeQuest.getInstance().getQuestManager().getQuest(id);
        if (quest == null) {
            throw new NullPointerException("Quest does not exist!");
        }
        refreshQuest(quest);
    }

    public void updateQuest(Quest quest) {
        if (quest == null) {
            throw new NullPointerException();
        }

        int id = quest.getId();
        String serialized = quest.serialize();

        try {
            CubeQuest.getInstance().getDatabaseFassade().updateQuest(id, serialized);
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not update quest with id " + id + ":\n" + serialized, e);
            return;
        }

        CubeQuest.getInstance().addWaitingForPlayer(() -> {
           Bukkit.getScheduler().scheduleSyncDelayedTask(CubeQuest.getInstance(), () -> {
               ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
               DataOutputStream msgout = new DataOutputStream(msgbytes);
               try {
                   msgout.writeInt(MsgType.QUEST_UPDATED.ordinal());
                   msgout.writeInt(id);
               } catch (IOException e) {
                   CubeQuest.getInstance().getLogger().log(Level.SEVERE, "IOException trying to send PluginMessage!", e);
                   return;
               }

               byte[] msgarry = msgbytes.toByteArray();

               for (String otherServer: CubeQuest.getInstance().getOtherBungeeServers()) {
                   ByteArrayDataOutput out = ByteStreams.newDataOutput();
                   out.writeUTF("Forward");
                   out.writeUTF(otherServer);
                   out.writeUTF("CubeQuest");
                   out.writeShort(msgarry.length);
                   out.write(msgarry);
                   Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
                   player.sendPluginMessage(CubeQuest.getInstance(), "BungeeCord", out.toByteArray());
               }
           }, 1L);
        });
    }

    public void updateQuest(int id) {
        Quest quest = CubeQuest.getInstance().getQuestManager().getQuest(id);
        if (quest == null) {
            throw new NullPointerException("Quest does not exist!");
        }
        updateQuest(quest);
    }

}
