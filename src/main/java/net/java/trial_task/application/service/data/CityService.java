package net.java.trial_task.application.service.data;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.java.trial_task.application.domain.entity.data.City;
import net.java.trial_task.application.exceptions.data.CityAlreadyExistsException;
import net.java.trial_task.application.exceptions.data.CityNotFoundException;
import net.java.trial_task.application.repository.data.CityRepository;

@Service
public class CityService {
  private final CityRepository repository;

  public CityService(@Autowired CityRepository cityRepository) {
    this.repository = cityRepository;
  }

  @Transactional 
  public City create(String cityName) {
    if (repository.existsByCityName(cityName)) throw new CityAlreadyExistsException(cityName);

    return repository.saveAndFlush(new City(cityName));
  }

  public List<City> findAll() {
    return repository.findAll();
  }

  public City findById(Long id) {
    return repository.findById(id).orElseThrow(() -> new CityNotFoundException(id));
  }

  @Transactional
  public City update(Long id, String cityName) {
    City city = findById(id);
    city.setCityName(cityName);
    return city;
  }

  @Transactional 
  public void delete(Long id) {
    repository.delete(findById(id));
  }
}