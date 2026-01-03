package de.iani.cubequest.commands;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.util.ChatAndTextUtil;
import de.iani.cubesideutils.bukkit.commands.SubCommand;
import de.iani.cubesideutils.commands.ArgsParser;
import java.util.Collections;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;


public class VersionCommand extends SubCommand {

    public static final String COMMAND_PATH = "version";
    public static final String FULL_COMMAND = "quest " + COMMAND_PATH;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

        ChatAndTextUtil.sendNormalMessage(sender,
                ChatColor.GREEN + "--- " + ChatColor.BLUE + "[CubeQuest]" + ChatColor.GREEN + " ---");
        ChatAndTextUtil.sendNormalMessage(sender, "Version " + CubeQuest.getInstance().getPluginMeta().getVersion());
        ChatAndTextUtil.sendNormalMessage(sender, "Entwickelt von Starjon");
        ChatAndTextUtil.sendNormalMessage(sender, "Exklusiv auf Cubeside ;)");

        return true;
    }

    @Override
    public String getRequiredPermission() {
        return null;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, ArgsParser args) {
        return Collections.emptyList();
    }

}
