package io.kx.loanproc.view

import io.kx.loanproc.api
import com.google.protobuf.any.{Any => ScalaPbAny}
import io.kx.loanproc.domain.{Approved, Declined, LoanProcDomainStatus, ProcessStarted}
import kalix.scalasdk.view.View.UpdateEffect
import kalix.scalasdk.view.ViewContext

// This class was initially generated based on the .proto definition by Kalix tooling.
//
// As long as this file exists it will not be overwritten: you can maintain it yourself,
// or delete it so it is regenerated as needed.

class LoanProcByStatusView(context: ViewContext) extends AbstractLoanProcByStatusView {

  override def emptyState: LoanProcViewState = LoanProcViewState.defaultInstance

  override def onSubmitted(
    state: LoanProcViewState, processStarted: ProcessStarted): UpdateEffect[LoanProcViewState] = {
    val newState = LoanProcViewState(
      api.LoanProcStatus.STATUS_READY_FOR_REVIEW.value,
      api.LoanProcStatus.STATUS_READY_FOR_REVIEW,
      processStarted.loanAppId,
      processStarted.eventTimestamp
    )
    effects.updateState(newState)
  }

  override def onApproved(
    state: LoanProcViewState, approved: Approved): UpdateEffect[LoanProcViewState] = {
    val newState = state
      .withStatusId(api.LoanProcStatus.STATUS_APPROVED.value)
      .withStatus(api.LoanProcStatus.STATUS_APPROVED)
      .withLastUpdateTimestamp(approved.eventTimestamp)
    effects.updateState(newState)
  }

  override def onDeclined(
    state: LoanProcViewState, declined: Declined): UpdateEffect[LoanProcViewState] = {
    val newState = state
      .withStatusId(api.LoanProcStatus.STATUS_DECLINED.value)
      .withStatus(api.LoanProcStatus.STATUS_DECLINED)
      .withLastUpdateTimestamp(declined.eventTimestamp)
    effects.updateState(newState)
  }


  override def ignoreOtherEvents(
    state: LoanProcViewState, any: ScalaPbAny): UpdateEffect[LoanProcViewState] =
    effects.ignore()
}
