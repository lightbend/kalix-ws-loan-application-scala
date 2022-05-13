package io.kx.loanproc.domain

import com.google.protobuf.empty.Empty
import io.kx.loanproc.api
import kalix.scalasdk.eventsourcedentity.EventSourcedEntity
import kalix.scalasdk.testkit.EventSourcedResult
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.util.UUID

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class LoanProcEntitySpec extends AnyWordSpec with Matchers {
  "The LoanProcEntity" should {
    "process loan happy path" in {
      val loanAppId = UUID.randomUUID.toString
      val testKit = LoanProcEntityTestKit(loanAppId,new LoanProcEntity(_))
      process(testKit,loanAppId)
    }

    "approve loan happy path" in {
      val loanAppId = UUID.randomUUID.toString
      val reviewerId = UUID.randomUUID.toString
      val testKit = LoanProcEntityTestKit(loanAppId,new LoanProcEntity(_))
      process(testKit,loanAppId)
      val result: EventSourcedResult[Empty] = testKit.approve(api.ApproveCommand(loanAppId,reviewerId))
      val event: Approved = result.nextEvent[Approved]
      event.loanAppId shouldBe loanAppId
      get(testKit,loanAppId,api.LoanProcStatus.STATUS_APPROVED)
    }
    "decline loan happy path" in {
      val loanAppId = UUID.randomUUID.toString
      val reason = "Some reason"
      val reviewerId = UUID.randomUUID.toString
      val testKit = LoanProcEntityTestKit(loanAppId,new LoanProcEntity(_))
      process(testKit,loanAppId)
      val result: EventSourcedResult[Empty] = testKit.decline(api.DeclineCommand(loanAppId,reason,reviewerId))
      val event: Declined = result.nextEvent[Declined]
      event.loanAppId shouldBe loanAppId
      event.reason shouldBe reason
      get(testKit,loanAppId,api.LoanProcStatus.STATUS_DECLINED)
    }
  }

  private def process(testKit: LoanProcEntityTestKit, loanAppId: String) = {
    val result: EventSourcedResult[Empty] = testKit.process(create(loanAppId))
    val event: ProcessStarted = result.nextEvent[ProcessStarted]
    event.loanAppId shouldBe loanAppId
    get(testKit,loanAppId,api.LoanProcStatus.STATUS_READY_FOR_REVIEW)
  }

  private def create(loanAppId: String): api.ProcessCommand =
    api.ProcessCommand(loanAppId, 1000, 500, 24)

  private def get(testKit: LoanProcEntityTestKit, loanAppId: String, status: api.LoanProcStatus): Unit = {
    val result: EventSourcedResult[api.LoanProcState] = testKit.get(api.GetCommand(loanAppId))
    result.didEmitEvents shouldBe false
    result.reply.status shouldBe status
  }
}
