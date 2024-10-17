package io.hhplus.tdd.point.entities;

import io.hhplus.tdd.CustomPointException;

public record UserPoint(
        long id,
        long point,
        long updateMillis
) {

    public UserPoint {
        if(point < 0){
            throw new CustomPointException("포인트 금액이 음수 입니다.");
        }
    }

    public static UserPoint empty(long id) {
        return new UserPoint(id, 0, System.currentTimeMillis());
    }
}
