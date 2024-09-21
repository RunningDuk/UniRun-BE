package com.runningduk.unirun.common;

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;

public class DateUtils {
    public static LocalDate convertToLocalDate(Date dateToConvert) {
//        return dateToConvert.toInstant()
//                .atZone(ZoneId.systemDefault())
//                .toLocalDate();
        return dateToConvert.toLocalDate();
    }
}
