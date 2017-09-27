package de.iani.cubequest.questGiving;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.quests.Quest;
import de.iani.interactiveBookAPI.InteractiveBookAPI;
import de.iani.interactiveBookAPI.InteractiveBookAPIPlugin;
import net.citizensnpcs.api.npc.NPC;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;

public class QuestGiver implements ConfigurationSerializable {

    private static final Comparator<Quest> QUEST_DISPLAY_COMPARATOR = (q1, q2) -> q1.getId() - q2.getId();

    private int npcId;
    private String name;

    private Set<Quest> quests;

    public QuestGiver(NPC npc, String name) {
        Verify.verify(CubeQuest.getInstance().hasCitizensPlugin());
        Verify.verify(CubeQuest.getInstance().hasInteractiveBooksAPI());

        this.npcId = npc.getId();
        this.name = name;

        this.quests = new HashSet<Quest>();

        saveConfig();
    }

    @SuppressWarnings("unchecked")
    public QuestGiver(Map<String, Object> serialized) throws InvalidConfigurationException {
        Verify.verify(CubeQuest.getInstance().hasCitizensPlugin());
        Verify.verify(CubeQuest.getInstance().hasInteractiveBooksAPI());

        try {
            npcId = (int) serialized.get("npcId");
            name = (String) serialized.get("name");
            quests = new HashSet<Quest>();
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
            return true;
        }
        return false;
    }

    public void showQuestsToPlayer(Player player) {
        List<Quest> givables = new ArrayList<Quest>();
        PlayerData playerData = CubeQuest.getInstance().getPlayerData(player);
        quests.stream().filter(q -> q.fullfillsGivingConditions(playerData) && playerData.getPlayerStatus(q.getId()) == Status.NOTGIVENTO).forEach(q -> givables.add(q));
        givables.sort(QUEST_DISPLAY_COMPARATOR);

        InteractiveBookAPI bookAPI = InteractiveBookAPIPlugin.getPlugin(InteractiveBookAPIPlugin.class);
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setDisplayName("Quests");

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
        }

        bookAPI.showBookToPlayer(player, book);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = new HashMap<String, Object>();

        result.put("npcId", npcId);
        result.put("name", name);

        List<Integer> questIdList = new ArrayList<Integer>();
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
