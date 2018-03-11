package io.github.htimur.transactionstats.common.time;

import org.springframework.stereotype.Component;

@Component
public class ApplicationClock implements Clock {

  private final java.time.Clock clock;

  public ApplicationClock() {
    clock = java.time.Clock.systemUTC();
  }

  @Override
  public long getCurrentUTCMillis() {
    return clock.millis();
  }
}
