package de.iani.cubequest.interaction;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.logging.Level;
import de.iani.cubequest.CubeQuest;

public class InteractorCreator {
    
    public Interactor createInteractor(InteractorType type, Object identifier) {
        Interactor result;
        try {
            result = type.concreteClass.getConstructor(type.idenfierClass).newInstance(identifier);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            CubeQuest.getInstance().getLogger().log(Level.SEVERE,
                    "Could not create new Interactor of type " + type.name() + "!", e);
            return null;
        }
        return result;
    }
    
    public Object parseIdentifier(InteractorType type, String identifierAsString) {
        if (type == null || identifierAsString == null) {
            throw new NullPointerException();
        }
        
        if (type.idenfierClass == Integer.class) {
            return Integer.parseInt(identifierAsString);
        }
        if (type.idenfierClass == UUID.class) {
            return UUID.fromString(identifierAsString);
        }
        
        throw new IllegalArgumentException("Unkown identifierClass " + type.idenfierClass);
    }
    
}
