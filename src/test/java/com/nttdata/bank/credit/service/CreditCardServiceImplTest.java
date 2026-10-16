package com.nttdata.bank.credit.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.nttdata.bank.credit.client.CustomerClient;
import com.nttdata.bank.credit.client.dto.CustomerDto;
import com.nttdata.bank.credit.dto.AmountRequest;
import com.nttdata.bank.credit.dto.CreditCardRequest;
import com.nttdata.bank.credit.exception.BusinessRuleException;
import com.nttdata.bank.credit.exception.ResourceNotFoundException;
import com.nttdata.bank.credit.model.CreditCard;
import com.nttdata.bank.credit.model.CreditMovement;
import com.nttdata.bank.credit.model.CustomerType;
import com.nttdata.bank.credit.repository.CreditCardRepository;
import com.nttdata.bank.credit.repository.CreditMovementRepository;
import com.nttdata.bank.credit.service.impl.CreditCardServiceImpl;
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

  private CreditCard card(String available) {
    return CreditCard.builder().id("cc1").creditLimit(new BigDecimal("1000"))
        .availableCredit(new BigDecimal(available)).build();
  }

  private void saveCardAndMovement() {
    when(creditCardRepository.save(any(CreditCard.class)))
        .thenAnswer(invocation -> Single.just(invocation.getArgument(0)));
    when(movementRepository.save(any(CreditMovement.class)))
        .thenAnswer(invocation -> Single.just(invocation.getArgument(0)));
  }

  @Test
  void createCreditCard() {
    when(customerClient.findById("1"))
        .thenReturn(Single.just(new CustomerDto("1", CustomerType.PERSONAL, "45678912", "Ana")));
    when(creditCardRepository.save(any(CreditCard.class)))
        .thenAnswer(invocation -> Single.just(invocation.getArgument(0)));

    creditCardService.create(new CreditCardRequest("1", new BigDecimal("1000"))).test()
        .assertValue(card -> card.getCardNumber().length() == 16);
  }

  @Test
  void findAllCards() {
    when(creditCardRepository.findAll()).thenReturn(Flowable.just(card("1000")));

    creditCardService.findAll(null).test().assertValueCount(1);
  }

  @Test
  void findCardsByCustomer() {
    when(creditCardRepository.findByCustomerId("1")).thenReturn(Flowable.just(card("1000")));

    creditCardService.findAll("1").test().assertValueCount(1);
  }

  @Test
  void findCardNotFound() {
    when(creditCardRepository.findById("x")).thenReturn(Maybe.empty());

    creditCardService.findById("x").test().assertError(ResourceNotFoundException.class);
  }

  @Test
  void updateCreditLimit() {
    when(creditCardRepository.findById("cc1")).thenReturn(Maybe.just(card("700")));
    when(creditCardRepository.save(any(CreditCard.class)))
        .thenAnswer(invocation -> Single.just(invocation.getArgument(0)));

    creditCardService.update("cc1", new AmountRequest(new BigDecimal("2000"))).test()
        .assertValue(card -> card.getAvailableCredit().intValue() == 1700);
  }

  @Test
  void deleteCard() {
    CreditCard card = card("1000");
    when(creditCardRepository.findById("cc1")).thenReturn(Maybe.just(card));
    when(creditCardRepository.delete(card)).thenReturn(Completable.complete());

    creditCardService.delete("cc1").test().assertComplete();
  }

  @Test
  void chargeReducesAvailableCredit() {
    CreditCard card = card("1000");
    when(creditCardRepository.findById("cc1")).thenReturn(Maybe.just(card));
    saveCardAndMovement();

    creditCardService.charge("cc1", new AmountRequest(new BigDecimal("300"))).test()
        .assertComplete();
    assertEquals(0, new BigDecimal("700").compareTo(card.getAvailableCredit()));
  }

  @Test
  void chargeOverLimit() {
    when(creditCardRepository.findById("cc1")).thenReturn(Maybe.just(card("1000")));

    creditCardService.charge("cc1", new AmountRequest(new BigDecimal("1500"))).test()
        .assertError(BusinessRuleException.class);
  }

  @Test
  void payCard() {
    CreditCard card = card("600");
    when(creditCardRepository.findById("cc1")).thenReturn(Maybe.just(card));
    saveCardAndMovement();

    creditCardService.pay("cc1", new AmountRequest(new BigDecimal("400"))).test()
        .assertComplete();
    assertEquals(0, new BigDecimal("1000").compareTo(card.getAvailableCredit()));
  }

  @Test
  void payMoreThanUsed() {
    when(creditCardRepository.findById("cc1")).thenReturn(Maybe.just(card("900")));

    creditCardService.pay("cc1", new AmountRequest(new BigDecimal("400"))).test()
        .assertError(BusinessRuleException.class);
  }

  @Test
  void cardBalance() {
    when(creditCardRepository.findById("cc1")).thenReturn(Maybe.just(card("600")));

    creditCardService.getBalance("cc1").test()
        .assertValue(balance -> balance.available().intValue() == 600);
  }

  @Test
  void cardMovements() {
    when(movementRepository.findByProductIdOrderByDateDesc("cc1"))
        .thenReturn(Flowable.just(new CreditMovement()));

    creditCardService.findMovements("cc1").test().assertValueCount(1);
  }
}
