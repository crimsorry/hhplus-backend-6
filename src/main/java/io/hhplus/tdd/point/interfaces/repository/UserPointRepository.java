package io.hhplus.tdd.point.interfaces.repository;

import io.hhplus.tdd.point.entities.UserPoint;

public interface UserPointRepository {
    UserPoint findById(long userId);
    UserPoint insertOrUpdate(long userId, long amount);
}
