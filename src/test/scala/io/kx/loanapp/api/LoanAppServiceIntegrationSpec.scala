package io.kx.loanapp.api

import io.Main
import io.kx.loanapp.api
import kalix.scalasdk.testkit.KalixTestKit
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.Millis
import org.scalatest.time.Seconds
import org.scalatest.time.Span
import org.scalatest.wordspec.AnyWordSpec

import java.util.UUID

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class LoanAppServiceIntegrationSpec
    extends AnyWordSpec
    with Matchers
    with BeforeAndAfterAll
    with ScalaFutures {

  implicit private val patience: PatienceConfig =
    PatienceConfig(Span(5, Seconds), Span(500, Millis))

  private val testKit = KalixTestKit(Main.createKalix()).start()

  private val client = testKit.getGrpcClient(classOf[LoanAppService])

  "LoanAppService" must {

    "submit loan happy path" in {
      val loanAppId = UUID.randomUUID.toString
      client.submit(create(loanAppId)).futureValue
      get(loanAppId, api.LoanAppStatus.STATUS_IN_REVIEW)
    }

    "approve loan happy path" in {
      val loanAppId = UUID.randomUUID.toString
      client.submit(create(loanAppId)).futureValue
      get(loanAppId, api.LoanAppStatus.STATUS_IN_REVIEW)

      client.approve(api.ApproveCommand(loanAppId)).futureValue
      get(loanAppId, api.LoanAppStatus.STATUS_APPROVED)
    }

    "decline loan happy path" in {
      val loanAppId = UUID.randomUUID.toString
      client.submit(create(loanAppId)).futureValue
      get(loanAppId, api.LoanAppStatus.STATUS_IN_REVIEW)

      client.decline(api.DeclineCommand(loanAppId,"some reason")).futureValue
      get(loanAppId, api.LoanAppStatus.STATUS_DECLINED)
    }

  }

  private def create(loanAppId: String): api.SubmitCommand =
    api.SubmitCommand(loanAppId,"clientId", 1000, 500, 24)

  private def get(loanAppId: String, status: api.LoanAppStatus) = {
    val result : api.LoanAppState = client.get(api.GetCommand(loanAppId)).futureValue
    result.status shouldBe status

  }

  override def afterAll(): Unit = {
    testKit.stop()
    super.afterAll()
  }
}
