package io.kx.loanproc.action

import io.kx.loanapp.{api => loanappapi}
import com.google.protobuf.any.{Any => ScalaPbAny}
import com.google.protobuf.empty.Empty
import io.kx.loanproc.domain.Approved
import io.kx.loanproc.domain.Declined
import kalix.scalasdk.action.Action
import kalix.scalasdk.action.ActionCreationContext
import org.slf4j.{Logger, LoggerFactory}

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class LoanProcEventingToAppAction(creationContext: ActionCreationContext) extends AbstractLoanProcEventingToAppAction {

  val log: Logger = LoggerFactory.getLogger(classOf[LoanProcEventingToAppAction])
  override def onApproved(approved: Approved): Action.Effect[Empty] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val futureEffect =
      components.loanAppEntity.approve(loanappapi.ApproveCommand(approved.loanAppId))
        .execute()
        .map{ _ =>
          Empty.defaultInstance
        }
        .recover{ ex =>
          log.error("onApproved error [{}]: {}",approved.loanAppId,ex)
          Empty.defaultInstance
        }
    effects.asyncReply(futureEffect)
  }
  override def onDeclined(declined: Declined): Action.Effect[Empty] = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val futureEffect =
      components.loanAppEntity.decline(loanappapi.DeclineCommand(declined.loanAppId,declined.reason))
        .execute()
        .map{ _ =>
          Empty.defaultInstance
        }
        .recover{ ex =>
          log.error("onDeclined error [{}]: {}",declined.loanAppId,ex)
          Empty.defaultInstance
        }
    effects.asyncReply(futureEffect)
  }
  override def ignoreOtherEvents(any: ScalaPbAny): Action.Effect[Empty] = effects.reply(Empty.defaultInstance)
}

