package com.nttdata.bank.credit.controller;

import com.nttdata.bank.credit.dto.AmountRequest;
import com.nttdata.bank.credit.dto.BalanceResponse;
import com.nttdata.bank.credit.dto.CreditCardRequest;
import com.nttdata.bank.credit.model.CreditCard;
import com.nttdata.bank.credit.model.CreditMovement;
import com.nttdata.bank.credit.service.CreditCardService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** REST endpoints for personal and business credit cards. */
@RestController
@RequestMapping("/credit-cards")
@RequiredArgsConstructor
public class CreditCardController {
  private final CreditCardService creditCardService;

  /** Issues a credit card. */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Single<CreditCard> create(@Valid @RequestBody CreditCardRequest request) {
    return creditCardService.create(request);
  }

  /** Lists credit cards, optionally filtered by customer. */
  @GetMapping
  public Flowable<CreditCard> findAll(
      @RequestParam(required = false) String customerId) {
    return creditCardService.findAll(customerId);
  }

  /** Gets a credit card by identifier. */
  @GetMapping("/{id}")
  public Single<CreditCard> findById(@PathVariable String id) {
    return creditCardService.findById(id);
  }

  /** Changes the credit limit of a card. */
  @PutMapping("/{id}")
  public Single<CreditCard> update(@PathVariable String id,
      @Valid @RequestBody AmountRequest request) {
    return creditCardService.update(id, request);
  }

  /** Deletes a credit card. */
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Completable delete(@PathVariable String id) {
    return creditCardService.delete(id);
  }

  /** Charges a purchase to a card. */
  @PostMapping("/{id}/charges")
  @ResponseStatus(HttpStatus.CREATED)
  public Single<CreditMovement> charge(@PathVariable String id,
      @Valid @RequestBody AmountRequest request) {
    return creditCardService.charge(id, request);
  }

  /** Registers a payment on a card. */
  @PostMapping("/{id}/payments")
  @ResponseStatus(HttpStatus.CREATED)
  public Single<CreditMovement> pay(@PathVariable String id,
      @Valid @RequestBody AmountRequest request) {
    return creditCardService.pay(id, request);
  }

  /** Gets the balance of a credit card. */
  @GetMapping("/{id}/balance")
  public Single<BalanceResponse> getBalance(@PathVariable String id) {
    return creditCardService.getBalance(id);
  }

  /** Lists the movements of a credit card, newest first. */
  @GetMapping("/{id}/movements")
  public Flowable<CreditMovement> findMovements(@PathVariable String id) {
    return creditCardService.findMovements(id);
  }
}
