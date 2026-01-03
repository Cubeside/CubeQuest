package de.iani.cubequest.quests;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.commands.AddOrRemoveEntityTypeCommand;
import de.iani.cubequest.questStates.AmountQuestState;
import de.iani.cubesideutils.bukkit.updater.DataUpdater;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;

public abstract class EntityTypesAndAmountQuest extends EconomyInfluencingAmountQuest {

    private Set<EntityType> types;

    public EntityTypesAndAmountQuest(int id, String name, Component displayMessage, Collection<EntityType> types,
            int amount) {
        super(id, name, displayMessage, amount);

        this.types = (types == null) ? new HashSet<>() : new HashSet<>(types);
    }

    public EntityTypesAndAmountQuest(int id) {
        this(id, null, null, null, 0);
    }

    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);

        this.types.clear();
        List<String> typeList = yc.getStringList("types");
        for (String s : typeList) {
            if (s.equals("PIG_ZOMBIE")) {
                this.types.add(EntityType.ZOMBIFIED_PIGLIN);
            } else {
                this.types.add(EntityType.valueOf(DataUpdater.updateEntityTypeName(s)));
            }
        }
    }

    @Override
    protected String serializeToString(YamlConfiguration yc) {
        List<String> typeList = new ArrayList<>();
        for (EntityType m : this.types) {
            typeList.add(m.toString());
        }
        yc.set("types", typeList);

        return super.serializeToString(yc);
    }

    @Override
    public boolean isLegal() {
        return super.isLegal() && !this.types.isEmpty();
    }

    @Override
    public AmountQuestState createQuestState(UUID id) {
        return getId() < 0 ? null : new AmountQuestState(CubeQuest.getInstance().getPlayerData(id), getId());
    }

    @Override
    public List<Component> getQuestInfo() {
        List<Component> result = super.getQuestInfo();

        Component line = Component.text("Erlaubte Entity-Typen: ", NamedTextColor.DARK_AQUA);

        if (this.types.isEmpty()) {
            line = line.append(Component.text("Keine", NamedTextColor.RED));
        } else {
            List<EntityType> typeList = new ArrayList<>(this.types);
            typeList.sort((e1, e2) -> e1.name().compareTo(e2.name()));

            for (int i = 0; i < typeList.size(); i++) {
                line = line.append(Component.text(typeList.get(i).name(), NamedTextColor.GREEN));
                if (i + 1 < typeList.size()) {
                    line = line.append(Component.text(", ", NamedTextColor.GREEN));
                }
            }
        }

        result.add(suggest(line, AddOrRemoveEntityTypeCommand.FULL_ADD_COMMAND));
        result.add(Component.empty());

        return result;
    }

    public Set<EntityType> getTypes() {
        return Collections.unmodifiableSet(this.types);
    }

    public boolean addType(EntityType type) {
        if (this.types.add(type)) {
            updateIfReal();
            return true;
        }
        return false;
    }

    public boolean removeType(EntityType type) {
        if (this.types.remove(type)) {
            updateIfReal();
            return true;
        }
        return false;
    }

    public void clearTypes() {
        this.types.clear();
        updateIfReal();
    }

}
