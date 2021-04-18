package com.visu.locker.impl.reentrant;

import com.visu.locker.api.EntityLocker;
import com.visu.locker.impl.deadlock.DeadlockInfo;
import com.visu.locker.impl.deadlock.DeadlockPreventionException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ReentrantEntityLocker<T> implements EntityLocker<T> {
    private final int escalationThresholdThreadNumber;

    private final GlobalLock globalLock = new GlobalLock();
    private final Map<T, ReentrantEntityLock> lockRegistry = Collections.synchronizedMap(new WeakHashMap<>());

    public ReentrantEntityLocker(int escalationThresholdThreadNumber) {
        this.escalationThresholdThreadNumber = escalationThresholdThreadNumber;
    }

    public void runWithinLockGlobally(Runnable runnable) {
        try {
            lockGlobally();
            runnable.run();
        } finally {
            unlockGlobally();
        }
    }

    public void lockGlobally() {
        globalLock.lock();
    }

    public boolean tryLockGlobally(long timeout, TimeUnit unit) throws InterruptedException {
        return globalLock.lock(timeout, unit);
    }

    public void unlockGlobally() {
        globalLock.unlock();
    }

    public void runWithinLock(T entityId, Runnable runnable) throws DeadlockPreventionException {
        try {
            lock(entityId);
            runnable.run();
        } finally {
            unlock(entityId);
        }
    }

    public void lock(T entityId) throws DeadlockPreventionException {
        ReentrantEntityLock lock = lockRegistry.computeIfAbsent(entityId, i -> new ReentrantEntityLock());

        if (isEscalationNeeded(lock)) {
            collectLocksHeldByCurrentThread()
                    .forEach(l -> {
                        globalLock.unlockSoft();
                        l.unlock();
                    });

            globalLock.lock();
        } else {
            globalLock.lockSoft();
            lock.addThreadCandidate();

            checkDeadlocks(lock);
            lock.lock();
        }
    }

    public boolean tryLock(T entityId, long timeout, TimeUnit unit) throws InterruptedException {
        globalLock.lockSoft();

        ReentrantEntityLock lock = lockRegistry.computeIfAbsent(entityId, i -> new ReentrantEntityLock());
        return lock.tryLock(timeout, unit);
    }

    public void unlock(T entityId) {
        // escalation case
        if (globalLock.isLockedByCurrentThread()) {
            globalLock.unlock();
        } else {
            ReentrantEntityLock lock = lockRegistry.get(entityId);
            if (lock != null) {
                lock.unlock();
            }
            globalLock.unlockSoft();
        }
    }

    public boolean isLockedGlobally() {
        return globalLock.isLocked();
    }

    public boolean isLocked(T entityId) {
        ReentrantEntityLock lock = lockRegistry.get(entityId);
        return lock != null && lock.isLocked();
    }

    private void checkDeadlocks(ReentrantEntityLock lock) throws DeadlockPreventionException {
        List<DeadlockInfo> deadLocks = findDeadlocks(lock);
        if (!deadLocks.isEmpty()) {
            globalLock.unlockSoft();
            throw new DeadlockPreventionException(deadLocks);
        }
    }

    /**
     * find deadlocks by condition:
     * An owner thread of a lock that is acquiring by the current thread
     * is a waiter of any locks held by the current thread
     */
    private List<DeadlockInfo> findDeadlocks(ReentrantEntityLock lock) {
        Thread lockOwner = lock.getOwner();
        if (lockOwner == null || Thread.currentThread().equals(lockOwner)) {
            return Collections.emptyList();
        }

        List<ReentrantEntityLock> locksHeldByCurrentThread = collectLocksHeldByCurrentThread();
        return locksHeldByCurrentThread
                .stream()
                .filter(l -> !l.equals(lock)) // allow reentrant locking
                .filter(l -> l.hasQueuedThread(lockOwner) || l.hasCandidate(lockOwner))
                .map(l -> new DeadlockInfo(lockOwner, lock, l))
                .collect(Collectors.toList());
    }

    private boolean isEscalationNeeded(ReentrantEntityLock lock) {
        if (lock.isHeldByCurrentThread()) {
            return false;
        }

        return collectLocksHeldByCurrentThread().size() + 1 >= escalationThresholdThreadNumber;
    }

    private List<ReentrantEntityLock> collectLocksHeldByCurrentThread() {
        return lockRegistry.values()
                .stream()
                .filter(ReentrantEntityLock::isHeldByCurrentThread)
                .collect(Collectors.toList());
    }
}
