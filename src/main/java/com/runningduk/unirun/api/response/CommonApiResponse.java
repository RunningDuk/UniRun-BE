package com.runningduk.unirun.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Getter
@AllArgsConstructor
@Builder
public class CommonApiResponse<T> {
    private int status;
    private final String message;
    private final T data;
    private final ZonedDateTime sendTime = ZonedDateTime.now(ZoneId.of("Asia/Seoul"));

    public ResponseEntity<CommonApiResponse> toEntity(final HttpStatus httpStatus) {
        return ResponseEntity.status(httpStatus).body(this);
    }
}
