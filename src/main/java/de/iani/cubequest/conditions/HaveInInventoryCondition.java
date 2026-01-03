package de.iani.cubequest.conditions;

import de.iani.cubequest.PlayerData;
import de.iani.cubesideutils.bukkit.items.ItemStacks;
import de.iani.cubesideutils.bukkit.updater.DataUpdater;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


public class HaveInInventoryCondition extends QuestCondition {

    private ItemStack[] items;

    @SuppressWarnings("unchecked")
    public HaveInInventoryCondition(Map<String, Object> serialized) {
        super(serialized);

        List<?> itemsList = (List<?>) serialized.get("items");
        if (itemsList.isEmpty()) {
            this.items = new ItemStack[0];
        } else if (itemsList.get(0) instanceof ItemStack) {
            this.items = ((List<ItemStack>) itemsList).toArray(new ItemStack[0]);
        } else {
            this.items =
                    itemsList.stream().map(x -> (byte[]) x).map(ItemStack::deserializeBytes).toArray(ItemStack[]::new);
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
    public Component getConditionInfo() {
        return Component.text("Hat im Inventar: ").append(ItemStacks.toComponent(this.items))
                .color(NamedTextColor.DARK_AQUA);
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
