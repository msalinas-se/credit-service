package com.nttdata.bank.credit.service.impl;

import com.nttdata.bank.credit.dto.ProductReport;
import com.nttdata.bank.credit.model.CreditMovement;
import com.nttdata.bank.credit.model.ProductType;
import com.nttdata.bank.credit.repository.CreditMovementRepository;
import com.nttdata.bank.credit.service.ReportService;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Implementation of ReportService. */
@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
  private final CreditMovementRepository movementRepository;

  /** General report by product between two dates. */
  @Override
  public Single<Map<ProductType, ProductReport>> productReport(LocalDate from, LocalDate to) {
    return movementRepository.findByDateBetween(from.atStartOfDay(), to.plusDays(1).atStartOfDay())
        .toList()
        .map(movements -> movements.stream()
            .collect(Collectors.groupingBy(CreditMovement::getProductType,
                Collectors.collectingAndThen(Collectors.toList(), this::toReport))));
  }

  /** Last 10 movements of a credit card. */
  @Override
  public Flowable<CreditMovement> lastCardMovements(String creditCardId) {
    return movementRepository.findTop10ByProductIdOrderByDateDesc(creditCardId);
  }

  private ProductReport toReport(List<CreditMovement> movements) {
    long products = movements.stream().map(CreditMovement::getProductId).distinct().count();
    var totals = movements.stream()
        .collect(Collectors.groupingBy(CreditMovement::getType,
            Collectors.reducing(BigDecimal.ZERO, CreditMovement::getAmount, BigDecimal::add)));
    return new ProductReport(products, movements.size(), totals, movements);
  }
}
