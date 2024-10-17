package com.runningduk.unirun.api.message;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationMessage {  // 프론트에서 받는 위치 정보
    private Double latitude;
    private Double longitude;
}
