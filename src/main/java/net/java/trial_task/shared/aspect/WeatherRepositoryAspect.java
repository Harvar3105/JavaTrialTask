package net.java.trial_task.shared.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import net.java.trial_task.shared.exceptions.WeatherDataRepositoryUnavailableException;

@Aspect
@Component
public class WeatherRepositoryAspect {

  @Around("this(com.example.weather.WeatherRepository)")
  public Object handleRepositoryErrors(ProceedingJoinPoint joinPoint)
      throws Throwable {
    try {
      return joinPoint.proceed();
    } catch (DataAccessException ex) {
      throw new WeatherDataRepositoryUnavailableException(
        "Weather repository operation failed: "
          + joinPoint.getSignature().getName(),
        ex
      );
    }
  }
}

