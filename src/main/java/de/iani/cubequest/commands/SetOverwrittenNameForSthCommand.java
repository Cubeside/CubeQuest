package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.CommandQuest;
import de.iani.cubequest.quests.GotoQuest;
import de.iani.cubequest.quests.IncreaseStatisticQuest;
import de.iani.cubequest.quests.InteractorQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubesideutils.ComponentUtilAdventure;
import de.iani.cubesideutils.commands.ArgsParser;
import de.iani.cubesideutils.partialfunctions.PartialBiFunction;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SetOverwrittenNameForSthCommand extends AssistedSubCommand {

    public enum SpecificSth {

        STATE_MESSAGE("StateMessage", Quest.class, "setOverwrittenStateMessage"),
        STATISTIC("StatisticDescription", IncreaseStatisticQuest.class, "setStatisticsMessage"),
        INTERACTOR("InteractorName", InteractorQuest.class, "setInteractorName"),
        LOCATION("LocationName", GotoQuest.class, "setLocationName"),
        COMMAND("CommandName", CommandQuest.class, "setCommandName");

        public final String propertyName;
        public final String setCommandPath;
        public final String fullSetCommand;
        public final String resetCommandPath;
        public final String fullResetCommmand;
        public final Class<? extends Quest> questClass;
        public final Method setterMethod;

        private SpecificSth(String propertyName, Class<? extends Quest> questClass, String setterMethodName) {
            this.propertyName = propertyName;
            this.setCommandPath = "setQuest" + propertyName;
            this.fullSetCommand = "quest " + this.setCommandPath;
            this.resetCommandPath = "resetQuest" + propertyName;
            this.fullResetCommmand = "quest " + this.resetCommandPath;
            this.questClass = questClass;
            try {
                this.setterMethod = questClass.getMethod(setterMethodName, Component.class);
            } catch (NoSuchMethodException | SecurityException e) {
                throw new AssertionError(e);
            }
        }
    }

    private SpecificSth sth;

    private static ParameterDefiner[] getParameterDefiners(SpecificSth sth, boolean set) {
        ParameterDefiner[] result = new ParameterDefiner[set ? 2 : 1];
        result[0] = new ParameterDefiner(ParameterType.CURRENTLY_EDITED_QUEST, "Quest",
                parsed -> (!sth.questClass.isInstance(parsed[1])
                        ? "Nur " + sth.questClass.getSimpleName() + "s haben diese Eigenschaft!"
                        : null));
        if (set) {
            PartialBiFunction<CommandSender, String, Component, IllegalCommandArgumentException> parser =
                    (sender, nameString) -> {
                        try {
                            return ComponentUtilAdventure.deserializeComponent(nameString);
                        } catch (ParseException e) {
                            throw new IllegalCommandArgumentException(
                                    e.getMessage() + " (Index " + e.getErrorOffset() + ")");
                        }
                    };
            result[1] = sth == SpecificSth.STATE_MESSAGE
                    ? new OtherParameterDefiner<>(true, "Name", parser, parsed -> null, Component.empty())
                    : new OtherParameterDefiner<>(true, "Name", parser, parsed -> null);
        }

        return result;

    }

    private static Function<Object[], String> getPropertySetter(SpecificSth sth, boolean set) {
        return parsed -> {
            try {
                sth.setterMethod.invoke(parsed[1], set ? (Component) parsed[2] : null);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            return null;
        };
    }

    private static Function<Object[], Object> getSuccessMessageProvider(SpecificSth sth, boolean set) {
        return parsed -> {
            if (set) {
                return Component.textOfChildren(
                        Component.text(sth.propertyName + " für Quest " + ((Quest) parsed[1]).getId() + " auf "),
                        (Component) parsed[2], Component.text(" gesetzt."));
            } else {
                return sth.propertyName + " für Quest " + ((Quest) parsed[1]).getId() + " zurückgesetzt.";
            }
        };
    }

    public SetOverwrittenNameForSthCommand(SpecificSth sth, boolean set) {
        super("quest " + sth.setCommandPath, AssistedSubCommand.ACCEPTING_SENDER_CONSTRAINT,
                getParameterDefiners(sth, set), getPropertySetter(sth, set), getSuccessMessageProvider(sth, set));
        this.sth = sth;
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
        return "<" + this.sth.propertyName + ">";
    }

}
