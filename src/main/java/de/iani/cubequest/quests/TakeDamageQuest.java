package de.iani.cubequest.quests;

import de.iani.cubequest.PlayerData;
import de.iani.cubequest.commands.AddOrRemoveDamageCauseCommand;
import de.iani.cubequest.commands.SetTakeDamageQuestPropertyCommand.TakeDamageQuestPropertyType;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;


public class TakeDamageQuest extends ProgressableQuest {

    private EnumSet<DamageCause> causes;
    private boolean whitelist;

    private double hp;
    private boolean atOnce;

    private boolean cancel;

    public TakeDamageQuest(int id, String name, Component displayMessage, Collection<DamageCause> causes,
            boolean whitelist, double hp, boolean atOnce, boolean cancel) {
        super(id, name, displayMessage);

        this.causes = causes == null || causes.isEmpty() ? EnumSet.noneOf(DamageCause.class) : EnumSet.copyOf(causes);
        this.whitelist = whitelist;
        this.hp = hp;
        this.atOnce = atOnce;
        this.cancel = cancel;
    }

    public TakeDamageQuest(int id) {
        this(id, null, null, null, false, 0.0, false, false);
    }

    public Set<DamageCause> getCauses() {
        return Collections.unmodifiableSet(this.causes);
    }

    public boolean addCause(DamageCause cause) {
        if (this.causes.add(cause)) {
            updateIfReal();
            return true;
        }
        return false;
    }

    public boolean removeCause(DamageCause cause) {
        if (this.causes.remove(cause)) {
            updateIfReal();
            return true;
        }
        return false;
    }

    public void clearCauses() {
        this.causes.clear();
        updateIfReal();
    }

    public boolean isWhitelist() {
        return this.whitelist;
    }

    public void setWhitelist(boolean whitelist) {
        this.whitelist = whitelist;
        updateIfReal();
    }

    public double getHp() {
        return this.hp;
    }

    public void setHp(double hp) {
        if (hp < 0) {
            throw new IllegalArgumentException("hp may not be negative");
        }
        this.hp = hp;
        updateIfReal();
    }

    public boolean isAtOnce() {
        return this.atOnce;
    }

    public void setAtOnce(boolean atOnce) {
        this.atOnce = atOnce;
        updateIfReal();
    }

    public boolean isCancel() {
        return this.cancel;
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
        updateIfReal();
    }

    @Override
    public boolean isLegal() {
        if (this.hp < 0) {
            return false;
        }
        if (this.atOnce && this.hp == 0) {
            return false;
        }

        if (this.whitelist) {
            return !this.causes.isEmpty();
        } else {
            return !EnumSet.complementOf(this.causes).isEmpty();
        }
    }

    @Override
    public boolean onEntityDamageEvent(EntityDamageEvent event, QuestState state) {
        if (!(event.getEntity() instanceof Player)) {
            return false;
        }
        if (!doesCount(event.getCause())) {
            return false;
        }

        Player player = (Player) event.getEntity();
        if (!this.fulfillsProgressConditions(player, state.getPlayerData())) {
            return false;
        }

        if (this.atOnce && event.getDamage() >= this.hp
                || !this.atOnce && player.getHealth() - event.getDamage() <= this.hp) {
            onSuccess(player);
            if (this.cancel) {
                event.setCancelled(true);
            }
            return true;
        }

        return false;
    }

    private boolean doesCount(DamageCause cause) {
        boolean contained = this.causes.contains(cause);
        return this.whitelist ? contained : !contained;
    }

    @Override
    public List<Component> getQuestInfo() {
        List<Component> result = super.getQuestInfo();

        boolean noneEffective = (this.whitelist ? this.causes.isEmpty() : EnumSet.complementOf(this.causes).isEmpty());
        NamedTextColor causesColor = noneEffective ? NamedTextColor.RED : NamedTextColor.GREEN;

        String causesText = this.causes.isEmpty() ? "KEINE"
                : this.causes.stream().map(DamageCause::name).collect(Collectors.joining(", "));

        Component causesLine = Component.text("Festgelegte Schadenstypen: ", NamedTextColor.DARK_AQUA)
                .append(Component.text(causesText, causesColor));

        result.add(suggest(causesLine, AddOrRemoveDamageCauseCommand.ADD_FULL_COMMAND));

        Component whitelistLine =
                Component.text("Zähle nur festgelegte (sonst alle anderen): ", NamedTextColor.DARK_AQUA)
                        .append(Component.text(String.valueOf(this.whitelist), NamedTextColor.GREEN));

        result.add(suggest(whitelistLine, TakeDamageQuestPropertyType.WHITELIST.fullCommand));

        NamedTextColor hpColor = (!this.atOnce || this.hp > 0) ? NamedTextColor.GREEN : NamedTextColor.RED;
        Component hpLine = Component.text("HP (halbe Herzen): ", NamedTextColor.DARK_AQUA)
                .append(Component.text(String.valueOf(this.hp), hpColor));

        result.add(suggest(hpLine, TakeDamageQuestPropertyType.HP.fullCommand));

        Component atOnceLine = Component
                .text("Zähle auf einmal erhaltenen Schaden (sonst verbleibende HP): ", NamedTextColor.DARK_AQUA)
                .append(Component.text(String.valueOf(this.atOnce), NamedTextColor.GREEN));

        result.add(suggest(atOnceLine, TakeDamageQuestPropertyType.AT_ONCE.fullCommand));

        Component cancelLine = Component.text("Schaden blockieren: ", NamedTextColor.DARK_AQUA)
                .append(Component.text(String.valueOf(this.cancel), NamedTextColor.GREEN));

        result.add(suggest(cancelLine, TakeDamageQuestPropertyType.CANCEL.fullCommand));

        result.add(Component.empty());
        return result;
    }

    // TODO: make damage cause translatable once possible
    @Override
    protected List<Component> getSpecificStateInfoInternal(PlayerData data, int indentionLevel) {
        List<Component> result = new ArrayList<>();

        Status status = data.getPlayerStatus(getId());

        Component baseIndent = ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel);
        Component prefix = baseIndent;

        if (!Component.empty().equals(getDisplayName())) {
            result.add(baseIndent.append(ChatAndTextUtil.getStateStringStartingToken(status))
                    .append(Component.text(" ")).append(getDisplayName().colorIfAbsent(NamedTextColor.GOLD))
                    .color(NamedTextColor.DARK_AQUA));
            prefix = prefix.append(Quest.INDENTION);
        } else {
            prefix = prefix.append(ChatAndTextUtil.getStateStringStartingToken(status)).append(Component.text(" "));
        }

        Component msg = Component.text(this.atOnce ? "Auf einmal " + (this.hp / 2) + " Herzen Schaden genommen"
                : "Auf " + (this.hp / 2) + " Herzen gefallen");

        if (this.whitelist) {
            msg = msg.append(Component.text(" (durch "))
                    .append(Component
                            .text(this.causes.stream().map(DamageCause::name).collect(Collectors.joining(", "))))
                    .append(Component.text(")"));
        } else if (!this.causes.isEmpty()) {
            msg = msg.append(Component.text(" (außer durch "))
                    .append(Component
                            .text(this.causes.stream().map(DamageCause::name).collect(Collectors.joining(", "))))
                    .append(Component.text(")"));
        }

        msg = msg.append(Component.text(": "))
                .append(Component.text(status == Status.SUCCESS ? "ja" : "nein").color(status.color));

        result.add(prefix.append(msg).color(NamedTextColor.DARK_AQUA));
        return result;
    }

    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);

        this.causes = yc.getStringList("causes").stream().map(DamageCause::valueOf)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(DamageCause.class)));
        this.whitelist = yc.getBoolean("whitelist");
        this.hp = yc.getDouble("hp");
        this.atOnce = yc.getBoolean("atOnce");
        this.cancel = yc.getBoolean("cancel");
    }

    @Override
    protected String serializeToString(YamlConfiguration yc) {
        yc.set("causes", this.causes.stream().map(DamageCause::name).collect(Collectors.toList()));
        yc.set("whitelist", this.whitelist);
        yc.set("hp", this.hp);
        yc.set("atOnce", this.atOnce);
        yc.set("cancel", this.cancel);

        return super.serializeToString(yc);
    }

}
