package com.nttdata.bank.credit.service.impl;

import com.nttdata.bank.credit.client.CustomerClient;
import com.nttdata.bank.credit.dto.AmountRequest;
import com.nttdata.bank.credit.dto.BalanceResponse;
import com.nttdata.bank.credit.dto.CreditCardRequest;
import com.nttdata.bank.credit.exception.BusinessRuleException;
import com.nttdata.bank.credit.exception.ResourceNotFoundException;
import com.nttdata.bank.credit.model.CreditCard;
import com.nttdata.bank.credit.model.CreditMovement;
import com.nttdata.bank.credit.model.CreditMovementType;
import com.nttdata.bank.credit.model.ProductType;
import com.nttdata.bank.credit.repository.CreditCardRepository;
import com.nttdata.bank.credit.repository.CreditMovementRepository;
import com.nttdata.bank.credit.service.CreditCardService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Implementation of CreditCardService backed by MongoDB. */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreditCardServiceImpl implements CreditCardService {
  private final CreditCardRepository creditCardRepository;
  private final CreditMovementRepository movementRepository;
  private final CustomerClient customerClient;

  /** Creates a credit card. */
  @Override
  public Single<CreditCard> create(CreditCardRequest request) {
    return customerClient.findById(request.customerId())
        .flatMap(customer -> creditCardRepository.save(CreditCard.builder()
            .cardNumber(String.valueOf(ThreadLocalRandom.current()
                .nextLong(4_000_000_000_000_000L, 4_999_999_999_999_999L)))
            .customerId(customer.id())
            .customerType(customer.type())
            .creditLimit(request.creditLimit())
            .availableCredit(request.creditLimit())
            .build()))
        .doOnSuccess(card -> log.info("Credit card created: {}", card.getId()));
  }

  /** Lists credit cards, optionally by customer. */
  @Override
  public Flowable<CreditCard> findAll(String customerId) {
    return customerId == null
        ? creditCardRepository.findAll()
        : creditCardRepository.findByCustomerId(customerId);
  }

  /** Finds a credit card by its identifier. */
  @Override
  public Single<CreditCard> findById(String id) {
    return creditCardRepository.findById(id)
        .switchIfEmpty(Single.error(new ResourceNotFoundException("Credit card not found: " + id)));
  }

  /** Updates the credit limit of a card. */
  @Override
  public Single<CreditCard> update(String id, AmountRequest request) {
    return findById(id)
        .flatMap(card -> {
          BigDecimal used = card.getCreditLimit().subtract(card.getAvailableCredit());
          card.setCreditLimit(request.amount());
          card.setAvailableCredit(request.amount().subtract(used));
          return creditCardRepository.save(card);
        });
  }

  /** Deletes a credit card. */
  @Override
  public Completable delete(String id) {
    return findById(id).flatMapCompletable(creditCardRepository::delete);
  }

  /** Charges a purchase to a card. */
  @Override
  public Single<CreditMovement> charge(String id, AmountRequest request) {
    return findById(id)
        .flatMap(card -> {
          if (request.amount().compareTo(card.getAvailableCredit()) > 0) {
            return Single.error(new BusinessRuleException("CREDIT_LIMIT_EXCEEDED",
                "Charge is greater than the available credit"));
          }
          card.setAvailableCredit(card.getAvailableCredit().subtract(request.amount()));
          return save(card, CreditMovementType.CHARGE, request);
        });
  }

  /** Registers a payment on a card. */
  @Override
  public Single<CreditMovement> pay(String id, AmountRequest request) {
    return findById(id)
        .flatMap(card -> {
          BigDecimal used = card.getCreditLimit().subtract(card.getAvailableCredit());
          if (request.amount().compareTo(used) > 0) {
            return Single.error(new BusinessRuleException("PAYMENT_EXCEEDS_DEBT",
                "Payment is greater than the used credit"));
          }
          card.setAvailableCredit(card.getAvailableCredit().add(request.amount()));
          return save(card, CreditMovementType.PAYMENT, request);
        });
  }

  /** Returns the balance of a card. */
  @Override
  public Single<BalanceResponse> getBalance(String id) {
    return findById(id)
        .map(card -> new BalanceResponse(id, card.getCreditLimit(), card.getAvailableCredit()));
  }

  /** Lists the movements of a card. */
  @Override
  public Flowable<CreditMovement> findMovements(String id) {
    return movementRepository.findByProductIdOrderByDateDesc(id);
  }

  private Single<CreditMovement> save(CreditCard card, CreditMovementType type,
      AmountRequest request) {
    return creditCardRepository.save(card)
        .flatMap(saved -> movementRepository.save(CreditMovement.builder()
            .productId(saved.getId())
            .productType(ProductType.CREDIT_CARD)
            .type(type)
            .amount(request.amount())
            .date(LocalDateTime.now())
            .build()))
        .doOnSuccess(movement -> log.info("{} of {} on card {}", type, request.amount(),
            card.getId()));
  }
}
