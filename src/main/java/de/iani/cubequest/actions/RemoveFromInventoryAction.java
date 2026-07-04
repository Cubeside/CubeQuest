package de.iani.cubequest.actions;

import de.iani.cubequest.PlayerData;
import de.iani.cubequest.quests.DeliveryQuest;
import de.iani.cubesideutils.bukkit.items.ItemStacks;
import de.iani.cubesideutils.bukkit.items.ItemStacks.RemovalPolicy;
import de.iani.cubesideutils.bukkit.updater.DataUpdater;
import io.papermc.paper.datacomponent.DataComponentType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


public class RemoveFromInventoryAction extends DelayableAction {

    private static final DataComponentType[] IGNORED_COMPONENT_TYPES = DeliveryQuest.getIgnoredComponentTypes();

    private ItemStack[] items;

    public RemoveFromInventoryAction(long delay, ItemStack[] items) {
        super(delay);

        this.items = items == null ? new ItemStack[0] : ItemStacks.shrink(items);
    }

    @SuppressWarnings("unchecked")
    public RemoveFromInventoryAction(Map<String, Object> serialized) {
        super(serialized);

        ItemStack[] itemsArray;
        Object itemsObj = serialized.get("items");
        if (itemsObj == null) {
            itemsArray = new ItemStack[0];
        } else if (itemsObj instanceof ItemStack[]) {
            itemsArray = (ItemStack[]) itemsObj;
        } else if (itemsObj instanceof List) {
            List<?> itemsList = (List<?>) itemsObj;
            if (itemsList.isEmpty()) {
                itemsArray = new ItemStack[0];
            } else if (itemsList.get(0) instanceof ItemStack) {
                itemsArray = ((List<ItemStack>) itemsList).toArray(new ItemStack[0]);
            } else {
                itemsArray = itemsList.stream().map(x -> (byte[]) x).map(ItemStack::deserializeBytes)
                        .toArray(ItemStack[]::new);
            }
        } else {
            itemsArray = new ItemStack[0];
        }

        this.items = ItemStacks.shrink(itemsArray);
    }

    @Override
    protected BiConsumer<Player, PlayerData> getActionPerformer() {
        return (player, data) -> {
            ItemStacks.doesHave(player, this.items, RemovalPolicy.ALWAYS, true, IGNORED_COMPONENT_TYPES);
        };
    }

    @Override
    public Component getActionInfo() {
        Component msg = Component.empty();

        Component delayComp = getDelayComponent();
        if (delayComp != null) {
            msg = msg.append(delayComp);
        }

        Component itemsComp;
        if (ItemStacks.isEmpty(this.items)) {
            itemsComp = Component.text("KEINE", NamedTextColor.RED);
        } else {
            itemsComp = ItemStacks.toComponent(this.items, Style.style(NamedTextColor.GREEN));
        }

        msg = Component.textOfChildren(msg, Component.text("Items aus Inventar entfernen: "), itemsComp)
                .color(NamedTextColor.DARK_AQUA);

        return msg;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("items", Arrays.stream(this.items).map(ItemStack::serializeAsBytes).toList());
        return result;
    }

    @Override
    public QuestAction performDataUpdate() {
        ItemStack[] updated = Arrays.stream(this.items).map(i -> i == null ? null : DataUpdater.updateItemStack(i))
                .toArray(ItemStack[]::new);
        if (!Arrays.equals(updated, this.items)) {
            this.items = updated;
        }
        return super.performDataUpdate();
    }

    public ItemStack[] getItems() {
        return Arrays.copyOf(this.items, this.items.length);
    }

    public void setItems(ItemStack[] items) {
        this.items = items == null ? new ItemStack[0] : ItemStacks.shrink(items);
    }

}
