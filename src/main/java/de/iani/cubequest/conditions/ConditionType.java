package de.iani.cubequest.conditions;


public enum ConditionType {
    
    NEGATED(NegatedQuestCondition.class),
    RENAMED(RenamedCondition.class),
    GAMEMODE(GameModeCondition.class),
    MINIMUM_QUEST_LEVEL(MinimumQuestLevelCondition.class),
    HAVE_QUEST_STATUS(HaveQuestStatusCondition.class),
    SERVER_FLAG(ServerFlagCondition.class),
    BE_IN_AREA(BeInAreaCondition.class);
    
    public final Class<? extends QuestCondition> concreteClass;
    
    public static ConditionType match(String s) {
        String u = s.toUpperCase();
        String l = s.toLowerCase();
        
        try {
            return valueOf(u);
        } catch (IllegalArgumentException e) {
            // ignore
        }
        
        if (l.startsWith("not") || l.startsWith("nicht") || l.contains("negated")) {
            return NEGATED;
        }
        if (l.contains("rename")) {
            return RENAMED;
        }
        if (l.startsWith("gm")) {
            return GAMEMODE;
        }
        if (l.contains("level")) {
            return MINIMUM_QUEST_LEVEL;
        }
        if (l.contains("status") || l.contains("state")) {
            return HAVE_QUEST_STATUS;
        }
        if (l.contains("flag")) {
            return SERVER_FLAG;
        }
        if (l.contains("area")) {
            return BE_IN_AREA;
        }
        
        return null;
    }
    
    private ConditionType(Class<? extends QuestCondition> concreteClass) {
        this.concreteClass = concreteClass;
    }
    
}
