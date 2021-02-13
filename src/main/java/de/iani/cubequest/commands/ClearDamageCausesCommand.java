package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.TakeDamageQuest;
import java.util.function.Function;

public class ClearDamageCausesCommand extends AssistedSubCommand {
    
    public static final String COMMAND_PATH = "clearDamageCauses";
    public static final String FULL_COMMAND = "quest " + COMMAND_PATH;
    
    private static ParameterDefiner[] parameterDefiners;
    private static Function<Object[], String> propertySetter;
    private static Function<Object[], String> successMessageProvider;
    
    static {
        parameterDefiners = new ParameterDefiner[] {new ParameterDefiner(ParameterType.CURRENTLY_EDITED_QUEST, "Quest",
                parsed -> (parsed[1] instanceof TakeDamageQuest) ? null : "Diese Quest erfordert keinen DamageCause.")};
        
        propertySetter = parsed -> {
            ((TakeDamageQuest) parsed[1]).clearCauses();
            return null;
        };
        successMessageProvider = parsed -> "DamageCauses für Quest " + parsed[1] + " gecleart.";
    }
    
    public ClearDamageCausesCommand() throws IllegalArgumentException {
        super(COMMAND_PATH, ACCEPTING_SENDER_CONSTRAINT, parameterDefiners, propertySetter, successMessageProvider);
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
}
