package de.iani.cubequest.quests;

import java.util.Arrays;
import java.util.List;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import de.iani.cubequest.Reward;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.ItemStackUtil;
import de.iani.cubequest.wrapper.NPCClickEventWrapper;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class DeliveryQuest extends NPCQuest {

    private ItemStack[] delivery;

    public DeliveryQuest(int id, String name, String giveMessage, String successMessage, Reward successReward, Integer recipient,
            ItemStack[] delivery) {
        super(id, name, giveMessage, successMessage, successReward, recipient);

        setDelivery(delivery, false);
    }

    public DeliveryQuest(int id) {
        this(id, null, null, null, null, null, null);
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
    public boolean onNPCClickEvent(NPCClickEventWrapper event, QuestState state) {
        if (!super.onNPCClickEvent(event, state)) {
            return false;
        }
        ItemStack[] toDeliver = Arrays.copyOf(delivery, delivery.length);
        ItemStack[] his = Arrays.copyOf(event.getOriginal().getClicker().getInventory().getContents(), 36);
        ItemStack[] oldHis = Arrays.copyOf(event.getOriginal().getClicker().getInventory().getContents(), 36);
        boolean has = true;
        outer:
        for (ItemStack toStack: toDeliver) {
            for (ItemStack hisStack: his) {
                if (hisStack == null || hisStack.getAmount() <= 0) {
                    continue;
                }
                if (hisStack.getType() != toStack.getType()) {
                    continue;
                }
                if (!hisStack.getItemMeta().equals(toStack.getItemMeta())) {
                    continue;
                }
                if (toStack.getAmount() > hisStack.getAmount()) {
                    toStack.setAmount(toStack.getAmount() - hisStack.getAmount());
                    hisStack = null;
                    continue;
                } else if (toStack.getAmount() < hisStack.getAmount()) {
                    hisStack.setAmount(hisStack.getAmount() - toStack.getAmount());
                    toStack.setAmount(0);
                    continue outer;
                } else {
                    hisStack = null;
                    toStack.setAmount(0);
                    continue outer;
                }
            }
            has = false;
            break;
        }

        if (!has) {
            ChatAndTextUtil.sendWarningMessage(event.getOriginal().getClicker(), "Du hast nicht genügend Items im Inventar, um diese Quest abzuschließen!");
            return false;
        }

        event.getOriginal().getClicker().getInventory().setContents(his);
        event.getOriginal().getClicker().updateInventory();

        if (!onSuccess(event.getOriginal().getClicker())) {
            event.getOriginal().getClicker().getInventory().setContents(oldHis);
            event.getOriginal().getClicker().updateInventory();
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
