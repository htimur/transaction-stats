package io.github.htimur.transactionstats;

import io.github.htimur.transactionstats.common.domain.Transaction;
import io.github.htimur.transactionstats.common.domain.TransactionAggregate;
import io.github.htimur.transactionstats.common.time.ApplicationClock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.*;

import org.mockito.Mockito;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TransactionStatsApplicationTests {

  @MockBean
  private ApplicationClock clock;

  @Autowired
	private TestRestTemplate rest;

  @Before
  public void setup() {
    Mockito.when(clock.getCurrentUTCMillis()).thenReturn(System.currentTimeMillis());
  }

	@Test
	public void transactionsEndpingReturns201ForSuccessfulRequestTest() {
    Transaction currentTransaction = new Transaction(100, System.currentTimeMillis());

    ResponseEntity<Void> response1 = rest.postForEntity("/transactions", currentTransaction, Void.class);

    assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.CREATED);

  }
	@Test
	public void transactionsEndpingReturns204ForOutdatedRequestTest() {
    Transaction oldTransaction = new Transaction(100, System.currentTimeMillis() - 60000);

    ResponseEntity<Void> response2 = rest.postForEntity("/transactions", oldTransaction, Void.class);

    assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
	}

	@Test
	public void statisticsEndpointReturnsTheCurrentStatsTest() {
    Transaction currentTransaction = new Transaction(100, System.currentTimeMillis());

    ResponseEntity<Void> response1 = rest.postForEntity("/transactions", currentTransaction, Void.class);

    assertThat(response1.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    Transaction oldTransaction = new Transaction(40, System.currentTimeMillis());

    ResponseEntity<Void> response2 = rest.postForEntity("/transactions", oldTransaction, Void.class);

    assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    await().atMost(200, TimeUnit.MILLISECONDS).untilAsserted(() -> {
      ResponseEntity<TransactionAggregate> result = rest.getForEntity("/statistics", TransactionAggregate.class);

      assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(result.getBody()).isEqualTo(new TransactionAggregate(140, 70, 100, 40, 2));
    });
	}

}
