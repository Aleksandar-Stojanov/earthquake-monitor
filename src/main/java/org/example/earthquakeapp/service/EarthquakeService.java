package org.example.earthquakeapp.service;

import org.example.earthquakeapp.dto.EarthquakeResponse;
import org.example.earthquakeapp.dto.Feature;
import org.example.earthquakeapp.dto.GeoJsonResponse;
import org.example.earthquakeapp.dto.Properties;
import org.example.earthquakeapp.exception.EarthquakeNotFoundException;
import org.example.earthquakeapp.exception.UsgsApiException;
import org.example.earthquakeapp.entity.Earthquake;
import org.example.earthquakeapp.repository.EarthquakeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class EarthquakeService {

    @Value("${usgs.api.url}")
    private String usgsUrl;

    private final EarthquakeRepository repository;
    private final RestTemplate restTemplate;

    public EarthquakeService(EarthquakeRepository repository, RestTemplate restTemplate) {
        this.repository = repository;
        this.restTemplate = restTemplate;
    }

    public void refreshEarthquakes() {
        GeoJsonResponse response;

        try {
            response = restTemplate.getForObject(usgsUrl, GeoJsonResponse.class);
        } catch (Exception ex) {
            throw new UsgsApiException("Failed to connect to USGS API", ex);
        }

        if (response == null || response.getFeatures() == null) {
            throw new UsgsApiException("USGS API returned empty or invalid response");
        }

        List<Earthquake> earthquakesToSave = response.getFeatures().stream()
                .filter(f -> f.getProperties() != null)
                .filter(f -> f.getProperties().getMag() != null)
                .map(this::mapToEarthquake)
                .toList();

        repository.deleteAll();

        if (!earthquakesToSave.isEmpty()) {
            repository.saveAll(earthquakesToSave);
        }
    }
    private Earthquake mapToEarthquake(Feature f) {
        Properties p = f.getProperties();
        Earthquake eq = new Earthquake();

        eq.setId(f.getId());
        eq.setMag(p.getMag());
        eq.setMagType(p.getMagType());
        eq.setPlace(p.getPlace());
        eq.setTitle(p.getTitle());
        eq.setTime(p.getTime());
        eq.setSig(p.getSig());
        eq.setTsunami(p.getTsunami());
        eq.setStatus(p.getStatus());

        if (f.getGeometry() != null && f.getGeometry().getCoordinates() != null
                && f.getGeometry().getCoordinates().size() >= 2) {
            eq.setLongitude(f.getGeometry().getCoordinates().get(0));
            eq.setLatitude(f.getGeometry().getCoordinates().get(1));

            if (f.getGeometry().getCoordinates().size() >= 3) {
                eq.setDepth(f.getGeometry().getCoordinates().get(2));
            }
        }

        return eq;
    }
    public void deleteById(String id) {
        if (!repository.existsById(id)) {
            throw new EarthquakeNotFoundException(id);
        }
        repository.deleteById(id);
    }


    public List<EarthquakeResponse> getFilteredEarthquakes(Double minMag, Long afterTime) {
        List<Earthquake> earthquakes;

        if (minMag != null && afterTime != null) {
            earthquakes = repository.findByMagGreaterThanEqualAndTimeGreaterThan(minMag, afterTime);
        } else if (minMag != null) {
            earthquakes = repository.findByMagGreaterThanEqual(minMag);
        } else if (afterTime != null) {
            earthquakes = repository.findByTimeGreaterThan(afterTime);
        } else {
            earthquakes = repository.findAll();
        }

        return earthquakes.stream()
                .map(EarthquakeResponse::fromEntity)
                .toList();
    }

}