package com.nttdata.bank.credit.service.impl;

import com.nttdata.bank.credit.event.DebtEventPublisher;
import com.nttdata.bank.credit.event.DebtStatusEvent;
import com.nttdata.bank.credit.exception.BusinessRuleException;
import com.nttdata.bank.credit.model.Credit;
import com.nttdata.bank.credit.model.CreditCard;
import com.nttdata.bank.credit.repository.CreditCardRepository;
import com.nttdata.bank.credit.repository.CreditRepository;
import com.nttdata.bank.credit.service.DebtService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Implementation of DebtService. */
@Service
@RequiredArgsConstructor
public class DebtServiceImpl implements DebtService {
  private final CreditRepository creditRepository;
  private final CreditCardRepository creditCardRepository;
  private final DebtEventPublisher debtEventPublisher;

  /** Checks if the customer has an overdue debt in a credit or credit card. */
  @Override
  public Single<Boolean> hasOverdueDebt(String customerId) {
    Single<Boolean> credits = creditRepository.findByCustomerId(customerId)
        .any(Credit::isOverdue);
    Single<Boolean> cards = creditCardRepository.findByCustomerId(customerId)
        .any(CreditCard::isOverdue);
    return Single.zip(credits, cards, (credit, card) -> credit || card);
  }

  /** Validates that the customer has no overdue debt. */
  @Override
  public Completable validateNoOverdueDebt(String customerId) {
    return hasOverdueDebt(customerId)
        .flatMapCompletable(overdue -> overdue
            ? Completable.error(new BusinessRuleException("OVERDUE_DEBT",
                "The customer has an overdue debt in a credit product"))
            : Completable.complete());
  }

  /** Publishes the current debt status of the customer. */
  @Override
  public Completable publishStatus(String customerId) {
    return hasOverdueDebt(customerId)
        .flatMapCompletable(overdue ->
            debtEventPublisher.publish(new DebtStatusEvent(customerId, overdue)));
  }

  /** Publishes the debt status of every customer with credit products. */
  @Override
  public Completable publishAll() {
    return Flowable.merge(
            creditRepository.findAll().map(Credit::getCustomerId),
            creditCardRepository.findAll().map(CreditCard::getCustomerId))
        .distinct()
        .flatMapCompletable(this::publishStatus);
  }
}
