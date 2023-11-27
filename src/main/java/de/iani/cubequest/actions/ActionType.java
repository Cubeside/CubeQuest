package de.iani.cubequest.actions;

public enum ActionType {

    ACTION_BAR_MESSAGE(ActionBarMessageAction.class),
    BOSS_BAR_MESSAGE(BossBarMessageAction.class),
    CHAT_MESSAGE(ChatMessageAction.class),
    REWARD(RewardAction.class),
    // QUEST_STATUS_CHANGE(null),
    REDSTONE_SIGNAL(RedstoneSignalAction.class),
    POTION_EFFECT(PotionEffectAction.class),
    REMOVE_POTION_EFFECT(RemovePotionEffectAction.class),
    PARTICLE(ParticleAction.class),
    EFFECT(EffectAction.class),
    SOUND(SoundAction.class),
    SPAWN_ENTITY(SpawnEntityAction.class),
    STOP_SOUND(StopSoundAction.class),
    TELEPORT(TeleportationAction.class),
    TITLE_MESSAGE(TitleMessageAction.class);

    public final Class<? extends QuestAction> concreteClass;

    public static ActionType match(String s) {
        String u = s.toUpperCase();

        try {
            return valueOf(u);
        } catch (IllegalArgumentException e) {
            // ignore
        }

        String l = s.toLowerCase();

        if (l.replaceAll("\\_", "").startsWith("actionbar")) {
            return ACTION_BAR_MESSAGE;
        }
        if (l.startsWith("boss")) {
            return BOSS_BAR_MESSAGE;
        }
        if (l.startsWith("title")) {
            return TITLE_MESSAGE;
        }
        if (l.contains("chat") || l.contains("message") || l.startsWith("msg")) {
            return CHAT_MESSAGE;
        }
        if (l.contains("reward")) {
            return REWARD;
        }
        // if (l.contains("state") || l.contains("status")) {
        // return QUEST_STATUS_CHANGE;
        // }
        if (l.contains("redstone") || l.contains("signal")) {
            return REDSTONE_SIGNAL;
        }
        if (l.contains("potion")) {
            if (l.contains("remove")) {
                return REMOVE_POTION_EFFECT;
            }
            return POTION_EFFECT;
        }
        if (l.contains("particle")) {
            return PARTICLE;
        }
        if (l.startsWith("effect")) {
            return EFFECT;
        }
        if (l.contains("sound")) {
            return SOUND;
        }
        if (l.contains("entity")) {
            return SPAWN_ENTITY;
        }

        return null;
    }

    private ActionType(Class<? extends QuestAction> concreteClass) {
        this.concreteClass = concreteClass;
    }

}
