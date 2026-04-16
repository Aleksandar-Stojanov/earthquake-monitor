package org.example.earthquakeapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Properties {
    private Double mag;
    private String magType;
    private String place;
    private String title;
    private Long time;
    private String type;
    private Integer sig;
    private Integer tsunami;
    private String status;

}