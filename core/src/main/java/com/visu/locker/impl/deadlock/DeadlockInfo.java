package com.visu.locker.impl.deadlock;

import com.visu.locker.impl.reentrant.ReentrantEntityLock;
import lombok.Data;

@Data
public class DeadlockInfo {
    private final Thread targetThread;
    private final ReentrantEntityLock targetLock;

    private final ReentrantEntityLock heldLock;
}
