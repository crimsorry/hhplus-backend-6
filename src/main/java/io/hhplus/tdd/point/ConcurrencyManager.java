package io.hhplus.tdd.point;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class ConcurrencyManager {
    private final ConcurrentHashMap<Long, Lock> userLocks = new ConcurrentHashMap<>();

    // 특정 유저 Lock 가져오거나 없으면 생성
    public Lock getUserLock(long userId) {
        return userLocks.computeIfAbsent(userId, id -> new ReentrantLock());
    }
}