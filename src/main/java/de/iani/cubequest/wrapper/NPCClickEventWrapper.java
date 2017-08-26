package de.iani.cubequest.wrapper;

import net.citizensnpcs.api.event.NPCClickEvent;

public class NPCClickEventWrapper {

    private NPCClickEvent original;

    public NPCClickEventWrapper(NPCClickEvent original) {
        this.original = original;
    }

    public NPCClickEvent getOriginal() {
        return original;
    }

}
