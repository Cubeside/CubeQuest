package de.iani.cubequest.commands;

import java.util.function.Consumer;
import java.util.function.Function;
import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.ComplexQuest;

public class SetOnDeleteCascadeCommand extends AssistedSubCommand {
    
    private static ArgumentDefiner[] argumentDefiners;
    private static Consumer<Object[]> propertySetter;
    private static Function<Object[], String> successMessageProvider;
    
    static {
        argumentDefiners = new ArgumentDefiner[] {
                new ArgumentDefiner(ArgumentType.CURRENTLY_EDITED_QUEST, "Quest",
                        parsed -> (!(parsed[0] instanceof ComplexQuest)
                                ? "Nur ComplexQuests haben diese Eigenschaft!"
                                : null)),
                new ArgumentDefiner(ArgumentType.BOOLEAN, "OnDeleteCascade", parsed -> null)};
        
        propertySetter =
                parsed -> ((ComplexQuest) parsed[0]).setOnDeleteCascade((Boolean) parsed[1]);
        
        successMessageProvider = parsed -> "OnDeleteCascade für Quest "
                + ((ComplexQuest) parsed[0]).getId() + " auf " + parsed[1] + " gesetzt.";
    }
    
    public SetOnDeleteCascadeCommand() {
        super("quest setOnDeleteCascade", argumentDefiners, propertySetter, successMessageProvider);
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
    
}
