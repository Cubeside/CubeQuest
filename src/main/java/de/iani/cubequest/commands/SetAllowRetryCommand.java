package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.quests.Quest.RetryOption;
import java.util.function.Function;

public class SetAllowRetryCommand extends AssistedSubCommand {
    
    private static ParameterDefiner[] argumentDefiners(boolean success) {
        return new ParameterDefiner[] {
                new ParameterDefiner(ParameterType.CURRENTLY_EDITED_QUEST, "Quest", parsed -> null),
                new EnumParameterDefiner<>(RetryOption.class,
                        "AllowRetryOn" + (success ? "Success" : "Fail"), parsed -> null)};
    }
    
    private static Function<Object[], String> propertySetter(boolean success) {
        return parsed -> {
            if (success) {
                ((Quest) parsed[1]).setAllowRetryOnSuccess((RetryOption) parsed[2]);
            } else {
                ((Quest) parsed[1]).setAllowRetryOnFail((RetryOption) parsed[2]);
            }
            return null;
        };
    }
    
    private static Function<Object[], String> successMessageProvider(boolean success) {
        return parsed -> "AllowRetryOn " + (success ? "Success" : "Fail") + " für Quest "
                + ((Quest) parsed[1]).getId() + " auf " + parsed[2] + " gesetzt.";
    }
    
    public SetAllowRetryCommand(boolean success) {
        super("quest setAllowRetryOn" + (success ? "Success" : "Fail"), ACCEPTING_SENDER_CONSTRAINT,
                argumentDefiners(success), propertySetter(success),
                successMessageProvider(success));
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
    
}
