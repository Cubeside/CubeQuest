package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestGiver;
import de.iani.cubequest.interaction.PlayerInteractInteractorEvent;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.Util;
import de.iani.cubesideutils.ComponentUtilAdventure;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import java.text.ParseException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class AddQuestGiverCommand extends SubCommand implements Listener {

    private Map<UUID, Component> currentlySelectingInteractor;

    public AddQuestGiverCommand() {
        this.currentlySelectingInteractor = new HashMap<>();
        Bukkit.getPluginManager().registerEvents(this, CubeQuest.getInstance());
        CubeQuest.getInstance().getEventListener()
                .addOnPlayerQuit(player -> this.currentlySelectingInteractor.remove(player.getUniqueId()));
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteractInteractorEvent(PlayerInteractInteractorEvent<?> event) {
        Component name = this.currentlySelectingInteractor.remove(event.getPlayer().getUniqueId());
        if (name == null) {
            return;
        }
        String rawName = ComponentUtilAdventure.rawText(name);

        event.setCancelled(true);

        if (CubeQuest.getInstance().getQuestGiver(rawName) != null) {
            ChatAndTextUtil.sendWarningMessage(event.getPlayer(),
                    "In der Zwischenzeit wurde bereits ein QuestGiver mit diesem Namen angelegt. Auswahl abgebrochen.");
            return;
        }
        QuestGiver other = CubeQuest.getInstance().getQuestGiver(event.getInteractor());
        if (other != null) {
            ChatAndTextUtil.sendWarningMessage(event.getPlayer(),
                    "Dieser Interactor ist bereits als QuestGiver mit dem Namen " + other.getRawName()
                            + " eingetragen.");
            return;
        }

        CubeQuest.getInstance().addQuestGiver(new QuestGiver(event.getInteractor(), name));
        ChatAndTextUtil.sendNormalMessage(event.getPlayer(), "QuestGiver ", name, " gesetzt!");
    }

    @EventHandler(ignoreCancelled = false)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) {
            return;
        }
        if (this.currentlySelectingInteractor.remove(event.getPlayer().getUniqueId()) != null) {
            ChatAndTextUtil.sendWarningMessage(event.getPlayer(), "Auswahl abgebrochen.");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

        if (this.currentlySelectingInteractor == null) {
            ChatAndTextUtil.sendErrorMessage(sender,
                    "Auf dem Server müssen das Citizens-Plugin und die InteractiveBooksAPI installiert sein, eins von beidem ist nicht der Fall!");
            return true;
        }

        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib einen Namen für den neuen QuestGiver an.");
            return true;
        }

        String nameString = args.getNext();
        Component name;
        try {
            name = ComponentUtilAdventure.deserializeComponent(nameString);
        } catch (ParseException e) {
            ChatAndTextUtil.sendWarningMessage(sender, "Ungültiger Name: ", e.getMessage());
            return true;
        }

        String rawName = ComponentUtilAdventure.rawText(name);
        if (args.hasNext() || !Util.isSafeGiverName(rawName)) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "QuestGiver-Namen dürfen nur Buchstaben, Zahlen sowie die Zeichen '&' und '_' enthalten, insbesondere keine Leerzeichen.");
            return true;
        }

        if (CubeQuest.getInstance().getQuestGiver(rawName) != null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Einen QuestGiver mit diesem Namen gibt es bereits.");
            return true;
        }

        this.currentlySelectingInteractor.put(((Player) sender).getUniqueId(), name);
        ChatAndTextUtil.sendNormalMessage(sender,
                "Bitte rechtsklicke den Interactor für diesen QuestGiver. Rechtsklicke irgendetwas anderes, um die Auswahl abzubrechen.");
        return true;
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUEST_GIVERS_PERMISSION;
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        return Collections.emptyList();
    }

    @Override
    public String getUsage() {
        return "<QuestGiverName>";
    }

}
