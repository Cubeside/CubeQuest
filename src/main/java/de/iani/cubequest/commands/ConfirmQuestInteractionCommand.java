package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ConfirmQuestInteractionCommand extends AssistedSubCommand {
    
    public static final String COMMAND_PATH = "confirmQuestInteraction";
    public static final String FULL_COMMAND = "quest " + COMMAND_PATH;
    
    private static ParameterDefiner[] argumentDefiners;
    private static Function<Object[], String> propertySetter;
    private static Function<Object[], String> successMessageProvider;
    
    static {
        argumentDefiners = new ParameterDefiner[] {new ParameterDefiner(ParameterType.UUID, "Quest-Schlüssel", parsed -> null)};
        
        propertySetter = parsed -> {
            CubeQuest.getInstance().getInteractionConfirmationHandler().interactionConfirmedCommand((Player) parsed[0], (UUID) parsed[1]);
            return null;
        };
        
        successMessageProvider = parsed -> null;
    }
    
    public ConfirmQuestInteractionCommand() {
        super(FULL_COMMAND, ACCEPTING_SENDER_CONSTRAINT, argumentDefiners, propertySetter, successMessageProvider);
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.ACCEPT_QUESTS_PERMISSION;
    }
    
    @Override
    public boolean isVisible(CommandSender sender) {
        return false;
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
        return "<BestätigungsKey>";
    }
}
