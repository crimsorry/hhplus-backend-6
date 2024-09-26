package io.hhplus.tdd.point.usecase;

import io.hhplus.tdd.point.entities.PointHistory;
import io.hhplus.tdd.point.entities.UserPoint;

import java.util.List;

public interface PointService {
    UserPoint chargePoints(long userId, long amount);
    UserPoint getPoints(long userId);
    UserPoint usePoints(long userId, long amount);
    List<PointHistory> chargeUsePoints(long userId);
}
