package org.example.earthquakeapp.repository;

import org.example.earthquakeapp.entity.Earthquake;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EarthquakeRepository extends JpaRepository<Earthquake, String> {

    List<Earthquake> findByMagGreaterThanEqualAndTimeGreaterThan(Double mag, Long time);
    List<Earthquake> findByMagGreaterThanEqual(Double mag);
    List<Earthquake> findByTimeGreaterThan(Long time);
}