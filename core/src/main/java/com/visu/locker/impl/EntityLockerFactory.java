package com.visu.locker.impl;

import com.visu.locker.api.EntityLocker;
import com.visu.locker.impl.reentrant.ReentrantEntityLocker;

public class EntityLockerFactory {
    private static final int DEFAULT_ESCALATION_THRESHOLD_THREAD_NUMBER = 3;

    public static <T> EntityLocker<T> createReentrantEntityLocker() {
        return new ReentrantEntityLocker<>(DEFAULT_ESCALATION_THRESHOLD_THREAD_NUMBER);
    }

    public static <T> EntityLocker<T> createReentrantEntityLocker(int escalationThresholdThreadNumber) {
        return new ReentrantEntityLocker<>(escalationThresholdThreadNumber);
    }
}
