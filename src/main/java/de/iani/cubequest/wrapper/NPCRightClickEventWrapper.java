package de.iani.cubequest.wrapper;

import net.citizensnpcs.api.event.NPCRightClickEvent;

public class NPCRightClickEventWrapper {
    
    private NPCRightClickEvent original;
    
    public NPCRightClickEventWrapper(NPCRightClickEvent original) {
        this.original = original;
    }
    
    public NPCRightClickEvent getOriginal() {
        return original;
    }
    
}
