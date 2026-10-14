package com.nttdata.bank.credit.repository;

import com.nttdata.bank.credit.model.CreditMovement;
import io.reactivex.rxjava3.core.Flowable;
import org.springframework.data.repository.reactive.RxJava3CrudRepository;

/** Reactive repository for credit movements. */
public interface CreditMovementRepository extends RxJava3CrudRepository<CreditMovement, String> {

  /** Lists the movements of a credit product. */
  Flowable<CreditMovement> findByProductIdOrderByDateDesc(String productId);
}
