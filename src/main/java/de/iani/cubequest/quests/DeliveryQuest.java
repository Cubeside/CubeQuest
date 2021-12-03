package de.iani.cubequest.quests;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.commands.SetDeliveryInventoryCommand;
import de.iani.cubequest.interaction.Interactor;
import de.iani.cubequest.questStates.QuestState;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.bukkit.items.ItemStacks;
import de.iani.cubesideutils.bukkit.items.ItemsAndStrings;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.DelegateDeserialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@DelegateDeserialization(Quest.class)
public class DeliveryQuest extends InteractorQuest {
    
    private ItemStack[] delivery;
    
    public DeliveryQuest(int id, String name, String displayMessage, Interactor recipient, ItemStack[] delivery) {
        super(id, name, displayMessage, recipient);
        
        setDelivery(delivery, false);
    }
    
    public DeliveryQuest(int id) {
        this(id, null, null, null, null);
    }
    
    @Override
    public void deserialize(YamlConfiguration yc) throws InvalidConfigurationException {
        super.deserialize(yc);
        
        setDelivery(yc.getList("delivery").toArray(new ItemStack[0]), false);
    }
    
    @Override
    protected String serializeToString(YamlConfiguration yc) {
        
        yc.set("delivery", Arrays.asList(this.delivery));
        
        return super.serializeToString(yc);
    }
    
    @Override
    public boolean isLegal() {
        return super.isLegal() && this.delivery != null;
    }
    
    @Override
    public List<BaseComponent[]> getQuestInfo() {
        List<BaseComponent[]> result = super.getQuestInfo();
        
        String deliveryString = ChatColor.DARK_AQUA + "Lieferung: ";
        if (ItemStacks.isEmpty(this.delivery)) {
            deliveryString += ChatColor.RED + "KEINE";
        } else {
            deliveryString += ItemsAndStrings.toNiceString(this.delivery, ChatColor.GREEN.toString());
        }
        
        result.add(new ComponentBuilder("").append(new TextComponent(TextComponent.fromLegacyText(deliveryString)))
                .event(new ClickEvent(Action.SUGGEST_COMMAND, "/" + SetDeliveryInventoryCommand.FULL_COMMAND))
                .event(SUGGEST_COMMAND_HOVER_EVENT).create());
        result.add(new ComponentBuilder("").create());
        
        return result;
    }
    
    @Override
    public List<BaseComponent[]> getSpecificStateInfoInternal(PlayerData data, int indentionLevel) {
        List<BaseComponent[]> result = new ArrayList<>();
        QuestState state = data.getPlayerState(getId());
        Status status = state == null ? Status.NOTGIVENTO : state.getStatus();
        
        ComponentBuilder interactorClickedBuilder =
                new ComponentBuilder(ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel));
        
        if (!getDisplayName().equals("")) {
            result.add(new ComponentBuilder(ChatAndTextUtil.repeat(Quest.INDENTION, indentionLevel)
                    + ChatAndTextUtil.getStateStringStartingToken(state)).append(" ")
                            .append(TextComponent.fromLegacyText(ChatColor.GOLD + getDisplayName())).create());
            interactorClickedBuilder.append(Quest.INDENTION);
        } else {
            interactorClickedBuilder.append(ChatAndTextUtil.getStateStringStartingToken(state) + " ");
        }
        
        interactorClickedBuilder
                .append(TextComponent
                        .fromLegacyText(ItemsAndStrings.toNiceString(this.delivery, ChatColor.DARK_AQUA.toString())))
                .append(" an ").color(ChatColor.DARK_AQUA)
                .append(TextComponent.fromLegacyText(String.valueOf(getInteractorName()))).append(" geliefert: ")
                .color(ChatColor.DARK_AQUA);
        interactorClickedBuilder.append(status == Status.SUCCESS ? "ja" : "nein").color(status.color);
        
        result.add(interactorClickedBuilder.create());
        
        return result;
    }
    
    public ItemStack[] getDelivery() {
        return Arrays.copyOf(this.delivery, this.delivery.length);
    }
    
    public void setDelivery(ItemStack[] arg) {
        setDelivery(arg, true);
    }
    
    private void setDelivery(ItemStack[] arg, boolean updateInDB) {
        arg = arg == null ? new ItemStack[0] : arg;
        this.delivery = ItemStacks.shrink(arg);
        if (updateInDB) {
            updateIfReal();
        }
    }
    
    @Override
    public boolean playerConfirmedInteraction(Player player, QuestState state) {
        if (!super.playerConfirmedInteraction(player, state)) {
            return false;
        }
        
        ItemStack[] oldContent = player.getInventory().getContents();
        ItemStack[] missing = ItemStacks.doesHave(player, getDelivery(), true, true);
        
        if (missing.length > 0) {
            ChatAndTextUtil.sendWarningMessage(state.getPlayerData().getPlayer(),
                    "Du hast nicht genügend Items im Inventar, um diese Quest abzuschließen!");
            ChatAndTextUtil.sendWarningMessage(state.getPlayerData().getPlayer(),
                    "Dir fehl" + (missing.length == 1 && missing[0].getAmount() == 1 ? "t" : "en") + ": "
                            + ItemsAndStrings.toNiceString(missing, ChatColor.GOLD.toString()));
            return false;
        }
        
        if (!onSuccess(state.getPlayerData().getPlayer())) {
            state.getPlayerData().getPlayer().getInventory().setContents(oldContent);
            state.getPlayerData().getPlayer().updateInventory();
            return false;
        }
        
        CubeQuest.getInstance().getLogger().log(Level.INFO,
                "Player " + player.getName() + " deliverd " + Arrays.toString(this.delivery) + ".");
        return true;
    }
    
}
