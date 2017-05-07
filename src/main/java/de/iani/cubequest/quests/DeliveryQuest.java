package de.iani.cubequest.quests;

import java.util.Arrays;

import org.bukkit.inventory.ItemStack;

import de.iani.cubequest.CubeQuest;
import net.citizensnpcs.api.event.NPCClickEvent;
import net.citizensnpcs.api.npc.NPC;

public class DeliveryQuest extends Quest {

    private Integer recipientID;
    private ItemStack[] delivery;

    public DeliveryQuest(String name, String giveMessage, String successMessage, Reward successReward,
            NPC recipient, ItemStack[] delivery) {
        super(name, giveMessage, successMessage, successReward);

        this.recipientID = recipient == null? null : recipient.getId();
        this.delivery = delivery;
    }

    public DeliveryQuest(String name) {
        this(name, null, null, null, null, null);
    }

    @Override
    public void onNPCClickEvent(NPCClickEvent event) {
        if (event.getNPC().getId() != recipientID) {
            return;
        }
        if (getPlayerStatus(event.getClicker().getUniqueId()) != Status.GIVENTO) {
            return;
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
    }

    @Override
    public boolean isLegal() {
        return recipientID != null && delivery != null;
    }

    public NPC getNPC() {
        if (recipientID == null) {
            return null;
        }
        return CubeQuest.getInstance().getNPCReg().getById(recipientID);
    }

    //TODO NPCs setzen: fertigen übergeben oder Daten übergeben und dann erstellen?

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
