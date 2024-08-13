package com.runningduk.unirun.api.request;

import com.runningduk.unirun.domain.entity.RunningSchedule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Getter
@ToString
public class RunningSchedulePostReq {
    @NotBlank(message = "Type is required")
    private String type;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Creew is required")
    private String crew;

    @NotBlank(message = "Date is required")
    @Pattern(regexp = "^\\d{4}\\.\\d{2}\\.\\d{2}$", message = "Date must be in yyyy.MM.dd format")
    private String date;

    @NotBlank(message = "Start time is required")
    @Pattern(regexp = "^\\d{2}:\\d{2}$", message = "Start time must be in hh:mm format")
    private String startTime;

    @NotBlank(message = "End time is required")
    @Pattern(regexp = "^\\d{2}:\\d{2}$", message = "End time must be in hh:mm format")
    private String endTime;

    @NotBlank(message = "Place is required")
    private String place;

    @NotBlank(message = "Audience type is required")
    private String audienceType;

    public RunningSchedule toEntity() {
        String formattedDate = date.replace(".", "-");

        String formattedStartTime = startTime + ":00";
        String formattedEndTime = endTime + ":00";

        RunningSchedule runningSchedule = RunningSchedule.builder()
                .type(type)
                .title(title)
                .runningCrew(crew)
                .runningDate(java.sql.Date.valueOf(formattedDate))
                .startTime(java.sql.Time.valueOf(formattedStartTime))
                .endTime(java.sql.Time.valueOf(formattedEndTime))
                .place(place)
                .audienceType(audienceType)
                .build();

        return runningSchedule;
    }

    public void validateDateAndTime() {
        LocalDate todayDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy.MM.dd"));
        if (todayDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("The date must be today or a future date.");
        }

        LocalTime start = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm"));
        LocalTime end = LocalTime.parse(endTime, DateTimeFormatter.ofPattern("HH:mm"));
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("Start time must be before end time.");
        }
    }
}
