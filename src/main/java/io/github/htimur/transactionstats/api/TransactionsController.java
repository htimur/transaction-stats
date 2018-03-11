package io.github.htimur.transactionstats.api;

import io.github.htimur.transactionstats.common.domain.Transaction;
import io.github.htimur.transactionstats.common.time.ApplicationClock;
import io.github.htimur.transactionstats.services.TransactionsAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@RestController
public class TransactionsController {

  private final ApplicationClock clock;
  private final TransactionsAggregator aggregator;

  @Autowired
  public TransactionsController(ApplicationClock clock, TransactionsAggregator aggregator) {
    this.clock = clock;
    this.aggregator = aggregator;
  }

  @RequestMapping(value = "/transactions", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
  public CompletionStage<ResponseEntity<Void>> post(@RequestBody Transaction transaction) {
    if (clock.getCurrentUTCMillis() - transaction.getTimestamp() > 60000) {
      return CompletableFuture.completedFuture(new ResponseEntity<>(HttpStatus.NO_CONTENT));
    } else {
      return aggregator.add(transaction).thenApply(r -> new ResponseEntity<>(HttpStatus.CREATED));
    }
  }
}
