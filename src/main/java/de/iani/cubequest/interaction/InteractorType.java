package de.iani.cubequest.interaction;

import de.iani.cubequest.util.BlockLocation;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public enum InteractorType {

    NPC(NPCInteractor.class, UUID.class),
    ENTITY(EntityInteractor.class, UUID.class),
    BLOCK(BlockInteractor.class, BlockLocation.class);

    private static final Map<Class<? extends Interactor>, InteractorType> byClass;

    static {
        byClass = new HashMap<>();

        for (InteractorType type : values()) {
            byClass.put(type.concreteClass, type);
        }
    }

    public final Class<? extends Interactor> concreteClass;
    public final Class<?> idenfierClass;

    @SuppressWarnings("unchecked")
    public static InteractorType fromClass(Class<? extends Interactor> from) {
        InteractorType result = byClass.get(from);
        while (result == null && from != Interactor.class) {
            from = (Class<? extends Interactor>) from.getSuperclass();
            result = byClass.get(from);
        }

        return result;
    }

    public static InteractorType fromString(String from) {
        from = from.toUpperCase();
        try {
            return valueOf(from);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private InteractorType(Class<? extends Interactor> concreteClass, Class<?> identifierClass) {
        this.concreteClass = concreteClass;
        this.idenfierClass = identifierClass;
    }

}
