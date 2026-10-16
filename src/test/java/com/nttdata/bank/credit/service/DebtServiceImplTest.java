package com.nttdata.bank.credit.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.nttdata.bank.credit.event.DebtEventPublisher;
import com.nttdata.bank.credit.event.DebtStatusEvent;
import com.nttdata.bank.credit.exception.BusinessRuleException;
import com.nttdata.bank.credit.model.Credit;
import com.nttdata.bank.credit.model.CreditCard;
import com.nttdata.bank.credit.repository.CreditCardRepository;
import com.nttdata.bank.credit.repository.CreditRepository;
import com.nttdata.bank.credit.service.impl.DebtServiceImpl;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DebtServiceImplTest {

  @Mock
  private CreditRepository creditRepository;

  @Mock
  private CreditCardRepository creditCardRepository;

  @Mock
  private DebtEventPublisher debtEventPublisher;

  @InjectMocks
  private DebtServiceImpl debtService;

  private Credit credit(String balance, LocalDate dueDate) {
    return Credit.builder().customerId("1").amount(new BigDecimal("100"))
        .outstandingBalance(new BigDecimal(balance)).dueDate(dueDate).build();
  }

  private CreditCard card(String available, LocalDate dueDate) {
    return CreditCard.builder().customerId("1").creditLimit(new BigDecimal("100"))
        .availableCredit(new BigDecimal(available)).paymentDueDate(dueDate).build();
  }

  @Test
  void overdueCredit() {
    when(creditRepository.findByCustomerId("1"))
        .thenReturn(Flowable.just(credit("50", LocalDate.now().minusDays(1))));
    when(creditCardRepository.findByCustomerId("1")).thenReturn(Flowable.empty());

    debtService.hasOverdueDebt("1").test().assertValue(true);
  }

  @Test
  void overdueCreditCard() {
    when(creditRepository.findByCustomerId("1")).thenReturn(Flowable.empty());
    when(creditCardRepository.findByCustomerId("1"))
        .thenReturn(Flowable.just(card("40", LocalDate.now().minusDays(3))));

    debtService.validateNoOverdueDebt("1").test().assertError(BusinessRuleException.class);
  }

  @Test
  void paidOrNotExpiredProductsAreNotOverdue() {
    when(creditRepository.findByCustomerId("1")).thenReturn(Flowable.just(
        credit("0", LocalDate.now().minusDays(1)), credit("50", LocalDate.now().plusDays(5))));
    when(creditCardRepository.findByCustomerId("1"))
        .thenReturn(Flowable.just(card("100", LocalDate.now().minusDays(1))));

    debtService.validateNoOverdueDebt("1").test().assertComplete();
  }

  @Test
  void publishStatus() {
    when(creditRepository.findByCustomerId("1")).thenReturn(Flowable.empty());
    when(creditCardRepository.findByCustomerId("1")).thenReturn(Flowable.empty());
    when(debtEventPublisher.publish(new DebtStatusEvent("1", false)))
        .thenReturn(Completable.complete());

    debtService.publishStatus("1").test().assertComplete();
  }

  @Test
  void publishAllCustomersOnce() {
    when(creditRepository.findAll()).thenReturn(Flowable.just(credit("0", null)));
    when(creditCardRepository.findAll()).thenReturn(Flowable.just(card("100", null)));
    when(creditRepository.findByCustomerId("1")).thenReturn(Flowable.empty());
    when(creditCardRepository.findByCustomerId("1")).thenReturn(Flowable.empty());
    when(debtEventPublisher.publish(any())).thenReturn(Completable.complete());

    debtService.publishAll().test().assertComplete();
    verify(debtEventPublisher, times(1)).publish(any());
  }
}
