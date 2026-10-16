package com.nttdata.bank.credit.repository;

import com.nttdata.bank.credit.model.CreditMovement;
import io.reactivex.rxjava3.core.Flowable;
import java.time.LocalDateTime;
import org.springframework.data.repository.reactive.RxJava3CrudRepository;

/** Reactive repository for credit movements. */
public interface CreditMovementRepository extends RxJava3CrudRepository<CreditMovement, String> {

  /** Lists the movements of a credit product. */
  Flowable<CreditMovement> findByProductIdOrderByDateDesc(String productId);

  /** Lists the last 10 movements of a credit product. */
  Flowable<CreditMovement> findTop10ByProductIdOrderByDateDesc(String productId);

  /** Lists the movements registered between two dates. */
  Flowable<CreditMovement> findByDateBetween(LocalDateTime from, LocalDateTime to);
}
