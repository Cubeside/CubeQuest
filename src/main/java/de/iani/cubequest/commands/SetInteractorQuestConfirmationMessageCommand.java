package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.InteractorQuest;
import de.iani.cubesideutils.ComponentUtilAdventure;
import de.iani.cubesideutils.StringUtil;
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
    private static Function<Object[], String> successMessageProvider;

    static {
        argumentDefiners = new ParameterDefiner[] {new ParameterDefiner(ParameterType.CURRENTLY_EDITED_QUEST, "Quest",
                parsed -> (!(parsed[1] instanceof InteractorQuest) ? "Nur InteractorQuests haben diese Eigenschaft!"
                        : null)),
                new ParameterDefiner(ParameterType.STRING, "InteractionConfirmationMessage", parsed -> null, null)};

        propertySetter = parsed -> {
            Component message;
            try {
                message = ComponentUtilAdventure.deserializeComponent((String) parsed[2]);
            } catch (ParseException e) {
                return "Ungültige Nachricht: " + e.getMessage();
            }
            ((InteractorQuest) parsed[1]).setConfirmationMessage(message);
            return null;
        };

        successMessageProvider =
                parsed -> "InteractionConfirmationMessage für Quest " + ((InteractorQuest) parsed[1]).getId()
                        + (parsed[2] != null ? " auf \"" + StringUtil.convertColors((String) parsed[2]) + "\" gesetzt."
                                : " zurückgesetzt.");
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
