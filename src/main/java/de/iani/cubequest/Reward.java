package de.iani.cubequest;

import com.google.common.base.Verify;
import de.iani.cubequest.events.QuestRewardDeliveredEvent;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.bukkit.items.ItemGroups;
import de.iani.cubesideutils.bukkit.items.ItemStacks;
import de.iani.cubesideutils.bukkit.items.ItemsAndStrings;
import de.iani.cubesideutils.bukkit.updater.DataUpdater;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.block.ShulkerBox;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class Reward implements ConfigurationSerializable {

    public static enum NotificationSetting {

        ALWAYS, TREASURE_CHEST, NEVER;

        public static NotificationSetting parse(String s) {
            s = s.toUpperCase();
            try {
                return valueOf(s);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        public static final NotificationSetting DEFAULT = ALWAYS;
    }

    public static enum DirectPayoutSetting {

        ALWAYS, SERVER_SETTING, NEVER;

        public static final DirectPayoutSetting DEFAULT = SERVER_SETTING;

        public static DirectPayoutSetting parse(String s) {
            s = s.toUpperCase();
            if (s.equals("SERVER")) {
                return SERVER_SETTING;
            }
            try {
                return valueOf(s);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        public boolean check() {
            return switch (this) {
                case ALWAYS -> true;
                case SERVER_SETTING -> CubeQuest.getInstance().isPayRewards();
                case NEVER -> false;
            };
        }
    }

    private int cubes;
    private int questPoints;
    private int xp;
    private ItemStack[] items;

    private NotificationSetting notificationSetting;
    private DirectPayoutSetting directPayoutSetting;

    public Reward() {
        this(0, new ItemStack[0]);
    }

    public Reward(int cubes) {
        this(cubes, new ItemStack[0]);
    }

    public Reward(ItemStack[] items) {
        this(0, items);
    }

    public Reward(int cubes, ItemStack[] items) {
        this(cubes, 0, 0, items);

    }

    public Reward(int cubes, int questPoints, int xp, ItemStack[] items) {
        this(cubes, questPoints, xp, items, NotificationSetting.DEFAULT, DirectPayoutSetting.DEFAULT);
    }

    public Reward(int cubes, int questPoints, int xp, ItemStack[] items, NotificationSetting notificationSetting,
            DirectPayoutSetting directPayoutSetting) {
        Verify.verify(cubes >= 0);
        Verify.verify(questPoints >= 0);
        Verify.verify(xp >= 0);

        this.cubes = cubes;
        this.questPoints = questPoints;
        this.xp = xp;
        this.items = items == null ? new ItemStack[0] : ItemStacks.shrink(items);
        this.notificationSetting = Objects.requireNonNull(notificationSetting);
        this.directPayoutSetting = Objects.requireNonNull(directPayoutSetting);
    }

    @SuppressWarnings("unchecked")
    public Reward(Map<String, Object> serialized) throws InvalidConfigurationException {
        try {
            this.cubes = serialized.containsKey("cubes") ? (Integer) serialized.get("cubes") : 0;
            this.questPoints = serialized.containsKey("questPoints") ? (Integer) serialized.get("questPoints") : 0;
            this.xp = serialized.containsKey("xp") ? (Integer) serialized.get("xp") : 0;
            if (!serialized.containsKey("items")) {
                this.items = new ItemStack[0];
            } else {
                List<?> itemsList = (List<?>) serialized.get("items");
                if (itemsList.isEmpty()) {
                    this.items = new ItemStack[0];
                } else if (itemsList.get(0) instanceof ItemStack) {
                    this.items = ((List<ItemStack>) itemsList).toArray(new ItemStack[0]);
                } else {
                    this.items = itemsList.stream().map(x -> (byte[]) x).map(ItemStack::deserializeBytes)
                            .toArray(ItemStack[]::new);
                }
            }
            // attempt to fix shulkers duplicating after server update
            for (int i = 0; i < this.items.length; i++) {
                this.items[i] = this.items[i].ensureServerConversions();
                if (ItemGroups.isShulkerBox(this.items[i].getType())) {
                    BlockStateMeta meta = (BlockStateMeta) this.items[i].getItemMeta();
                    ShulkerBox state = (ShulkerBox) meta.getBlockState();
                    ItemStack[] content = state.getInventory().getContents();
                    for (int j = 0; j < content.length; j++) {
                        if (content[j] != null) {
                            content[j] = content[j].ensureServerConversions();
                        }
                    }
                    state.getInventory().setContents(content);
                    meta.setBlockState(state);
                    state.update();
                    this.items[i].setItemMeta(meta);
                }
            }

            this.notificationSetting = NotificationSetting.valueOf(
                    (String) serialized.getOrDefault("notificationSetting", NotificationSetting.DEFAULT.name()));
            this.directPayoutSetting = DirectPayoutSetting.valueOf(
                    (String) serialized.getOrDefault("directPayoutSetting", DirectPayoutSetting.DEFAULT.name()));
        } catch (Exception e) {
            throw new InvalidConfigurationException(e);
        }
    }

    public int getCubes() {
        return this.cubes;
    }

    public int getQuestPoints() {
        return this.questPoints;
    }

    public int getXp() {
        return this.xp;
    }

    public ItemStack[] getItems() {
        return this.items;
    }

    public NotificationSetting getNotificationSetting() {
        return this.notificationSetting;
    }

    public DirectPayoutSetting getDirectPayoutSetting() {
        return this.directPayoutSetting;
    }

    public Reward add(Reward other) {
        ItemStack newItems[] = new ItemStack[this.items.length + other.items.length];
        for (int i = 0; i < this.items.length; i++) {
            newItems[i] = this.items[i];
        }
        for (int i = 0; i < other.items.length; i++) {
            newItems[i + this.items.length] = other.items[i];
        }

        return new Reward(this.cubes + other.cubes, this.questPoints + other.questPoints, this.xp + other.xp, newItems,
                this.notificationSetting, this.directPayoutSetting);
    }

    public void pay(Player player) {
        if (this.notificationSetting == NotificationSetting.ALWAYS) {
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        }

        if (!this.directPayoutSetting.check()) {
            if (this.notificationSetting == NotificationSetting.ALWAYS) {
                ChatAndTextUtil.sendXpAndQuestPointsMessage(player, this.xp, this.questPoints);
            }
            CubeQuest.getInstance().getPlayerData(player).applyQuestPointsAndXP(this);
            CubeQuest.getInstance().getLogger().log(Level.INFO, "Player " + player.getName() + " received " + this.xp
                    + " xp and " + this.questPoints + " questPoints.");

            boolean putInTreasureChest = this.cubes != 0 || this.items.length != 0;
            if (putInTreasureChest) {
                addToTreasureChest(player.getUniqueId());
                if (this.notificationSetting != NotificationSetting.NEVER) {
                    ChatAndTextUtil.sendNormalMessage(player, "Deine Belohnung wurde in deine Schatzkiste gelegt.");
                }
            }
            callEvent(player, !putInTreasureChest);
            return;
        }

        if (this.items.length != 0) {
            if (!ItemStacks.addToInventoryIfFits(player.getInventory(), this.items)) {
                if (this.notificationSetting != NotificationSetting.NEVER) {
                    ChatAndTextUtil.sendWarningMessage(player,
                            "Du hast nicht genügend Platz in deinem Inventar! Deine Belohnung wird in deine Schatzkiste gelegt.");

                }
                if (this.notificationSetting == NotificationSetting.ALWAYS) {
                    ChatAndTextUtil.sendXpAndQuestPointsMessage(player, this.xp, this.questPoints);
                }
                CubeQuest.getInstance().getPlayerData(player).applyQuestPointsAndXP(this);
                CubeQuest.getInstance().getLogger().log(Level.INFO, "Player " + player.getName() + " received "
                        + this.xp + " xp and " + this.questPoints + " questPoints.");
                addToTreasureChest(player.getUniqueId());
                callEvent(player, false);
                return;
            }

            CubeQuest.getInstance().getLogger().log(Level.INFO,
                    "Player " + player.getName() + " received " + Arrays.toString(this.items) + ".");

            if (this.notificationSetting == NotificationSetting.ALWAYS) {
                for (ItemStack stack : this.items) {
                    StringBuilder t = new StringBuilder("  ");
                    if (stack.getAmount() > 1) {
                        t.append(stack.getAmount()).append(" ");
                    }
                    t.append(ChatAndTextUtil.capitalize(stack.getType().name(), true));
                    ItemMeta meta = stack.getItemMeta();
                    if (meta.hasDisplayName()) {
                        t.append(" (").append(meta.getDisplayName()).append(ChatColor.YELLOW).append(")");
                    }
                    ChatAndTextUtil.sendMessage(player, t.toString());
                }
            }
        }

        if (this.notificationSetting == NotificationSetting.ALWAYS) {
            ChatAndTextUtil.sendXpAndQuestPointsMessage(player, this.xp, this.questPoints);
        }
        CubeQuest.getInstance().getPlayerData(player).applyQuestPointsAndXP(this);
        CubeQuest.getInstance().payCubes(player, this.cubes);
        CubeQuest.getInstance().getLogger().log(Level.INFO, "Player " + player.getName() + " received " + this.xp
                + " xp, " + this.questPoints + " questPoints and " + this.cubes + " cubes.");

        if (this.notificationSetting == NotificationSetting.ALWAYS) {
            if (this.cubes != 0) {
                ChatAndTextUtil.sendNormalMessage(player, "Du hast " + this.cubes + " Cubes erhalten.");
            }
            ChatAndTextUtil.sendMessage(player, ChatColor.GRAY + "Du hast eine Belohnung bekommen!");
        }
        callEvent(player, true);
    }

    public void addToTreasureChest(UUID playerId) {
        if (!CubeQuest.getInstance().addToTreasureChest(playerId, this)) {
            try {
                CubeQuest.getInstance().getDatabaseFassade().addRewardToDeliver(new Reward(getCubes(), getItems()),
                        playerId);
            } catch (SQLException e) {
                CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                        "Could not add Quest-Reward to database for player with UUID " + playerId, e);
            }
        } else {
            CubeQuest.getInstance().getLogger().log(Level.INFO, "Reward for player " + playerId + " cotaining "
                    + this.cubes + " cubes and Items (" + Arrays.toString(this.items) + ") was put in treasure chest.");
        }
    }

    public boolean giveItemsDirectly(Player player) {
        if (ItemStacks.addToInventoryIfFits(player.getInventory(), this.items)) {
            player.updateInventory();
            return true;
        }
        return false;
    }

    private void callEvent(Player player, boolean directly) {
        QuestRewardDeliveredEvent event = new QuestRewardDeliveredEvent(player, this, directly);
        Bukkit.getPluginManager().callEvent(event);
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> data = new HashMap<>();
        data.put("cubes", this.cubes);
        data.put("questPoints", this.questPoints);
        data.put("xp", this.xp);
        data.put("items", Arrays.stream(this.items).map(ItemStack::serializeAsBytes).toList());
        data.put("notificationSetting", this.notificationSetting.name());
        data.put("directPayoutSetting", this.directPayoutSetting.name());
        return data;
    }

    public Reward performDataUpdate() {
        ItemStack[] updated = Arrays.stream(this.items).map(i -> i == null ? null : DataUpdater.updateItemStack(i))
                .toArray(ItemStack[]::new);
        if (updated.equals(this.items)) {
            return this;
        }
        return new Reward(this.cubes, this.questPoints, this.xp, updated, this.notificationSetting,
                this.directPayoutSetting);
    }

    public boolean isEmpty() {
        return this.cubes == 0 && this.questPoints == 0 && this.xp == 0 && this.items.length == 0;
    }

    public String toNiceString() {
        String result = "";
        if (this.notificationSetting != NotificationSetting.DEFAULT
                || this.directPayoutSetting != DirectPayoutSetting.DEFAULT) {
            result += "(Nachricht " + this.notificationSetting + ", Auszahlung " + this.directPayoutSetting + ") ";
        }

        if (isEmpty()) {
            return result + "Nichts";
        }

        result += this.cubes + " Cubes";
        result += ", " + this.questPoints + " Punkte";
        result += ", " + this.xp + " XP";

        if (this.items.length != 0) {
            result += ", Items: ";
            result += ItemsAndStrings.toNiceString(this.items);
        }

        return result;
    }

}
