package net.java.trial_task.application.service.condition;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import net.java.trial_task.application.domain.entity.condition.RBF;
import net.java.trial_task.application.exceptions.condition.RBFNotFoundException;
import net.java.trial_task.application.repository.condition.RBFRepository;

@Service
public class RBFService {
  private final RBFRepository repository;

  public RBFService(RBFRepository repository) {
    this.repository = repository;
  }

  public List<RBF> findAll() {
    return repository.findAll();
  }

  public RBF findById(Long id) {
    return repository.findById(id).orElseThrow(() -> new RBFNotFoundException(id));
  }

  @Transactional
  public RBF create(Long cityId, Long vehicleTypeId, BigDecimal fee) {
    return repository.saveAndFlush(new RBF(cityId, vehicleTypeId, fee));
  }

  @Transactional
  public RBF updateFee(Long id, BigDecimal fee) {
    RBF entity = findById(id);
    entity.setFee(fee);
    return entity;
  }

  @Transactional
  public void delete(Long id) {
    repository.delete(findById(id));
  }
}