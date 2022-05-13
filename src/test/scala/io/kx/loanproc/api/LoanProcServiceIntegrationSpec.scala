package io.kx.loanproc.api


import io.Main
import io.kx.loanproc.api
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

class LoanProcServiceIntegrationSpec
    extends AnyWordSpec
    with Matchers
    with BeforeAndAfterAll
    with ScalaFutures {

  implicit private val patience: PatienceConfig =
    PatienceConfig(Span(5, Seconds), Span(500, Millis))

  private val testKit = KalixTestKit(Main.createKalix()).start()

  private val client = testKit.getGrpcClient(classOf[LoanProcService])

  "LoanProcService" must {

    "submit loan happy path" in {
      val loanAppId = UUID.randomUUID.toString
      client.process(create(loanAppId)).futureValue
      get(loanAppId, api.LoanProcStatus.STATUS_READY_FOR_REVIEW)
    }

    "approve loan happy path" in {
      val loanAppId = UUID.randomUUID.toString
      client.process(create(loanAppId)).futureValue
      get(loanAppId, api.LoanProcStatus.STATUS_READY_FOR_REVIEW)
      client.approve(api.ApproveCommand(loanAppId))
      get(loanAppId, api.LoanProcStatus.STATUS_APPROVED)
    }

    "decline loan happy path" in {
      val loanAppId = UUID.randomUUID.toString
      client.process(create(loanAppId)).futureValue
      get(loanAppId, api.LoanProcStatus.STATUS_READY_FOR_REVIEW)
      client.decline(api.DeclineCommand(loanAppId,"some reason"))
      get(loanAppId, api.LoanProcStatus.STATUS_DECLINED)
    }

  }

  private def create(loanAppId: String): api.ProcessCommand =
    api.ProcessCommand(loanAppId, 1000, 500, 24)

  private def get(loanAppId: String, status: api.LoanProcStatus) = {
    val result : api.LoanProcState = client.get(api.GetCommand(loanAppId)).futureValue
    result.status shouldBe status

  }
  override def afterAll(): Unit = {
    testKit.stop()
    super.afterAll()
  }
}
