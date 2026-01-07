package de.iani.cubequest.quests;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.commands.SetDeliveryInventoryCommand;
import de.iani.cubequest.interaction.Interactor;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.bukkit.items.ItemStacks;
import de.iani.cubesideutils.bukkit.updater.DataUpdater;
import io.papermc.paper.datacomponent.DataComponentType;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@DelegateDeserialization(Quest.class)
public class DeliveryQuest extends InteractorQuest {

    private static final DataComponentType[] IGNORED_COMPONENT_TYPES;
    static {
        DataComponentType bucketEntityDataType =
                RegistryAccess.registryAccess().getRegistry(RegistryKey.DATA_COMPONENT_TYPE)
                        .getOrThrow(NamespacedKey.fromString("bucket_entity_data"));
        IGNORED_COMPONENT_TYPES = new DataComponentType[] {bucketEntityDataType, DataComponentTypes.SALMON_SIZE};
    }

    private ItemStack[] delivery;

    public DeliveryQuest(int id, Component name, Component displayMessage, Interactor recipient, ItemStack[] delivery) {
        super(id, name, displayMessage, recipient);

        setDelivery(delivery, false);
    }

    public DeliveryQuest(int id) {
        this(id, null, null, null, null);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);

        ItemStack[] items;
        List<?> itemsList = (List<?>) yc.get("delivery");
        if (itemsList.isEmpty()) {
            items = new ItemStack[0];
        } else if (itemsList.get(0) instanceof ItemStack) {
            items = ((List<ItemStack>) itemsList).toArray(new ItemStack[0]);
        } else {
            items = itemsList.stream().map(x -> (byte[]) x).map(ItemStack::deserializeBytes).toArray(ItemStack[]::new);
        }

        setDelivery(items, false);
    }

    @Override
    protected String serializeToString(YamlConfiguration yc) {
        yc.set("delivery", Arrays.stream(this.delivery).map(ItemStack::serializeAsBytes).toList());

        return super.serializeToString(yc);
    }

    @Override
    public boolean isLegal() {
        return super.isLegal() && this.delivery != null;
    }


    @Override
    public List<Component> getQuestInfo() {
        List<Component> result = super.getQuestInfo();

        Component deliveryLine = Component.text("Lieferung: ");

        if (ItemStacks.isEmpty(this.delivery)) {
            deliveryLine = Component.textOfChildren(deliveryLine, Component.text("KEINE", NamedTextColor.RED));
        } else {
            deliveryLine = Component.textOfChildren(deliveryLine,
                    ItemStacks.toComponent(this.delivery, Style.style(NamedTextColor.GREEN)));
        }

        result.add(suggest(deliveryLine.color(NamedTextColor.DARK_AQUA), SetDeliveryInventoryCommand.FULL_COMMAND));
        result.add(Component.empty());

        return result;
    }

    @Override
    public List<Component> getSpecificStateInfoInternal(PlayerData data, int indentionLevel) {
        List<Component> result = new ArrayList<>();

        QuestState state = data.getPlayerState(getId());
        Status status = (state == null) ? Status.NOTGIVENTO : state.getStatus();

        Component baseIndent = ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel);
        Component prefix = baseIndent;

        if (!Component.empty().equals(getDisplayName())) {
            result.add(baseIndent.append(ChatAndTextUtil.getStateStringStartingToken(state)).append(Component.text(" "))
                    .append(getDisplayName().colorIfAbsent(NamedTextColor.GOLD)).color(NamedTextColor.DARK_AQUA));
            prefix = prefix.append(Quest.INDENTION);
        } else {
            prefix = prefix.append(ChatAndTextUtil.getStateStringStartingToken(state)).append(Component.text(" "));
        }

        Component deliveryComp = ItemStacks.isEmpty(this.delivery) ? Component.text("KEINE", NamedTextColor.RED)
                : ItemStacks.toComponent(this.delivery, Style.style(NamedTextColor.DARK_AQUA));

        Component line = prefix.append(deliveryComp).append(Component.text(" an ")).append(getInteractorName())
                .append(Component.text(" geliefert: "))
                .append(Component.text(status == Status.SUCCESS ? "ja" : "nein").color(status.color))
                .color(NamedTextColor.DARK_AQUA);

        result.add(line);
        return result;
    }

    public ItemStack[] getDelivery() {
        return Arrays.copyOf(this.delivery, this.delivery.length);
    }

    public void setDelivery(ItemStack[] arg) {
        setDelivery(arg, true);
    }

    private void setDelivery(ItemStack[] arg, boolean updateInDB) {
        arg = arg == null ? new ItemStack[0] : arg;
        this.delivery = ItemStacks.shrink(arg);
        if (updateInDB) {
            updateIfReal();
        }
    }

    @Override
    public boolean playerConfirmedInteraction(Player player, QuestState state) {
        if (!super.playerConfirmedInteraction(player, state)) {
            return false;
        }

        ItemStack[] oldContent = player.getInventory().getContents();
        ItemStack[] missing = ItemStacks.doesHave(player, getDelivery(), true, true, IGNORED_COMPONENT_TYPES);

        if (missing.length > 0) {
            ChatAndTextUtil.sendWarningMessage(state.getPlayerData().getPlayer(),
                    "Du hast nicht genügend Items im Inventar, um diese Quest abzuschließen!");

            Component missingMsg = Component
                    .text("Dir fehl" + (missing.length == 1 && missing[0].getAmount() == 1 ? "t" : "en") + ": ",
                            NamedTextColor.GOLD)
                    .append(ItemStacks.toComponent(missing, Style.style(NamedTextColor.GOLD)));

            ChatAndTextUtil.sendWarningMessage(state.getPlayerData().getPlayer(), missingMsg);
            return false;
        }

        if (!onSuccess(state.getPlayerData().getPlayer())) {
            state.getPlayerData().getPlayer().getInventory().setContents(oldContent);
            state.getPlayerData().getPlayer().updateInventory();
            return false;
        }

        CubeQuest.getInstance().getLogger().log(Level.INFO,
                "Player " + player.getName() + " deliverd " + Arrays.toString(this.delivery) + ".");
        return true;
    }

    @Override
    public boolean performDataUpdate() {
        boolean changed = super.performDataUpdate();
        ItemStack[] updated = Arrays.stream(this.delivery).map(i -> i == null ? null : DataUpdater.updateItemStack(i))
                .toArray(ItemStack[]::new);
        if (!updated.equals(this.delivery)) {
            this.delivery = updated;
            changed = true;
        }
        return changed;
    }
}
