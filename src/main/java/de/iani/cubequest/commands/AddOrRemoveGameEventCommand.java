package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.TriggerBlockReceiveGameEventQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bukkit.GameEvent;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class AddOrRemoveGameEventCommand extends AssistedSubCommand {

    public static final String ADD_COMMAND_PATH = "addGameEvent";
    public static final String ADD_FULL_COMMAND = "quest " + ADD_COMMAND_PATH;
    public static final String REMOVE_COMMAND_PATH = "removeGameEvent";
    public static final String REMOVE_FULL_COMMAND = "quest " + REMOVE_COMMAND_PATH;

    private static ParameterDefiner[] parameterDefiners;
    private static Function<Object[], String> addPropertySetter;
    private static Function<Object[], String> removePropertySetter;
    private static Function<Object[], String> addSuccessMessageProvider;
    private static Function<Object[], String> removeSuccessMessageProvider;

    static {
        parameterDefiners = new ParameterDefiner[] {
                new ParameterDefiner(ParameterType.CURRENTLY_EDITED_QUEST, "Quest",
                        parsed -> (parsed[1] instanceof TriggerBlockReceiveGameEventQuest) ? null
                                : "Diese Quest erfordert kein GameEvent."),
                new OtherParameterDefiner<>("GameEvent", (sender, arg) -> {
                    NamespacedKey key = NamespacedKey.fromString(arg);
                    if (key == null) {
                        return null;
                    }
                    return Registry.GAME_EVENT.get(key);
                }, parsed -> parsed[2] == null ? "Unbekanntes GameEvent." : null)};

        addPropertySetter = parsed -> {
            if (!((TriggerBlockReceiveGameEventQuest) parsed[1]).addEvent((GameEvent) parsed[2])) {
                return "Dieses GameEvent war bereits eingetragen.";
            }
            return null;
        };
        removePropertySetter = parsed -> {
            if (!((TriggerBlockReceiveGameEventQuest) parsed[1]).removeEvent((GameEvent) parsed[2])) {
                return "Dieses GameEvent war nicht eingetragen.";
            }
            return null;
        };

        addSuccessMessageProvider =
                parsed -> "GameEvent " + ((GameEvent) parsed[2]).getKey() + " zu Quest " + parsed[1] + " hinzugefügt.";
        removeSuccessMessageProvider =
                parsed -> "GameEvent " + ((GameEvent) parsed[2]).getKey() + " von Quest " + parsed[1] + " entfernt.";
    }

    public AddOrRemoveGameEventCommand(boolean add) throws IllegalArgumentException {
        super(add ? ADD_COMMAND_PATH : REMOVE_COMMAND_PATH, ACCEPTING_SENDER_CONSTRAINT, parameterDefiners,
                add ? addPropertySetter : removePropertySetter,
                add ? addSuccessMessageProvider : removeSuccessMessageProvider);
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }


    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        List<String> result = Registry.GAME_EVENT.stream().map(GameEvent::getKey).map(NamespacedKey::asMinimalString)
                .collect(Collectors.toList());
        return ChatAndTextUtil.polishTabCompleteList(result, args.getNext(""));
    }

    @Override
    public String getUsage() {
        return "<GameEvent>";
    }
}
