package io.github.htimur.transactionstats.services;

import akka.actor.ActorSystem;
import akka.japi.function.Function;
import akka.stream.*;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.stream.javadsl.SourceQueueWithComplete;
import com.google.common.collect.EvictingQueue;
import com.google.common.math.Stats;
import io.github.htimur.transactionstats.common.domain.Transaction;
import io.github.htimur.transactionstats.common.domain.TransactionAggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.concurrent.duration.Duration;
import scala.concurrent.duration.FiniteDuration;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class TransactionsAggregator {

  public static class AggregationFlow {

    static Flow<Transaction, TransactionAggregate, ?> flow(AggregateConsumer consumer) {
      return Flow.of(Transaction.class)
        .groupedWithin(BUFFER_SIZE, PARTITION_DURATION)
        .mapAsync(60,
          groups -> CompletableFuture.supplyAsync(() -> {
            List<Double> values = groups.stream()
              .filter(t -> t.getAmount() > 0)
              .map(Transaction::getAmount)
              .collect(Collectors.toList());

            if (values.isEmpty()) {
              return TransactionAggregate.empty();
            }

            Stats transactionStats = Stats.of(values);

            return new TransactionAggregate(
              transactionStats.sum(),
              transactionStats.mean(),
              transactionStats.max(),
              transactionStats.min(),
              transactionStats.count()
            );
          })
        )
        .map(consumer::consume);
    }
  }

  private static final Logger logger = LoggerFactory.getLogger(TransactionsAggregator.class);

  static final int BUFFER_SIZE = 200000;
  static final int PARTITION_QUEUE_SIZE = 600;
  static final FiniteDuration PARTITION_DURATION = Duration.create(100, TimeUnit.MILLISECONDS);

  private static final Function<Throwable, Supervision.Directive> decider = exc -> {
    logger.error("Error during stream processing", exc);
    return Supervision.stop();
  };

  private final SourceQueueWithComplete<Transaction> aggregator;

  private final EvictingQueue<TransactionAggregate> aggregates;

  @Autowired
  public TransactionsAggregator(ActorSystem sys) {
    ActorMaterializer mat = ActorMaterializer.create(
      ActorMaterializerSettings.create(sys).withSupervisionStrategy(decider), sys
    );

    Source<Transaction, ?> tickSource = Source.tick(
      Duration.create(0, TimeUnit.MILLISECONDS),
      PARTITION_DURATION,
      Transaction.empty()
    );

    aggregates = EvictingQueue.create(PARTITION_QUEUE_SIZE);
    AggregateConsumer consumer = agg -> {
      aggregates.add(agg);
      return agg;
    };

    aggregator = Source.<Transaction>queue(BUFFER_SIZE, OverflowStrategy.dropHead())
      .merge(tickSource)
      .via(AggregationFlow.flow(consumer))
      .to(Sink.ignore())
      .run(mat);
  }

  public CompletionStage<Boolean> add(Transaction t) {
    return aggregator.offer(t).thenApply(r -> r == QueueOfferResult.enqueued());
  }

  public CompletionStage<TransactionAggregate> calculate() {
    return CompletableFuture.supplyAsync(() -> {
      Optional<TransactionAggregate> mayBeAgg = aggregates.stream()
        .filter(a -> !TransactionAggregate.isEmpty(a))
        .reduce(
          (a, b) -> new TransactionAggregate(
            a.getSum() + b.getSum(),
            0,
            Math.max(a.getMax(), b.getMax()),
            Math.min(a.getMin(), b.getMin()),
            a.getCount() + b.getCount()
          )
        );

      return mayBeAgg
        .map(agg ->
          new TransactionAggregate(
            agg.getSum(),
            agg.getSum() / agg.getCount(),
            agg.getMax(),
            agg.getMin(),
            agg.getCount()
          )
        )
        .orElse(TransactionAggregate.empty());
    });
  }
}
