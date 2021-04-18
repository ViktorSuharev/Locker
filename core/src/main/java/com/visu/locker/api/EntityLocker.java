package com.visu.locker.api;

import com.visu.locker.impl.deadlock.DeadlockPreventionException;

import java.util.concurrent.TimeUnit;

/**
 * An interface defines API and provides synchronization mechanism similar to row-level DB locking. </br>
 * The class is supposed to be used by the components that are responsible for managing storage </br>
 * and caching of different type of entities in the application. </br>
 * EntityLocker itself does not deal with the entities, only with the IDs (primary keys) of the entities. </br>
 *
 * @param <T> is an arbitrary reference type
 */
public interface EntityLocker<T> {

    void runWithinLockGlobally(Runnable runnable);

    void lockGlobally();

    boolean tryLockGlobally(long timeout, TimeUnit unit) throws InterruptedException;

    void unlockGlobally();

    void runWithinLock(T entityId, Runnable runnable) throws DeadlockPreventionException;

    void lock(T entityId) throws DeadlockPreventionException;

    boolean tryLock(T entityId, long timeout, TimeUnit unit) throws InterruptedException;

    void unlock(T entityId);

    // monitoring
    boolean isLocked(T entityId);

    boolean isLockedGlobally();
}
