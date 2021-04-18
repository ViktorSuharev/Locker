package com.visu.locker.impl.deadlock;

import java.util.List;

public class DeadlockPreventionException extends Exception {
    public DeadlockPreventionException(List<DeadlockInfo> deadLocks) {
        super("Deadlocks found: " + deadLocks);
    }
}
