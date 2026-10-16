package com.nttdata.bank.credit.service;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

/** Overdue debt of the customers in their credit products. */
public interface DebtService {

  /** Checks if the customer has an overdue debt in a credit or credit card. */
  Single<Boolean> hasOverdueDebt(String customerId);

  /** Validates that the customer has no overdue debt. */
  Completable validateNoOverdueDebt(String customerId);

  /** Publishes the current debt status of the customer. */
  Completable publishStatus(String customerId);

  /** Publishes the debt status of every customer with credit products. */
  Completable publishAll();
}
