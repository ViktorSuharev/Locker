package com.visu.locker.impl.reentrant;

import com.visu.locker.api.EntityLocker;
import com.visu.locker.impl.EntityLockerFactory;
import com.visu.locker.impl.deadlock.DeadlockPreventionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

class ReentrantEntityLockerTest {
    private final EntityLocker<String> locker = EntityLockerFactory.createReentrantEntityLocker();

    @Test
    void testLockDifferentEntities() throws Exception {
        locker.lock("1");
        Thread thread = new Thread(() -> {
            try {
                locker.lock("2");
            } catch (DeadlockPreventionException e) {
                throw new RuntimeException(e);
            } finally {
                locker.unlock("2");
            }
        });
        thread.start();

        thread.join();
        locker.unlock("1");
    }

    @Test
    void testTryLockGlobally_stopWaitingAfterTimeout() throws Exception {
        locker.lockGlobally();
        Thread thread = new Thread(() -> {
            try {
                boolean isLocked = locker.tryLockGlobally(1, TimeUnit.MILLISECONDS);
                Assertions.assertFalse(isLocked);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();

        thread.join();
        locker.unlockGlobally();
    }

    @Test
    void testLock_entityOnly() throws Exception {
        locker.lock("1");
        Assertions.assertTrue(locker.isLocked("1"));

        Assertions.assertFalse(locker.isLocked("2"));
        Assertions.assertFalse(locker.isLockedGlobally());
    }

    @Test
    void testLock_serialReentrant() throws Exception {
        locker.lock("1");
        Assertions.assertTrue(locker.isLocked("1"));
        locker.lock("1");
        Assertions.assertTrue(locker.isLocked("1"));

        locker.unlock("1");
        Assertions.assertTrue(locker.isLocked("1"));

        locker.unlock("1");
        Assertions.assertFalse(locker.isLocked("1"));
    }

    @Test
    void testTryLock_stopWaitingAfterTimeout() throws Exception {
        locker.lock("1");
        Thread thread = new Thread(() -> {
            try {
                boolean isLocked = locker.tryLock("1", 1, TimeUnit.MILLISECONDS);
                Assertions.assertFalse(isLocked);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        thread.start();

        thread.join();
        locker.unlock("1");
    }
}
