package de.iani.cubequest;

import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.quests.InteractorQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.interactiveBookAPI.InteractiveBookAPI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class InteractionConfirmationHandler {
    
    private InteractiveBookAPI booksApi;
    private List<InteractorQuest> showOnNextBook;
    private Map<UUID, Map<UUID, InteractorQuest>> awaitingConfirmation;
    
    public InteractionConfirmationHandler() {
        this.booksApi =
                (InteractiveBookAPI) Bukkit.getPluginManager().getPlugin("InteractiveBookAPI");
        this.awaitingConfirmation = new HashMap<>();
    }
    
    public void addQuestToNextBook(InteractorQuest quest) {
        if (this.showOnNextBook == null) {
            this.showOnNextBook = new ArrayList<>();
        }
        this.showOnNextBook.add(quest);
    }
    
    public void showBook(Player player) {
        if (this.showOnNextBook == null) {
            return;
        }
        
        this.showOnNextBook.sort((q1, q2) -> {
            int result = q1.getName().compareTo(q2.getName());
            return result != 0 ? result : q1.getId() - q2.getId();
        });
        
        Map<UUID, InteractorQuest> entry = new HashMap<>();
        ItemStack bookStack = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) bookStack.getItemMeta();
        
        List<BaseComponent> currentPage = new ArrayList<>();
        
        for (InteractorQuest quest: this.showOnNextBook) {
            UUID secretKey = UUID.randomUUID();
            entry.put(secretKey, quest);
            
            if (currentPage.isEmpty()) {
                currentPage.addAll(getBaseComponents(quest, secretKey));
                continue;
            }
            
            List<BaseComponent> nextEntry = getBaseComponents(quest, secretKey);
            List<BaseComponent> extendedPage = new ArrayList<>(currentPage);
            extendedPage.addAll(Arrays.asList(new ComponentBuilder("\n\n").create()));
            extendedPage.addAll(currentPage);
            
            if (this.booksApi
                    .fitsPage(extendedPage.toArray(new BaseComponent[extendedPage.size()]))) {
                currentPage = extendedPage;
            } else {
                this.booksApi.addPage(bookMeta,
                        currentPage.toArray(new BaseComponent[currentPage.size()]));
                currentPage = new ArrayList<>(nextEntry);
            }
            
        }
        
        this.awaitingConfirmation.put(player.getUniqueId(), entry);
        this.showOnNextBook = null;
        
        this.booksApi.addPage(bookMeta, currentPage.toArray(new BaseComponent[currentPage.size()]));
        bookStack.setItemMeta(bookMeta);
        this.booksApi.showBookToPlayer(player, bookStack);
    }
    
    private List<BaseComponent> getBaseComponents(InteractorQuest quest, UUID secretKey) {
        ComponentBuilder builder = new ComponentBuilder(quest.getConfirmationMessage());
        
        builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Quest abgeben.").create()));
        builder.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                "/quest confirmQuestInteraction " + secretKey.toString()));
        
        return Arrays.asList(builder.create());
    }
    
    public void interactionConfirmedCommand(Player player, UUID secretKey) {
        Map<UUID, InteractorQuest> awaiting =
                this.awaitingConfirmation.remove(player.getUniqueId());
        
        if (awaiting == null) {
            ChatAndTextUtil.sendErrorMessage(player, "Du kannst so keine Quest abgeben!");
            CubeQuest.getInstance().getLogger().log(Level.INFO,
                    "Player " + player.getName()
                            + " tried to confirm InteractorQuest, but wasn't registered"
                            + " for any Quest. His given secret key: " + secretKey.toString());
            return;
        }
        
        InteractorQuest quest = awaiting.get(secretKey);
        if (quest == null) {
            ChatAndTextUtil.sendErrorMessage(player, "Du kannst so keine Quest abgeben!");
            CubeQuest.getInstance().getLogger().log(Level.INFO,
                    "Player " + player.getName()
                            + " tried to confirm InteractorQuest, but wasn't registered"
                            + " with this key. His given secret key: " + secretKey.toString());
            return;
        }
        
        QuestState state =
                CubeQuest.getInstance().getPlayerData(player).getPlayerState(quest.getId());
        if (state.getStatus() != Status.GIVENTO) {
            ChatAndTextUtil.sendWarningMessage(player,
                    "Diese Quest ist für dich nicht mehr aktiv.");
            return;
        }
        
        double distance = quest.getInteractor().getLocation().distance(player.getLocation());
        if (distance > 7) {
            ChatAndTextUtil.sendWarningMessage(player,
                    "Du bist zu weit vom Ziel der Quest entfernt, um diese abzuschließen.");
            return;
        }
        
        quest.playerConfirmedInteraction(state);
    }
    
    public void playerLeft(Player player) {
        this.awaitingConfirmation.remove(player.getUniqueId());
    }
    
}
