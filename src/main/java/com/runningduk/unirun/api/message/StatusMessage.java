package com.runningduk.unirun.api.message;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusMessage {    // 프론트에서 받는 러닝 상태 정보
    private boolean start;
    private boolean pause;
    private boolean restart;
    private boolean finish;
}
