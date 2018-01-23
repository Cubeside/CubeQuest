package de.iani.cubequest.commands;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;

public abstract class AssistedSubCommand extends SubCommand {
    
    public enum ArgumentType {
        ANY_INTEGER,
        AT_LEAST_ZERO_INTEGER,
        POSITIVE_INTEGER,
        ANY_DOUBLE,
        AT_LEAST_ZERO_DOUBLE,
        POSITIVE_DOUBLE,
        STRING,
        BOOLEAN,
        QUEST,
        CURRENTLY_EDITED_QUEST(false),
        /* CURRENTLY_EDITED_QUEST_AS_DEFAULT */;
        
        public final boolean needsArgument;
        
        private ArgumentType() {
            this(true);
        }
        
        private ArgumentType(boolean needsArgument) {
            this.needsArgument = needsArgument;
        }
    }
    
    public static class ArgumentDefiner {
        
        private ArgumentType type;
        private String name;
        private Function<Object[], String> constraint;
        
        public ArgumentDefiner(ArgumentType type, String name,
                Function<Object[], String> constraint) {
            super();
            
            this.type = type;
            this.name = name;
            this.constraint = constraint;
        }
        
    }
    
    private static class IllegalParameterException extends Exception {
        
        private static final long serialVersionUID = 1L;
        
        public IllegalParameterException(String message) {
            super(message);
        }
        
    }
    
    private static final String[] trueStrings = new String[] {"true", "t", "on", "ja", "j", "1"};
    private static final String[] falseStrings =
            new String[] {"false", "f", "off", "nein", "n", "0"};
    
    private String command;
    private ArgumentDefiner[] argumentDefiners;
    private Consumer<Object[]> propertySetter;
    private Function<Object[], String> successMessageProvider;
    
    public AssistedSubCommand(String command, ArgumentDefiner[] argumentDefiners,
            Consumer<Object[]> propertySetter, Function<Object[], String> successMessageProvider) {
        super();
        
        this.command = command.startsWith("/") ? command.substring(1) : command;
        this.argumentDefiners = Arrays.copyOf(argumentDefiners, argumentDefiners.length);
        this.propertySetter = propertySetter;
        this.successMessageProvider = successMessageProvider;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args) {
        
        Object[] parsedArgs = new Object[this.argumentDefiners.length];
        
        for (int currentArgIndex =
                0; currentArgIndex < this.argumentDefiners.length; currentArgIndex++) {
            
            ArgumentDefiner argDefiner = this.argumentDefiners[currentArgIndex];
            try {
                if (argDefiner.type.needsArgument) {
                    if (!args.hasNext()) {
                        ChatAndTextUtil.sendWarningMessage(sender,
                                "Bitte gib den Parameter \"" + argDefiner.name + "\" an.");
                        return true;
                    }
                    
                    parsedArgs[currentArgIndex] =
                            parseArgument(sender, currentArgIndex, parsedArgs, args);
                } else {
                    parsedArgs[currentArgIndex] =
                            parseArgument(sender, currentArgIndex, parsedArgs, null);
                }
            } catch (IllegalParameterException e) {
                ChatAndTextUtil.sendWarningMessage(sender, e.getMessage());
                return true;
                
            }
            
            if (parsedArgs[currentArgIndex] == null) {
                return true;
            }
            
            String errorMsg =
                    argDefiner.constraint.apply(Arrays.copyOf(parsedArgs, currentArgIndex + 1));
            if (errorMsg != null) {
                ChatAndTextUtil.sendWarningMessage(sender, errorMsg);
                return true;
            }
        }
        
        this.propertySetter.accept(parsedArgs);
        ChatAndTextUtil.sendNormalMessage(sender, this.successMessageProvider.apply(parsedArgs));
        return true;
    }
    
    private Object parseArgument(CommandSender sender, int currentArgIndex, Object[] parsedArgs,
            ArgsParser args) throws IllegalParameterException {
        switch (this.argumentDefiners[currentArgIndex].type) {
            case ANY_INTEGER:
                return parseNumber(true, null, currentArgIndex, args.next());
            case AT_LEAST_ZERO_INTEGER:
                return parseNumber(true, false, currentArgIndex, args.next());
            case POSITIVE_INTEGER:
                return parseNumber(true, true, currentArgIndex, args.next());
            case ANY_DOUBLE:
                return parseNumber(false, null, currentArgIndex, args.next());
            case AT_LEAST_ZERO_DOUBLE:
                return parseNumber(false, false, currentArgIndex, args.next());
            case POSITIVE_DOUBLE:
                return parseNumber(false, true, currentArgIndex, args.next());
            case STRING:
                return args.next();
            case BOOLEAN:
                return parseBoolean(currentArgIndex, args.next());
            case QUEST:
                return parseQuest(sender, currentArgIndex, parsedArgs, args);
            case CURRENTLY_EDITED_QUEST:
                return getCurrentlyEditedQuest(false, sender, currentArgIndex, parsedArgs, args);
            /*
             * case CURRENTLY_EDITED_QUEST_AS_DEFAULT: return getCurrentlyEditedQuest(true, sender,
             * currentArgIndex, parsedArgs, args);
             */
            default:
                throw new IllegalArgumentException("ArgumentType is null.");
                
        }
    }
    
    private Boolean parseBoolean(int currentArgIndex, String arg) throws IllegalParameterException {
        arg = arg.toLowerCase();
        for (String other: trueStrings) {
            if (arg.equals(other)) {
                return true;
            }
        }
        for (String other: falseStrings) {
            if (arg.equals(other)) {
                return false;
            }
        }
        
        throw new IllegalParameterException(
                "Bitte gib für den Parameter \"" + this.argumentDefiners[currentArgIndex].name
                        + "\" einen der Werte \"true\" oder \"false\" an.");
    }
    
    private Number parseNumber(boolean integer, Boolean strict, int currentArgIndex, String arg)
            throws IllegalParameterException {
        try {
            Number result = integer ? Integer.parseInt(arg) : Double.parseDouble(arg);
            if (strict != null
                    && (strict ? !(result.doubleValue() > 0) : !(result.doubleValue() >= 0))) {
                throw new NumberFormatException();
            }
            
            return result;
        } catch (NumberFormatException e) {
            throw new IllegalParameterException("Bitte gib für den Parameter + \""
                    + this.argumentDefiners[currentArgIndex].name + "\" eine "
                    + (strict != null ? strict ? "echt positive " : "nicht negative " : "")
                    + (integer ? "Ganzzahl" : "Kommazahl") + " an.");
        }
    }
    
    private Quest parseQuest(CommandSender sender, int currentArgIndex, Object[] parsedArgs,
            ArgsParser args) {
        String commandOnSelectionByClickingPreId = "/" + this.command;
        for (int i = 0; i < currentArgIndex; i++) {
            commandOnSelectionByClickingPreId +=
                    " " + toArgString(this.argumentDefiners[i], parsedArgs[i]);
        }
        commandOnSelectionByClickingPreId += " ";
        
        String hoverTextPreId = "Quest ";
        String hoverTextPostId =
                " als Parameter \"" + this.argumentDefiners[currentArgIndex].name + "\" auswählen.";
        
        return ChatAndTextUtil.getQuest(sender, args, commandOnSelectionByClickingPreId, "",
                hoverTextPreId, hoverTextPostId);
    }
    
    private Quest getCurrentlyEditedQuest(boolean onlyAsDefault, CommandSender sender,
            int currentArgIndex, Object[] parsedArgs, ArgsParser args)
            throws IllegalParameterException {
        Quest result = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (result == null) {
            if (onlyAsDefault) {
                return parseQuest(sender, currentArgIndex, parsedArgs, args);
            } else {
                throw new IllegalParameterException(
                        "Für diesen Befehl musst du eine Quest bearbeiten.");
            }
        }
        
        return result;
    }
    
    private String toArgString(ArgumentDefiner argDefiner, Object arg) {
        if (argDefiner.type == ArgumentType.QUEST) {
            return "" + ((Quest) arg).getId();
        }
        
        return arg.toString();
    }
    
}
