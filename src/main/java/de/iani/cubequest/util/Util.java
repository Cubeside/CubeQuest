package de.iani.cubequest.util;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.logging.Level;

import org.bukkit.entity.EntityType;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.quests.ComplexQuest;
import de.iani.cubequest.quests.ComplexQuest.Structure;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.quests.WaitForDateQuest;
import net.md_5.bungee.api.ChatColor;

public class Util {

    @SuppressWarnings("deprecation")
    public static EntityType matchEntityType(String from) {
        from = from.replaceAll("\\,", "");
        from = from.toUpperCase(Locale.ENGLISH);

        try {
            return EntityType.valueOf(from);
        } catch (IllegalArgumentException e) {
            // ignored
        }
        EntityType res = EntityType.fromName(from);
        if (res != null) {
            return res;
        }
        try {
            return EntityType.fromId(Integer.parseInt(from));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static <T> T randomElement(List<T> list, Random ran) {
        if (list.isEmpty()) {
            throw new NoSuchElementException();
        }

        return list.get(ran.nextInt(list.size()));
    }

    public static Quest addTimeLimit(Quest targetQuest, Date deadline) {
        WaitForDateQuest timeoutQuest = CubeQuest.getInstance().getQuestCreator().createQuest(WaitForDateQuest.class);
        timeoutQuest.setDate(deadline);
        timeoutQuest.setReady(true);

        try {
            int dailyQuestId = CubeQuest.getInstance().getDatabaseFassade().reserveNewQuest();
            Quest result = new ComplexQuest(dailyQuestId, targetQuest.getName(), targetQuest.getDisplayMessage(),
                    null, null, // Messages
                    CubeQuest.PLUGIN_TAG + " " + ChatColor.RED + "Die Zeit für deine Quest \"" + targetQuest.getName() + "\" ist leider abgelaufen.",
                    null, null, // Rewards
                    Structure.ALLTOBEDONE,
                    new HashSet<>(Arrays.asList(targetQuest)),
                    timeoutQuest, null);
            QuestManager.getInstance().addQuest(result);
            result.setReady(true);
            return result;
        } catch (SQLException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE, "Could not add deadline to quest.", e);
            return null;
        }
    }

}
