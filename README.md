# Transaction Statistics

The application is built using:
* Java 1.8
* Spring Boot
* Akka Streams 2.5

## Aggregation logic details

Application aggregates the incoming sales amounts in to 600 buckets with 100ms frequency. In this way we can achieve accuracy with 100ms precision. This results in storing only 600 elements in memory with constant memory footprint.

Statistics aggregation is constant time aggregation over the 600 elements we store in memory.

It's possible to increase or decrease the precision with a trade-offs for performance or memory.

## How to run

To build and run the application you need Gradle or you can use provided `gradlew` script.

```bash
./gradlew bootRun
```

Run tests with:
```bash
./gradlew test
```

## Throughput/Load test

A simple Gatling simulation is implemented to check the application under the load.

To run the test start the application in production mode and start the test use:

```bash
SPRING_PROFILES_ACTIVE=prod ./gradlew bootRun
```

```bash
./gradlew gatlingRun
```

The test is configured to simulate a constant load of 200 users with various RPMS from 100 to 400 over 10 minutes (the configuration is easy to understand, feel free to change based on your needs).

Application is capable to run with 64Mb of memory, but under the high load the GC pressure is limiting the throughput. Memory usage is constant as expected.

On my local machine with 512Mb of heap, and the 2 core CPU 3,1 GHz Intel Core i7 I've got the following results:

```
> request count                                     109341 (OK=68089  KO=41252 )
> min response time                                      1 (OK=1      KO=1     )
> max response time                                   1176 (OK=1176   KO=137   )
> mean response time                                     8 (OK=9      KO=6     )
> std deviation                                         20 (OK=23     KO=11    )
> response time 50th percentile                          4 (OK=4      KO=4     )
> response time 75th percentile                          5 (OK=5      KO=5     )
> response time 95th percentile                         26 (OK=34     KO=14    )
> response time 99th percentile                         94 (OK=112    KO=67    )
> mean requests/sec                                173.557 (OK=108.078 KO=65.479)
---- Response Time Distribution ------------------------------------------------
> t < 100 ms                                         67201 ( 61%)
> 100 ms < t < 500 ms                                  873 (  1%)
> t > 500 ms                                            15 (  0%)
> failed                                             41252 ( 38%)
---- Errors --------------------------------------------------------------------
> status.find.is(201), but actually found 204                     41252 (100.0%)
================================================================================
```