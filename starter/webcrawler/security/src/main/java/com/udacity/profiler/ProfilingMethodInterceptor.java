package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

final class ProfilingMethodInterceptor implements InvocationHandler {

  private final Clock clock;

  private final Object delegate;
  private final ProfilingState state;

  ProfilingMethodInterceptor(Clock clock, Object delegate, ProfilingState state) {
    this.clock = Objects.requireNonNull(clock);
    this.delegate = Objects.requireNonNull(delegate);
    this.state = Objects.requireNonNull(state);
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    Objects.requireNonNull(method);

    if (method.getDeclaringClass() == Object.class) {
      return method.invoke(delegate, args);
    }

    boolean profiled = method.isAnnotationPresent(Profiled.class);
    Instant start = null;
    try {
      if (profiled) {
        start = clock.instant();
      }
      return method.invoke(delegate, args);
    } catch (Throwable t) {
      Throwable cause = t.getCause();
      throw (cause == null) ? t : cause;
    } finally {
      if (profiled && start != null) {
        Duration elapsed = Duration.between(start, clock.instant());
        state.record(delegate.getClass(), method, elapsed);
      }
    }
  }
}
