package de.iani.cubequest.util;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ParameterizedConsumer<P, T> implements Consumer<T> {
    
    private P param = null;
    private BiConsumer<P, T> action;
    
    public ParameterizedConsumer(BiConsumer<P, T> action) {
        this.action = action;
    }
    
    @Override
    public void accept(T state) {
        this.action.accept(this.param, state);
    }
    
    public P getParam() {
        return this.param;
    }
    
    public void setParam(P event) {
        this.param = event;
    }
    
}
