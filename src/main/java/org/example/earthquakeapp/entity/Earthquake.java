package org.example.earthquakeapp.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "earthquakes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Earthquake {

    @Id
    private String id;

    private Double mag;
    private String magType;
    private String place;
    private String title;
    private Long time;
    private Double longitude;
    private Double latitude;
    private Double depth;

    private Integer sig;
    private Integer tsunami;
    private String status;
}