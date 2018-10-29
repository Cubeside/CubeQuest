package de.iani.cubequest.interaction;


public interface InteractorProtecting {
    
    public Interactor getInteractor();
    
    public boolean onInteractorDamagedEvent(InteractorDamagedEvent<?> event);
    
    public void onCacheChanged();
}
