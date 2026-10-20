package net.java.trial_task.shared.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionException;

import net.java.trial_task.shared.exceptions.WeatherDataIntegrityViolationException;
import net.java.trial_task.shared.exceptions.WeatherDataRepositoryUnavailableException;
import net.java.trial_task.shared.repository.WeatherRepository;

/**
 * Aspect that translates Spring's persistence exceptions raised by {@link WeatherRepository}
 * into {@link WeatherDataRepositoryUnavailableException}.
 *
 * <p>Purpose: callers of the weather repository depend on a single module-specific exception
 * type and do not need to handle {@link DataAccessException} and its subclasses.
 *
 * <p>Scope: every method invoked through a Spring proxy of a bean that implements
 * {@link WeatherRepository}, including methods inherited from
 * {@link org.springframework.data.jpa.repository.JpaRepository}. The advice is applied by
 * Spring AOP through proxies, so only calls that pass through the proxy are intercepted.
 * Exceptions that do not originate in a repository method call are not covered, in
 * particular failures raised later when an enclosing transaction is flushed or committed.
 *
 * <p>The class holds no state.
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WeatherRepositoryAspect {

  /**
   * Executes the intercepted repository method and converts a {@link DataAccessException}
   * thrown by it.
   *
   * <p>Behavior:
   * <ul>
   *   <li>on normal completion, the return value of the repository method is returned
   *       unchanged;</li>
   *   <li>on {@link DataAccessException}, a {@link WeatherDataRepositoryUnavailableException}
   *       is thrown; its message contains the name of the failed method (without
   *       arguments) and the original exception is preserved as its cause;</li>
   *   <li>any other throwable is propagated unchanged.</li>
   * </ul>
   *
   * <p>The advice does not retry the call and does not log the exception.
   *
   * @param joinPoint the intercepted repository method invocation, provided by the AOP
   *     framework
   * @return the value returned by the repository method, possibly {@code null} if the method
   *     returns {@code null}
   * @throws WeatherDataRepositoryUnavailableException if the repository method throws a
   *     {@link DataAccessException}
   * @throws Throwable any other throwable raised by the repository method; declared because
   *     {@link ProceedingJoinPoint#proceed()} declares it
   */
  @Around("this(net.java.trial_task.shared.repository.WeatherRepository)")
  public Object handleRepositoryErrors(ProceedingJoinPoint joinPoint)
      throws Throwable {
    try {
      return joinPoint.proceed();
    } catch (DataIntegrityViolationException ex) {
      throw new WeatherDataIntegrityViolationException("Provided invalid data!");
    } catch (DataAccessException | TransactionException ex) {
      throw new WeatherDataRepositoryUnavailableException(
        "Weather repository operation failed: "
          + joinPoint.getSignature().getName(),
        ex
      );
    }
  }
}

