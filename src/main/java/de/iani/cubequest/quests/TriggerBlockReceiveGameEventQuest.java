package de.iani.cubequest.quests;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.commands.AddOrRemoveGameEventCommand;
import de.iani.cubequest.commands.SetQuestBlockCommand;
import de.iani.cubequest.commands.SetQuestIgnoreCancelledEventsCommand;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.SafeLocation;
import de.iani.cubesideutils.StringUtil;
import de.iani.cubesideutils.bukkit.StringUtilBukkit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.GameEvent;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockReceiveGameEvent;


public class TriggerBlockReceiveGameEventQuest extends ServerDependendQuest {

    private SafeLocation block;
    private Set<GameEvent> events;
    private boolean ignoreCancelled;

    public TriggerBlockReceiveGameEventQuest(int id) {
        super(id);

        this.events = new LinkedHashSet<>();
    }

    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);

        Set<GameEvent> events = new LinkedHashSet<>();
        List<String> eventKeys = yc.getStringList("events");
        for (String eventKey : eventKeys) {
            GameEvent event = Registry.GAME_EVENT.get(NamespacedKey.fromString(eventKey));
            if (event == null) {
                CubeQuest.getInstance().getLogger().log(Level.SEVERE, "GameEvent with key \"" + eventKey
                        + "\" could not be converted for quest " + toString() + "! Now removed from the quest.");
                continue;
            }
            events.add(event);
        }

        this.events = events;
        this.ignoreCancelled = yc.getBoolean("ignoreCancelled");
        this.block = (SafeLocation) yc.get("block");
    }

    @Override
    protected String serializeToString(YamlConfiguration yc) {
        yc.set("events", this.events.stream().map(GameEvent::getKey).map(NamespacedKey::asString).toList());
        yc.set("ignoreCancelled", this.ignoreCancelled);
        yc.set("block", this.block);

        return super.serializeToString(yc);
    }

    @Override
    public boolean onBlockReceiveGameEvent(BlockReceiveGameEvent event, Player player, QuestState state) {
        if (!isForThisServer()) {
            return false;
        }

        if (event.isCancelled() && this.ignoreCancelled) {
            return false;
        }

        if (!this.events.contains(event.getEvent())) {
            return false;
        }

        if (!this.block.isSimilar(event.getBlock().getLocation())) {
            return false;
        }

        if (!fulfillsProgressConditions(player)) {
            return false;
        }

        onSuccess(player);
        return true;
    }

    @Override
    public List<Component> getQuestInfo() {
        List<Component> result = super.getQuestInfo();

        result.add(suggest(
                Component.text("Block: ", NamedTextColor.DARK_AQUA).append(ChatAndTextUtil.getLocationInfo(this.block)),
                SetQuestBlockCommand.FULL_COMMAND));

        Component eventsLine = Component.text("Erlaubte GameEvents: ", NamedTextColor.DARK_AQUA);

        if (this.events.isEmpty()) {
            eventsLine = eventsLine.append(Component.text("Keine", NamedTextColor.RED));
        } else {
            List<GameEvent> eventList = new ArrayList<>(this.events);
            eventList.sort((e1, e2) -> e1.getKey().compareTo(e2.getKey()));

            for (int i = 0; i < eventList.size(); i++) {
                eventsLine =
                        eventsLine.append(Component.text(eventList.get(i).getKey().getKey(), NamedTextColor.GREEN));
                if (i + 1 < eventList.size()) {
                    eventsLine = eventsLine.append(Component.text(", ", NamedTextColor.GREEN));
                }
            }
        }

        result.add(suggest(eventsLine, AddOrRemoveGameEventCommand.ADD_FULL_COMMAND));

        result.add(suggest(
                Component.text("Ignoriert gecancellete Events: ", NamedTextColor.DARK_AQUA)
                        .append(Component.text(String.valueOf(this.ignoreCancelled), NamedTextColor.GREEN)),
                SetQuestIgnoreCancelledEventsCommand.FULL_COMMAND));

        result.add(Component.empty());
        return result;
    }

    @Override
    protected List<Component> getSpecificStateInfoInternal(PlayerData data, int indentionLevel) {
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

        Component line = prefix.append(Component.text(multipleEventsString())).append(Component.text(" bei "))
                .append(ChatAndTextUtil.getLocationInfo(this.block)).append(Component.text(" ausgelöst: "))
                .append(Component.text(status == Status.SUCCESS ? "ja" : "nein").color(status.color))
                .color(NamedTextColor.DARK_AQUA);

        result.add(line);
        return result;
    }

    private String multipleEventsString() {
        String result = "";

        for (GameEvent event : this.events) {
            result += StringUtilBukkit.toNiceString(event.getKey());
            result += ", ";
        }

        result = StringUtil.replaceLast(result, ", ", "");
        result = StringUtil.replaceLast(result, ", ", " oder ");

        return result;
    }

    @Override
    public boolean isLegal() {
        return !this.events.isEmpty() && this.block != null;
    }

    public Location getBlock() {
        return isForThisServer() && this.block != null ? this.block.getLocation() : null;
    }

    public void setBlock(Location arg) {
        if (arg == null) {
            throw new NullPointerException("arg may not be null");
        }
        if (!isForThisServer()) {
            changeServerToThis();
        }
        this.block = new SafeLocation(arg);
        this.block = this.block.toBlockLocation();

        updateIfReal();
    }

    public Set<GameEvent> getEvents() {
        return Collections.unmodifiableSet(this.events);
    }

    public boolean addEvent(GameEvent event) {
        if (this.events.add(event)) {
            updateIfReal();
            return true;
        }
        return false;
    }

    public boolean removeEvent(GameEvent event) {
        if (this.events.remove(event)) {
            updateIfReal();
            return true;
        }
        return false;
    }

    public boolean isIgnoreCancelled() {
        return this.ignoreCancelled;
    }

    public void setIgnoreCancelled(boolean arg) {
        this.ignoreCancelled = arg;
        updateIfReal();
    }

}
