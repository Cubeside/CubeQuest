package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.TakeDamageQuest;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SetTakeDamageQuestPropertyCommand extends AssistedSubCommand {
    
    public static enum TakeDamageQuestPropertyType {
        
        WHITELIST("setTreatAsWhitelist", "<true|false>",
                new ParameterDefiner[] {
                        new ParameterDefiner(ParameterType.CURRENTLY_EDITED_QUEST, "Quest",
                                parsed -> (parsed[1] instanceof TakeDamageQuest) ? null
                                        : "Diese Quest hat diese Eigenschaft nicht."),
                        new ParameterDefiner(ParameterType.BOOLEAN, "TreatAsWhitelist", parsed -> null)},
                parsed -> {
                    ((TakeDamageQuest) parsed[1]).setWhitelist((Boolean) parsed[2]);
                    return null;
                },
                parsed -> ((Boolean) parsed[2]) ? "DamageCauses werden jetzt als Whitelist interpretiert."
                        : "DamageCauses werden jetzt als Blacklist interpretiert."),
        HP("setHp", "<halbe Herzen>",
                new ParameterDefiner[] {
                        new ParameterDefiner(ParameterType.CURRENTLY_EDITED_QUEST, "Quest",
                                parsed -> (parsed[1] instanceof TakeDamageQuest) ? null
                                        : "Diese Quest hat diese Eigenschaft nicht."),
                        new ParameterDefiner(ParameterType.AT_LEAST_ZERO_DOUBLE, "HP", parsed -> null)},
                parsed -> {
                    ((TakeDamageQuest) parsed[1]).setHp((Double) parsed[2]);
                    return null;
                }, parsed -> "HP gesetzt."),
        AT_ONCE("setAtOnce", "<true|false>",
                new ParameterDefiner[] {
                        new ParameterDefiner(ParameterType.CURRENTLY_EDITED_QUEST, "Quest",
                                parsed -> (parsed[1] instanceof TakeDamageQuest) ? null
                                        : "Diese Quest hat diese Eigenschaft nicht."),
                        new ParameterDefiner(ParameterType.BOOLEAN, "AdOnce", parsed -> null)},
                parsed -> {
                    ((TakeDamageQuest) parsed[1]).setAtOnce((Boolean) parsed[2]);
                    return null;
                },
                parsed -> ((Boolean) parsed[2]) ? "Schaden muss nun auf einmal erfolgen."
                        : "Verbleibendes Leben muss jetzt soweit sinken."),
        CANCEL("setCancel", "<true|false>",
                new ParameterDefiner[] {
                        new ParameterDefiner(ParameterType.CURRENTLY_EDITED_QUEST, "Quest",
                                parsed -> (parsed[1] instanceof TakeDamageQuest) ? null
                                        : "Diese Quest hat diese Eigenschaft nicht."),
                        new ParameterDefiner(ParameterType.BOOLEAN, "Cancel", parsed -> null)},
                parsed -> {
                    ((TakeDamageQuest) parsed[1]).setCancel((Boolean) parsed[2]);
                    return null;
                }, parsed -> ((Boolean) parsed[2]) ? "Schaden wird jetzt belockiert."
                        : "Schaden wird nicht mehr blockiert.");
        
        
        
        public final String commandPath;
        public final String fullCommand;
        private String usage;
        private ParameterDefiner[] parameterDefiners;
        private Function<Object[], String> propertySetter;
        private Function<Object[], String> successMessageProvider;
        
        private TakeDamageQuestPropertyType(String commandPath, String usage, ParameterDefiner[] parameterDefiners,
                Function<Object[], String> propertySetter, Function<Object[], String> successMessageProvider) {
            this.commandPath = commandPath;
            this.fullCommand = "quest " + commandPath;
            this.usage = usage;
            this.parameterDefiners = parameterDefiners;
            this.propertySetter = propertySetter;
            this.successMessageProvider = successMessageProvider;
        }
        
    }
    
    private TakeDamageQuestPropertyType type;
    
    public SetTakeDamageQuestPropertyCommand(TakeDamageQuestPropertyType type) throws IllegalArgumentException {
        super(type.commandPath, ACCEPTING_SENDER_CONSTRAINT, type.parameterDefiners, type.propertySetter,
                type.successMessageProvider);
        this.type = type;
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
        return this.type.usage;
    }
    
}
