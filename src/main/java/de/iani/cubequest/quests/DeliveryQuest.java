package de.iani.cubequest.quests;

import java.util.Arrays;
import java.util.List;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.inventory.ItemStack;

import de.iani.cubequest.Reward;
import de.iani.cubequest.interaction.Interactor;
import de.iani.cubequest.interaction.PlayerInteractInteractorEvent;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.ItemStackUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

@DelegateDeserialization(Quest.class)
public class DeliveryQuest extends InteractorQuest {

    private ItemStack[] delivery;

    public DeliveryQuest(int id, String name, String displayMessage, String giveMessage, String successMessage, Reward successReward, Interactor recipient,
            ItemStack[] delivery) {
        super(id, name, displayMessage, giveMessage, successMessage, successReward, recipient);

        setDelivery(delivery, false);
    }

    public DeliveryQuest(int id) {
        this(id, null, null, null, null, null, null, null);
    }

    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);

        setDelivery(yc.getList("delivery").toArray(new ItemStack[0]), false);
    }

    @Override
    protected String serializeToString(YamlConfiguration yc) {

        yc.set("delivery", Arrays.asList(delivery));

        return super.serializeToString(yc);
    }

    @Override
    public boolean onPlayerInteractInteractorEvent(PlayerInteractInteractorEvent event, QuestState state) {
        if (!super.onPlayerInteractInteractorEvent(event, state)) {
            return false;
        }
        ItemStack[] toDeliver = new ItemStack[delivery.length];
        for (int i=0; i<delivery.length; i++) {
            toDeliver[i] = delivery[i].clone();
        }
        ItemStack[] his = state.getPlayerData().getPlayer().getInventory().getStorageContents();
        ItemStack[] oldHis = state.getPlayerData().getPlayer().getInventory().getStorageContents();
        boolean has = true;
        outer:
        for (ItemStack toStack: toDeliver) {
            for (int i=0; i<his.length; i++) {
                ItemStack hisStack = his[i];
                if (hisStack == null || hisStack.getAmount() <= 0) {
                    continue;
                }
                if (!hisStack.isSimilar(toStack)) {
                    continue;
                }
                if (toStack.getAmount() > hisStack.getAmount()) {
                    toStack.setAmount(toStack.getAmount() - hisStack.getAmount());
                    his[i] = null;
                    continue;
                } else if (toStack.getAmount() < hisStack.getAmount()) {
                    hisStack.setAmount(hisStack.getAmount() - toStack.getAmount());
                    toStack.setAmount(0);
                    continue outer;
                } else {
                    his[i] = null;
                    toStack.setAmount(0);
                    continue outer;
                }
            }
            has = false;
            break;
        }

        if (!has) {
            ChatAndTextUtil.sendWarningMessage(state.getPlayerData().getPlayer(), "Du hast nicht genügend Items im Inventar, um diese Quest abzuschließen!");
            return false;
        }

        state.getPlayerData().getPlayer().getInventory().setContents(his);
        state.getPlayerData().getPlayer().updateInventory();

        if (!onSuccess(state.getPlayerData().getPlayer())) {
            state.getPlayerData().getPlayer().getInventory().setContents(oldHis);
            state.getPlayerData().getPlayer().updateInventory();
            return false;
        }
        return true;
    }

    @Override
    public boolean isLegal() {
        return super.isLegal() && delivery != null;
    }

    @Override
    public List<BaseComponent[]> getQuestInfo() {
        List<BaseComponent[]> result = super.getQuestInfo();

        String deliveryString = ChatColor.DARK_AQUA + "Lieferung: ";
        if (ItemStackUtil.isEmpty(delivery)) {
            deliveryString += ChatColor.RED + "KEINE";
        } else {
            deliveryString += ChatColor.GREEN;
            for (ItemStack item: delivery) {
                deliveryString += ItemStackUtil.toNiceString(item) + ", ";
            }
            deliveryString = deliveryString.substring(0, deliveryString.length() - ", ".length());
        }

        result.add(new ComponentBuilder(deliveryString).create());
        result.add(new ComponentBuilder("").create());

        return result;
    }

    public ItemStack[] getDelivery() {
        return Arrays.copyOf(delivery, delivery.length);
    }

    public void setDelivery(ItemStack[] arg) {
        setDelivery(arg, true);
    }

    private void setDelivery(ItemStack[] arg, boolean updateInDB) {
        arg = arg == null? new ItemStack[0] : arg;
        this.delivery = ItemStackUtil.shrinkItemStack(arg);
        if (updateInDB) {
            updateIfReal();
        }
    }

}
