package com.runningduk.unirun.api.message;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocationMessage {
    private Double latitude;
    private Double longitude;
}
