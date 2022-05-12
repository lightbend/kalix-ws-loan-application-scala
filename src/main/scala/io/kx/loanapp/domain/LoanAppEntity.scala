package io.kx.loanapp.domain

import com.google.protobuf.empty.Empty
import io.kx.loanapp.api
import io.kx.loanapp.domain.LoanAppEntity.{ERROR_NOT_FOUND, ERROR_WRONG_STATUS}
import kalix.scalasdk.eventsourcedentity.EventSourcedEntity
import kalix.scalasdk.eventsourcedentity.EventSourcedEntityContext

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.
object LoanAppEntity{
  val ERROR_NOT_FOUND = "Not found"
  val ERROR_WRONG_STATUS = "Wrong status"
}
class LoanAppEntity(context: EventSourcedEntityContext) extends AbstractLoanAppEntity {
  override def emptyState: LoanAppDomainState = LoanAppDomainState.defaultInstance;

  override def submit(currentState: LoanAppDomainState, submitCommand: api.SubmitCommand): EventSourcedEntity.Effect[Empty] =
    currentState match {
      case LoanAppDomainState.defaultInstance =>
        val event = Submitted(
          submitCommand.loanAppId,
          submitCommand.clientId,
          submitCommand.clientMonthlyIncomeCents,
          submitCommand.loanAmountCents,
          submitCommand.loanDurationMonths,
          System.currentTimeMillis())
        effects.emitEvent(event)
          .thenReply(_ => Empty.defaultInstance)
      case LoanAppDomainState(_,_,_,_,status,_,_,_) if status == LoanAppDomainStatus.STATUS_IN_REVIEW  =>
        effects.reply(Empty.defaultInstance)
      case _ =>
        effects.error(ERROR_WRONG_STATUS)
    }

  override def get(currentState: LoanAppDomainState, getCommand: api.GetCommand): EventSourcedEntity.Effect[api.LoanAppState] =
    currentState match {
      case LoanAppDomainState.defaultInstance =>
        effects.error(ERROR_NOT_FOUND)
      case _ =>
        effects.reply(map(currentState))
    }

  private def map(state: LoanAppDomainState): api.LoanAppState =
    api.LoanAppState(state.clientId,
      state.clientMonthlyIncomeCents,
      state.loanAmountCents,
      state.loanDurationMonths,
      map(state.status),
      state.declineReason)

  private def map(status: LoanAppDomainStatus): api.LoanAppStatus =
    api.LoanAppStatus.fromValue(status.value)

  override def approve(currentState: LoanAppDomainState, approveCommand: api.ApproveCommand): EventSourcedEntity.Effect[Empty] =
    currentState match {
      case LoanAppDomainState.defaultInstance =>
        effects.error(ERROR_NOT_FOUND)
      case LoanAppDomainState(_,_,_,_,status,_,_,_) if status == LoanAppDomainStatus.STATUS_IN_REVIEW  =>
        val event = Approved(
          approveCommand.loanAppId,
          System.currentTimeMillis())
        effects.emitEvent(event)
          .thenReply(_ => Empty.defaultInstance)
      case LoanAppDomainState(_,_,_,_,status,_,_,_) if status == LoanAppDomainStatus.STATUS_APPROVED  =>
        effects.reply(Empty.defaultInstance)
      case _ =>
        effects.error(ERROR_WRONG_STATUS)
    }

  override def decline(currentState: LoanAppDomainState, declineCommand: api.DeclineCommand): EventSourcedEntity.Effect[Empty] =
    currentState match {
      case LoanAppDomainState.defaultInstance =>
        effects.error(ERROR_NOT_FOUND)
      case LoanAppDomainState(_,_,_,_,status,_,_,_) if status == LoanAppDomainStatus.STATUS_IN_REVIEW  =>
        val event = Declined(
          declineCommand.loanAppId,
          declineCommand.reason,
          System.currentTimeMillis())
        effects.emitEvent(event)
          .thenReply(_ => Empty.defaultInstance)
      case LoanAppDomainState(_,_,_,_,status,_,_,_) if status == LoanAppDomainStatus.STATUS_DECLINED  =>
        effects.reply(Empty.defaultInstance)
      case _ =>
        effects.error(ERROR_WRONG_STATUS)
    }

  override def submitted(currentState: LoanAppDomainState, submitted: Submitted): LoanAppDomainState =
    LoanAppDomainState(
      submitted.clientId,
      submitted.clientMonthlyIncomeCents,
      submitted.loanAmountCents,
      submitted.loanDurationMonths,
      LoanAppDomainStatus.STATUS_IN_REVIEW,
      "",
      submitted.eventTimestamp
    )

  override def approved(currentState: LoanAppDomainState, approved: Approved): LoanAppDomainState =
    currentState
      .withStatus(LoanAppDomainStatus.STATUS_APPROVED)
      .withLastUpdateTimestamp(approved.eventTimestamp)

  override def declined(currentState: LoanAppDomainState, declined: Declined): LoanAppDomainState =
    currentState
      .withStatus(LoanAppDomainStatus.STATUS_DECLINED)
      .withDeclineReason(declined.reason)
      .withLastUpdateTimestamp(declined.eventTimestamp)

}
