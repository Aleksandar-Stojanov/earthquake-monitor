package org.example.earthquakeapp.service;

import org.example.earthquakeapp.dto.*;
import org.example.earthquakeapp.entity.Earthquake;
import org.example.earthquakeapp.exception.UsgsApiException;
import org.example.earthquakeapp.exception.EarthquakeNotFoundException;
import org.example.earthquakeapp.repository.EarthquakeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EarthquakeServiceTest {

    @Mock
    private EarthquakeRepository repository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private EarthquakeService service;

    @InjectMocks
    private GeminiService serviceGemini;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "usgsUrl", "https://earthquake.usgs.gov/earthquakes/feed/v1.0/summary/all_hour.geojson");
    }

    @Test
    void refreshEarthquakes_successfullySavesEarthquakes() {
        GeoJsonResponse mockResponse = buildMockGeoJsonResponse(3.5);
        when(restTemplate.getForObject(anyString(), eq(GeoJsonResponse.class)))
                .thenReturn(mockResponse);

        service.refreshEarthquakes();

        verify(repository).deleteAll();
        verify(repository).saveAll(anyList());
    }

    @Test
    void refreshEarthquakes_throwsUsgsApiException_whenResponseIsNull() {
        when(restTemplate.getForObject(anyString(), eq(GeoJsonResponse.class)))
                .thenReturn(null);

        assertThrows(UsgsApiException.class, () -> service.refreshEarthquakes());
    }

    @Test
    void refreshEarthquakes_throwsUsgsApiException_whenApiFails() {
        when(restTemplate.getForObject(anyString(), eq(GeoJsonResponse.class)))
                .thenThrow(new RuntimeException("Connection refused"));

        assertThrows(UsgsApiException.class, () -> service.refreshEarthquakes());
    }

    @Test
    void refreshEarthquakes_skipsFeatures_withNullProperties() {
        GeoJsonResponse mockResponse = new GeoJsonResponse();
        Feature badFeature = new Feature();
        badFeature.setId("bad1");
        badFeature.setProperties(null);
        mockResponse.setFeatures(List.of(badFeature));

        when(restTemplate.getForObject(anyString(), eq(GeoJsonResponse.class)))
                .thenReturn(mockResponse);

        service.refreshEarthquakes();

        verify(repository).deleteAll();
        verify(repository, never()).saveAll(anyList());
    }

    @Test
    void refreshEarthquakes_skipsFeatures_withNullMagnitude() {
        GeoJsonResponse mockResponse = buildMockGeoJsonResponse(null);

        when(restTemplate.getForObject(anyString(), eq(GeoJsonResponse.class)))
                .thenReturn(mockResponse);

        service.refreshEarthquakes();

        verify(repository).deleteAll();
        verify(repository, never()).saveAll(anyList());
    }

    @Test
    void deleteById_throwsEarthquakeNotFoundException_whenNotExists() {
        when(repository.existsById("missing")).thenReturn(false);

        assertThrows(EarthquakeNotFoundException.class, () -> service.deleteById("missing"));
    }

    @Test
    void getFilteredEarthquakes_filtersByMinMag() {
        Earthquake eq1 = new Earthquake("1", 1.5, null, null, null, 1000L, null, null, null, null, null, null);
        Earthquake eq2 = new Earthquake("2", 3.0, null, null, null, 2000L, null, null, null, null, null, null);

        when(repository.findByMagGreaterThanEqual(2.0))
                .thenReturn(List.of(eq2));

        List<EarthquakeResponse> result = service.getFilteredEarthquakes(2.0, null);

        assertEquals(1, result.size());
        assertEquals(3.0, result.get(0).getMag());
    }

    @Test
    void shouldBuildPromptCorrectly() {
        String question = "What is the strongest earthquake?";
        List<Earthquake> mockData = List.of(
                new Earthquake("1", 4.5, null, "Japan", null, 123L, null, null, null, null, null, null)
        );

        when(repository.findAll()).thenReturn(mockData);

        String prompt = serviceGemini.buildPrompt(question);

        assertTrue(prompt.contains("Japan"));
        assertTrue(prompt.contains(question));
        assertTrue(prompt.contains("LAST 1 HOUR"));
    }

    @Test
    void shouldHandleEmptyEarthquakeData() {
        when(repository.findAll()).thenReturn(List.of());

        String prompt = serviceGemini.buildPrompt("test");

        assertTrue(prompt.contains("EARTHQUAKE DATA"));
    }


    private GeoJsonResponse buildMockGeoJsonResponse(Double mag) {
        Properties props = new Properties();
        props.setMag(mag);
        props.setMagType("ml");
        props.setPlace("Test Place");
        props.setTitle("M Test");
        props.setTime(1700000000000L);
        props.setSig(100);
        props.setTsunami(0);
        props.setStatus("automatic");

        Geometry geometry = new Geometry();
        geometry.setCoordinates(List.of(-120.0, 35.0, 10.0));

        Feature feature = new Feature();
        feature.setId("eq1");
        feature.setProperties(props);
        feature.setGeometry(geometry);

        GeoJsonResponse response = new GeoJsonResponse();
        response.setFeatures(List.of(feature));
        return response;
    }
}