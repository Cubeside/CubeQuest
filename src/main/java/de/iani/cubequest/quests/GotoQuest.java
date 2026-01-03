package de.iani.cubequest.quests;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.commands.SetGotoInvertedCommand;
import de.iani.cubequest.commands.SetGotoLocationCommand;
import de.iani.cubequest.commands.SetGotoToleranceCommand;
import de.iani.cubequest.commands.SetOverwrittenNameForSthCommand;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerMoveEvent;

@DelegateDeserialization(Quest.class)
public class GotoQuest extends ServerDependendQuest {

    private String world;
    private double x, y, z;
    private double tolarance;
    private boolean inverted;

    private Component overwrittenLocationName;

    public GotoQuest(int id, Component name, Component displayMessage, int serverId, String world, double x, double y,
            double z, double tolarance) {
        super(id, name, displayMessage, serverId);

        if (isForThisServer() && world != null) {
            World w = Bukkit.getWorld(world);
            if (w == null) {
                throw new IllegalArgumentException("World " + w + " not found.");
            }
        }

        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.tolarance = tolarance;
    }

    public GotoQuest(int id, Component name, Component displayMessage, Location location, double tolarance) {
        this(id, name, displayMessage, CubeQuest.getInstance().getServerId(), location.getWorld().getName(),
                location.getX(), location.getY(), location.getZ(), tolarance);
    }

    public GotoQuest(int id) {
        this(id, null, null, CubeQuest.getInstance().getServerId(), null, 0, 0, 0, 0.5);
    }

    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);

        String world = yc.getString("target.world");
        if (isForThisServer() && world != null) {
            World w = Bukkit.getWorld(world);
            if (w == null) {
                throw new IllegalArgumentException("World " + w + " not found.");
            }
        }

        this.world = world;
        this.x = yc.getDouble("target.x");
        this.y = yc.getDouble("target.y");
        this.z = yc.getDouble("target.z");
        this.tolarance = yc.getDouble("tolarance");
        this.inverted = yc.getBoolean("inverted", false);
        this.overwrittenLocationName = getComponentOrConvert(yc, "overwrittenLocationName");
    }

    @Override
    protected String serializeToString(YamlConfiguration yc) {

        yc.createSection("target");
        yc.set("target.world", this.world);
        yc.set("target.x", this.x);
        yc.set("target.y", this.y);
        yc.set("target.z", this.z);
        yc.set("tolarance", this.tolarance);
        yc.set("inverted", this.inverted);
        yc.set("overwrittenLocationName", this.overwrittenLocationName);

        return super.serializeToString(yc);
    }

    @Override
    public boolean onPlayerMoveEvent(PlayerMoveEvent event, QuestState state) {
        return checkForSuccess(event.getTo(), event.getPlayer());
    }

    private boolean checkForSuccess(Location loc, Player player) {
        if (this.inverted == nearTarget(loc, player)) {
            return false;
        }

        if (!this.fulfillsProgressConditions(player, CubeQuest.getInstance().getPlayerData(player))) {
            return false;
        }

        onSuccess(player);
        return true;
    }

    private boolean nearTarget(Location loc, Player player) {
        if (!isForThisServer()) {
            return false;
        }
        if (!loc.getWorld().getName().equals(this.world)) {
            return false;
        }
        if (Math.abs(loc.getX() - this.x) > this.tolarance || Math.abs(loc.getY() - this.y) > this.tolarance
                || Math.abs(loc.getZ() - this.z) > this.tolarance) {
            return false;
        }
        return true;
    }

    @Override
    public void giveToPlayer(Player player) {
        super.giveToPlayer(player);
        Bukkit.getScheduler().scheduleSyncDelayedTask(CubeQuest.getInstance(), () -> {
            if (CubeQuest.getInstance().getPlayerData(player).isGivenTo(getId())) {
                checkForSuccess(player.getLocation(), player);
            }
        }, 1L);
    }

    @Override
    public boolean isLegal() {
        return this.world != null && this.tolarance >= 0 && (!isForThisServer() || Bukkit.getWorld(this.world) != null);
    }

    @Override
    public List<Component> getQuestInfo() {
        List<Component> result = super.getQuestInfo();

        result.add(suggest(
                Component.text("Zu erreichender Ort: ", NamedTextColor.DARK_AQUA).append(ChatAndTextUtil
                        .getLocationInfo(this.world, this.x, this.y, this.z).colorIfAbsent(NamedTextColor.DARK_AQUA)),
                SetGotoLocationCommand.FULL_COMMAND));

        result.add(suggest(ChatAndTextUtil.getToleranceInfo(this.tolarance).colorIfAbsent(NamedTextColor.DARK_AQUA),
                SetGotoToleranceCommand.FULL_COMMAND));

        result.add(suggest(
                Component.text("Invertiert: ", NamedTextColor.DARK_AQUA)
                        .append(Component.text(String.valueOf(this.inverted), NamedTextColor.GREEN)),
                SetGotoInvertedCommand.FULL_COMMAND));

        TextColor nameStatusColor = (this.overwrittenLocationName == null) ? NamedTextColor.GOLD : NamedTextColor.GREEN;
        String nameStatusText = (this.overwrittenLocationName == null) ? "(automatisch)" : "(gesetzt)";

        result.add(suggest(
                Component.text("Name: ", NamedTextColor.DARK_AQUA)
                        .append(getLocationName().colorIfAbsent(NamedTextColor.GREEN)).append(Component.text(" "))
                        .append(Component.text(nameStatusText, nameStatusColor)),
                SetOverwrittenNameForSthCommand.SpecificSth.LOCATION.fullSetCommand));

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

        Component line = prefix.append(getLocationName().colorIfAbsent(NamedTextColor.DARK_AQUA))
                .append(Component.text(" " + (this.inverted ? "verlassen" : "erreicht") + ": "))
                .append(Component.text(status == Status.SUCCESS ? "ja" : "nein").color(status.color))
                .color(NamedTextColor.DARK_AQUA);

        result.add(line);
        return result;
    }

    public Location getTargetLocation() {
        return isForThisServer() && this.world != null
                ? new Location(Bukkit.getWorld(this.world), this.x, this.y, this.z)
                : null;
    }

    public double getTargetX() {
        return this.x;
    }

    public double getTargetY() {
        return this.y;
    }

    public double getTargetZ() {
        return this.z;
    }

    public String getTargetWorld() {
        return this.world;
    }

    public void setLocation(Location arg) {
        if (arg == null) {
            throw new NullPointerException("arg may not be null");
        }
        if (!isForThisServer()) {
            changeServerToThis();
        }
        this.world = arg.getWorld().getName();
        this.x = arg.getX();
        this.y = arg.getY();
        this.z = arg.getZ();

        updateIfReal();
    }

    public double getTolarance() {
        return this.tolarance;
    }

    public void setTolarance(double arg) {
        if (arg < 0) {
            throw new IllegalArgumentException("arg may not be negative");
        }
        this.tolarance = arg;
        updateIfReal();
    }

    public boolean getInverted() {
        return this.inverted;
    }

    public void setInverted(boolean inverted) {
        this.inverted = inverted;
        updateIfReal();
    }

    public Component getLocationName() {
        if (this.overwrittenLocationName != null) {
            return this.overwrittenLocationName;
        }

        String x, y, z;
        if (this.tolarance < 0.5) {
            x = Double.toString(this.x);
            y = Double.toString(this.y);
            z = Double.toString(this.z);
        } else {
            x = Integer.toString((int) Math.ceil(this.x));
            y = Integer.toString((int) Math.round(this.y));
            z = Integer.toString((int) Math.ceil(this.z));
        }

        String name = this.tolarance > 2 ? "ca. " : "";
        name += "x = " + x;
        name += ", y = " + y;
        name += ", z = " + z;
        if (this.tolarance < 0.5) {
            name += " ± " + this.tolarance;
        }
        name += " in Welt \"" + this.world + "\"";

        return Component.text(name);
    }

    public void setLocationName(Component name) {
        this.overwrittenLocationName = name;
        updateIfReal();
    }

}
