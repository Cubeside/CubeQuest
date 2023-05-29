package de.iani.cubequest.generation;

import de.iani.cubequest.CubeQuest;
import de.iani.cubequest.QuestManager;
import de.iani.cubequest.quests.Quest;
import de.iani.cubequest.util.ChatAndTextUtil;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.apache.commons.lang.time.DateUtils;

public class DailyQuestData {

    private int id;
    private Quest[] quests;
    private List<Quest> immutableQuestList;
    private String dateString;
    private Date nextDayDate;

    public DailyQuestData(int id, int numOfQuests) {
        this.id = id;
        this.quests = new Quest[numOfQuests];
        this.immutableQuestList = Collections.unmodifiableList(Arrays.asList(this.quests));
        Calendar today = DateUtils.truncate(Calendar.getInstance(), Calendar.DATE);
        this.dateString = (new SimpleDateFormat(ChatAndTextUtil.DATE_FORMAT_STRING)).format(today.getTime());
        today.add(Calendar.DATE, 1);
        this.nextDayDate = today.getTime();

        try {
            saveToDatabase();
        } catch (SQLException e) {
            throw new RuntimeException("Could not save DailyQuestData to database.", e);
        }
    }

    public DailyQuestData(int id, List<Integer> questIds, String dateString, Date nextDayDate) {
        this.id = id;
        this.quests = new Quest[questIds.size()];
        this.immutableQuestList = Collections.unmodifiableList(Arrays.asList(this.quests));
        for (int i = 0; i < this.quests.length; i++) {
            this.quests[i] = questIds.get(i) == null ? null : QuestManager.getInstance().getQuest(questIds.get(i));
        }
        this.dateString = dateString;
        this.nextDayDate = nextDayDate;
    }

    public int getId() {
        return this.id;
    }

    public String getDateString() {
        return this.dateString;
    }

    public Date getNextDayDate() {
        return this.nextDayDate;
    }

    void setQuest(int ordinal, Quest quest) {
        this.quests[ordinal] = quest;
    }

    public List<Quest> getQuests() {
        return this.immutableQuestList;
    }

    public List<Quest> getNonNullQuests() {
        return getQuests().stream().filter(Objects::nonNull).toList();
    }

    public void saveToDatabase() throws SQLException {
        CubeQuest.getInstance().getDatabaseFassade().updateDailyQuestData(this);
    }

    @Override
    public String toString() {
        return "DailyQuestData[id:" + this.id + ", dateString:\"" + this.dateString + "\" nextDayDate:"
                + this.nextDayDate + " quests:" + Arrays.stream(this.quests)
                        .map(q -> q == null ? "null" : ("" + q.getId())).collect(Collectors.joining(",", "[", "]"))
                + "]";
    }

}
