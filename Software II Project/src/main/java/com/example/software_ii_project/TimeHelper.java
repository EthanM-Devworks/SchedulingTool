package com.example.software_ii_project;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeHelper {

    /**
     * Formats a given time so that it works when submitted to SQL server.
     * @param time
     * @return ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
     */
    public static String formatTime(String time) {
        Timestamp currentTimeStamp = Timestamp.valueOf(String.valueOf(time));
        LocalDateTime ldt = currentTimeStamp.toLocalDateTime();
        return ldt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

}
