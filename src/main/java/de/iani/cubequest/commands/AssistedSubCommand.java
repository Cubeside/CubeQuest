package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Function;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * Class to provide a framework for common SubCommands.
 * <p>
 * Handles the parsing of different argument/parameter types. Parameter constraints and the action
 * to be performed when they are met are specified in the constructor.
 * 
 * @author Jonas Becker
 *
 */
public class AssistedSubCommand extends SubCommand {
    
    /**
     * A type of parameter.
     * 
     * @author Jonas Becker
     *
     */
    public enum ParameterType {
        
        /**
         * Any integer.
         */
        ANY_INTEGER,
        
        /**
         * A non-negative integer.
         */
        AT_LEAST_ZERO_INTEGER,
        
        /**
         * A strictly positive integer.
         */
        POSITIVE_INTEGER,
        
        /**
         * Any floating point number.
         */
        ANY_DOUBLE,
        
        /**
         * A non-negative floating point number.
         */
        AT_LEAST_ZERO_DOUBLE,
        
        /**
         * A strictly positive floating point number.
         */
        POSITIVE_DOUBLE,
        
        /**
         * A single word of characters (no spaces).
         */
        WORD,
        
        /**
         * Any string of characters (must be the last argument).
         */
        STRING,
        
        /**
         * A boolean value.
         */
        BOOLEAN,
        
        /**
         * A UUID.
         */
        UUID,
        
        /**
         * A single quest, specified by name or id.
         */
        QUEST,
        
        /**
         * The quest currently edited by the command sender.
         */
        CURRENTLY_EDITED_QUEST(false),
        
        /**
         * The quest currently edited by the command sender, if he currently edits a quest. A single
         * quest, specified by name or id, otherwise
         */
        CURRENTLY_EDITED_QUEST_AS_DEFAULT(false, CURRENTLY_EDITED_QUEST);
        
        private final boolean needsArgument;
        private final ParameterType ifNoDefault;
        
        private ParameterType() {
            this(true);
        }
        
        private ParameterType(boolean needsArgument) {
            this(needsArgument, null);
        }
        
        private ParameterType(boolean needsArgument, ParameterType ifNoDefault) {
            this.needsArgument = needsArgument;
            this.ifNoDefault = ifNoDefault;
        }
    }
    
    /**
     * Defines a specific parameter.
     * <p>
     * A parameter is definde by it's type, it's name and the constraint it shall respect.
     * 
     * @author Jonas Becker
     *
     */
    public static class ParameterDefiner {
        
        private ParameterType type;
        private String name;
        private Function<Object[], String> constraint;
        
        /**
         * Creates a new ParameterDefiner.
         * <p>
         * After the parameter's value is successfully optained, it's constraint is invoked. The
         * passed array contains the command sender followed by all values optained until now in the
         * order their parameters were defined in. If the new value fullfills the constraint, it
         * shall return {@code null}. Otherwise, it shall return the error message to be send to the
         * command sender.
         * 
         * @param type this parameters ParameterType
         * @param name this parameters name
         * @param constraint the condition this parameter shall fullfill
         */
        public ParameterDefiner(ParameterType type, String name,
                Function<Object[], String> constraint) {
            super();
            
            this.type = type;
            this.name = name;
            this.constraint = constraint;
        }
        
        @Override
        public String toString() {
            return getClass().getSimpleName() + ": " + this.type + " \"" + this.name + "\"";
        }
        
    }
    
    /**
     * Signals an illegal command argument.
     * <p>
     * Differs from an IllegalArgumentException in the way that it's checked and should never leave
     * this class.
     * 
     * @author Jonas Becker
     *
     */
    private static class IllegalCommandArgumentException extends Exception {
        
        private static final long serialVersionUID = 1L;
        
        public IllegalCommandArgumentException(String message) {
            super(message);
        }
        
    }
    
    /**
     * Signals that a parameters default-value cannot be optained.
     * 
     * @author Jonas Becker
     *
     */
    private class NoDefaultException extends Exception {
        
        private static final long serialVersionUID = 1L;
        
    }
    
    /**
     * A sender constraint acception all command senders.
     */
    public static final Function<CommandSender, String> ACCEPTING_SENDER_CONSTRAINT =
            (parsed) -> null;
    
    /**
     * An argument constraint acception all arguments
     */
    public static final Function<Object[], String> ACCEPTING_ARGUMENT_CONSTRAINT = (parsed) -> null;
    
    private static final String[] trueStrings = new String[] {"true", "t", "on", "ja", "j", "1"};
    private static final String[] falseStrings =
            new String[] {"false", "f", "off", "nein", "n", "0"};
    
    private String command;
    private Function<CommandSender, String> senderConstraint;
    private ParameterDefiner[] parameterDefiners;
    private Function<Object[], String> propertySetter;
    private Function<Object[], String> successMessageProvider;
    
    /**
     * Constructor.
     * <p>
     * On command, first the senderConstraint is invoked. If it returns a non-null String, that
     * String is reported to the sender as an error message. Otherwise, the execution continues.
     * Then, values are optained for all parameters specified in the constructor in order of their
     * specification. Once a value is optained, it's constraint is invoked. See
     * {@link ParameterDefiner#ParameterDefiner(ParameterType, String, Function)} for more details
     * about that.
     * <p>
     * After all values have been optained successfully, the propertySetter is invoked. The passed
     * array contains the command sender followed by all optained values in the order their
     * parameters were defined in. If the propertySetter returns a non-null String, that String is
     * reported to the sender as an error message. Otherwise, the successMEssageProvider is invoked
     * with the same arguments and it's result is reported to the sender as a success message.
     * <p>
     * No exceptions thrown by the specified functions are caught by this class.
     * 
     * @param command The command string leading to this command. A leading slash may be dropped.
     * @param senderConstraint the condition the command sender shall fullfill
     * @param parameterDefiners the ParameterDefiners to define this command's parameters
     * @param propertySetter the consumer to invoke once the values for all parameters have been
     *        successfully optained
     * @param successMessageProvider the function to generate a success message after successfull
     *        execution of the command
     * 
     * @throws IllegalArgumentException if any argument is null
     */
    public AssistedSubCommand(String command, Function<CommandSender, String> senderConstraint,
            ParameterDefiner[] parameterDefiners, Function<Object[], String> propertySetter,
            Function<Object[], String> successMessageProvider) throws IllegalArgumentException {
        super();
        
        if (command == null) {
            throw new IllegalArgumentException("command is null");
        }
        if (senderConstraint == null) {
            throw new IllegalArgumentException("senderConstraint is null");
        }
        if (parameterDefiners == null) {
            throw new IllegalArgumentException("parameterDefiners is null");
        }
        if (propertySetter == null) {
            throw new IllegalArgumentException("propertySetter is null");
        }
        if (successMessageProvider == null) {
            throw new IllegalArgumentException("successMessageProvider is null");
        }
        
        for (int i = 0; i < parameterDefiners.length; i++) {
            if (parameterDefiners[i].type == ParameterType.STRING
                    && i != parameterDefiners.length - 1) {
                throw new IllegalArgumentException(
                        "ParameterType STRING must be the last parameter.");
            }
        }
        
        this.command = command.startsWith("/") ? command.substring(1) : command;
        this.senderConstraint = senderConstraint;
        this.parameterDefiners = Arrays.copyOf(parameterDefiners, parameterDefiners.length);
        this.propertySetter = propertySetter;
        this.successMessageProvider = successMessageProvider;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args) {
        
        String errorMsg = this.senderConstraint.apply(sender);
        if (errorMsg != null) {
            ChatAndTextUtil.sendWarningMessage(sender, errorMsg);
            return true;
        }
        
        Object[] parsedArgs = new Object[this.parameterDefiners.length + 1];
        parsedArgs[0] = sender;
        
        for (int currentArgIndex =
                0; currentArgIndex < this.parameterDefiners.length; currentArgIndex++) {
            
            try {
                parsedArgs[currentArgIndex + 1] =
                        getNextParameter(sender, currentArgIndex, parsedArgs, args);
            } catch (IllegalCommandArgumentException e) {
                ChatAndTextUtil.sendWarningMessage(sender, e.getMessage());
                return true;
            }
            
            if (parsedArgs[currentArgIndex + 1] == null) {
                return true;
            }
            
            errorMsg = this.parameterDefiners[currentArgIndex].constraint
                    .apply(Arrays.copyOf(parsedArgs, currentArgIndex + 2));
            if (errorMsg != null) {
                ChatAndTextUtil.sendWarningMessage(sender, errorMsg);
                return true;
            }
            
        }
        
        errorMsg = this.propertySetter.apply(parsedArgs);
        if (errorMsg != null) {
            ChatAndTextUtil.sendWarningMessage(sender, errorMsg);
            return true;
        }
        
        String successMessage = this.successMessageProvider.apply(parsedArgs);
        if (successMessage != null) {
            ChatAndTextUtil.sendNormalMessage(sender, successMessage);
        }
        return true;
    }
    
    private Object getNextParameter(CommandSender sender, int currentArgIndex, Object[] parsedArgs,
            ArgsParser args) throws IllegalCommandArgumentException {
        
        ParameterType expectedType = this.parameterDefiners[currentArgIndex].type;
        if (expectedType.needsArgument) {
            if (!args.hasNext()) {
                ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib den Parameter \""
                        + this.parameterDefiners[currentArgIndex].name + "\" an.");
                return true;
            }
            
            return parseArgument(sender, expectedType, currentArgIndex, parsedArgs, args);
        } else {
            try {
                return getNextDefaultParam(sender, currentArgIndex, parsedArgs, null);
            } catch (NoDefaultException e) {
                if (expectedType.ifNoDefault == null) {
                    ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib den Parameter \""
                            + this.parameterDefiners[currentArgIndex].name + "\" an.");
                    return true;
                }
                
                return parseArgument(sender, expectedType.ifNoDefault, currentArgIndex, parsedArgs,
                        args);
            }
            
        }
    }
    
    private Object parseArgument(CommandSender sender, ParameterType expectedType,
            int currentArgIndex, Object[] parsedArgs, ArgsParser args)
            throws IllegalCommandArgumentException {
        switch (expectedType) {
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
            case WORD:
                return args.next();
            case STRING:
                return args.getAll(null);
            case BOOLEAN:
                return parseBoolean(currentArgIndex, args.next());
            case UUID:
                return parseUUID(currentArgIndex, args.next());
            case QUEST:
                return parseQuest(sender, currentArgIndex, parsedArgs, args);
            case CURRENTLY_EDITED_QUEST:
                return getCurrentlyEditedQuest(sender, currentArgIndex, parsedArgs, args);
            case CURRENTLY_EDITED_QUEST_AS_DEFAULT:
                throw new IllegalArgumentException("Parameters with default are not to be parsed.");
                
            default:
                throw new IllegalArgumentException("ArgumentType is null.");
                
        }
    }
    
    private Object getNextDefaultParam(CommandSender sender, int currentArgIndex,
            Object[] parsedArgs, ArgsParser args)
            throws NoDefaultException, IllegalCommandArgumentException {
        
        switch (this.parameterDefiners[currentArgIndex].type) {
            case CURRENTLY_EDITED_QUEST:
                return getCurrentlyEditedQuest(sender, currentArgIndex, parsedArgs, args);
            case CURRENTLY_EDITED_QUEST_AS_DEFAULT:
                return getCurrentlyEditedQuestAsDefault(sender, currentArgIndex, parsedArgs, args);
            default:
                throw new NoDefaultException();
        }
    }
    
    private Number parseNumber(boolean integer, Boolean strict, int currentArgIndex, String arg)
            throws IllegalCommandArgumentException {
        try {
            Number result = integer ? Integer.parseInt(arg) : Double.parseDouble(arg);
            if (strict != null
                    && (strict ? !(result.doubleValue() > 0) : !(result.doubleValue() >= 0))) {
                throw new NumberFormatException();
            }
            
            return result;
        } catch (NumberFormatException e) {
            throw new IllegalCommandArgumentException("Bitte gib für den Parameter + \""
                    + this.parameterDefiners[currentArgIndex].name + "\" eine "
                    + (strict != null ? strict ? "echt positive " : "nicht negative " : "")
                    + (integer ? "Ganzzahl" : "Kommazahl") + " an.");
        }
    }
    
    private Boolean parseBoolean(int currentArgIndex, String arg)
            throws IllegalCommandArgumentException {
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
        
        throw new IllegalCommandArgumentException(
                "Bitte gib für den Parameter \"" + this.parameterDefiners[currentArgIndex].name
                        + "\" einen der Werte \"true\" oder \"false\" an.");
    }
    
    private UUID parseUUID(int currentArgIndex, String arg) throws IllegalCommandArgumentException {
        try {
            return UUID.fromString(arg);
        } catch (IllegalArgumentException e) {
            throw new IllegalCommandArgumentException(
                    "Bitte gib für den Parameter \"" + this.parameterDefiners[currentArgIndex].name
                            + "\" eine gültige UUID in Text-Darstellung an.");
        }
    }
    
    private Quest parseQuest(CommandSender sender, int currentArgIndex, Object[] parsedArgs,
            ArgsParser args) {
        String commandOnSelectionByClickingPreId = "/" + this.command;
        for (int i = 0; i < currentArgIndex; i++) {
            commandOnSelectionByClickingPreId +=
                    " " + toArgString(this.parameterDefiners[i], parsedArgs[i]);
        }
        commandOnSelectionByClickingPreId += " ";
        
        String hoverTextPreId = "Quest ";
        String hoverTextPostId = " als Parameter \"" + this.parameterDefiners[currentArgIndex].name
                + "\" auswählen.";
        
        return ChatAndTextUtil.getQuest(sender, args, commandOnSelectionByClickingPreId, "",
                hoverTextPreId, hoverTextPostId);
    }
    
    private Quest getCurrentlyEditedQuest(CommandSender sender, int currentArgIndex,
            Object[] parsedArgs, ArgsParser args) throws IllegalCommandArgumentException {
        Quest result = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (result == null) {
            throw new IllegalCommandArgumentException(
                    "Für diesen Befehl musst du eine Quest bearbeiten.");
        }
        
        return result;
    }
    
    private Quest getCurrentlyEditedQuestAsDefault(CommandSender sender, int currentArgIndex,
            Object[] parsedArgs, ArgsParser args) throws NoDefaultException {
        Quest result = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (result == null) {
            throw new NoDefaultException();
        }
        
        return result;
    }
    
    private String toArgString(ParameterDefiner argDefiner, Object arg) {
        if (argDefiner.type == ParameterType.QUEST) {
            return "" + ((Quest) arg).getId();
        }
        
        return arg.toString();
    }
    
}
