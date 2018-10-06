package de.iani.cubequest.cubeshop;

import de.iani.cubeshop.CubeShop;
import org.bukkit.plugin.java.JavaPlugin;

public class Registrator {
    
    public void register() {
        CubeShop cubeShop = JavaPlugin.getPlugin(CubeShop.class);
        cubeShop.getPriceFactory().registerPriceType(QuestPointsPriceType.getInstance());
    }
    
}
