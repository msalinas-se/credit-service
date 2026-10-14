package com.nttdata.bank.credit.repository;

import com.nttdata.bank.credit.model.CreditCard;
import io.reactivex.rxjava3.core.Flowable;
import org.springframework.data.repository.reactive.RxJava3CrudRepository;

/** Reactive repository for CreditCard documents based on RxJava. */
public interface CreditCardRepository extends RxJava3CrudRepository<CreditCard, String> {

  /** Lists the credit cards of a customer. */
  Flowable<CreditCard> findByCustomerId(String customerId);
}
