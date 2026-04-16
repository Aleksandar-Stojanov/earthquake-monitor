package org.example.earthquakeapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.example.earthquakeapp.entity.Earthquake;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;


@Getter
@Setter
@AllArgsConstructor
public class EarthquakeResponse {

    private String id;
    private Double mag;
    private String magType;
    private String place;
    private String title;
    private Long time;
    private String timeFormatted;
    private Double depth;
    private Integer sig;
    private Integer tsunami;
    private String status;
    private Double latitude;
    private Double longitude;

    public static EarthquakeResponse fromEntity(Earthquake eq) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                .withZone(ZoneId.systemDefault());

        return new EarthquakeResponse(
                eq.getId(),
                eq.getMag(),
                eq.getMagType(),
                eq.getPlace(),
                eq.getTitle(),
                eq.getTime(),
                formatter.format(Instant.ofEpochMilli(eq.getTime())),
                eq.getDepth(),
                eq.getSig(),
                eq.getTsunami(),
                eq.getStatus(),
                eq.getLatitude(),
                eq.getLongitude()
        );
    }
}