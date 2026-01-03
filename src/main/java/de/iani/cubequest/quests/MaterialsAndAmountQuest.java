package de.iani.cubequest.quests;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.commands.AddOrRemoveMaterialCommand;
import de.iani.cubequest.generation.MaterialCombination;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

public abstract class MaterialsAndAmountQuest extends EconomyInfluencingAmountQuest {

    private MaterialCombination types;

    public MaterialsAndAmountQuest(int id, Component name, Component displayMessage, Collection<Material> types,
            int amount) {
        super(id, name, displayMessage, amount);

        this.types = types == null ? new MaterialCombination() : new MaterialCombination(types);
    }

    public MaterialsAndAmountQuest(int id) {
        this(id, null, null, null, 0);
    }

    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);

        Object typesObject = yc.get("types");
        if (typesObject instanceof MaterialCombination) {
            this.types = new MaterialCombination((MaterialCombination) typesObject);
        } else {
            this.types.clear();
            List<String> typeList = yc.getStringList("types");
            for (String s : typeList) {
                Material mat = Material.getMaterial(s, true);
                if (mat == null) {
                    CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Material with name \"" + s
                            + "\" could not be converted for quest " + toString() + "! Now removed from the quest.");
                    continue;
                }

                this.types.add(mat);
            }
        }
    }

    @Override
    protected String serializeToString(YamlConfiguration yc) {
        yc.set("types", this.types);

        return super.serializeToString(yc);
    }

    @Override
    public boolean isLegal() {
        return super.isLegal() && this.types.isLegal();
    }

    @Override
    public List<Component> getQuestInfo() {
        List<Component> result = super.getQuestInfo();

        Component line = Component.text("Erlaubte Materialien: ", NamedTextColor.DARK_AQUA);

        if (this.types.isEmpty()) {
            line = line.append(Component.text("Keine", NamedTextColor.RED));
        } else {
            List<Material> typeList = new ArrayList<>(this.types.getContent());
            typeList.sort((e1, e2) -> e1.name().compareTo(e2.name()));

            for (int i = 0; i < typeList.size(); i++) {
                Material type = typeList.get(i);
                line = line.append(Component.text(type.name(), NamedTextColor.GREEN));

                if (i < typeList.size() - 1) {
                    line = line.append(Component.text(", ", NamedTextColor.GREEN));
                }
            }
        }

        result.add(suggest(line, AddOrRemoveMaterialCommand.FULL_ADD_COMMAND));
        result.add(Component.empty());

        return result;
    }

    public Set<Material> getTypes() {
        return this.types.getContent();
    }

    public boolean addType(Material type) {
        if (this.types.add(type)) {
            updateIfReal();
            return true;
        }
        return false;
    }

    public boolean removeType(Material type) {
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
