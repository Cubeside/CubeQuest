package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.ComponentUtilAdventure;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class SetQuestNameCommand extends SubCommand {

    public static final String INTERNAL_COMMAND_PATH = "setInternalName";
    public static final String FULL_INTERNAL_COMMAND = "quest " + INTERNAL_COMMAND_PATH;

    public static final String DISPLAY_COMMAND_PATH = "setDisplayName";
    public static final String FULL_DISPLAY_COMMAND = "quest " + DISPLAY_COMMAND_PATH;

    public static final String REMOVE_DISPLAY_COMMAND_PATH = "removeDisplayName";
    public static final String FULL_REMOVE_DISPLAY_COMMAND = "quest " + REMOVE_DISPLAY_COMMAND_PATH;

    private boolean internalName;
    private boolean set;

    public SetQuestNameCommand(boolean internalName, boolean set) {
        if (internalName && !set) {
            throw new IllegalArgumentException("Can only remove display name.");
        }

        this.internalName = internalName;
        this.set = set;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            ChatAndTextUtil.sendNotEditingQuestMessage(sender);
            return true;
        }

        String nameString = this.set ? args.getAll("") : null;
        Component nameComponent = null;
        if (!this.internalName) {
            try {
                nameComponent = ComponentUtilAdventure.deserializeComponent(nameString);
            } catch (ParseException e) {
                ChatAndTextUtil.sendWarningMessage(sender, "Ungültiger Name: ", e.getMessage());
                return true;
            }
        }

        if (this.internalName) {
            quest.setInternalName(nameString);
        } else {
            quest.setDisplayName(nameComponent);
        }
        ChatAndTextUtil.sendNormalMessage(sender,
                Component.text(quest.getTypeName() + " [" + quest.getId() + "] heißt jetzt "
                        + (this.internalName ? "(intern)" : "(angezeigt)") + " \""),
                nameString == null ? Component.text("NULL", NamedTextColor.GOLD) : Component.text(nameString),
                Component.text("\"."));
        return true;
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
        return this.set ? "<Name>" : "";
    }

}
