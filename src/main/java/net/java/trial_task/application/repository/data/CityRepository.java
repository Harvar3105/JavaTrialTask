package net.java.trial_task.application.repository.data;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import net.java.trial_task.application.domain.entity.data.City;

public interface CityRepository extends JpaRepository<City, Long> {
  Optional<City> getByCityName(String cityName);
  boolean existsByCityName(String cityName);
}
