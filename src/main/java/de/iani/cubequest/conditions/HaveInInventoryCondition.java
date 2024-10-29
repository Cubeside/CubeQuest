package de.iani.cubequest.conditions;

import de.iani.cubequest.PlayerData;
import de.iani.cubesideutils.bukkit.items.ItemStacks;
import de.iani.cubesideutils.bukkit.items.ItemsAndStrings;
import de.iani.cubesideutils.bukkit.updater.DataUpdater;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


public class HaveInInventoryCondition extends QuestCondition {

    private ItemStack[] items;

    @SuppressWarnings("unchecked")
    public HaveInInventoryCondition(Map<String, Object> serialized) {
        super(serialized);

        List<?> itemsList = (List<?>) serialized.get("items");
        if (itemsList.isEmpty()) {
            items = new ItemStack[0];
        } else if (itemsList.get(0) instanceof ItemStack) {
            items = ((List<ItemStack>) itemsList).toArray(new ItemStack[0]);
        } else {
            items = itemsList.stream().map(x -> (byte[]) x).map(ItemStack::deserializeBytes)
                    .toArray(ItemStack[]::new);
        }
    }

    public HaveInInventoryCondition(boolean visible, ItemStack[] items) {
        super(visible);

        this.items = ItemStacks.shrink(items);
    }

    @Override
    public boolean fulfills(Player player, PlayerData data) {
        return ItemStacks.doesHave(player, this.items, false, true).length == 0;
    }

    @Override
    public BaseComponent[] getConditionInfo() {
        return new ComponentBuilder(
                ChatColor.DARK_AQUA + "Hat im Inventar: " + ItemsAndStrings.toNiceString(this.items)).create();
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("items", Arrays.stream(this.items).map(ItemStack::serializeAsBytes).toList());
        return result;
    }

    @Override
    public QuestCondition performDataUpdate() {
        ItemStack[] updated = Arrays.stream(this.items).map(i -> i == null ? null : DataUpdater.updateItemStack(i))
                .toArray(ItemStack[]::new);
        if (updated.equals(this.items)) {
            return this;
        }
        return new HaveInInventoryCondition(isVisible(), updated);
    }

}
