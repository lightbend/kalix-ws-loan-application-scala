package io.kx.loanapp.domain

import com.google.protobuf.empty.Empty
import io.kx.loanapp.api
import kalix.scalasdk.testkit.EventSourcedResult
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

import java.util.UUID

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class LoanAppEntitySpec extends AnyWordSpec with Matchers {
  "The LoanAppEntity" should {

    "submit loan happy path" in {
      val loanAppId = UUID.randomUUID.toString
      val testKit = LoanAppEntityTestKit(loanAppId,new LoanAppEntity(_))
      submitLoan(testKit,loanAppId)
    }

    "approve loan happy path" in {
      val loanAppId = UUID.randomUUID.toString
      val testKit = LoanAppEntityTestKit(loanAppId,new LoanAppEntity(_))
      submitLoan(testKit,loanAppId)
      val result: EventSourcedResult[Empty] = testKit.approve(api.ApproveCommand(loanAppId))
      val event: Approved = result.nextEvent[Approved]
      event.loanAppId shouldBe loanAppId
      get(testKit,loanAppId,api.LoanAppStatus.STATUS_APPROVED)
    }
    "decline loan happy path" in {
      val loanAppId = UUID.randomUUID.toString
      val reason = "Some reason"
      val testKit = LoanAppEntityTestKit(loanAppId,new LoanAppEntity(_))
      submitLoan(testKit,loanAppId)
      val result: EventSourcedResult[Empty] = testKit.decline(api.DeclineCommand(loanAppId,reason))
      val event: Declined = result.nextEvent[Declined]
      event.loanAppId shouldBe loanAppId
      event.reason shouldBe reason
      get(testKit,loanAppId,api.LoanAppStatus.STATUS_DECLINED)
    }

  }

  private def submitLoan(testKit: LoanAppEntityTestKit, loanAppId: String) = {
    val result: EventSourcedResult[Empty] = testKit.submit(create(loanAppId))
    val event: Submitted = result.nextEvent[Submitted]
    event.loanAppId shouldBe loanAppId
    get(testKit,loanAppId,api.LoanAppStatus.STATUS_IN_REVIEW)
  }

  private def create(loanAppId: String): api.SubmitCommand =
    api.SubmitCommand(loanAppId,"clientId", 1000, 500, 24)

  private def get(testKit: LoanAppEntityTestKit, loanAppId: String, status: api.LoanAppStatus): Unit = {
    val result: EventSourcedResult[api.LoanAppState] = testKit.get(api.GetCommand(loanAppId))
    result.didEmitEvents shouldBe false
    result.reply.status shouldBe status
  }

}
