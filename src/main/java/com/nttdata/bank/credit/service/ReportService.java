package com.nttdata.bank.credit.service;

import com.nttdata.bank.credit.dto.ProductReport;
import com.nttdata.bank.credit.model.CreditMovement;
import com.nttdata.bank.credit.model.ProductType;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import java.time.LocalDate;
import java.util.Map;

/** Reports of the credit products. */
public interface ReportService {

  /** General report by product between two dates. */
  Single<Map<ProductType, ProductReport>> productReport(LocalDate from, LocalDate to);

  /** Last 10 movements of a credit card. */
  Flowable<CreditMovement> lastCardMovements(String creditCardId);
}
