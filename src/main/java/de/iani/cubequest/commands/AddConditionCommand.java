package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.conditions.BeInAreaCondition;
import de.iani.cubequest.conditions.ConditionType;
import de.iani.cubequest.conditions.GameModeCondition;
import de.iani.cubequest.conditions.HaveQuestStatusCondition;
import de.iani.cubequest.conditions.MinimumQuestLevelCondition;
import de.iani.cubequest.conditions.NegatedQuestCondition;
import de.iani.cubequest.conditions.QuestCondition;
import de.iani.cubequest.conditions.RenamedCondition;
import de.iani.cubequest.conditions.ServerFlagCondition;
import de.iani.cubequest.questStates.QuestState.Status;
import de.iani.cubequest.quests.ProgressableQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubequest.util.SafeLocation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;


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
    public boolean onCommand(CommandSender sender, Command command, String alias,
            String commandString, ArgsParser args) {
        
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
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Bedingungstyp " + typeString + " nicht gefunden.");
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
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Bitte gib den GameMode an, den die Bedingung haben soll.");
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
                ChatAndTextUtil.sendWarningMessage(sender,
                        "GameMode " + gmString + " nicht gefunden.");
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
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Bitte gib den Status an, den die Bedingung haben soll.");
                throw new ConditionParseException();
            }
            
            String statusString = args.next();
            Status status = Status.match(statusString);
            
            if (status == null) {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Status " + statusString + " nicht gefunden.");
                throw new ConditionParseException();
            }
            
            String preIdCommand = (this.giving ? AddConditionCommand.FULL_GIVING_COMMAND
                    : AddConditionCommand.FULL_PROGRESS_COMMAND) + " " + type.name() + " "
                    + status.name() + " ";
            Quest other = ChatAndTextUtil.getQuest(sender, args, preIdCommand, "", "Quest ",
                    " für Bedingung wählen");
            
            if (other == null) {
                throw new ConditionParseException();
            }
            
            return new HaveQuestStatusCondition(visible, other, status);
        }
        
        if (type == ConditionType.SERVER_FLAG) {
            if (!args.hasNext()) {
                ChatAndTextUtil.sendWarningMessage(sender,
                        "Bitte gib die Flag an, die der Server haben soll.");
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
        
        throw new AssertionError("Unknown ConditionType " + type + "!");
    }
    
    private RenamedCondition parseRenamedCondition(CommandSender sender, ArgsParser args,
            Quest quest) {
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
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Eine Bedingung mit diesem Index hat die Quest nicht.");
            throw new ConditionParseException();
        }
        
        if (!args.hasNext()) {
            ChatAndTextUtil.sendWarningMessage(sender,
                    "Bitte gib die Bezeichnung der neuen Bedingung an.");
            throw new ConditionParseException();
        }
        
        String text = ChatAndTextUtil.convertColors(args.getAll(""));
        return RenamedCondition.rename(text, original);
    }
    
    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }
    
}
