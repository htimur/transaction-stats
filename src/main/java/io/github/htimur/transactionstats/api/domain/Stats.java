package io.github.htimur.transactionstats.api.domain;

public class Stats {
  private final double sum;
  private final double avg;
  private final double max;
  private final double min;
  private final long count;

  public Stats(double sum, double avg, double max, double min, long count) {
    this.sum = sum;
    this.avg = avg;
    this.max = max;
    this.min = min;
    this.count = count;
  }

  public double getSum() {
    return sum;
  }

  public double getAvg() {
    return avg;
  }

  public double getMax() {
    return max;
  }

  public double getMin() {
    return min;
  }

  public long getCount() {
    return count;
  }
}
