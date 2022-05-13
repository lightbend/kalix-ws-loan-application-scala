package io.kx.loanapp.action

import com.google.protobuf.any.{ Any => ScalaPbAny }
import com.google.protobuf.empty.Empty
import io.kx.loanapp.domain.Submitted
import kalix.scalasdk.action.Action
import kalix.scalasdk.testkit.ActionResult
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class LoanAppEventingToProcActionSpec
    extends AnyWordSpec
    with Matchers {

  "LoanAppEventingToProcAction" must {

    "have example test that can be removed" in {
      val service = LoanAppEventingToProcActionTestKit(new LoanAppEventingToProcAction(_))
      pending
      // use the testkit to execute a command
      // and verify final updated state:
      // val result = service.someOperation(SomeRequest)
      // verify the reply
      // result.reply shouldBe expectedReply
    }

    "handle command OnSubmitted" in {
      val service = LoanAppEventingToProcActionTestKit(new LoanAppEventingToProcAction(_))
          pending
      // val result = service.onSubmitted(Submitted(...))
    }

    "handle command IgnoreOtherEvents" in {
      val service = LoanAppEventingToProcActionTestKit(new LoanAppEventingToProcAction(_))
          pending
      // val result = service.ignoreOtherEvents(ScalaPbAny(...))
    }

  }
}
