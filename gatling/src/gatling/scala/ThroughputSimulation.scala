import java.time.Clock
import java.util.UUID

import io.gatling.core.Predef._
import io.gatling.core.scenario.Simulation
import io.gatling.http.Predef._

import scala.concurrent.duration._
import scala.util.Random

class ThroughputSimulation extends Simulation {

  private val httpConf = http.baseURL("http://localhost:8080") // Here is the root for all relative URLs

  val gen = new Random(UUID.randomUUID().getMostSignificantBits)
  val clock = Clock.systemUTC()
  private val saleAmountFeeder = for (_ <- 0 to 100) yield Map("amount" -> gen.nextDouble())

  private val sales =
    scenario("Post transaction data")
      .feed(saleAmountFeeder.circular)
      .exec { session =>
        session.set("tms", clock.millis())
      }
      .exec(
        http("transactions")
          .post("/transactions")
          .body(StringBody("""{"amount":${amount}, "timestamp": ${tms}}""")).asJSON
          .check(status.is(201)))


  private val statistics =
    scenario("Get statistics")
      .exec(
        http("statistics")
          .get("/statistics")
          .check(status.is(200)))

  setUp(
    sales.inject(constantUsersPerSec(200) during 10.minutes),
    statistics.pause(1.second).inject(constantUsersPerSec(5) during 10.minutes)
  ).throttle(
    reachRps(200) in 20.seconds,
    holdFor(3.minutes),
    jumpToRps(300),
    holdFor(1.minutes),
    jumpToRps(400),
    holdFor(10.seconds),
    jumpToRps(100),
    holdFor(3.minute),
    jumpToRps(200),
    holdFor(3.minute)
  )
    .protocols(httpConf)
}
