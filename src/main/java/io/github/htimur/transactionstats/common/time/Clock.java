package io.github.htimur.transactionstats.common.time;

import java.time.OffsetDateTime;

public interface Clock {
  OffsetDateTime getCurrentDateTime();
}
