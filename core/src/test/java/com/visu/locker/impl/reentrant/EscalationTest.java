package com.visu.locker.impl.reentrant;

import com.visu.locker.api.EntityLocker;
import com.visu.locker.impl.EntityLockerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EscalationTest {
    private final EntityLocker<String> locker = EntityLockerFactory.createReentrantEntityLocker();

    @Test
    void testNoEscalation_reentrantLocking() throws Exception {
        locker.lock("1");
        locker.lock("1");
        locker.lock("1");

        Assertions.assertFalse(locker.isLockedGlobally());
    }

    @Test
    void testNoEscalation_notEnoughLocks() throws Exception {
        locker.lock("1");
        locker.lock("2");

        Assertions.assertFalse(locker.isLockedGlobally());
    }

    @Test
    void testNoEscalation_notEnoughLocksWithReentrant() throws Exception {
        locker.lock("1");
        locker.lock("2");
        locker.lock("1");
        locker.lock("2");

        Assertions.assertFalse(locker.isLockedGlobally());
    }

    @Test
    void testEscalation() throws Exception {
        locker.lock("1");
        locker.lock("2");
        locker.lock("3");

        Assertions.assertTrue(locker.isLockedGlobally());
    }
}
