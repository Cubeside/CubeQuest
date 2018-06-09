package de.iani.cubequest.conditions;


public enum ConditionType {
    
    MINIMUM_QUEST_LEVEL(MinimumQuestLevelCondition.class),
    HAVE_QUEST_STATUS(HaveQuestStatusCondition.class),
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
        
        if (l.startsWith("min") && l.contains("level")) {
            return MINIMUM_QUEST_LEVEL;
        }
        if (l.contains("status")) {
            return HAVE_QUEST_STATUS;
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
