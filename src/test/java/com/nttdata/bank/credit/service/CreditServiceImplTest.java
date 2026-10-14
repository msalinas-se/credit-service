package com.nttdata.bank.credit.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.nttdata.bank.credit.client.CustomerClient;
import com.nttdata.bank.credit.client.dto.CustomerDto;
import com.nttdata.bank.credit.config.CreditProperties;
import com.nttdata.bank.credit.dto.AmountRequest;
import com.nttdata.bank.credit.dto.CreditRequest;
import com.nttdata.bank.credit.exception.BusinessRuleException;
import com.nttdata.bank.credit.model.Credit;
import com.nttdata.bank.credit.model.CustomerType;
import com.nttdata.bank.credit.repository.CreditMovementRepository;
import com.nttdata.bank.credit.repository.CreditRepository;
import com.nttdata.bank.credit.service.impl.CreditServiceImpl;
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

  @Test
  void createPersonalCredit() {
    when(customerClient.findById("1"))
        .thenReturn(Single.just(new CustomerDto("1", CustomerType.PERSONAL, "45678912", "Ana")));
    when(creditRepository.countByCustomerId("1")).thenReturn(Single.just(0L));
    when(creditRepository.save(any(Credit.class)))
        .thenAnswer(invocation -> Single.just(invocation.getArgument(0)));

    creditService.create(new CreditRequest("1", new BigDecimal("5000"))).test()
        .assertValue(credit -> credit.getOutstandingBalance().intValue() == 5000);
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
  void paymentGreaterThanDebt() {
    Credit credit = Credit.builder().id("c1").amount(new BigDecimal("100"))
        .outstandingBalance(new BigDecimal("100")).build();
    when(creditRepository.findById("c1")).thenReturn(Maybe.just(credit));

    creditService.pay("c1", new AmountRequest(new BigDecimal("200"))).test()
        .assertError(BusinessRuleException.class);
  }
}
