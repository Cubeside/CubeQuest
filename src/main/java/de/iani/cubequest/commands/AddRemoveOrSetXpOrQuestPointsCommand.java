package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.PlayerData;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class AddRemoveOrSetXpOrQuestPointsCommand extends AssistedSubCommand {
    
    public enum PointAction {
        ADD("change", 1), REMOVE("change", -1), SET("set", 1);
        
        public final String methodNamePrefix;
        public final int argumentFactor;
        
        private final Method xpMethod;
        private final Method pointsMethod;
        
        private PointAction(String methodNamePrefix, int argumentFactor) {
            this.methodNamePrefix = methodNamePrefix;
            this.argumentFactor = argumentFactor;
            
            try {
                this.xpMethod = PlayerData.class.getMethod(methodNamePrefix + "Xp", int.class);
                this.pointsMethod =
                        PlayerData.class.getMethod(methodNamePrefix + "QuestPoints", int.class);
            } catch (NoSuchMethodException | SecurityException e) {
                throw new AssertionError(e);
            }
            
        }
        
        public Method getMethod(boolean xp) {
            return xp ? this.xpMethod : this.pointsMethod;
        }
    }
    
    private boolean xp;
    
    private static ParameterDefiner[] getParameterDefiners(PointAction action, boolean xp) {
        return new ParameterDefiner[] {
                new ParameterDefiner(ParameterType.OFFLINE_PLAYER, "Spieler",
                        parsed -> ((OfflinePlayer) parsed[1]).getUniqueId() == null
                                ? "Spieler nicht gefunden."
                                : null),
                new ParameterDefiner(
                        action == PointAction.SET ? ParameterType.AT_LEAST_ZERO_INTEGER
                                : ParameterType.POSITIVE_INTEGER,
                        xp ? "Quest-XP" : "Quest-Punkte", parsed -> null)};
    }
    
    private static Function<Object[], String> getPropertySetter(PointAction action, boolean xp) {
        return parsed -> {
            try {
                OfflinePlayer player = (OfflinePlayer) parsed[1];
                PlayerData data = CubeQuest.getInstance().getPlayerData(player.getUniqueId());
                action.getMethod(xp).invoke(data, action.argumentFactor * (Integer) parsed[2]);
            } catch (IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            return null;
        };
    }
    
    private static Function<Object[], String> getSuccessMessageProvider(PointAction action,
            boolean xp) {
        return parsed -> {
            OfflinePlayer player = (OfflinePlayer) parsed[1];
            String name = player.getName();
            name = name == null ? player.getUniqueId().toString() : name;
            return (xp ? "Quest-XP" : "Quest-Punkte") + " für Spieler " + name
                    + (action == PointAction.SET ? " auf " : " um ") + parsed[2]
                    + (action == PointAction.ADD ? " erhöht"
                            : action == PointAction.REMOVE ? " reduziert" : " gesetzt")
                    + ".";
        };
    }
    
    public AddRemoveOrSetXpOrQuestPointsCommand(PointAction action, boolean xp) {
        super("quest " + action.name().toLowerCase() + (xp ? "Xp" : "QuestPoints"),
                AssistedSubCommand.ACCEPTING_SENDER_CONSTRAINT, getParameterDefiners(action, xp),
                getPropertySetter(action, xp), getSuccessMessageProvider(action, xp));
        this.xp = xp;
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias,
            ArgsParser args) {
        String playerName = args.getNext("");
        if (args.hasNext()) {
            return Collections.emptyList();
        } else {
            List<String> raw = Bukkit.getOnlinePlayers().stream().map(p -> p.getName())
                    .collect(Collectors.toList());
            return ChatAndTextUtil.polishTabCompleteList(raw, playerName);
        }
    }
    
    @Override
    public String getUsage() {
        return "<" + (this.xp ? "XP" : "Punkte") + ">";
    }
    
}
