package de.iani.cubequest.questGiving;

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

import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import com.google.common.base.Verify;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.quests.Quest;
import de.iani.interactiveBookAPI.InteractiveBookAPI;
import de.iani.interactiveBookAPI.InteractiveBookAPIPlugin;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

public class QuestGiver implements ConfigurationSerializable {

    private int npcId;
    private String name;

    private Set<Quest> quests;
    private Map<UUID, Set<Quest>> mightGetFromHere;

    public QuestGiver(NPC npc, String name) {
        Verify.verify(CubeQuest.getInstance().hasCitizensPlugin());
        Verify.verify(CubeQuest.getInstance().hasInteractiveBooksAPI());

        this.npcId = npc.getId();
        this.name = name;

        this.quests = new HashSet<>();
        this.mightGetFromHere = new HashMap<>();

        saveConfig();
    }

    @SuppressWarnings("unchecked")
    public QuestGiver(Map<String, Object> serialized) throws InvalidConfigurationException {
        Verify.verify(CubeQuest.getInstance().hasCitizensPlugin());
        Verify.verify(CubeQuest.getInstance().hasInteractiveBooksAPI());

        mightGetFromHere = new HashMap<>();

        try {
            npcId = (int) serialized.get("npcId");
            name = (String) serialized.get("name");
            quests = new HashSet<>();
            List<Integer> questIdList = (List<Integer>) (serialized.get("quests"));
            questIdList.forEach(id -> {
                Quest q = QuestManager.getInstance().getQuest(id);
                if (q == null) {
                    throw new IllegalArgumentException("no quest with that id");
                }
                quests.add(q);
            });

            if (CubeQuest.getInstance().getNPCReg().getById(npcId) == null) {
                throw new IllegalArgumentException("no NPC with that id");
            }
        } catch (Exception e) {
            throw new InvalidConfigurationException(e);
        }
    }

    public NPC getNPC() {
        return CubeQuest.getInstance().getNPCReg().getById(npcId);
    }

    public String getName() {
        return name;
    }

    public Set<Quest> getQuests() {
        return Collections.unmodifiableSet(quests);
    }

    public boolean hasQuest(Quest quest) {
        return quests.contains(quest);
    }

    public boolean addQuest(Quest quest) {
        if (quests.add(quest)) {
            saveConfig();
            return true;
        }
        return false;
    }

    public boolean removeQuest(Quest quest) {
        if (quests.remove(quest)) {
            saveConfig();

            Iterator<Set<Quest>> it = mightGetFromHere.values().iterator();
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

    public boolean mightGetFromHere(Player player, Quest quest) {
        Set<Quest> set = mightGetFromHere.get(player.getUniqueId());
        return set == null? false: set.contains(quest);
    }

    public boolean addMightGetFromHere(Player player, Quest quest) {
        Set<Quest> set = mightGetFromHere.get(player.getUniqueId());
        if (set == null) {
            set = new HashSet<>();
            mightGetFromHere.put(player.getUniqueId(), set);
        }
        return set.add(quest);
    }

    public boolean removeMightGetFromHere(Player player, Quest quest) {
        Set<Quest> set = mightGetFromHere.get(player.getUniqueId());
        if (set == null) {
            return false;
        }
        boolean result = set.remove(quest);
        if (set.isEmpty()) {
            mightGetFromHere.remove(player.getUniqueId());
        }
        return result;
    }

    public boolean removeMightGetFromHere(Player player) {
        Set<Quest> set = mightGetFromHere.remove(player.getUniqueId());
        return set != null && !set.isEmpty();
    }

    public void showQuestsToPlayer(Player player) {
        List<Quest> givables = new ArrayList<>();
        PlayerData playerData = CubeQuest.getInstance().getPlayerData(player);
        quests.stream().filter(q -> q.fullfillsGivingConditions(playerData)).forEach(q -> givables.add(q));
        givables.sort(Quest.QUEST_DISPLAY_COMPARATOR);

        InteractiveBookAPI bookAPI = InteractiveBookAPIPlugin.getPlugin(InteractiveBookAPIPlugin.class);
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setDisplayName("Quests");

        if (givables.isEmpty()) {
            ComponentBuilder builder = new ComponentBuilder("");
            builder.append("Leider habe ich keine neuen Aufgaben für dich.").bold(true).color(ChatColor.GOLD);
            bookAPI.addPage(meta, builder.create());
        } else {
            for (Quest q: givables) {
                ComponentBuilder builder = new ComponentBuilder("");
                builder.append(q.getName()).bold(true).reset().append("\n");
                if (q.getDisplayMessage() != null) {
                    builder.append(q.getDisplayMessage()).reset().append("\n");
                }

                ClickEvent cEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/quest acceptQuest " + name + " " + q.getId());
                HoverEvent hEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Hier klicken").create());
                builder.append("Quest annehmen").color(ChatColor.GREEN).bold(true).event(cEvent).event(hEvent);
                bookAPI.addPage(meta, builder.create());

                addMightGetFromHere(player, q);
            }
        }

        meta.setAuthor(getName());
        book.setItemMeta(meta);
        bookAPI.showBookToPlayer(player, book);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<>();

        result.put("npcId", npcId);
        result.put("name", name);

        List<Integer> questIdList = new ArrayList<>();
        quests.forEach(q -> questIdList.add(q.getId()));
        result.put("quests", questIdList);

        return result;
    }

    public void saveConfig() {
        File folder = new File(CubeQuest.getInstance().getDataFolder(), "questGivers");
        folder.mkdirs();
        File configFile = new File(folder, name + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("giver", this);
        try {
            config.save(configFile);
        } catch (IOException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not save QuestGiver.", e);
        }
    }

}
