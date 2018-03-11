package io.github.htimur.transactionstats.common.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

import java.io.Serializable;

public class Transaction implements Serializable {
  private final double amount;
  private final long timestamp;

  public Transaction() {
    amount = 0;
    timestamp = 0;
  }

  public Transaction(double amount, long timestamp) {
    this.amount = amount;
    this.timestamp = timestamp;
  }

  public static Transaction empty() {
    return new Transaction(0, 0);
  }

  @JsonProperty
  public double getAmount() {
    return amount;
  }

  @JsonProperty
  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public String toString() {
    return "Transaction{" +
      "amount=" + amount +
      ", timestamp=" + timestamp +
      '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Transaction that = (Transaction) o;
    return Double.compare(that.amount, amount) == 0 &&
      timestamp == that.timestamp;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(amount, timestamp);
  }
}
