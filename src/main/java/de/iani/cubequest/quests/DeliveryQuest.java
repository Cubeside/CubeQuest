package de.iani.cubequest.quests;

import java.util.Arrays;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import de.iani.cubequest.CubeQuest;
import net.citizensnpcs.api.event.NPCClickEvent;
import net.citizensnpcs.api.npc.NPC;

public class DeliveryQuest extends NPCQuest {

    private ItemStack[] delivery;

    public DeliveryQuest(int id, String name, String giveMessage, String successMessage, Reward successReward, NPC recipient,
            ItemStack[] delivery) {
        super(id, name, giveMessage, successMessage, successReward, recipient);

        this.delivery = delivery;
    }

    public DeliveryQuest(int id) {
        this(id, null, null, null, null, null, null);
    }

    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);

        delivery = yc.getList("delivery").toArray(new ItemStack[0]);
    }

    @Override
    protected String serialize(YamlConfiguration yc) {

        yc.set("delivery", Arrays.asList(delivery));

        return super.serialize(yc);
    }

    @Override
    public boolean onNPCClickEvent(NPCClickEvent event) {
        if (!super.onNPCClickEvent(event)) {
            return false;
        }

        /* TODO:
         * if (zeugNichtImInventar()) {
         *      meldung();
         *      return;
         * }
         * else if (!hasSpaceforReward(event.getClicker())) {
         *      meldung();
         *      return;
         * }
         * nimmZeugAusInventar();
         *
         */
        onSuccess(event.getClicker());
        return true;
    }

    @Override
    public boolean isLegal() {
        return super.isLegal() && delivery != null;
    }

    public ItemStack[] getDelivery() {
        return Arrays.copyOf(delivery, delivery.length);
    }

    public void setDelivery(ItemStack[] arg) {
        if (arg == null) {
            throw new NullPointerException("arg may not be null");
        }
        this.delivery = Arrays.copyOf(arg, arg.length);
        CubeQuest.getInstance().getQuestCreator().updateQuest(this);
    }

}
