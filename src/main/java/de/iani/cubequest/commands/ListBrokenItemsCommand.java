package de.iani.cubequest.commands;

import de.cubeside.nmsutils.NbtUtils;
import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.actions.QuestAction;
import de.iani.cubequest.actions.RewardAction;
import de.iani.cubequest.quests.DeliveryQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class ListBrokenItemsCommand extends SubCommand {

    public static final String COMMAND_PATH = "listBrokenItems";

    public static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder().character(LegacyComponentSerializer.AMPERSAND_CHAR).hexCharacter(LegacyComponentSerializer.HEX_CHAR).hexColors().build();

    public ListBrokenItemsCommand() {
    }

    private record SimplifiedItemData(Material m, String name, List<String> lore, String skinURL) {
        public static SimplifiedItemData from(ItemStack stack) {
            if (stack == null) {
                return null;
            }
            Material m = stack.getType();
            ItemMeta meta = stack.getItemMeta();
            String displayName = meta.hasDisplayName() ? SERIALIZER.serialize(meta.displayName()) : null;
            ArrayList<String> lore = null;
            if (meta.hasLore()) {
                lore = new ArrayList<>();
                for (Component loreLine : meta.lore()) {
                    lore.add(SERIALIZER.serialize(loreLine));
                }
            }
            String skinUrl = null;
            if (meta instanceof SkullMeta skull && skull.hasOwner()) {
                if (skull.getPlayerProfile() != null) {
                    skinUrl = Objects.toString(skull.getPlayerProfile().getTextures().getSkin());
                }
            }
            return new SimplifiedItemData(m, displayName, lore, skinUrl);
        }
    }

    private record ItemInQuestData(int quest, String questDescription, boolean require, ItemStack item) {
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString, ArgsParser args) {
        boolean fixup = false;
        if (args.remaining() > 0) {
            if (args.remaining() > 1) {
                return false;
            }
            fixup = args.getNext().equals("FIXUP");
        }
        HashMap<SimplifiedItemData, List<ItemInQuestData>> itemsInQuests = new HashMap<>();
        for (Quest quest : CubeQuest.getInstance().getQuestManager().getQuests()) {
            if (quest instanceof DeliveryQuest deliveryQuest) {
                for (ItemStack s : deliveryQuest.getDelivery()) {
                    if (canBeChecked(s)) {
                        itemsInQuests.computeIfAbsent(SimplifiedItemData.from(s), si -> new ArrayList<>()).add(new ItemInQuestData(quest.getId(), "delivery", true, s));
                    }
                }
            }
            for (QuestAction a : quest.getGiveActions()) {
                if (a instanceof RewardAction ra) {
                    for (ItemStack s : ra.getReward().getItems()) {
                        if (canBeChecked(s)) {
                            itemsInQuests.computeIfAbsent(SimplifiedItemData.from(s), si -> new ArrayList<>()).add(new ItemInQuestData(quest.getId(), "giveaction", false, s));
                        }
                    }
                }
            }
            for (QuestAction a : quest.getFailActions()) {
                if (a instanceof RewardAction ra) {
                    for (ItemStack s : ra.getReward().getItems()) {
                        if (canBeChecked(s)) {
                            itemsInQuests.computeIfAbsent(SimplifiedItemData.from(s), si -> new ArrayList<>()).add(new ItemInQuestData(quest.getId(), "failaction", false, s));
                        }
                    }
                }
            }
            for (QuestAction a : quest.getSuccessActions()) {
                if (a instanceof RewardAction ra) {
                    for (ItemStack s : ra.getReward().getItems()) {
                        if (canBeChecked(s)) {
                            itemsInQuests.computeIfAbsent(SimplifiedItemData.from(s), si -> new ArrayList<>()).add(new ItemInQuestData(quest.getId(), "successaction", false, s));
                        }
                    }
                }
            }
        }
        ArrayList<List<ItemInQuestData>> failedItems = new ArrayList<>();
        for (Entry<SimplifiedItemData, List<ItemInQuestData>> e : itemsInQuests.entrySet()) {
            List<ItemInQuestData> items = e.getValue();
            int count = items.size();
            if (count > 1) {
                boolean notequal = false;
                outer: for (int i = 0; i < count - 1; i++) {
                    for (int j = i + 1; j < count; j++) {
                        ItemStack itemA = items.get(i).item;
                        ItemStack itemB = items.get(j).item;
                        if (!itemA.isSimilar(itemB)) {
                            notequal = true;
                            break outer;
                        }
                    }
                }
                if (notequal) {
                    boolean hasRequire = false;
                    boolean hasGive = false;
                    for (ItemInQuestData i : items) {
                        if (i.require) {
                            hasRequire = true;
                        } else {
                            hasGive = true;
                        }
                    }
                    if (hasRequire && hasGive) {
                        failedItems.add(items);
                    }
                }
            }
        }
        NbtUtils nbtUtils = CubeQuest.getInstance().getNmsUtils().getNbtUtils();
        for (List<ItemInQuestData> failedItem : failedItems) {
            TextComponent itemInfo = new TextComponent("Broken item " + failedItem.get(0).item.getType());
            itemInfo.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(failedItem.get(0).item.toString())));
            StringBuilder sb = new StringBuilder();
            for (ItemInQuestData i : failedItem) {
                sb.append(i.questDescription).append("\n");
                sb.append(nbtUtils.writeString(nbtUtils.getItemStackNbt(i.item))).append("\n\n");
            }
            itemInfo.setClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, sb.toString()));
            ChatAndTextUtil.sendNormalMessage(sender, itemInfo);
            for (ItemInQuestData i : failedItem) {
                ChatAndTextUtil.sendNormalMessage(sender, "  " + i.quest + " - " + i.questDescription);
            }
            if (fixup) {
                ItemStack fixedStack = null;
                for (ItemInQuestData i : failedItem) {
                    if (fixedStack == null && i.require) { // delivery quests have the newer items
                        fixedStack = i.item;
                    }
                }
                if (fixedStack != null) {
                    SimplifiedItemData simplifiedFixedStack = SimplifiedItemData.from(fixedStack);
                    for (ItemInQuestData i : failedItem) {
                        Quest quest = CubeQuest.getInstance().getQuestManager().getQuest(i.quest);
                        boolean save = false;
                        if (quest instanceof DeliveryQuest deliveryQuest) {
                            ItemStack[] stacks = deliveryQuest.getDelivery();
                            if (fixupStack(stacks, fixedStack, simplifiedFixedStack)) {
                                deliveryQuest.setDelivery(stacks);
                                save = true;
                            }
                        }
                        for (QuestAction a : quest.getGiveActions()) {
                            if (a instanceof RewardAction ra) {
                                ItemStack[] stacks = ra.getReward().getItems();
                                if (fixupStack(stacks, fixedStack, simplifiedFixedStack)) {
                                    // no setting, stacks is the live array
                                    save = true;
                                }
                            }
                        }
                        for (QuestAction a : quest.getFailActions()) {
                            if (a instanceof RewardAction ra) {
                                ItemStack[] stacks = ra.getReward().getItems();
                                if (fixupStack(stacks, fixedStack, simplifiedFixedStack)) {
                                    // no setting, stacks is the live array
                                    save = true;
                                }
                            }
                        }
                        for (QuestAction a : quest.getSuccessActions()) {
                            if (a instanceof RewardAction ra) {
                                ItemStack[] stacks = ra.getReward().getItems();
                                if (fixupStack(stacks, fixedStack, simplifiedFixedStack)) {
                                    // no setting, stacks is the live array
                                    save = true;
                                }
                            }
                        }
                        if (save) {
                            quest.updateIfReal();
                            ChatAndTextUtil.sendNormalMessage(sender, "  FIXED " + i.quest + " - " + i.questDescription);
                        }
                    }
                }
            }
            ChatAndTextUtil.sendNormalMessage(sender, "");
        }
        return true;
    }

    private boolean fixupStack(ItemStack[] stacks, ItemStack fixedStack, SimplifiedItemData simplifiedFixedStack) {
        boolean changed = false;
        for (int i = 0; i < stacks.length; i++) {
            SimplifiedItemData simplified = SimplifiedItemData.from(stacks[i]);
            if (canBeChecked(stacks[i]) && simplified.equals(simplifiedFixedStack) && !stacks[i].isSimilar(fixedStack)) {
                ItemStack newStack = fixedStack.clone();
                newStack.setAmount(stacks[i].getAmount());
                stacks[i] = newStack;
                changed = true;
            }
        }
        return changed;
    }

    private boolean canBeChecked(ItemStack s) {
        if (s == null || !s.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = s.getItemMeta();
        if (meta instanceof CompassMeta cm && cm.hasLodestone()) {
            return false;
        }
        if (meta.hasDisplayName() || meta.hasLore())
            return true;
        if (meta instanceof SkullMeta sm && sm.hasOwner()) {
            return true;
        }
        return false;
    }

    @Override
    public Collection<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        return List.of();
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }

}
