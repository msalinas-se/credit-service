package com.nttdata.bank.credit.controller;

import com.nttdata.bank.credit.dto.ProductReport;
import com.nttdata.bank.credit.model.CreditMovement;
import com.nttdata.bank.credit.model.ProductType;
import com.nttdata.bank.credit.service.ReportService;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import java.time.LocalDate;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** REST endpoints for the credit reports. */
@RestController
@RequiredArgsConstructor
public class ReportController {
  private final ReportService reportService;

  /** General report by product between two dates. */
  @GetMapping("/credits/report")
  public Single<Map<ProductType, ProductReport>> productReport(
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    return reportService.productReport(from, to);
  }

  /** Last 10 movements of a credit card. */
  @GetMapping("/credit-cards/{id}/movements/last")
  public Flowable<CreditMovement> lastCardMovements(@PathVariable String id) {
    return reportService.lastCardMovements(id);
  }
}
