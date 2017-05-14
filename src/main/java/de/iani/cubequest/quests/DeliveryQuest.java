package de.iani.cubequest.quests;

import java.util.Arrays;

import org.bukkit.inventory.ItemStack;

import net.citizensnpcs.api.event.NPCClickEvent;
import net.citizensnpcs.api.npc.NPC;

public class DeliveryQuest extends NPCQuest {

    private ItemStack[] delivery;

    public DeliveryQuest(String name, String giveMessage, String successMessage, Reward successReward, NPC recipient,
            ItemStack[] delivery) {
        super(name, giveMessage, successMessage, successReward, recipient);

        this.delivery = delivery;
    }

    public DeliveryQuest(String name) {
        this(name, null, null, null, null, null);
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

    public void setItemStack(ItemStack[] arg) {
        if (arg == null) {
            throw new NullPointerException("arg may not be null");
        }
        this.delivery = Arrays.copyOf(arg, arg.length);
    }

}
