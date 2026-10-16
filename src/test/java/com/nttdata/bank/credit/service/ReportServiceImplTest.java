package com.nttdata.bank.credit.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.nttdata.bank.credit.model.CreditMovement;
import com.nttdata.bank.credit.model.CreditMovementType;
import com.nttdata.bank.credit.model.ProductType;
import com.nttdata.bank.credit.repository.CreditMovementRepository;
import com.nttdata.bank.credit.service.impl.ReportServiceImpl;
import io.reactivex.rxjava3.core.Flowable;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

  @Mock
  private CreditMovementRepository movementRepository;

  @InjectMocks
  private ReportServiceImpl reportService;

  private CreditMovement movement(String productId, ProductType product,
      CreditMovementType type, String amount) {
    return CreditMovement.builder().productId(productId).productType(product).type(type)
        .amount(new BigDecimal(amount)).build();
  }

  @Test
  void reportGroupsMovementsByProduct() {
    when(movementRepository.findByDateBetween(any(), any())).thenReturn(Flowable.just(
        movement("cc1", ProductType.CREDIT_CARD, CreditMovementType.CHARGE, "100"),
        movement("cc1", ProductType.CREDIT_CARD, CreditMovementType.CHARGE, "50"),
        movement("cc2", ProductType.CREDIT_CARD, CreditMovementType.PAYMENT, "30"),
        movement("c1", ProductType.CREDIT, CreditMovementType.PAYMENT, "200")));

    reportService.productReport(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31)).test()
        .assertValue(report -> report.get(ProductType.CREDIT_CARD).products() == 2
            && report.get(ProductType.CREDIT_CARD).totalMovements() == 3
            && report.get(ProductType.CREDIT_CARD).totals().get(CreditMovementType.CHARGE)
                .intValue() == 150
            && report.get(ProductType.CREDIT).totalMovements() == 1);
  }

  @Test
  void lastCardMovements() {
    when(movementRepository.findTop10ByProductIdOrderByDateDesc("cc1"))
        .thenReturn(Flowable.just(new CreditMovement(), new CreditMovement()));

    reportService.lastCardMovements("cc1").test().assertValueCount(2);
  }
}
