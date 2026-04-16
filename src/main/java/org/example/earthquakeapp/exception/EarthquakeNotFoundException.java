package org.example.earthquakeapp.exception;

public class EarthquakeNotFoundException extends RuntimeException {
    public EarthquakeNotFoundException(String id) {
        super("Earthquake with id " + id + " not found");
    }
}