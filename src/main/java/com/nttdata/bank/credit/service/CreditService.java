package com.nttdata.bank.credit.service;

import com.nttdata.bank.credit.dto.AmountRequest;
import com.nttdata.bank.credit.dto.BalanceResponse;
import com.nttdata.bank.credit.dto.CreditRequest;
import com.nttdata.bank.credit.model.Credit;
import com.nttdata.bank.credit.model.CreditMovement;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;

/** Operations over personal and business credits. */
public interface CreditService {

  /** Creates a credit. */
  Single<Credit> create(CreditRequest request);

  /** Lists credits, optionally by customer. */
  Flowable<Credit> findAll(String customerId);

  /** Finds a credit by its identifier. */
  Single<Credit> findById(String id);

  /** Updates the amount of a credit. */
  Single<Credit> update(String id, AmountRequest request);

  /** Deletes a credit. */
  Completable delete(String id);

  /** Registers a payment on a credit. */
  Single<CreditMovement> pay(String id, AmountRequest request);

  /** Returns the balance of a credit. */
  Single<BalanceResponse> getBalance(String id);

  /** Lists the movements of a credit. */
  Flowable<CreditMovement> findMovements(String id);
}
