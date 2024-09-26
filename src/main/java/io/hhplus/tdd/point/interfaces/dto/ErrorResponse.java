package io.hhplus.tdd.point.interfaces.dto;

public record ErrorResponse (
        String code,
        String message
) {
}
