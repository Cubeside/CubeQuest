package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.conditions.BeInAreaCondition;
import de.iani.cubequest.conditions.ConditionType;
import de.iani.cubequest.conditions.GameModeCondition;
import de.iani.cubequest.conditions.HaveInInventoryCondition;
import de.iani.cubequest.conditions.HaveQuestStatusCondition;
import de.iani.cubequest.conditions.MinimumQuestLevelCondition;
import de.iani.cubequest.conditions.NegatedQuestCondition;
import de.iani.cubequest.conditions.QuestCondition;
import de.iani.cubequest.conditions.RenamedCondition;
import de.iani.cubequest.conditions.ServerFlagCondition;
import de.iani.cubequest.conditions.SpecialPlayerPropertyCondition;
import de.iani.cubequest.conditions.SpecialPlayerPropertyCondition.PropertyType;
import de.iani.cubequest.conditions.TimeOfDayCondition;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.quests.ProgressableQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.SafeLocation;
import de.iani.cubesideutils.StringUtil;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.bukkit.items.ItemStacks;
import de.iani.cubesideutils.bukkit.plugin.api.InventoryInputManager.InterruptCause;
import de.iani.cubesideutils.bukkit.plugin.api.UtilsApiBukkit;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;


public class AddConditionCommand extends SubCommand {

    public static final String GIVING_COMMAND_PATH = "addGivingCondition";
    public static final String FULL_GIVING_COMMAND = "quest " + GIVING_COMMAND_PATH;

    public static final String PROGRESS_COMMAND_PATH = "addProgressCondition";
    public static final String FULL_PROGRESS_COMMAND = "quest " + PROGRESS_COMMAND_PATH;

    public static final Set<String> NEGATION_STRINGS =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList("not", "nicht")));

    private static class ConditionParseException extends RuntimeException {

        private static final long serialVersionUID = 1L;

    }

    private boolean giving;

    public AddConditionCommand(boolean giving) {
        this.giving = giving;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            ChatAndTextUtil.sendNotEditingQuestMessage(sender);
            return true;
        }

        if (!this.giving && !(quest instanceof ProgressableQuest)) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Für diese Quest können keine Fortschrittsbedingungen festgelegt werden.");
            return true;
        }

        QuestCondition cond;
        try {
            cond = parseCondition(sender, args, quest);
        } catch (ConditionParseException e) {
            return true;
        }

        if (this.giving) {
            quest.addQuestGivingCondition(cond);
        } else {
            ((ProgressableQuest) quest).addQuestProgressCondition(cond);
        }

        ChatAndTextUtil.sendNormalMessage(sender,
                (this.giving ? "Vergabe" : "Fortschritts") + "bedingung hinzugefügt:");
        ChatAndTextUtil.sendBaseComponent(sender, cond.getConditionInfo());
        return true;
    }

    @SuppressWarnings("deprecation")
    private QuestCondition parseCondition(CommandSender sender, ArgsParser args, Quest quest) {
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib einen Bedingungstyp an.");
            throw new ConditionParseException();
        }

        String typeString;
        ConditionType type = ConditionType.match(typeString = args.next());
        if (type == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bedingungstyp " + typeString + " nicht gefunden.");
            throw new ConditionParseException();
        }

        if (type == ConditionType.NEGATED) {
            return NegatedQuestCondition.negate(parseCondition(sender, args, quest));
        }
        if (type == ConditionType.RENAMED) {
            return parseRenamedCondition(sender, args, quest);
        }

        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Bitte gib an, ob die Bedingung für Spieler sichtbar sein soll.");
            throw new ConditionParseException();
        }

        boolean visible;
        String visibleString = args.next();
        if (AssistedSubCommand.TRUE_STRINGS.contains(visibleString.toLowerCase())) {
            visible = true;
        } else if (AssistedSubCommand.FALSE_STRINGS.contains(visibleString.toLowerCase())) {
            visible = false;
        } else {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Bitte gib an, ob die Bedingung für Spieler sichtbar sein soll (true/false).");
            throw new ConditionParseException();
        }

        if (type == ConditionType.GAMEMODE) {
            if (!args.hasNext()) {
                ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib den GameMode an, den die Bedingung haben soll.");
                throw new ConditionParseException();
            }

            String gmString = args.seeNext("");
            int gmId = args.getNext(-1);
            GameMode gm = null;

            if (gmId != -1) {
                gm = GameMode.getByValue(gmId);
            } else {
                try {
                    gm = GameMode.valueOf(gmString.toUpperCase());
                } catch (IllegalArgumentException e) {
                    // ignore
                }
            }

            if (gm == null) {
                ChatAndTextUtil.sendWarningMessage(sender, "GameMode " + gmString + " nicht gefunden.");
                throw new ConditionParseException();
            }

            return new GameModeCondition(visible, gm);
        }

        if (type == ConditionType.MINIMUM_QUEST_LEVEL) {
            int minLevel = args.getNext(-1);
            if (minLevel < 0) {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Bitte gib das minimale Quest-Level als nicht-negative Ganzzahl an.");
                throw new ConditionParseException();
            }

            return new MinimumQuestLevelCondition(visible, minLevel);
        }

        if (type == ConditionType.HAVE_QUEST_STATUS) {
            if (!args.hasNext()) {
                ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib den Status an, den die Bedingung haben soll.");
                throw new ConditionParseException();
            }

            String statusString = args.next();
            Status status = Status.match(statusString);

            if (status == null) {
                ChatAndTextUtil.sendWarningMessage(sender, "Status " + statusString + " nicht gefunden.");
                throw new ConditionParseException();
            }

            String preIdCommand =
                    (this.giving ? AddConditionCommand.FULL_GIVING_COMMAND : AddConditionCommand.FULL_PROGRESS_COMMAND)
                            + " " + type.name() + " " + visible + " " + status.name() + " ";
            Quest other = ChatAndTextUtil.getQuest(sender, args, preIdCommand, "", "Quest ", " für Bedingung wählen");

            if (other == null) {
                throw new ConditionParseException();
            }

            return new HaveQuestStatusCondition(visible, other, status);
        }

        if (type == ConditionType.SERVER_FLAG) {
            if (!args.hasNext()) {
                ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Flag an, die der Server haben soll.");
                throw new ConditionParseException();
            }

            String flag = args.next().toLowerCase();

            return new ServerFlagCondition(visible, flag);
        }

        if (type == ConditionType.BE_IN_AREA) {
            double tolerance = args.getNext(-1.0);
            if (tolerance < 0.0) {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Bitte gib die Toleranz als nicht-negative Kommazahl (mit . statt ,) an.");
                throw new ConditionParseException();
            }

            SafeLocation loc = ChatAndTextUtil.getSafeLocation(sender, args, true, false);
            if (loc == null) {
                throw new ConditionParseException();
            }

            return new BeInAreaCondition(visible, loc, tolerance);
        }

        if (type == ConditionType.HAVE_IN_INVENTORY) {
            if (!(sender instanceof Player player)) {
                ChatAndTextUtil.sendErrorMessage(sender, "Dieser Befehl kann nur von Spielern ausgeführt werden.");
                throw new ConditionParseException();
            }

            Consumer<ItemStack[]> callback = items -> {
                if (ItemStacks.isEmpty(items)) {
                    ChatAndTextUtil.sendWarningMessage(sender, "Hinzufügen abgebrochen.");
                    return;
                }

                QuestCondition condition = new HaveInInventoryCondition(visible, items);
                if (this.giving) {
                    quest.addQuestGivingCondition(condition);
                } else {
                    ((ProgressableQuest) quest).addQuestProgressCondition(condition);
                }
                ChatAndTextUtil.sendNormalMessage(sender,
                        (this.giving ? "Vergabe" : "Fortschritts") + "bedingung hinzugefügt:");
                ChatAndTextUtil.sendBaseComponent(sender, condition.getConditionInfo());
            };

            Consumer<InterruptCause> interruptHAndler = cause -> {
                ChatAndTextUtil.sendWarningMessage(sender, "Hinzufügen abgebrochen.");
            };

            ItemStack[] defaultItems = new ItemStack[27];

            UtilsApiBukkit.getInstance().getInventoryInputManager().requestInventoryInput(player, callback,
                    interruptHAndler, defaultItems);
            throw new ConditionParseException();
        }

        if (type == ConditionType.PLAYER_PROPERTY) {
            String propertyTypeString = args.getNext("");
            PropertyType propertyType;
            try {
                propertyType = PropertyType.valueOf(propertyTypeString);
            } catch (IllegalArgumentException e) {
                ChatAndTextUtil.sendErrorMessage(sender, "Bitte gib die Eigenschaft an, die überprüft werden soll.");
                throw new ConditionParseException();
            }

            return new SpecialPlayerPropertyCondition(visible, propertyType);
        }

        if (type == ConditionType.TIME_OF_DAY) {
            int min = args.getNext(-1);
            int max = args.getNext(-1);
            if (min < 0 || min > max || max > 24000) {
                ChatAndTextUtil.sendErrorMessage(sender,
                        "Bitte gib minimale und maximale Tageszeit als Ganzzahlen zwischen 0 und 24000 an.");
                throw new ConditionParseException();
            }

            return new TimeOfDayCondition(visible, min, max);
        }

        throw new AssertionError("Unknown ConditionType " + type + "!");
    }

    private QuestCondition parseRenamedCondition(CommandSender sender, ArgsParser args, Quest quest) {
        int originalIndex = args.getNext(0) - 1;

        if (originalIndex < 0) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Bitte gib den Index der Original-Bedingung als positive Ganzzahl an.");
            throw new ConditionParseException();
        }

        QuestCondition original;
        try {
            original = this.giving ? quest.getQuestGivingConditions().get(originalIndex)
                    : ((ProgressableQuest) quest).getQuestProgressConditions().get(originalIndex);
        } catch (IndexOutOfBoundsException e) {
            ChatAndTextUtil.sendWarningMessage(sender, "Eine Bedingung mit diesem Index hat die Quest nicht.");
            throw new ConditionParseException();
        }

        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Bezeichnung der neuen Bedingung an.");
            throw new ConditionParseException();
        }

        String rawText = args.getAll("");
        String text = StringUtil.convertColors(rawText);

        if (rawText.equals("RESET")) {
            text = "";
        } else if (!rawText.startsWith("&")) {
            text = ChatColor.DARK_AQUA + text;
        }

        QuestCondition result = RenamedCondition.rename(text, original);
        if (this.giving) {
            quest.removeQuestGivingCondition(originalIndex);
        } else {
            ((ProgressableQuest) quest).removeQuestProgressCondition(originalIndex);
        }

        return result;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        if (!args.hasNext()) {
            return Collections.emptyList();
        }

        String conditionTypeString = args.getNext();
        if (!args.hasNext()) {
            List<String> list =
                    Arrays.stream(ConditionType.values()).map(ConditionType::name).collect(Collectors.toList());
            list.addAll(Arrays.asList("NOT", "NICHT", "RENAME", "GM", "LEVEL", "STATUS", "STATE", "FLAG", "AREA",
                    "PROPERTY", "TIME"));
            return list;
        }

        ConditionType conditionType = ConditionType.match(conditionTypeString);
        if (conditionType == null || conditionType == ConditionType.RENAMED) {
            return Collections.emptyList();
        }
        if (conditionType == ConditionType.NEGATED) {
            return onTabComplete(sender, command, alias, args);
        }

        args.next();
        if (!args.hasNext()) {
            return Arrays.asList("TRUE", "FALSE");
        }

        args.next();
        switch (conditionType) {
            case GAMEMODE:
                return Arrays.stream(GameMode.values()).map(GameMode::name).collect(Collectors.toList());
            case BE_IN_AREA:
            case MINIMUM_QUEST_LEVEL:
            case HAVE_IN_INVENTORY:
            case TIME_OF_DAY:
                return Collections.emptyList();
            case SERVER_FLAG:
                return new ArrayList<>(CubeQuest.getInstance().getServerFlags());
            case HAVE_QUEST_STATUS:
                if (!args.hasNext()) {
                    List<String> list = Arrays.stream(Status.values()).map(Status::name).collect(Collectors.toList());
                    list.addAll(Arrays.asList("NOTGIVEN", "GIVEN")); // aliases
                    return list;
                }
                break;
            case PLAYER_PROPERTY:
                return Arrays.stream(PropertyType.values()).map(PropertyType::name).collect(Collectors.toList());
            default:
                throw new AssertionError("unexpected conditionType " + conditionType);
        }

        return Collections.emptyList();
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }

}
