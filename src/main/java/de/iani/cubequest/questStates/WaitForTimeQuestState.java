package de.iani.cubequest.questStates;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import de.iani.cubequest.PlayerData;

public class WaitForTimeQuestState extends QuestState {

    private long goal;

    public WaitForTimeQuestState(PlayerData data, int questId, long ms) {
        super(data, questId);
        this.goal = System.currentTimeMillis() + ms;
    }

    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);

        goal = yc.getInt("goal");
    }

    @Override
    protected String serialize(YamlConfiguration yc) {
        yc.set("goal", goal);

        return super.serialize(yc);
    }

    public boolean isDue() {
        return goal <= System.currentTimeMillis();
    }

}
