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
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.GameEvent;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
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
            GameEvent event = GameEvent.getByKey(NamespacedKey.fromString(eventKey));
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
    public List<BaseComponent[]> getQuestInfo() {
        List<BaseComponent[]> result = super.getQuestInfo();

        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Block: " + ChatAndTextUtil.getLocationInfo(this.block))
                .event(new ClickEvent(Action.SUGGEST_COMMAND, "/" + SetQuestBlockCommand.FULL_COMMAND))
                .event(SUGGEST_COMMAND_HOVER_EVENT).create());

        String eventsString = ChatColor.DARK_AQUA + "Erlaubte GameEvents: ";
        if (this.events.isEmpty()) {
            eventsString += ChatColor.RED + "Keine";
        } else {
            eventsString += ChatColor.GREEN;
            List<GameEvent> eventList = new ArrayList<>(this.events);
            eventList.sort((e1, e2) -> e1.getKey().compareTo(e2.getKey()));
            for (GameEvent event : eventList) {
                eventsString += event.getKey().getKey() + ", ";
            }
            eventsString = eventsString.substring(0, eventsString.length() - ", ".length());
        }

        result.add(new ComponentBuilder(eventsString)
                .event(new ClickEvent(Action.SUGGEST_COMMAND, "/" + AddOrRemoveGameEventCommand.ADD_FULL_COMMAND))
                .event(SUGGEST_COMMAND_HOVER_EVENT).create());

        result.add(new ComponentBuilder(
                ChatColor.DARK_AQUA + "Ignoriert gecancellete Events: " + ChatColor.GREEN + this.ignoreCancelled).event(
                        new ClickEvent(Action.SUGGEST_COMMAND, "/" + SetQuestIgnoreCancelledEventsCommand.FULL_COMMAND))
                        .event(SUGGEST_COMMAND_HOVER_EVENT).create());

        result.add(new ComponentBuilder("").create());

        return result;
    }

    @Override
    protected List<BaseComponent[]> getSpecificStateInfoInternal(PlayerData data, int indentionLevel) {
        List<BaseComponent[]> result = new ArrayList<>();
        QuestState state = data.getPlayerState(getId());
        Status status = state == null ? Status.NOTGIVENTO : state.getStatus();

        ComponentBuilder eventTriggeredBuilder =
                new ComponentBuilder(ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel));

        if (!getDisplayName().equals("")) {
            result.add(new ComponentBuilder(ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel)
                    + ChatAndTextUtil.getStateStringStartingToken(state)).append(" ")
                            .append(TextComponent.fromLegacyText(ChatColor.GOLD + getDisplayName())).create());
            eventTriggeredBuilder.append(Quest.INDENTION);
        } else {
            eventTriggeredBuilder.append(ChatAndTextUtil.getStateStringStartingToken(state) + " ");
        }

        eventTriggeredBuilder.append("" + ChatColor.DARK_AQUA).append(multipleEventsString()).append(" bei ")
                .append(TextComponent.fromLegacyText(ChatAndTextUtil.getLocationInfo(this.block)))
                .append(" ausgelöst: ").color(ChatColor.DARK_AQUA);
        eventTriggeredBuilder.append(status == Status.SUCCESS ? "ja" : "nein").color(status.color);

        result.add(eventTriggeredBuilder.create());

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
