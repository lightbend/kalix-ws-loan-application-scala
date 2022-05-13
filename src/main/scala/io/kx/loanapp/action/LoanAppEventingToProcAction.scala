package io.kx.loanapp.action
import io.kx.loanproc.{api => loanprocapi}
import com.google.protobuf.any.{Any => ScalaPbAny}
import com.google.protobuf.empty.Empty
import io.kx.loanapp.domain.Submitted
import kalix.scalasdk.action.Action
import kalix.scalasdk.action.ActionCreationContext
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class LoanAppEventingToProcAction(creationContext: ActionCreationContext) extends AbstractLoanAppEventingToProcAction {

  val log: Logger = LoggerFactory.getLogger(classOf[LoanAppEventingToProcAction])
  override def onSubmitted(submitted: Submitted): Action.Effect[Empty] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val futureEffect =
      components.loanProcEntity.process(loanprocapi.ProcessCommand(
        submitted.loanAppId,
        submitted.clientMonthlyIncomeCents,
        submitted.loanAmountCents,
        submitted.loanDurationMonths
      )).execute()
        .map { _ =>
          Empty.defaultInstance
        }
        .recover{ ex =>
          log.error("onSubmitted error [{}]: {}",submitted.loanAppId,ex)
          Empty.defaultInstance
        }
    effects.asyncReply(futureEffect)
  }

  override def ignoreOtherEvents(any: ScalaPbAny): Action.Effect[Empty] = effects.reply(Empty.defaultInstance)
}

