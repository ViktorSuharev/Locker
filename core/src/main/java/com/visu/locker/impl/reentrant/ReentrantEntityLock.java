package com.visu.locker.impl.reentrant;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class ReentrantEntityLock extends ReentrantLock {
    private final List<Thread> threadCandidates = new CopyOnWriteArrayList<>();

    public Thread getOwner() {
        return super.getOwner();
    }

    @Override
    public void lock() {
        super.lock();
        threadCandidates.remove(Thread.currentThread());
    }

    public void addThreadCandidate() {
        threadCandidates.add(Thread.currentThread());
    }

    public boolean hasCandidate(Thread thread) {
        return threadCandidates
                .stream()
                .anyMatch(candidate -> candidate.getId() == thread.getId());
    }

    public boolean hasCandidates() {
        return threadCandidates.size() > 0;
    }
}
