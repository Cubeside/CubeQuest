package de.iani.cubequest.commands;

import java.util.function.Function;
import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.ComplexQuest;

public class SetOnDeleteCascadeCommand extends AssistedSubCommand {
    
    private static ParameterDefiner[] argumentDefiners;
    private static Function<Object[], String> propertySetter;
    private static Function<Object[], String> successMessageProvider;
    
    static {
        argumentDefiners = new ParameterDefiner[] {
                new ParameterDefiner(ParameterType.CURRENTLY_EDITED_QUEST, "Quest",
                        parsed -> (!(parsed[1] instanceof ComplexQuest)
                                ? "Nur ComplexQuests haben diese Eigenschaft!"
                                : null)),
                new ParameterDefiner(ParameterType.BOOLEAN, "OnDeleteCascade", parsed -> null)};
        
        propertySetter = parsed -> {
            ((ComplexQuest) parsed[1]).setOnDeleteCascade((Boolean) parsed[2]);
            return null;
        };
        
        successMessageProvider = parsed -> "OnDeleteCascade für Quest "
                + ((ComplexQuest) parsed[1]).getId() + " auf " + parsed[2] + " gesetzt.";
    }
    
    public SetOnDeleteCascadeCommand() {
        super("quest setOnDeleteCascade", ACCEPTING_SENDER_CONSTRAINT, argumentDefiners,
                propertySetter, successMessageProvider);
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
    
}
