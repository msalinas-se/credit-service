package com.nttdata.bank.credit.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.nttdata.bank.credit.dto.AmountRequest;
import com.nttdata.bank.credit.dto.BalanceResponse;
import com.nttdata.bank.credit.dto.CreditCardRequest;
import com.nttdata.bank.credit.dto.CreditRequest;
import com.nttdata.bank.credit.dto.PaymentRequest;
import com.nttdata.bank.credit.dto.ProductReport;
import com.nttdata.bank.credit.exception.BusinessRuleException;
import com.nttdata.bank.credit.exception.ResourceNotFoundException;
import com.nttdata.bank.credit.exception.ServiceUnavailableException;
import com.nttdata.bank.credit.model.Credit;
import com.nttdata.bank.credit.model.CreditCard;
import com.nttdata.bank.credit.model.CreditMovement;
import com.nttdata.bank.credit.model.ProductType;
import com.nttdata.bank.credit.service.CreditCardService;
import com.nttdata.bank.credit.service.CreditService;
import com.nttdata.bank.credit.service.ReportService;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;

@WebFluxTest({CreditController.class, CreditCardController.class, ReportController.class})
class CreditControllerTest {

  @Autowired
  private WebTestClient webTestClient;

  @MockBean
  private CreditService creditService;

  @MockBean
  private CreditCardService creditCardService;

  @MockBean
  private ReportService reportService;

  private final Map<String, Object> amount = Map.of("amount", 100);
  private final BalanceResponse balance = new BalanceResponse("1", BigDecimal.TEN, BigDecimal.ONE);

  @Test
  void creditEndpoints() {
    when(creditService.create(any(CreditRequest.class))).thenReturn(Single.just(new Credit()));
    when(creditService.findAll(null)).thenReturn(Flowable.just(new Credit()));
    when(creditService.findById("1")).thenReturn(Single.just(new Credit()));
    when(creditService.update(eq("1"), any(AmountRequest.class)))
        .thenReturn(Single.just(new Credit()));
    when(creditService.delete("1")).thenReturn(Completable.complete());
    when(creditService.pay(eq("1"), any(PaymentRequest.class)))
        .thenReturn(Single.just(new CreditMovement()));
    when(creditService.getBalance("1")).thenReturn(Single.just(balance));
    when(creditService.findMovements("1")).thenReturn(Flowable.just(new CreditMovement()));

    webTestClient.post().uri("/credits").bodyValue(Map.of("customerId", "1", "amount", 100))
        .exchange().expectStatus().isCreated();
    webTestClient.get().uri("/credits").exchange().expectStatus().isOk();
    webTestClient.get().uri("/credits/1").exchange().expectStatus().isOk();
    webTestClient.put().uri("/credits/1").bodyValue(amount).exchange().expectStatus().isOk();
    webTestClient.delete().uri("/credits/1").exchange().expectStatus().isNoContent();
    webTestClient.post().uri("/credits/1/payments").bodyValue(amount).exchange()
        .expectStatus().isCreated();
    webTestClient.get().uri("/credits/1/balance").exchange().expectStatus().isOk();
    webTestClient.get().uri("/credits/1/movements").exchange().expectStatus().isOk();
  }

  @Test
  void creditCardEndpoints() {
    when(creditCardService.create(any(CreditCardRequest.class)))
        .thenReturn(Single.just(new CreditCard()));
    when(creditCardService.findAll("1")).thenReturn(Flowable.just(new CreditCard()));
    when(creditCardService.findById("1")).thenReturn(Single.just(new CreditCard()));
    when(creditCardService.update(eq("1"), any(AmountRequest.class)))
        .thenReturn(Single.just(new CreditCard()));
    when(creditCardService.delete("1")).thenReturn(Completable.complete());
    when(creditCardService.charge(eq("1"), any(AmountRequest.class)))
        .thenReturn(Single.just(new CreditMovement()));
    when(creditCardService.pay(eq("1"), any(PaymentRequest.class)))
        .thenReturn(Single.just(new CreditMovement()));
    when(creditCardService.getBalance("1")).thenReturn(Single.just(balance));
    when(creditCardService.findMovements("1")).thenReturn(Flowable.just(new CreditMovement()));

    webTestClient.post().uri("/credit-cards")
        .bodyValue(Map.of("customerId", "1", "creditLimit", 100))
        .exchange().expectStatus().isCreated();
    webTestClient.get().uri("/credit-cards?customerId=1").exchange().expectStatus().isOk();
    webTestClient.get().uri("/credit-cards/1").exchange().expectStatus().isOk();
    webTestClient.put().uri("/credit-cards/1").bodyValue(amount).exchange()
        .expectStatus().isOk();
    webTestClient.delete().uri("/credit-cards/1").exchange().expectStatus().isNoContent();
    webTestClient.post().uri("/credit-cards/1/charges").bodyValue(amount).exchange()
        .expectStatus().isCreated();
    webTestClient.post().uri("/credit-cards/1/payments").bodyValue(amount).exchange()
        .expectStatus().isCreated();
    webTestClient.get().uri("/credit-cards/1/balance").exchange().expectStatus().isOk();
    webTestClient.get().uri("/credit-cards/1/movements").exchange().expectStatus().isOk();
  }

  @Test
  void reportEndpoints() {
    when(reportService.productReport(any(), any())).thenReturn(Single.just(
        Map.of(ProductType.CREDIT, new ProductReport(1, 1, Map.of(), List.of()))));
    when(reportService.lastCardMovements("1")).thenReturn(Flowable.just(new CreditMovement()));

    webTestClient.get().uri("/credits/report?from=2026-10-01&to=2026-10-31").exchange()
        .expectStatus().isOk()
        .expectBody().jsonPath("$.CREDIT.totalMovements").isEqualTo(1);
    webTestClient.get().uri("/credit-cards/1/movements/last").exchange().expectStatus().isOk();
  }

  @Test
  void errors() {
    when(creditService.findById("1")).thenReturn(Single.error(new ResourceNotFoundException("x")));
    when(creditService.findById("2"))
        .thenReturn(Single.error(new BusinessRuleException("CODE", "x")));
    when(creditService.findById("3"))
        .thenReturn(Single.error(new ServiceUnavailableException("x")));
    when(creditService.findById("4")).thenReturn(Single.error(new IllegalStateException("x")));

    webTestClient.get().uri("/credits/1").exchange().expectStatus().isNotFound();
    webTestClient.get().uri("/credits/2").exchange().expectStatus().isBadRequest();
    webTestClient.get().uri("/credits/3").exchange().expectStatus().isEqualTo(503);
    webTestClient.get().uri("/credits/4").exchange().expectStatus().is5xxServerError();
    webTestClient.post().uri("/credits").bodyValue(Map.of()).exchange()
        .expectStatus().isBadRequest();
  }
}
