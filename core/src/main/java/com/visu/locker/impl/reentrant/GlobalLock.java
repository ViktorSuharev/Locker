package com.visu.locker.impl.reentrant;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GlobalLock {
    private final ReentrantReadWriteLock internal = new ReentrantReadWriteLock();

    public void lock() {
        internal.writeLock().lock();
    }

    public boolean lock(long timeout, TimeUnit unit) throws InterruptedException {
        return internal.writeLock().tryLock(timeout, unit);
    }

    public void unlock() {
        internal.writeLock().unlock();
    }

    public void lockSoft() {
        internal.readLock().lock();
    }

    public void unlockSoft() {
        internal.readLock().unlock();
    }

    public boolean isLockedByCurrentThread() {
        return internal.isWriteLockedByCurrentThread();
    }

    public boolean isLocked() {
        return internal.isWriteLocked();
    }
}
