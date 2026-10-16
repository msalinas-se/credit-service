package com.nttdata.bank.credit.service.impl;

import com.nttdata.bank.credit.client.CustomerClient;
import com.nttdata.bank.credit.config.CreditProperties;
import com.nttdata.bank.credit.dto.AmountRequest;
import com.nttdata.bank.credit.dto.BalanceResponse;
import com.nttdata.bank.credit.dto.CreditRequest;
import com.nttdata.bank.credit.dto.PaymentRequest;
import com.nttdata.bank.credit.exception.BusinessRuleException;
import com.nttdata.bank.credit.exception.ResourceNotFoundException;
import com.nttdata.bank.credit.model.Credit;
import com.nttdata.bank.credit.model.CreditMovement;
import com.nttdata.bank.credit.model.CreditMovementType;
import com.nttdata.bank.credit.model.CustomerType;
import com.nttdata.bank.credit.model.ProductType;
import com.nttdata.bank.credit.repository.CreditMovementRepository;
import com.nttdata.bank.credit.repository.CreditRepository;
import com.nttdata.bank.credit.service.CreditService;
import com.nttdata.bank.credit.service.DebtService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Implementation of CreditService backed by MongoDB. */
@Slf4j
@Service
@RequiredArgsConstructor
public class CreditServiceImpl implements CreditService {
  private final CreditRepository creditRepository;
  private final CreditMovementRepository movementRepository;
  private final CustomerClient customerClient;
  private final CreditProperties creditProperties;
  private final DebtService debtService;

  /** Creates a credit. */
  @Override
  public Single<Credit> create(CreditRequest request) {
    return customerClient.findById(request.customerId())
        .flatMap(customer -> debtService.validateNoOverdueDebt(customer.id())
            .toSingleDefault(customer))
        .flatMap(customer -> {
          Credit credit = Credit.builder()
              .customerId(customer.id())
              .customerType(customer.type())
              .amount(request.amount())
              .outstandingBalance(request.amount())
              .dueDate(request.dueDate() == null
                  ? LocalDate.now().plusMonths(1) : request.dueDate())
              .build();
          if (customer.type() == CustomerType.BUSINESS) {
            return creditRepository.save(credit);
          }
          return creditRepository.countByCustomerId(customer.id())
              .flatMap(count -> count >= creditProperties.personalMaxCredits()
                  ? Single.<Credit>error(new BusinessRuleException("CREDIT_LIMIT_REACHED",
                      "Personal customers can only have one credit"))
                  : creditRepository.save(credit));
        })
        .flatMap(credit -> debtService.publishStatus(credit.getCustomerId())
            .toSingleDefault(credit))
        .doOnSuccess(credit -> log.info("Credit created: {}", credit.getId()));
  }

  /** Lists credits, optionally by customer. */
  @Override
  public Flowable<Credit> findAll(String customerId) {
    return customerId == null
        ? creditRepository.findAll()
        : creditRepository.findByCustomerId(customerId);
  }

  /** Finds a credit by its identifier. */
  @Override
  public Single<Credit> findById(String id) {
    return creditRepository.findById(id)
        .switchIfEmpty(Single.error(new ResourceNotFoundException("Credit not found: " + id)));
  }

  /** Updates the amount of a credit. */
  @Override
  public Single<Credit> update(String id, AmountRequest request) {
    return findById(id)
        .flatMap(credit -> {
          credit.setAmount(request.amount());
          credit.setOutstandingBalance(request.amount());
          return creditRepository.save(credit);
        });
  }

  /** Deletes a credit. */
  @Override
  public Completable delete(String id) {
    return findById(id).flatMapCompletable(creditRepository::delete);
  }

  /** Registers a payment on a credit. */
  @Override
  public Single<CreditMovement> pay(String id, PaymentRequest request) {
    return findById(id)
        .flatMap(credit -> {
          if (request.amount().compareTo(credit.getOutstandingBalance()) > 0) {
            return Single.error(new BusinessRuleException("PAYMENT_EXCEEDS_DEBT",
                "Payment is greater than the outstanding balance"));
          }
          credit.setOutstandingBalance(credit.getOutstandingBalance().subtract(request.amount()));
          return creditRepository.save(credit)
              .flatMap(saved -> movementRepository.save(CreditMovement.builder()
                  .productId(id)
                  .productType(ProductType.CREDIT)
                  .type(CreditMovementType.PAYMENT)
                  .amount(request.amount())
                  .payerId(request.payerId())
                  .date(LocalDateTime.now())
                  .build()))
              .flatMap(movement -> debtService.publishStatus(credit.getCustomerId())
                  .toSingleDefault(movement));
        })
        .doOnSuccess(movement -> log.info("Payment of {} on credit {}", request.amount(), id));
  }

  /** Returns the balance of a credit. */
  @Override
  public Single<BalanceResponse> getBalance(String id) {
    return findById(id)
        .map(credit -> new BalanceResponse(id, credit.getAmount(), credit.getOutstandingBalance()));
  }

  /** Lists the movements of a credit. */
  @Override
  public Flowable<CreditMovement> findMovements(String id) {
    return movementRepository.findByProductIdOrderByDateDesc(id);
  }
}
