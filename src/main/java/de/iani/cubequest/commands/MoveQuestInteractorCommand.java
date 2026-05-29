package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.interaction.Interactor;
import de.iani.cubequest.interaction.PlayerInteractInteractorEvent;
import de.iani.cubequest.quests.InteractorQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.bukkit.plugin.api.UtilsApiBukkit;
import de.iani.cubesideutils.commands.ArgsParser;
import de.iani.cubesideutils.plugin.api.PlayerData;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class MoveQuestInteractorCommand extends SubCommand implements Listener {

    public static String COMMAND_PATH = "moveQuestInteractor";
    public static String FULL_COMMAND = "quest " + COMMAND_PATH;

    private static final String FIRST_INTERACTOR_KEY = "CubeQuest.moveInteractor.firstInteractor";
    private static final String FIRST_QUEST_IDS_KEY = "CubeQuest.moveInteractor.questIds";
    private static final String SECOND_INTERACTOR_KEY = "CubeQuest.moveInteractor.secondInteractor";

    // Only needed locally: tracks players who have started but not yet clicked the source interactor.
    private final Set<Player> selecting = new HashSet<>();

    public MoveQuestInteractorCommand() {
        Bukkit.getPluginManager().registerEvents(this, CubeQuest.getInstance());
    }

    private static String serializeInteractor(Interactor interactor) {
        YamlConfiguration yc = new YamlConfiguration();
        yc.set("interactor", interactor);
        return yc.saveToString();
    }

    private static Interactor deserializeInteractor(String serialized) {
        if (serialized == null) {
            return null;
        }
        YamlConfiguration yc = new YamlConfiguration();
        try {
            yc.loadFromString(serialized);
            return (Interactor) yc.get("interactor");
        } catch (InvalidConfigurationException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Failed to deserialize interactor", e);
            return null;
        }
    }

    private static boolean hasFirstSelected(PlayerData pd) {
        return pd.getCustomData(FIRST_INTERACTOR_KEY) != null;
    }

    private static boolean hasSecondSelected(PlayerData pd) {
        return pd.getCustomData(SECOND_INTERACTOR_KEY) != null;
    }

    private static void clearState(PlayerData pd) {
        pd.removeCustomData(FIRST_INTERACTOR_KEY);
        pd.removeCustomData(FIRST_QUEST_IDS_KEY);
        pd.removeCustomData(SECOND_INTERACTOR_KEY);
    }

    @EventHandler(ignoreCancelled = false)
    public void onPlayerInteractInteractorEvent(PlayerInteractInteractorEvent<?> event) {
        PlayerData pd = UtilsApiBukkit.getInstance().getPlayerData(event.getPlayer());
        boolean awaitingFirst = this.selecting.contains(event.getPlayer());
        boolean awaitingSecond = !awaitingFirst && hasFirstSelected(pd) && !hasSecondSelected(pd);
        if (!awaitingFirst && !awaitingSecond) {
            return;
        }

        event.setCancelled(true);

        if (awaitingFirst) {
            this.selecting.remove(event.getPlayer());
            pd.setCustomData(FIRST_INTERACTOR_KEY, serializeInteractor(event.getInteractor()));

            List<Integer> questIds = CubeQuest.getInstance().getProtectedBy(event.getInteractor()).stream()
                    .filter(ip -> ip instanceof InteractorQuest).map(ip -> (InteractorQuest) ip).map(q -> q.getId())
                    .collect(Collectors.toList());
            pd.setCustomData(FIRST_QUEST_IDS_KEY,
                    questIds.stream().map(String::valueOf).collect(Collectors.joining(",")));

            ChatAndTextUtil.sendNormalMessage(event.getPlayer(), "Ausgangs-Interactor ausgewählt: ",
                    event.getInteractor().getInfo());
            ChatAndTextUtil.sendNormalMessage(event.getPlayer(), "Quests auf diesem Interactor:");
            CubeQuest.getInstance().getProtectedBy(event.getInteractor()).stream()
                    .filter(ip -> ip instanceof InteractorQuest).map(ip -> (InteractorQuest) ip).forEach(quest -> {
                        event.getPlayer().sendMessage(quest.toString());
                    });
            return;
        }

        pd.setCustomData(SECOND_INTERACTOR_KEY, serializeInteractor(event.getInteractor()));
        ChatAndTextUtil.sendNormalMessage(event.getPlayer(), "Ziel-Interactor ausgewählt: ",
                event.getInteractor().getInfo());
        ChatAndTextUtil.sendNormalMessage(event.getPlayer(), "Zum bestätigen, gib /", FULL_COMMAND, " confirm ein.");
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent event) {
        this.selecting.remove(event.getPlayer());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {
        Player player = (Player) sender;
        PlayerData pd = UtilsApiBukkit.getInstance().getPlayerData(player);

        if (args.seeNext("").equals("cancel")) {
            boolean wasActive = this.selecting.remove(player) || hasFirstSelected(pd);
            clearState(pd);
            if (wasActive) {
                ChatAndTextUtil.sendNormalMessage(sender, "Verschieben abgebrochen.");
            } else {
                ChatAndTextUtil.sendWarningMessage(sender, "Du verschiebst derzeit keinen Interactor.");
            }
            return true;
        }

        if (args.seeNext("").equals("confirm")) {
            String secondInteractorData = pd.getCustomData(SECOND_INTERACTOR_KEY);
            if (secondInteractorData == null) {
                ChatAndTextUtil.sendWarningMessage(sender, "Du hast keine Interactors zum verschieben ausgewählt.");
                return true;
            }

            Interactor secondInteractor = deserializeInteractor(secondInteractorData);
            if (secondInteractor == null || !secondInteractor.isForThisServer()) {
                ChatAndTextUtil.sendWarningMessage(sender, "Der Ziel-Interactor befindet sich nicht auf diesem Server. "
                        + "Bitte begib dich auf den richtigen Server, um das Verschieben zu bestätigen.");
                return true;
            }

            String questIdsData = pd.getCustomData(FIRST_QUEST_IDS_KEY);
            if (questIdsData != null && !questIdsData.isEmpty()) {
                Arrays.stream(questIdsData.split(",")).map(String::trim).filter(s -> !s.isEmpty())
                        .map(Integer::parseInt).map(id -> QuestManager.getInstance().getQuest(id))
                        .filter(q -> q instanceof InteractorQuest).map(q -> (InteractorQuest) q)
                        .forEach(quest -> quest.setInteractor(secondInteractor));
            }

            clearState(pd);
            ChatAndTextUtil.sendNormalMessage(sender, "Interactor verschoben.");
            return true;
        }

        this.selecting.remove(player);
        clearState(pd);
        this.selecting.add(player);

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
