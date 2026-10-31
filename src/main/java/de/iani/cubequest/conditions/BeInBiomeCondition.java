package de.iani.cubequest.conditions;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;


public class BeInBiomeCondition extends QuestCondition {

    private boolean backwardsIncompatible = false;
    private Biome biome;
    private String biomeString;
    private Component biomeComponent;

    public BeInBiomeCondition(boolean visible, Biome biome) {
        super(visible);
        this.biome = Objects.requireNonNull(biome);
        this.biomeString = RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME).getKey(biome).asString();
        this.biomeComponent = Component.translatable(biome, NamedTextColor.GREEN);
    }

    public BeInBiomeCondition(Map<String, Object> serialized) {
        super(serialized);

        this.biomeString = (String) serialized.get("biome");
        NamespacedKey key = NamespacedKey.fromString(this.biomeString);
        this.biome = key == null ? null : RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME).get(key);
        if (this.biome == null) {
            this.biomeComponent = Component.textOfChildren(Component.text(this.biomeString, NamedTextColor.GREEN),
                    Component.text(" (nicht vorhanden)", NamedTextColor.RED));
            this.backwardsIncompatible = true;
            Integer questId = CubeQuest.getInstance().getQuestCreator().getCurrentlyDeserializing();
            CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                    "Biome " + this.biomeString + " is no longer available! Quest-ID: " + questId);
        } else {
            this.biomeComponent = Component.translatable(this.biome, NamedTextColor.GREEN);
        }
    }

    @Override
    public boolean fulfills(Player player, PlayerData data) {
        if (this.backwardsIncompatible) {
            return false;
        }
        return this.biome.equals(player.getLocation().getBlock().getBiome());
    }

    @Override
    public Component getConditionInfo(boolean includeHiddenInfo) {
        return Component.textOfChildren(Component.text("Im Biom: ", NamedTextColor.DARK_AQUA), this.biomeComponent);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("biome", this.biomeString);
        return result;
    }

}
