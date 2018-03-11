package io.github.htimur.transactionstats.services;

import akka.actor.ActorSystem;
import akka.japi.Pair;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Source;
import akka.stream.testkit.TestPublisher;
import akka.stream.testkit.TestSubscriber;
import akka.stream.testkit.javadsl.TestSink;
import akka.stream.testkit.javadsl.TestSource;
import akka.testkit.javadsl.TestKit;
import io.github.htimur.transactionstats.common.domain.Transaction;
import io.github.htimur.transactionstats.common.domain.TransactionAggregate;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import scala.concurrent.duration.Duration;

import static io.github.htimur.transactionstats.services.TransactionsAggregator.PARTITION_DURATION;
import static org.awaitility.Awaitility.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;


@RunWith(SpringRunner.class)
public class TransactionsAggregatorTest {

  private static ActorSystem system;
  private TransactionsAggregator aggregator;

  @BeforeClass
  public static void start() {
    system = ActorSystem.create("TestSystem");
  }

  @AfterClass
  public static void setupAll() {
    TestKit.shutdownActorSystem(system);
  }

  @Before
  public void setup() {
    aggregator = new TransactionsAggregator(system);
  }

  @Test
  public void aggregatorShouldCalculateTheCorrectAggregationResultInDifferentPeriods() {
    for (int i = 1; i <= 20; i++) {
      CompletableFuture<Boolean> res = aggregator.add(new Transaction(i, System.currentTimeMillis())).toCompletableFuture();
      assertThat(res.join()).isEqualTo(true);
    }

    await().atMost(500, TimeUnit.MILLISECONDS).untilAsserted(() -> {
      TransactionAggregate res = aggregator.calculate().toCompletableFuture().join();
      assertThat(res).isEqualTo(new TransactionAggregate(210, 10.5, 20, 1, 20));
    });
  }

  @Test
  public void transactionAggregationFlowShouldAggregateMessageTo100msWindow() {
    ActorMaterializer mat = ActorMaterializer.create(system);

    Source<Transaction, ?> tickSource = Source.tick(
      Duration.create(0, TimeUnit.MILLISECONDS),
      PARTITION_DURATION,
      Transaction.empty()
    );

    AggregateConsumer consumer = c -> c;

    Pair<TestPublisher.Probe<Transaction>, TestSubscriber.Probe<TransactionAggregate>> prob = TestSource.<Transaction>probe(system)
      .merge(tickSource)
      .via(TransactionsAggregator.AggregationFlow.flow(consumer))
      .toMat(TestSink.probe(system), Keep.both())
      .run(mat);

    prob.second().request(3);
    prob.first().sendNext(new Transaction(1, System.currentTimeMillis()));
    prob.first().sendNext(new Transaction(1, System.currentTimeMillis()));

    prob.second().expectNext(new TransactionAggregate(2, 1, 1, 1, 2));
    prob.second().expectNext(TransactionAggregate.empty());

    prob.first().sendNext(new Transaction(1, System.currentTimeMillis()));
    prob.second().expectNext(new TransactionAggregate(1, 1, 1, 1, 1));

  }
}