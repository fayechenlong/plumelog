package com.plumelog.core.lang;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static java.util.Collections.sort;

/**
 *
 */
public class ShutdownHookCallbacks {

    public static final ShutdownHookCallbacks INSTANCE = new ShutdownHookCallbacks();

    private final List<ShutdownHookCallback> callbacks = new LinkedList<>();

    ShutdownHookCallbacks() {
    }

    public ShutdownHookCallbacks addCallback(ShutdownHookCallback callback) {
        synchronized (this) {
            this.callbacks.add(callback);
        }
        return this;
    }

    public Collection<ShutdownHookCallback> getCallbacks() {
        synchronized (this) {
            sort(this.callbacks);
            return this.callbacks;
        }
    }

    public void clear() {
        synchronized (this) {
            callbacks.clear();
        }
    }

    public void callback() {
        getCallbacks().forEach(callback -> callback.execute());
    }
}