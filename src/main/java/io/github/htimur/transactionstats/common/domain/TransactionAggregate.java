package io.github.htimur.transactionstats.common.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

import java.io.Serializable;

public class TransactionAggregate implements Serializable {
  private final double sum;
  private final double avg;
  private final double max;
  private final double min;
  private final long count;

  public TransactionAggregate() {
    this.sum = 0;
    this.avg = 0;
    this.max = 0;
    this.min = 0;
    this.count = 0;
  }

  public TransactionAggregate(double sum, double avg, double max, double min, long count) {
    this.sum = sum;
    this.avg = avg;
    this.max = max;
    this.min = min;
    this.count = count;
  }

  @JsonProperty
  public double getSum() {
    return sum;
  }

  @JsonProperty
  public double getAvg() {
    return avg;
  }

  @JsonProperty
  public double getMax() {
    return max;
  }

  @JsonProperty
  public double getMin() {
    return min;
  }

  @JsonProperty
  public long getCount() {
    return count;
  }

  public static TransactionAggregate empty() {
    return new TransactionAggregate(0, 0, 0, 0, 0);
  }

  public static boolean isEmpty(TransactionAggregate t) {
    return t.avg == 0 && t.count == 0 && t.max == 0 && t.min == 0 && t.sum == 0;
  }

  @Override
  public String toString() {
    return "TransactionAggregate{" +
      "sum=" + sum +
      ", avg=" + avg +
      ", max=" + max +
      ", min=" + min +
      ", count=" + count +
      '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    TransactionAggregate that = (TransactionAggregate) o;
    return Double.compare(that.sum, sum) == 0 &&
      Double.compare(that.avg, avg) == 0 &&
      Double.compare(that.max, max) == 0 &&
      Double.compare(that.min, min) == 0 &&
      count == that.count;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(sum, avg, max, min, count);
  }
}
