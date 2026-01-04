package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.InteractorQuest;
import de.iani.cubesideutils.ComponentUtilAdventure;
import de.iani.cubesideutils.commands.ArgsParser;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SetInteractorQuestConfirmationMessageCommand extends AssistedSubCommand {

    public static final String COMMAND_PATH = "setQuestConfirmationMessage";
    public static final String FULL_COMMAND = "quest " + COMMAND_PATH;

    private static ParameterDefiner[] argumentDefiners;
    private static Function<Object[], String> propertySetter;
    private static Function<Object[], Object> successMessageProvider;

    static {
        argumentDefiners = new ParameterDefiner[] {new ParameterDefiner(ParameterType.CURRENTLY_EDITED_QUEST, "Quest",
                parsed -> (!(parsed[1] instanceof InteractorQuest) ? "Nur InteractorQuests haben diese Eigenschaft!"
                        : null)),
                new OtherParameterDefiner<>(true, "InteractionConfirmationMessage", (sender, parsed) -> {
                    try {
                        return ComponentUtilAdventure.deserializeComponent(parsed);
                    } catch (ParseException e) {
                        throw new IllegalCommandArgumentException(
                                "Ungültige Nachricht: " + e.getMessage() + " (Index " + e.getErrorOffset() + ")");
                    }
                }, parsed -> null, null)};

        propertySetter = parsed -> {
            ((InteractorQuest) parsed[1]).setConfirmationMessage((Component) parsed[2]);
            return null;
        };

        successMessageProvider = parsed -> {
            if (parsed[2] != null) {
                return Component.textOfChildren(
                        Component.text("InteractionConfirmationMessage für Quest "
                                + ((InteractorQuest) parsed[1]).getId() + " auf \""),
                        (Component) parsed[2], Component.text("\" gesetzt."));
            } else {
                return "InteractionConfirmationMessage für Quest " + ((InteractorQuest) parsed[1]).getId()
                        + " zurückgesetzt.";
            }
        };
    }

    public SetInteractorQuestConfirmationMessageCommand() {
        super(FULL_COMMAND, ACCEPTING_SENDER_CONSTRAINT, argumentDefiners, propertySetter, successMessageProvider);
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        return Collections.emptyList();
    }

    @Override
    public String getUsage() {
        return "<Bestätigungstext>";
    }

}
