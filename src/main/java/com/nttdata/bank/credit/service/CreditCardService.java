package com.nttdata.bank.credit.service;

import com.nttdata.bank.credit.dto.AmountRequest;
import com.nttdata.bank.credit.dto.BalanceResponse;
import com.nttdata.bank.credit.dto.CreditCardRequest;
import com.nttdata.bank.credit.dto.PaymentRequest;
import com.nttdata.bank.credit.model.CreditCard;
import com.nttdata.bank.credit.model.CreditMovement;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/** Operations over personal and business credit cards. */
public interface CreditCardService {

  /** Creates a credit card. */
  Single<CreditCard> create(CreditCardRequest request);

  /** Lists credit cards, optionally by customer. */
  Flowable<CreditCard> findAll(String customerId);

  /** Finds a credit card by its identifier. */
  Single<CreditCard> findById(String id);

  /** Updates the credit limit of a card. */
  Single<CreditCard> update(String id, AmountRequest request);

  /** Deletes a credit card. */
  Completable delete(String id);

  /** Charges a purchase to a card. */
  Single<CreditMovement> charge(String id, AmountRequest request);

  /** Registers a payment on a card. */
  Single<CreditMovement> pay(String id, PaymentRequest request);

  /** Returns the balance of a card. */
  Single<BalanceResponse> getBalance(String id);

  /** Lists the movements of a card. */
  Flowable<CreditMovement> findMovements(String id);
}
