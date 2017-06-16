package de.iani.cubequest.questStates;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import de.iani.cubequest.PlayerData;
import de.iani.cubequest.QuestStateCreator.QuestStateType;
import de.iani.cubequest.quests.Quest;

public class QuestState {

    private Status status;
    private PlayerData data;
    private int questId;

    public enum Status {
        NOTGIVENTO, GIVENTO, SUCCESS, FAIL;

        private static Status[] values = values();

        public static Status fromOrdinal(int ordinal) {
            return values[ordinal];
        }
    }

    public QuestState(PlayerData data, int questId) {
        this.data = data;
        this.questId = questId;
    }

    protected void updated() {
        data.stateChanged(questId);
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        if (status == null) {
            throw new NullPointerException();
        }
        this.status = status;
        updated();
    }

    /**
     * Erzeugt eine neue YamlConfiguration aus dem String und ruft dann {@link Quest#deserialize(YamlConfigration)} auf.
     * @param serialized serialisierter Zustand
     * @throws InvalidConfigurationException wird weitergegeben
     */
    public void deserialize(String serialized) throws InvalidConfigurationException {
        if (this.getClass() == QuestState.class && serialized.equals("")) {
            return;
        }
        YamlConfiguration yc = new YamlConfiguration();
        yc.loadFromString(serialized);
        deserialize(yc);
    }

    /**
     * Wendet den Inhalt der YamlConfiguration auf die Quest an.
     * @param yc serialisierte Zustands-Daten
     * @throws InvalidConfigurationException  wird weitergegeben
     */
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        if (!yc.getString("type").equals(QuestStateType.getQuestStateType(this.getClass()).toString())) {
            throw new IllegalArgumentException("Serialized type doesn't match!");
        }
    }

    /**
     * Serialisiert den QuestState
     * @return serialisierter Zustand
     */
    public String serialize() {
        return (this.getClass() == QuestState.class)? "" : serialize(new YamlConfiguration());
    }

    /**
     * Unterklassen sollten ihre Daten in die YamlConfiguration eintragen und dann die Methode der Oberklasse aufrufen.
     * @param yc YamlConfiguration mit den Daten des QuestStates
     * @return serialisierter Zustand
     */
    protected String serialize(YamlConfiguration yc) {
        yc.set("type", QuestStateType.getQuestStateType(this.getClass()).toString());

        return yc.toString();
    }

}
