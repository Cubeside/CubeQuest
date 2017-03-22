package de.iani.cubequest.commands;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import de.iani.cubequest.CubeQuest;

public class TabCompleter implements org.bukkit.command.TabCompleter {

    private CubeQuest plugin;

    public TabCompleter(CubeQuest plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> result = new ArrayList<String>();

        //TODO

        return result;
    }

}
