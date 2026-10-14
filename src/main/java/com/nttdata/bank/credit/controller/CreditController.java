package com.nttdata.bank.credit.controller;

import com.nttdata.bank.credit.dto.AmountRequest;
import com.nttdata.bank.credit.dto.BalanceResponse;
import com.nttdata.bank.credit.dto.CreditRequest;
import com.nttdata.bank.credit.model.Credit;
import com.nttdata.bank.credit.model.CreditMovement;
import com.nttdata.bank.credit.service.CreditService;
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

/** REST endpoints for personal and business credits. */
@RestController
@RequestMapping("/credits")
@RequiredArgsConstructor
public class CreditController {
  private final CreditService creditService;

  /** Grants a credit. */
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Single<Credit> create(@Valid @RequestBody CreditRequest request) {
    return creditService.create(request);
  }

  /** Lists credits, optionally filtered by customer. */
  @GetMapping
  public Flowable<Credit> findAll(@RequestParam(required = false) String customerId) {
    return creditService.findAll(customerId);
  }

  /** Gets a credit by identifier. */
  @GetMapping("/{id}")
  public Single<Credit> findById(@PathVariable String id) {
    return creditService.findById(id);
  }

  /** Changes the amount of a credit without payments. */
  @PutMapping("/{id}")
  public Single<Credit> update(@PathVariable String id,
      @Valid @RequestBody AmountRequest request) {
    return creditService.update(id, request);
  }

  /** Deletes a credit. */
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public Completable delete(@PathVariable String id) {
    return creditService.delete(id);
  }

  /** Registers a payment on a credit. */
  @PostMapping("/{id}/payments")
  @ResponseStatus(HttpStatus.CREATED)
  public Single<CreditMovement> pay(@PathVariable String id,
      @Valid @RequestBody AmountRequest request) {
    return creditService.pay(id, request);
  }

  /** Gets the balance of a credit. */
  @GetMapping("/{id}/balance")
  public Single<BalanceResponse> getBalance(@PathVariable String id) {
    return creditService.getBalance(id);
  }

  /** Lists the movements of a credit, newest first. */
  @GetMapping("/{id}/movements")
  public Flowable<CreditMovement> findMovements(@PathVariable String id) {
    return creditService.findMovements(id);
  }
}
