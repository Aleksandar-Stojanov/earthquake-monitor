package org.example.earthquakeapp.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Feature {
    private String id;
    private Properties properties;
    private Geometry geometry;
}