package com.runningduk.unirun.api.message;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonMessage<T> {     // 프론트에 보낼 메시지 포맷
    private String type;
    private T payload;
}
