package de.iani.cubequest.interaction;

import net.md_5.bungee.api.chat.BaseComponent;

public interface InteractorProtecting {

    public Interactor getInteractor();

    public boolean onInteractorDamagedEvent(InteractorDamagedEvent<?> event);

    public void onCacheChanged();

    public BaseComponent[] getProtectingInfo();
}
