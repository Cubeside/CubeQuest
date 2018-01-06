package de.iani.cubequest.questStates;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.quests.Quest;

public class QuestState {
    
    private Status status;
    private PlayerData data;
    private Quest quest;
    
    public enum Status {
        NOTGIVENTO, GIVENTO, SUCCESS, FAIL;
        
        private static Status[] values = values();
        
        public static Status fromOrdinal(int ordinal) {
            return values[ordinal];
        }
    }
    
    public QuestState(PlayerData data, int questId, Status status) {
        this.status = status == null ? Status.NOTGIVENTO : status;
        this.data = data;
        this.quest = QuestManager.getInstance().getQuest(questId);
        if (quest == null) {
            throw new IllegalArgumentException("No quest for this questId");
        }
    }
    
    public QuestState(PlayerData data, int questId) {
        this(data, questId, null);
    }
    
    protected void updated() {
        data.stateChanged(this);
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status, boolean updatePlayerData) {
        if (status == null) {
            throw new NullPointerException();
        }
        this.status = status;
        if (updatePlayerData) {
            updated();
        }
    }
    
    public void setStatus(Status status) {
        setStatus(status, true);
    }
    
    public PlayerData getPlayerData() {
        return data;
    }
    
    public Quest getQuest() {
        return quest;
    }
    
    /**
     * Erzeugt eine neue YamlConfiguration aus dem String und ruft dann
     * {@link Quest#deserialize(YamlConfigration)} auf.
     * 
     * @param serialized serialisierter Zustand
     * @throws InvalidConfigurationException wird weitergegeben
     */
    public void deserialize(String serialized, Status status) throws InvalidConfigurationException {
        if (this.getClass() == QuestState.class && serialized.equals("")) {
            this.status = status == null ? Status.NOTGIVENTO : status;
            return;
        }
        YamlConfiguration yc = new YamlConfiguration();
        yc.loadFromString(serialized);
        deserialize(yc, status);
    }
    
    /**
     * Wendet den Inhalt der YamlConfiguration auf die Quest an.
     * 
     * @param yc serialisierte Zustands-Daten
     * @throws InvalidConfigurationException wird weitergegeben
     */
    public void deserialize(YamlConfiguration yc, Status status)
            throws InvalidConfigurationException {
        if (!yc.getString("type")
                .equals(QuestStateType.getQuestStateType(this.getClass()).toString())) {
            throw new IllegalArgumentException("Serialized type doesn't match!");
        }
        this.status = status == null ? Status.NOTGIVENTO : status;
    }
    
    /**
     * Serialisiert den QuestState
     * 
     * @return serialisierter Zustand
     */
    public String serialize() {
        return (this.getClass() == QuestState.class) ? "" : serialize(new YamlConfiguration());
    }
    
    /**
     * Unterklassen sollten ihre Daten in die YamlConfiguration eintragen und dann die Methode der
     * Oberklasse aufrufen.
     * 
     * @param yc YamlConfiguration mit den Daten des QuestStates
     * @return serialisierter Zustand
     */
    protected String serialize(YamlConfiguration yc) {
        yc.set("type", QuestStateType.getQuestStateType(this.getClass()).toString());
        
        return yc.saveToString();
    }
    
}
