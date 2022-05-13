package io.kx.loanproc.api

import io.Main
import io.kx.loanproc.api
import io.kx.loanproc.view.{GetLoanProcByStatusRequest, GetLoanProcByStatusResponse, LoanProcByStatus}
import kalix.scalasdk.testkit.KalixTestKit
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpec

import java.util.UUID

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class LoanProcServiceViewIntegrationSpec
    extends AnyWordSpec
    with Matchers
    with BeforeAndAfterAll
    with ScalaFutures {

  implicit private val patience: PatienceConfig =
    PatienceConfig(Span(5, Seconds), Span(500, Millis))

  private val testKit = KalixTestKit(Main.createKalix()).start()

  private val client = testKit.getGrpcClient(classOf[LoanProcService])
  private val viewClient = testKit.getGrpcClient(classOf[LoanProcByStatus])

  "LoanProcService" must {

    "view test" in {
      val loanAppId = UUID.randomUUID.toString
      val reviewerId = UUID.randomUUID.toString
      client.process(create(loanAppId)).futureValue
      get(loanAppId, api.LoanProcStatus.STATUS_READY_FOR_REVIEW)
      client.approve(api.ApproveCommand(loanAppId,reviewerId))
      get(loanAppId, api.LoanProcStatus.STATUS_APPROVED)

      val loanAppId2 = UUID.randomUUID.toString
      client.process(create(loanAppId2)).futureValue

      val loanAppId3 = UUID.randomUUID.toString
      client.process(create(loanAppId3)).futureValue

      view(api.LoanProcStatus.STATUS_APPROVED, 1)
      view(api.LoanProcStatus.STATUS_READY_FOR_REVIEW, 2)

    }

  }

  private def view(status: api.LoanProcStatus, expectedResults: Int) = {
    Thread.sleep(10000); //eventual consistency
    val response : GetLoanProcByStatusResponse = viewClient.getLoanAppsByStatus(GetLoanProcByStatusRequest(status.value)).futureValue
    response.results.length shouldBe expectedResults
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
