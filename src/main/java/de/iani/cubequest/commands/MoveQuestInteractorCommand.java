package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.interaction.Interactor;
import de.iani.cubequest.interaction.PlayerInteractInteractorEvent;
import de.iani.cubequest.quests.InteractorQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.Pair;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class MoveQuestInteractorCommand extends SubCommand implements Listener {

    public static String COMMAND_PATH = "moveQuestInteractor";
    public static String FULL_COMMAND = "quest " + COMMAND_PATH;

    private Set<Player> selecting;
    private Map<Player, Interactor> firstSelected;
    private Map<Player, Pair<Interactor, Interactor>> secondSelected;

    public MoveQuestInteractorCommand() {
        this.selecting = new HashSet<>();
        this.firstSelected = new HashMap<>();
        this.secondSelected = new HashMap<>();

        Bukkit.getPluginManager().registerEvents(this, CubeQuest.getInstance());
    }

    @EventHandler(ignoreCancelled = false)
    public void onPlayerInteractInteractorEvent(PlayerInteractInteractorEvent<?> event) {
        if (!this.selecting.contains(event.getPlayer()) || this.secondSelected.get(event.getPlayer()) != null) {
            return;
        }

        event.setCancelled(true);

        if (!this.firstSelected.containsKey(event.getPlayer())) {
            this.firstSelected.put(event.getPlayer(), event.getInteractor());
            ChatAndTextUtil.sendNormalMessage(event.getPlayer(), "Ausgangs-Interactor ausgewählt: ",
                    event.getInteractor().getInfo());
            ChatAndTextUtil.sendNormalMessage(event.getPlayer(), "Quests auf diesem Interactor:");
            CubeQuest.getInstance().getProtectedBy(event.getInteractor()).stream()
                    .filter(ip -> ip instanceof InteractorQuest).map(ip -> (InteractorQuest) ip).forEach(quest -> {
                        event.getPlayer().sendMessage(quest.toString());
                    });
            return;
        }

        Interactor first = this.firstSelected.get(event.getPlayer());
        this.secondSelected.put(event.getPlayer(), new Pair<>(first, event.getInteractor()));
        ChatAndTextUtil.sendNormalMessage(event.getPlayer(), "Ziel-Interactor ausgewählt: ",
                event.getInteractor().getInfo());
        ChatAndTextUtil.sendNormalMessage(event.getPlayer(), "Zum bestätigen, gib /", FULL_COMMAND, " confirm ein.");
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        this.selecting.remove(event.getPlayer());
        this.firstSelected.remove(event.getPlayer());
        this.secondSelected.remove(event.getPlayer());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {
        if (args.seeNext("").equals("cancel")) {
            boolean result = this.selecting.remove(sender);
            result |= (this.firstSelected.remove(sender) != null);
            result |= (this.secondSelected.remove(sender) != null);
            if (result) {
                ChatAndTextUtil.sendNormalMessage(sender, "Verschieben abgebrochen.");
            } else {
                ChatAndTextUtil.sendWarningMessage(sender, "Du verschiebst derzeit keinen Interactor.");
            }
            return true;
        }

        if (args.seeNext("").equals("confirm")) {
            if (this.secondSelected.get(sender) == null) {
                ChatAndTextUtil.sendWarningMessage(sender, "Du hast keine Interactors zum verschieben ausgewählt.");
                return true;
            }

            Pair<Interactor, Interactor> selected = this.secondSelected.get(sender);
            new ArrayList<>(CubeQuest.getInstance().getProtectedBy(selected.first())).stream()
                    .filter(ip -> ip instanceof InteractorQuest).map(ip -> (InteractorQuest) ip).forEach(quest -> {
                        quest.setInteractor(selected.second());
                    });

            this.selecting.remove(sender);
            this.firstSelected.remove(sender);
            this.secondSelected.remove(sender);
            ChatAndTextUtil.sendNormalMessage(sender, "Interactor verschoben.");
            return true;
        }

        this.selecting.add((Player) sender);
        this.firstSelected.remove(sender);
        this.secondSelected.remove(sender);

        ChatAndTextUtil.sendNormalMessage(sender,
                "Verschieben gestartet. Rechtsklicke erst den Ausgangs- und dann den Ziel-Interactor.");
        return true;

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
