package io.kx.loanproc.domain

import com.google.protobuf.empty.Empty
import io.kx.loanproc.api
import io.kx.loanproc.domain.LoanProcEntity.{ERROR_NOT_FOUND, ERROR_WRONG_STATUS}
import kalix.scalasdk.eventsourcedentity.EventSourcedEntity
import kalix.scalasdk.eventsourcedentity.EventSourcedEntityContext

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

object LoanProcEntity{
  val ERROR_NOT_FOUND = "Not found"
  val ERROR_WRONG_STATUS = "Wrong status"
}
class LoanProcEntity(context: EventSourcedEntityContext) extends AbstractLoanProcEntity {
  override def emptyState: LoanProcDomainState = LoanProcDomainState.defaultInstance

  override def process(currentState: LoanProcDomainState, processCommand: api.ProcessCommand): EventSourcedEntity.Effect[Empty] =
    currentState match {
      case LoanProcDomainState.defaultInstance =>
        val event = ProcessStarted(
          processCommand.loanAppId,
          System.currentTimeMillis())
        effects.emitEvent(event)
          .thenReply(_ => Empty.defaultInstance)
      case LoanProcDomainState(_,status,_,_,_) if status == LoanProcDomainStatus.STATUS_READY_FOR_REVIEW  =>
        effects.reply(Empty.defaultInstance)
      case _ =>
        effects.error(ERROR_WRONG_STATUS)
    }

  override def get(currentState: LoanProcDomainState, getCommand: api.GetCommand): EventSourcedEntity.Effect[api.LoanProcState] =
    currentState match {
      case LoanProcDomainState.defaultInstance =>
        effects.error(ERROR_NOT_FOUND)
      case _ =>
        effects.reply(map(currentState))
    }

  private def map(state: LoanProcDomainState): api.LoanProcState =
    api.LoanProcState(
      state.reviewerId,
      map(state.status),
      state.declineReason,
      state.lastUpdateTimestamp)

  private def map(status: LoanProcDomainStatus): api.LoanProcStatus =
    api.LoanProcStatus.fromValue(status.value)

  override def approve(currentState: LoanProcDomainState, approveCommand: api.ApproveCommand): EventSourcedEntity.Effect[Empty] =
    currentState match {
      case LoanProcDomainState.defaultInstance =>
        effects.error(ERROR_NOT_FOUND)
      case LoanProcDomainState(_,status,_,_,_) if status == LoanProcDomainStatus.STATUS_READY_FOR_REVIEW  =>
        val event = Approved(
          approveCommand.loanAppId,
          approveCommand.reviewerId,
          System.currentTimeMillis())
        effects.emitEvent(event)
          .thenReply(_ => Empty.defaultInstance)
      case LoanProcDomainState(_,status,_,_,_) if status == LoanProcDomainStatus.STATUS_APPROVED  =>
        effects.reply(Empty.defaultInstance)
      case _ =>
        effects.error(ERROR_WRONG_STATUS)
    }

  override def decline(currentState: LoanProcDomainState, declineCommand: api.DeclineCommand): EventSourcedEntity.Effect[Empty] =
    currentState match {
      case LoanProcDomainState.defaultInstance =>
        effects.error(ERROR_NOT_FOUND)
      case LoanProcDomainState(_,status,_,_,_) if status == LoanProcDomainStatus.STATUS_READY_FOR_REVIEW  =>
        val event = Declined(
          declineCommand.loanAppId,
          declineCommand.reviewerId,
          declineCommand.reason,
          System.currentTimeMillis())
        effects.emitEvent(event)
          .thenReply(_ => Empty.defaultInstance)
      case LoanProcDomainState(_,status,_,_,_) if status == LoanProcDomainStatus.STATUS_DECLINED  =>
        effects.reply(Empty.defaultInstance)
      case _ =>
        effects.error(ERROR_WRONG_STATUS)
    }

  override def processStarted(currentState: LoanProcDomainState, processStarted: ProcessStarted): LoanProcDomainState =
    LoanProcDomainState(
      "",
      LoanProcDomainStatus.STATUS_READY_FOR_REVIEW,
      "",
      processStarted.eventTimestamp
    )

  override def approved(currentState: LoanProcDomainState, approved: Approved): LoanProcDomainState =
    currentState
      .withStatus(LoanProcDomainStatus.STATUS_APPROVED)
      .withReviewerId(approved.reviewerId)
      .withLastUpdateTimestamp(approved.eventTimestamp)

  override def declined(currentState: LoanProcDomainState, declined: Declined): LoanProcDomainState =
    currentState
      .withStatus(LoanProcDomainStatus.STATUS_DECLINED)
      .withReviewerId(declined.reviewerId)
      .withDeclineReason(declined.reason)
      .withLastUpdateTimestamp(declined.eventTimestamp)

}
