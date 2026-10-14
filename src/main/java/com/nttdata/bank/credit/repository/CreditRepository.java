package com.nttdata.bank.credit.repository;

import com.nttdata.bank.credit.model.Credit;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import org.springframework.data.repository.reactive.RxJava3CrudRepository;

/** Reactive repository for Credit documents based on RxJava. */
public interface CreditRepository extends RxJava3CrudRepository<Credit, String> {

  /** Lists the credits of a customer. */
  Flowable<Credit> findByCustomerId(String customerId);

  /** Counts the credits of a customer. */
  Single<Long> countByCustomerId(String customerId);
}
