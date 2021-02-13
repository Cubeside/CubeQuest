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
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
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
    
    public TakeDamageQuest(int id, String name, String displayMessage, Collection<DamageCause> causes,
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
    public List<BaseComponent[]> getQuestInfo() {
        List<BaseComponent[]> result = super.getQuestInfo();
        
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Festgelegte Schadenstypen: "
                + ((this.whitelist ? this.causes.isEmpty() : EnumSet.complementOf(this.causes).isEmpty())
                        ? ChatColor.RED
                        : ChatColor.GREEN)
                + (this.causes.isEmpty() ? "KEINE"
                        : this.causes.stream().map(DamageCause::name).collect(Collectors.joining(", "))))
                                .event(new ClickEvent(Action.SUGGEST_COMMAND,
                                        "/" + AddOrRemoveDamageCauseCommand.ADD_FULL_COMMAND))
                                .event(SUGGEST_COMMAND_HOVER_EVENT).create());
        result.add(new ComponentBuilder(
                ChatColor.DARK_AQUA + "Zähle nur festgelegte (sonst alle anderen): " + ChatColor.GREEN + this.whitelist)
                        .event(new ClickEvent(Action.SUGGEST_COMMAND,
                                "/" + TakeDamageQuestPropertyType.WHITELIST.fullCommand))
                        .event(SUGGEST_COMMAND_HOVER_EVENT).create());
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "HP (halbe Herzen): "
                + (!this.atOnce || this.hp > 0 ? ChatColor.GREEN : ChatColor.RED) + this.hp)
                        .event(new ClickEvent(Action.SUGGEST_COMMAND, "/" + TakeDamageQuestPropertyType.HP.fullCommand))
                        .event(SUGGEST_COMMAND_HOVER_EVENT).create());
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA
                + "Zähle auf einmal erhaltenen Schaden (sonst verbleibende HP): " + ChatColor.GREEN + this.atOnce)
                        .event(new ClickEvent(Action.SUGGEST_COMMAND,
                                "/" + TakeDamageQuestPropertyType.AT_ONCE.fullCommand))
                        .event(SUGGEST_COMMAND_HOVER_EVENT).create());
        result.add(new ComponentBuilder(ChatColor.DARK_AQUA + "Schaden blockieren: " + ChatColor.GREEN + this.cancel)
                .event(new ClickEvent(Action.SUGGEST_COMMAND, "/" + TakeDamageQuestPropertyType.CANCEL.fullCommand))
                .event(SUGGEST_COMMAND_HOVER_EVENT).create());
        result.add(new ComponentBuilder("").create());
        
        return result;
    }
    
    @Override
    protected List<BaseComponent[]> getSpecificStateInfoInternal(PlayerData data, int indentionLevel) {
        List<BaseComponent[]> result = new ArrayList<>();
        Status status = data.getPlayerStatus(getId());
        
        String damageTakenString = ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel);
        
        if (!getDisplayName().equals("")) {
            result.add(new ComponentBuilder(ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel)
                    + ChatAndTextUtil.getStateStringStartingToken(status)).append(" ")
                            .append(TextComponent.fromLegacyText(ChatColor.GOLD + getDisplayName())).create());
            damageTakenString += Quest.INDENTION;
        } else {
            damageTakenString += ChatAndTextUtil.getStateStringStartingToken(status) + " ";
        }
        
        if (this.atOnce) {
            damageTakenString += "Auf einmal " + (this.hp / 2) + " Herzen Schaden genommen";
        } else {
            damageTakenString += "Auf " + (this.hp / 2) + " Herzen gefallen";
        }
        if (this.whitelist) {
            damageTakenString +=
                    " (durch " + this.causes.stream().map(DamageCause::name).collect(Collectors.joining(", ")) + ")";
        } else if (!this.causes.isEmpty()) {
            damageTakenString += " (außer durch "
                    + this.causes.stream().map(DamageCause::name).collect(Collectors.joining(", ")) + ")";
        }
        
        damageTakenString += ": ";
        damageTakenString += status.color + "" + (status == Status.SUCCESS ? "ja" : "nein");
        
        result.add(new ComponentBuilder(damageTakenString).create());
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
