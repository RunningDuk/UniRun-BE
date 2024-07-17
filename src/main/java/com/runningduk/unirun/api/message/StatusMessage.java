package com.runningduk.unirun.api.message;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusMessage {
    private boolean start;
    private boolean pause;
    private boolean restart;
    private boolean finish;
}
