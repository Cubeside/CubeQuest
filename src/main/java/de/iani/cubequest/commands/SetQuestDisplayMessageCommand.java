package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.Quest;
import java.util.function.Function;

public class SetQuestDisplayMessageCommand extends AssistedSubCommand {
    
    private static ParameterDefiner[] argumentDefiners;
    private static Function<Object[], String> propertySetter;
    private static Function<Object[], String> successMessageProvider;
    
    static {
        argumentDefiners = new ParameterDefiner[] {
                new ParameterDefiner(ParameterType.CURRENTLY_EDITED_QUEST, "Quest", parsed -> null),
                new ParameterDefiner(ParameterType.STRING, "OnDeleteCascade", parsed -> null)};
        
        propertySetter = parsed -> {
            ((Quest) parsed[1]).setDisplayMessage((String) parsed[2]);
            return null;
        };
        
        successMessageProvider = parsed -> "DisplayMessage für Quest " + ((Quest) parsed[1]).getId()
                + " auf \"" + parsed[2] + "\" gesetzt.";
    }
    
    public SetQuestDisplayMessageCommand() {
        super("quest setQuestDisplayMessage", ACCEPTING_SENDER_CONSTRAINT, argumentDefiners,
                propertySetter, successMessageProvider);
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
    
}
