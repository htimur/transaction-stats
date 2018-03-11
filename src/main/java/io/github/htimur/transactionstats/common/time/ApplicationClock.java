package io.github.htimur.transactionstats.common.time;

import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class ApplicationClock implements Clock {

  @Override
  public OffsetDateTime getCurrentDateTime() {
    return OffsetDateTime.now();
  }

}
