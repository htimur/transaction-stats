package io.github.htimur.transactionstats.services;

import io.github.htimur.transactionstats.common.domain.TransactionAggregate;

public interface AggregateConsumer {
  public TransactionAggregate consume(TransactionAggregate agg);
}
