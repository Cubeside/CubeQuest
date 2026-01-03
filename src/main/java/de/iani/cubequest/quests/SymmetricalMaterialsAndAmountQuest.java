package de.iani.cubequest.quests;

import de.iani.cubequest.commands.SetIgnoreOppositeCommand;
import java.util.Collection;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;


public abstract class SymmetricalMaterialsAndAmountQuest extends MaterialsAndAmountQuest {

    private boolean ignoreOpposite;

    public SymmetricalMaterialsAndAmountQuest(int id, Component name, Component displayMessage,
            Collection<Material> types, int amount) {
        super(id, name, displayMessage, types, amount);
    }

    public SymmetricalMaterialsAndAmountQuest(int id) {
        this(id, null, null, null, 0);
    }

    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);

        this.ignoreOpposite = yc.getBoolean("ignoreOpposite", false);
    }

    @Override
    public String serializeToString(YamlConfiguration yc) {
        yc.set("ignoreOpposite", this.ignoreOpposite);

        return super.serializeToString(yc);
    }

    @Override
    public List<Component> getQuestInfo() {
        List<Component> result = super.getQuestInfo();
        result.remove(result.size() - 1); // Remove blank line

        Component line = Component.text("Ignoriert entgegengesetzte Aktionen: ", NamedTextColor.DARK_AQUA)
                .append(Component.text(String.valueOf(this.ignoreOpposite),
                        this.ignoreOpposite ? NamedTextColor.GREEN : NamedTextColor.GOLD));

        result.add(suggest(line, SetIgnoreOppositeCommand.FULL_COMMAND));
        result.add(Component.empty());

        return result;
    }

    public boolean isIgnoreOpposite() {
        return this.ignoreOpposite;
    }

    public void setIgnoreOpposite(boolean ignoreOpposite) {
        this.ignoreOpposite = ignoreOpposite;
        updateIfReal();
    }

}
