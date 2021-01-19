package com.plumelog.core.lang;

import java.util.concurrent.atomic.AtomicBoolean;

public class PlumeShutdownHook extends Thread {

    private static final PlumeShutdownHook PLUME_SHUTDOWN_HOOK = new PlumeShutdownHook("PlumeShutdownHook");

    /**
     * Has it already been registered or not?
     */
    private final AtomicBoolean registered = new AtomicBoolean(false);

    /**
     * Has it already been destroyed or not?
     */
    private static final AtomicBoolean destroyed = new AtomicBoolean(false);


    private PlumeShutdownHook(String name) {
        super(name);
    }

    public static PlumeShutdownHook getPlumeShutdownHook() {
        return PLUME_SHUTDOWN_HOOK;
    }

    @Override
    public void run() {
        callback();
    }

    private void callback() {
        if (destroyed.compareAndSet(false, true)) {
            ShutdownHookCallbacks.INSTANCE.callback();
        }
    }

    public static void destroyAll() {
        new Thread(() -> {
            if (destroyed.compareAndSet(false, true)) {
                ShutdownHookCallbacks.INSTANCE.callback();
            }
        }).start();
    }

    /**
     * Register the ShutdownHook
     */
    public void register() {
        if (registered.compareAndSet(false, true)) {
            PlumeShutdownHook plumeShutdownHook = getPlumeShutdownHook();
            Runtime.getRuntime().addShutdownHook(plumeShutdownHook);
        }
    }


}
