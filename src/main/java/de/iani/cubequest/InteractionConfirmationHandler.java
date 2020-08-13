package de.iani.cubequest;

import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.quests.InteractorQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.interactiveBookAPI.InteractiveBookAPI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class InteractionConfirmationHandler {
    
    private InteractiveBookAPI booksApi;
    private List<InteractorQuest> showOnNextBook;
    private Map<UUID, Map<UUID, InteractorQuest>> awaitingConfirmation;
    
    public InteractionConfirmationHandler() {
        this.booksApi = CubeQuest.getInstance().getBookApi();
        this.awaitingConfirmation = new HashMap<>();
    }
    
    public void addQuestToNextBook(InteractorQuest quest) {
        if (this.showOnNextBook == null) {
            this.showOnNextBook = new ArrayList<>();
        }
        this.showOnNextBook.add(quest);
    }
    
    public boolean showBook(Player player) {
        if (this.showOnNextBook == null) {
            return false;
        }
        
        this.showOnNextBook.sort(Quest.QUEST_DISPLAY_COMPARATOR);
        
        Map<UUID, InteractorQuest> entry = new HashMap<>();
        ItemStack bookStack = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) bookStack.getItemMeta();
        
        List<BaseComponent[]> confirmationMessageList = new ArrayList<>(this.showOnNextBook.size() * 2);
        for (InteractorQuest quest : this.showOnNextBook) {
            UUID secretKey = UUID.randomUUID();
            entry.put(secretKey, quest);
            
            if (!confirmationMessageList.isEmpty()) {
                confirmationMessageList.add(null);
            }
            confirmationMessageList.add(getBaseComponents(quest, secretKey));
        }
        
        ChatAndTextUtil.writeIntoBook(bookMeta, confirmationMessageList);
        
        this.awaitingConfirmation.put(player.getUniqueId(), entry);
        this.showOnNextBook = null;
        
        bookMeta.setTitle("Quest abgeben");
        bookMeta.setAuthor("CubeQuest");
        bookStack.setItemMeta(bookMeta);
        this.booksApi.showBookToPlayer(player, bookStack);
        
        return true;
    }
    
    private BaseComponent[] getBaseComponents(InteractorQuest quest, UUID secretKey) {
        ComponentBuilder builder = new ComponentBuilder("")
                .append(new TextComponent(TextComponent.fromLegacyText(quest.getConfirmationMessage())));
        
        builder.event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Quest abgeben.")));
        builder.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                "/quest confirmQuestInteraction " + secretKey.toString()));
        
        return builder.create();
    }
    
    public void interactionConfirmedCommand(Player player, UUID secretKey) {
        Map<UUID, InteractorQuest> awaiting = this.awaitingConfirmation.remove(player.getUniqueId());
        
        if (awaiting == null) {
            ChatAndTextUtil.sendErrorMessage(player, "Du kannst so keine Quest abgeben!");
            CubeQuest.getInstance().getLogger().log(Level.INFO,
                    "Player " + player.getName() + " tried to confirm InteractorQuest, but wasn't registered"
                            + " for any Quest. His given secret key: " + secretKey.toString());
            return;
        }
        
        InteractorQuest quest = awaiting.get(secretKey);
        if (quest == null) {
            ChatAndTextUtil.sendErrorMessage(player, "Du kannst so keine Quest abgeben!");
            CubeQuest.getInstance().getLogger().log(Level.INFO,
                    "Player " + player.getName() + " tried to confirm InteractorQuest, but wasn't registered"
                            + " with this key. His given secret key: " + secretKey.toString());
            return;
        }
        
        QuestState state = CubeQuest.getInstance().getPlayerData(player).getPlayerState(quest.getId());
        if (state.getStatus() != Status.GIVENTO) {
            ChatAndTextUtil.sendWarningMessage(player, "Diese Quest ist für dich nicht mehr aktiv.");
            return;
        }
        
        double distance = quest.getInteractor().getLocation().distance(player.getLocation());
        if (distance > 7) {
            ChatAndTextUtil.sendWarningMessage(player,
                    "Du bist zu weit vom Ziel der Quest entfernt, um diese abzuschließen.");
            return;
        }
        
        quest.playerConfirmedInteraction(player, state);
    }
    
    public void playerLeft(Player player) {
        this.awaitingConfirmation.remove(player.getUniqueId());
    }
    
}
