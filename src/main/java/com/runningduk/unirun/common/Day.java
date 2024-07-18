package com.runningduk.unirun.common;

import java.sql.Date;
import java.util.Calendar;

public enum Day {
    MON, TUE, WED, THU, FRI, SAT, SUN;

    public static Day fromDate(Date date) throws IllegalAccessException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        return switch (dayOfWeek) {
            case Calendar.SUNDAY -> SUN;
            case Calendar.MONDAY -> MON;
            case Calendar.TUESDAY -> TUE;
            case Calendar.WEDNESDAY -> WED;
            case Calendar.THURSDAY -> THU;
            case Calendar.FRIDAY -> FRI;
            case Calendar.SATURDAY -> SAT;
            default -> throw new IllegalAccessException("Invalid day of week: " + dayOfWeek);
        };
    }
}
