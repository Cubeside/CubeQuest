package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.interaction.InteractorProtecting;
import de.iani.cubequest.interaction.PlayerInteractInteractorEvent;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;


public class ListInteractingProtectingCommand extends SubCommand implements Listener {

    private Set<UUID> currentlySelecting;

    public ListInteractingProtectingCommand() {
        this.currentlySelecting = new HashSet<>();
        Bukkit.getPluginManager().registerEvents(this, CubeQuest.getInstance());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {
        this.currentlySelecting.add(((Player) sender).getUniqueId());
        ChatAndTextUtil.sendNormalMessage(sender,
                "Rechtsklicke den Interactor, dessen Info du einsehen möchtest (Rechtsklicke etwas anderes zum Abbrechen).");
        return true;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerInteractInteractorEvent(PlayerInteractInteractorEvent<?> event) {
        if (!this.currentlySelecting.remove(event.getPlayer().getUniqueId())) {
            return;
        }

        event.setCancelled(true);
        ChatAndTextUtil.sendNormalMessage(event.getPlayer(), "Protectors von ", event.getInteractor(), ":");
        for (InteractorProtecting protecting : CubeQuest.getInstance().getProtectedBy(event.getInteractor())) {
            ChatAndTextUtil.sendNormalMessage(event.getPlayer(), (Object[]) protecting.getProtectingInfo());
        }
    }

    @EventHandler(ignoreCancelled = false)
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (event.getAction() == Action.PHYSICAL) {
            return;
        }
        if (this.currentlySelecting.remove(event.getPlayer().getUniqueId())) {
            ChatAndTextUtil.sendWarningMessage(event.getPlayer(), "Auswahl abgebrochen.");
        }
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }

    @Override
    public boolean requiresPlayer() {
        return true;
    }

}
