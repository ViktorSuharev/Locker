package com.visu.locker.impl.reentrant;

import com.visu.locker.api.EntityLocker;
import com.visu.locker.impl.EntityLockerFactory;
import com.visu.locker.impl.deadlock.DeadlockPreventionException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

class DeadlockTest {
    private final EntityLocker<String> locker = EntityLockerFactory.createReentrantEntityLocker();

    @Test
    void testDeadlockDetected_2threads() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger deadLockCount = new AtomicInteger(0);

        assertLockAcquiringSuccess.accept("1");

        new Thread(() -> {
            assertLockAcquiringSuccess.accept("2");
            latch.countDown();
            deadLockCount.addAndGet(assertLockAcquiringLeadsToDeadlock.apply("1"));
        }).start();

        latch.await();
        deadLockCount.addAndGet(assertLockAcquiringLeadsToDeadlock.apply("2"));

        Assertions.assertTrue(deadLockCount.get() == 1 || deadLockCount.get() == 2);
    }

    private final Consumer<String> assertLockAcquiringSuccess = (entityId) -> {
        try {
            locker.lock(entityId);
        } catch (Exception e) {
            Assertions.fail("No exceptions are expected, but", e);
        }
    };

    private final Function<String, Integer> assertLockAcquiringLeadsToDeadlock = (entityId) -> {
        try {
            locker.lock(entityId);
            return 0;
        } catch (Exception e) {
            Assertions.assertTrue(e instanceof DeadlockPreventionException);
            return 1;
        }
    };
}
