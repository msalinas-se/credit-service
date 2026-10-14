package com.nttdata.bank.credit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.nttdata.bank.credit.client.CustomerClient;
import com.nttdata.bank.credit.dto.AmountRequest;
import com.nttdata.bank.credit.exception.BusinessRuleException;
import com.nttdata.bank.credit.model.CreditCard;
import com.nttdata.bank.credit.model.CreditMovement;
import com.nttdata.bank.credit.repository.CreditCardRepository;
import com.nttdata.bank.credit.repository.CreditMovementRepository;
import com.nttdata.bank.credit.service.impl.CreditCardServiceImpl;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreditCardServiceImplTest {

  @Mock
  private CreditCardRepository creditCardRepository;

  @Mock
  private CreditMovementRepository movementRepository;

  @Mock
  private CustomerClient customerClient;

  private CreditCardServiceImpl creditCardService;

  @BeforeEach
  void setUp() {
    creditCardService = new CreditCardServiceImpl(creditCardRepository, movementRepository,
        customerClient);
  }

  private CreditCard card() {
    return CreditCard.builder().id("cc1").creditLimit(new BigDecimal("1000"))
        .availableCredit(new BigDecimal("1000")).build();
  }

  @Test
  void chargeReducesAvailableCredit() {
    CreditCard card = card();
    when(creditCardRepository.findById("cc1")).thenReturn(Maybe.just(card));
    when(creditCardRepository.save(any(CreditCard.class)))
        .thenAnswer(invocation -> Single.just(invocation.getArgument(0)));
    when(movementRepository.save(any(CreditMovement.class)))
        .thenAnswer(invocation -> Single.just(invocation.getArgument(0)));

    creditCardService.charge("cc1", new AmountRequest(new BigDecimal("300"))).test()
        .assertComplete();
    assertEquals(0,
        new BigDecimal("700").compareTo(card.getAvailableCredit()));
  }

  @Test
  void chargeOverLimit() {
    when(creditCardRepository.findById("cc1")).thenReturn(Maybe.just(card()));

    creditCardService.charge("cc1", new AmountRequest(new BigDecimal("1500"))).test()
        .assertError(BusinessRuleException.class);
  }
}
