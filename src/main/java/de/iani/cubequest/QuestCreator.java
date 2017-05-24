package de.iani.cubequest;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

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

public class QuestCreator {

    public enum QuestType {
        BLOCK_BREAK_QUEST, BLOCK_PLACE_QUEST, COMMAND_QUEST, COMPLEX_QUEST, DELIVERY_QUEST, FISHING_QUEST, GOTO_QUEST, KILL_ENTITIES_QUEST, TALK_QUEST;

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
        }

        public static QuestType getQuestType(Class<? extends Quest> c) {
            return types.get(c);
        }

        public Class<? extends Quest> getQuestClass() {
            return classes.get(this);
        }

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
        if (serialized == null) {
            throw new NullPointerException("Quest does not exist!");
        }
        YamlConfiguration yc = new YamlConfiguration();
        try {
            yc.loadFromString(serialized);
        } catch (InvalidConfigurationException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not deserialize quest with id " + id + ":\n" + serialized, e);
            return null;
        }
        QuestType type = QuestType.valueOf(yc.getString("type"));
        if (type == null) {
            throw new IllegalArgumentException("Invalid type!");
        }

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

        YamlConfiguration yc = new YamlConfiguration();
        try {
            yc.loadFromString(serialized);
        } catch (InvalidConfigurationException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not deserialize quest with id " + id + ":\n" + serialized, e);
            return;
        }
        QuestType type = QuestType.valueOf(yc.getString("type"));
        if (type == null) {
            throw new IllegalArgumentException("Invalid type!");
        }

        try {
            quest.deserialize(serialized);
        } catch (InvalidConfigurationException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not deserialize quest with id " + id + ":\n" + serialized, e);
        }

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
        }
    }

    public void updateQuest(int id) {
        Quest quest = CubeQuest.getInstance().getQuestManager().getQuest(id);
        if (quest == null) {
            throw new NullPointerException("Quest does not exist!");
        }
        updateQuest(quest);
    }

}
