package io.kx

import io.Main
import io.kx.loanapp.{api => loanappapi}
import io.kx.loanproc.{api => loanprocapi}
import io.kx.loanapp.api.LoanAppService
import io.kx.loanproc.api.LoanProcService
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

class SystemIntegrationSpec
    extends AnyWordSpec
    with Matchers
    with BeforeAndAfterAll
    with ScalaFutures {

  implicit private val patience: PatienceConfig =
    PatienceConfig(Span(5, Seconds), Span(500, Millis))

  private val testKit = KalixTestKit(Main.createKalix()).start()

  private val loanAppClient = testKit.getGrpcClient(classOf[LoanAppService])
  private val loanProcClient = testKit.getGrpcClient(classOf[LoanProcService])

  "System" must {

    "approve loan happy path" in {
      val loanAppId = UUID.randomUUID.toString
      val reviewerId = UUID.randomUUID.toString
      loanAppClient.submit(create(loanAppId)).futureValue
      loanAppGet(loanAppId, loanappapi.LoanAppStatus.STATUS_IN_REVIEW)
      Thread.sleep(10000)//eventual consistency
      loanProcClient.approve(loanprocapi.ApproveCommand(loanAppId,reviewerId))
      Thread.sleep(10000)//eventual consistency
      loanAppGet(loanAppId, loanappapi.LoanAppStatus.STATUS_APPROVED)

    }

    "decline loan happy path" in {
      val loanAppId = UUID.randomUUID.toString
      val reviewerId = UUID.randomUUID.toString
      loanAppClient.submit(create(loanAppId)).futureValue
      loanAppGet(loanAppId, loanappapi.LoanAppStatus.STATUS_IN_REVIEW)
      Thread.sleep(10000)//eventual consistency
      loanProcClient.decline(loanprocapi.DeclineCommand(loanAppId,"some reason",reviewerId))
      Thread.sleep(10000)//eventual consistency
      loanAppGet(loanAppId, loanappapi.LoanAppStatus.STATUS_DECLINED)
    }

  }

  private def create(loanAppId: String): loanappapi.SubmitCommand =
    loanappapi.SubmitCommand(loanAppId,"clientId", 1000, 500, 24)

  private def loanAppGet(loanAppId: String, status: loanappapi.LoanAppStatus) = {
    val result : loanappapi.LoanAppState = loanAppClient.get(loanappapi.GetCommand(loanAppId)).futureValue
    result.status shouldBe status

  }
  private def loanProcGet(loanAppId: String, status: loanprocapi.LoanProcStatus) = {
    val result : loanprocapi.LoanProcState = loanProcClient.get(loanprocapi.GetCommand(loanAppId)).futureValue
    result.status shouldBe status

  }

  override def afterAll(): Unit = {
    testKit.stop()
    super.afterAll()
  }
}
