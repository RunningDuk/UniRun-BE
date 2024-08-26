package com.runningduk.unirun.common;


import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
@AllArgsConstructor(access= AccessLevel.PRIVATE)
@NoArgsConstructor
@Data
@Builder
public class CommonResVO<T> {
    private String successOrNot;
    private String statusCode;
    private String errorMessage;
    private T data;
}
