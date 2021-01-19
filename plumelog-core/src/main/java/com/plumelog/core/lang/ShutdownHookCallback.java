package com.plumelog.core.lang;

/**
 *
 */
public interface ShutdownHookCallback  extends Prioritized {

    void execute();
}
