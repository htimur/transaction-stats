package io.github.htimur.transactionstats.api;

import io.github.htimur.transactionstats.common.domain.TransactionAggregate;
import io.github.htimur.transactionstats.services.TransactionsAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletionStage;

@RestController
public class StatisticsController {
  private final TransactionsAggregator aggregator;

  @Autowired
  public StatisticsController(TransactionsAggregator aggregator) {
    this.aggregator = aggregator;
  }

  @RequestMapping(value = "/statistics", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public CompletionStage<TransactionAggregate> stats() {
    return aggregator.calculate();
  }
}
