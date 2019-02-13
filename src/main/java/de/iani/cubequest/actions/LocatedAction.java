package de.iani.cubequest.actions;

import java.util.Map;
import java.util.Objects;


public abstract class LocatedAction extends QuestAction {
    
    private ActionLocation location;
    
    public LocatedAction(ActionLocation location) {
        init(location);
    }
    
    public LocatedAction(Map<String, Object> serialized) {
        init((ActionLocation) serialized.get("location"));
    }
    
    private void init(ActionLocation location) {
        this.location = Objects.requireNonNull(location);
    }
    
    public ActionLocation getLocation() {
        return this.location;
    }
    
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("location", this.location);
        return result;
    }
    
}
