package org.example.earthquakeapp.web;

import org.example.earthquakeapp.dto.EarthquakeResponse;
import org.example.earthquakeapp.service.EarthquakeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/earthquakes")
@CrossOrigin(origins = "*")
public class EarthquakeController {


    private final EarthquakeService service;

    public EarthquakeController(EarthquakeService service) {
        this.service = service;
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refresh() {
        service.refreshEarthquakes();
        return ResponseEntity.ok("Earthquake data refreshed successfully!");
    }

    @GetMapping
    public List<EarthquakeResponse> getFiltered(
            @RequestParam(required = false) Double minMag,
            @RequestParam(required = false) Long after) {
        return service.getFilteredEarthquakes(minMag, after);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable String id) {
        service.deleteById(id);
        return ResponseEntity.ok("Earthquake deleted successfully");
    }
}