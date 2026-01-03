package de.iani.cubequest.bubbles;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.QuestGiver;
import de.iani.cubequest.interaction.Interactor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;


public class QuestGiverBubbleTarget extends BubbleTarget {

    private static Color[] bubbleColors =
            new Color[] {Color.BLUE, Color.NAVY, Color.TEAL, Color.AQUA, Color.OLIVE, Color.GREEN};

    private QuestGiver giver;

    public QuestGiverBubbleTarget(QuestGiver giver) {
        this.giver = giver;
    }

    @Override
    public String getName() {
        return this.giver.getRawName();
    }

    @Override
    public Location getLocation(boolean ignoreCache) {
        return getInteractor().getLocation(ignoreCache);
    }

    @Override
    public double getHeight() {
        return BubbleTarget.getStrechingFactor(getInteractor(), true) * getInteractor().getHeight();
    }

    @Override
    public double getWidth() {
        return BubbleTarget.getStrechingFactor(getInteractor(), false) * getInteractor().getWidth();
    }

    @Override
    protected boolean conditionMet(Player player, PlayerData playerData) {
        return player.hasPermission(CubeQuest.ACCEPT_QUESTS_PERMISSION)
                && this.giver.hasQuestForPlayer(player, playerData);
    }

    @Override
    protected Color[] getBubbleColors() {
        return bubbleColors;
    }

    @Override
    public Interactor getInteractor() {
        return this.giver.getInteractor();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": " + getName();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof QuestGiverBubbleTarget)) {
            return false;
        }

        return this.giver.equals(((QuestGiverBubbleTarget) other).giver);
    }

    @Override
    public int hashCode() {
        return this.giver.hashCode();
    }

}
