package com.nttdata.bank.credit.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.nttdata.bank.credit.client.CustomerClient;
import com.nttdata.bank.credit.client.dto.CustomerDto;
import com.nttdata.bank.credit.config.CreditProperties;
import com.nttdata.bank.credit.dto.AmountRequest;
import com.nttdata.bank.credit.dto.CreditRequest;
import com.nttdata.bank.credit.exception.BusinessRuleException;
import com.nttdata.bank.credit.exception.ResourceNotFoundException;
import com.nttdata.bank.credit.model.Credit;
import com.nttdata.bank.credit.model.CreditMovement;
import com.nttdata.bank.credit.model.CustomerType;
import com.nttdata.bank.credit.repository.CreditMovementRepository;
import com.nttdata.bank.credit.repository.CreditRepository;
import com.nttdata.bank.credit.service.impl.CreditServiceImpl;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreditServiceImplTest {

  @Mock
  private CreditRepository creditRepository;

  @Mock
  private CreditMovementRepository movementRepository;

  @Mock
  private CustomerClient customerClient;

  private CreditServiceImpl creditService;

  @BeforeEach
  void setUp() {
    creditService = new CreditServiceImpl(creditRepository, movementRepository, customerClient,
        new CreditProperties(1));
  }

  private Credit credit() {
    return Credit.builder().id("c1").amount(new BigDecimal("100"))
        .outstandingBalance(new BigDecimal("100")).build();
  }

  private void saveReturnsArgument() {
    when(creditRepository.save(any(Credit.class)))
        .thenAnswer(invocation -> Single.just(invocation.getArgument(0)));
  }

  @Test
  void createPersonalCredit() {
    when(customerClient.findById("1"))
        .thenReturn(Single.just(new CustomerDto("1", CustomerType.PERSONAL, "45678912", "Ana")));
    when(creditRepository.countByCustomerId("1")).thenReturn(Single.just(0L));
    saveReturnsArgument();

    creditService.create(new CreditRequest("1", new BigDecimal("5000"))).test()
        .assertValue(credit -> credit.getOutstandingBalance().intValue() == 5000);
  }

  @Test
  void createBusinessCredit() {
    when(customerClient.findById("2"))
        .thenReturn(Single.just(new CustomerDto("2", CustomerType.BUSINESS, "20123456789", "Acme")));
    saveReturnsArgument();

    creditService.create(new CreditRequest("2", new BigDecimal("5000"))).test().assertComplete();
  }

  @Test
  void personalCustomerCannotHaveTwoCredits() {
    when(customerClient.findById("1"))
        .thenReturn(Single.just(new CustomerDto("1", CustomerType.PERSONAL, "45678912", "Ana")));
    when(creditRepository.countByCustomerId("1")).thenReturn(Single.just(1L));

    creditService.create(new CreditRequest("1", new BigDecimal("5000"))).test()
        .assertError(BusinessRuleException.class);
  }

  @Test
  void findAllCredits() {
    when(creditRepository.findAll()).thenReturn(Flowable.just(credit()));

    creditService.findAll(null).test().assertValueCount(1);
  }

  @Test
  void findCreditsByCustomer() {
    when(creditRepository.findByCustomerId("1")).thenReturn(Flowable.just(credit()));

    creditService.findAll("1").test().assertValueCount(1);
  }

  @Test
  void findCreditNotFound() {
    when(creditRepository.findById("x")).thenReturn(Maybe.empty());

    creditService.findById("x").test().assertError(ResourceNotFoundException.class);
  }

  @Test
  void updateCredit() {
    when(creditRepository.findById("c1")).thenReturn(Maybe.just(credit()));
    saveReturnsArgument();

    creditService.update("c1", new AmountRequest(new BigDecimal("300"))).test()
        .assertValue(credit -> credit.getAmount().intValue() == 300);
  }

  @Test
  void deleteCredit() {
    Credit credit = credit();
    when(creditRepository.findById("c1")).thenReturn(Maybe.just(credit));
    when(creditRepository.delete(credit)).thenReturn(Completable.complete());

    creditService.delete("c1").test().assertComplete();
  }

  @Test
  void payCredit() {
    when(creditRepository.findById("c1")).thenReturn(Maybe.just(credit()));
    saveReturnsArgument();
    when(movementRepository.save(any(CreditMovement.class)))
        .thenAnswer(invocation -> Single.just(invocation.getArgument(0)));

    creditService.pay("c1", new AmountRequest(new BigDecimal("40"))).test()
        .assertValue(movement -> movement.getAmount().intValue() == 40);
  }

  @Test
  void paymentGreaterThanDebt() {
    when(creditRepository.findById("c1")).thenReturn(Maybe.just(credit()));

    creditService.pay("c1", new AmountRequest(new BigDecimal("200"))).test()
        .assertError(BusinessRuleException.class);
  }

  @Test
  void creditBalance() {
    when(creditRepository.findById("c1")).thenReturn(Maybe.just(credit()));

    creditService.getBalance("c1").test()
        .assertValue(balance -> balance.available().intValue() == 100);
  }

  @Test
  void creditMovements() {
    when(movementRepository.findByProductIdOrderByDateDesc("c1"))
        .thenReturn(Flowable.just(new CreditMovement()));

    creditService.findMovements("c1").test().assertValueCount(1);
  }
}
