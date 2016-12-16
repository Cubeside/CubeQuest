package de.iani.cubequest.quests;

import org.bukkit.inventory.ItemStack;

import com.google.common.base.Verify;

import net.citizensnpcs.api.event.NPCClickEvent;
import net.citizensnpcs.api.npc.NPC;

public class DeliveryQuest extends Quest {

    private int recipientID;
    private ItemStack[] delivery;

    public DeliveryQuest(String name, String giveMessage, String successMessage, Reward successReward,
            NPC recipient, ItemStack[] delivery) {
        super(name, giveMessage, successMessage, successReward);
        Verify.verifyNotNull(recipient);

        this.recipientID = recipient.getId();
        this.delivery = delivery;
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

}
