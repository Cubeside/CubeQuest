package de.iani.cubequest.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.quests.GotoQuest;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;

public class SetGotoLocationCommand extends SubCommand {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String alias, String commandString,
            ArgsParser args) {

        Quest quest = CubeQuest.getInstance().getQuestEditor().getEditingQuest(sender);
        if (quest == null) {
            ChatAndTextUtil.sendWarningMessage(sender, "Du bearbeitest derzeit keine Quest!");
            return true;
        }

        if (!(quest instanceof GotoQuest)) {
            ChatAndTextUtil.sendWarningMessage(sender, "Diese Quest erfordert keinen Ort.");
            return true;
        }
        Location location = null;
        if (args.remaining() < 4) {
            if (args.hasNext()) {
                ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Welt und die x-, y- und z-Koordinate des Orts an.");
                return true;
            }
            if (sender instanceof Player) {
                location = ((Player) sender).getLocation();
            } else {
                ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die Welt und die x-, y- und z-Koordinate des Orts an.");
                return true;
            }
        } else {
            String worldString = args.getNext();
            World world = Bukkit.getWorld(worldString);
            if (world == null) {
                ChatAndTextUtil.sendWarningMessage(sender, "Welt " + worldString + " nicht gefunden.");
                return true;
            }
            int x, y, z;
            try {
                x = Integer.parseInt(args.getNext());
                y = Integer.parseInt(args.getNext());
                z = Integer.parseInt(args.getNext());
            } catch (NumberFormatException e) {
                ChatAndTextUtil.sendWarningMessage(sender, "Bitte gib die x- y- und z-Koordinate des Orts als ganze Zahlen an.");
                return true;
            }
            location = new Location(world, x, y, z);
        }

        ((GotoQuest) quest).setLocation(location);
        ChatAndTextUtil.sendNormalMessage(sender, "Ort gesetzt.");
        return true;
    }

    @Override
    public String getRequiredPermission() {
        return CubeQuest.EDIT_QUESTS_PERMISSION;
    }

}
