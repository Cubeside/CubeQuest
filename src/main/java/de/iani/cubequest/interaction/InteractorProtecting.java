package de.iani.cubequest.interaction;

import net.kyori.adventure.text.Component;

public interface InteractorProtecting {

    public Interactor getInteractor();

    public boolean onInteractorDamagedEvent(InteractorDamagedEvent<?> event);

    public void onCacheChanged();

    public Component getProtectingInfo();
}
