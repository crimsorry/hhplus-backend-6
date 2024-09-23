package io.hhplus.tdd.point;

public interface UserPointRepository {
    UserPoint findById(long userId);
    UserPoint insertOrUpdate(long userId, long amount);
}
