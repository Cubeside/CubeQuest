package de.iani.cubequest;

import com.google.common.base.Verify;
import de.iani.cubequest.interaction.Interactor;
import de.iani.cubequest.interaction.InteractorDamagedEvent;
import de.iani.cubequest.interaction.InteractorProtecting;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.interactiveBookAPI.InteractiveBookAPI;
import de.iani.interactiveBookAPI.InteractiveBookAPIPlugin;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class QuestGiver implements InteractorProtecting, ConfigurationSerializable {
    
    private Interactor interactor;
    private String name;
    
    private Set<Quest> quests;
    private Map<UUID, Set<Quest>> mightGetFromHere;
    
    public QuestGiver(Interactor interactor, String name) {
        Verify.verify(interactor != null && interactor.isLegal());
        
        this.interactor = interactor;
        this.name = name;
        
        this.quests = new HashSet<>();
        this.mightGetFromHere = new HashMap<>();
        
        saveConfig();
    }
    
    @SuppressWarnings("unchecked")
    public QuestGiver(Map<String, Object> serialized) throws InvalidConfigurationException {
        this.mightGetFromHere = new HashMap<>();
        
        try {
            this.interactor = (Interactor) serialized.get("interactor");
            if (this.interactor == null || !this.interactor.isLegal()) {
                throw new InvalidConfigurationException("interactor is null or invalid");
            }
            this.name = (String) serialized.get("name");
            this.quests = new HashSet<>();
            List<Integer> questIdList = (List<Integer>) (serialized.get("quests"));
            questIdList.forEach(id -> {
                Quest q = QuestManager.getInstance().getQuest(id);
                if (q == null) {
                    CubeQuest.getInstance().getLogger().log(Level.WARNING,
                            "Quest with id " + id + ", which was included in QuestGiver "
                                    + this.name + " not found (maybe was deleted).");
                } else {
                    this.quests.add(q);
                }
            });
        } catch (Exception e) {
            throw new InvalidConfigurationException(e);
        }
    }
    
    @Override
    public Interactor getInteractor() {
        return this.interactor;
    }
    
    public String getName() {
        return this.name;
    }
    
    public Set<Quest> getQuests() {
        return Collections.unmodifiableSet(this.quests);
    }
    
    public boolean hasQuest(Quest quest) {
        return this.quests.contains(quest);
    }
    
    public boolean addQuest(Quest quest) {
        if (this.quests.add(quest)) {
            saveConfig();
            return true;
        }
        return false;
    }
    
    public boolean removeQuest(Quest quest) {
        if (this.quests.remove(quest)) {
            saveConfig();
            
            Iterator<Set<Quest>> it = this.mightGetFromHere.values().iterator();
            while (it.hasNext()) {
                Set<Quest> set = it.next();
                if (set.isEmpty()) {
                    it.remove();
                }
            }
            return true;
        }
        return false;
    }
    
    public boolean hasQuestFoPlayer(Player player) {
        return hasQuestForPlayer(player, CubeQuest.getInstance().getPlayerData(player));
    }
    
    public boolean hasQuestForPlayer(Player player, PlayerData playerData) {
        for (Quest quest: this.quests) {
            if (quest.fullfillsGivingConditions(player, playerData)) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean mightGetFromHere(Player player, Quest quest) {
        Set<Quest> set = this.mightGetFromHere.get(player.getUniqueId());
        return set == null ? false : set.contains(quest);
    }
    
    public boolean addMightGetFromHere(Player player, Quest quest) {
        Set<Quest> set = this.mightGetFromHere.get(player.getUniqueId());
        if (set == null) {
            set = new HashSet<>();
            this.mightGetFromHere.put(player.getUniqueId(), set);
        }
        return set.add(quest);
    }
    
    public boolean removeMightGetFromHere(Player player, Quest quest) {
        Set<Quest> set = this.mightGetFromHere.get(player.getUniqueId());
        if (set == null) {
            return false;
        }
        boolean result = set.remove(quest);
        if (set.isEmpty()) {
            this.mightGetFromHere.remove(player.getUniqueId());
        }
        return result;
    }
    
    public boolean removeMightGetFromHere(Player player) {
        Set<Quest> set = this.mightGetFromHere.remove(player.getUniqueId());
        return set != null && !set.isEmpty();
    }
    
    public void showQuestsToPlayer(Player player) {
        if (!player.hasPermission(CubeQuest.ACCEPT_QUESTS_PERMISSION)) {
            return;
        }
        
        List<Quest> givables = new ArrayList<>();
        PlayerData playerData = CubeQuest.getInstance().getPlayerData(player);
        this.quests.stream().filter(q -> q.fullfillsGivingConditions(player, playerData))
                .forEach(q -> givables.add(q));
        givables.sort(Quest.QUEST_DISPLAY_COMPARATOR);
        
        InteractiveBookAPI bookAPI = JavaPlugin.getPlugin(InteractiveBookAPIPlugin.class);
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setDisplayName("Quests");
        
        if (givables.isEmpty()) {
            ComponentBuilder builder = new ComponentBuilder("");
            builder.append("Leider habe ich keine neuen Aufgaben für dich.").bold(true)
                    .color(ChatColor.GOLD);
            bookAPI.addPage(meta, builder.create());
        } else {
            for (Quest q: givables) {
                List<BaseComponent[]> displayMessageList = ChatAndTextUtil.getQuestDescription(q);
                
                ComponentBuilder builder = new ComponentBuilder("");
                ClickEvent cEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                        "/quest acceptQuest " + this.name + " " + q.getId());
                HoverEvent hEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder("Hier klicken").create());
                builder.append("Quest annehmen").color(ChatColor.GREEN).bold(true).event(cEvent)
                        .event(hEvent);
                displayMessageList.add(builder.create());
                
                ChatAndTextUtil.writeIntoBook(meta, displayMessageList);
                
                addMightGetFromHere(player, q);
            }
        }
        
        meta.setAuthor(getName());
        book.setItemMeta(meta);
        bookAPI.showBookToPlayer(player, book);
    }
    
    @Override
    public boolean onInteractorDamagedEvent(InteractorDamagedEvent<?> event) {
        if (event.getInteractor().equals(this.interactor)) {
            event.setCancelled(true);
            return true;
        }
        
        return false;
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();
        
        result.put("interactor", this.interactor);
        result.put("name", this.name);
        
        List<Integer> questIdList = new ArrayList<>();
        this.quests.forEach(q -> questIdList.add(q.getId()));
        result.put("quests", questIdList);
        
        return result;
    }
    
    public void saveConfig() {
        File folder = new File(CubeQuest.getInstance().getDataFolder(), "questGivers");
        folder.mkdirs();
        File configFile = new File(folder, this.name + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("giver", this);
        try {
            config.save(configFile);
        } catch (IOException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not save QuestGiver.", e);
        }
    }
    
}
