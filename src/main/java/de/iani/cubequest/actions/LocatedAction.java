package de.iani.cubequest.actions;

import java.util.Map;
import java.util.Objects;


public abstract class LocatedAction extends DelayableAction {

    private ActionLocation location;

    public LocatedAction(long delay, ActionLocation location) {
        super(delay);

        init(location);
    }

    public LocatedAction(Map<String, Object> serialized) {
        super(serialized);

        init((ActionLocation) serialized.get("location"));
    }

    private void init(ActionLocation location) {
        this.location = Objects.requireNonNull(location);
    }

    protected final boolean runIfOffline() {
        return false;
    }

    public ActionLocation getLocation() {
        return this.location;
    }

    public abstract LocatedAction relocate(ActionLocation location);

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> result = super.serialize();
        result.put("location", this.location);
        return result;
    }

}
